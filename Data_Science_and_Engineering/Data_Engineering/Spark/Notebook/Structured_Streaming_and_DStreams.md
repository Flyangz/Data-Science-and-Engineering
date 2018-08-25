## Part V. Streaming

以Structured Streaming为主，DStreams在最后补充。

------

### Stream Processing Fundamentals

#### 1.概念

流处理就是不断地整合新数据计算新结果。批量处理是固定输入量计算一次。Structured Stream集合这两个功能并加上交互式查询。

流处理用于notifications/alerting，实时报告，累积ETL，实时决策，更新服务数据（GA），线上ML

现实里，流处理有很多挑战，这产生了不同的流处理设计。



#### 2.Stream Processing Design Points

Record-at-a-Time Versus Declarative APIs

将每个事件传给App，让App作出相应行动，如Storm。这种方式，当App需要对数据完全控制时特别重要。但实现难度大，App要实现长时间跟踪state，及时清空数据，failure后如何采取不同行动等。

新的流系统提供declarative APIs，App定义计算什么而不是怎么计算，如DStream，Kafka。

**Event Time Versus Processing Time**

事件发生的事件和收到数据处理的时间。前者的记录可能乱序，有时差。

**Continuous Versus Micro-Batch Execution**

前者中，各个节点不断从上游节点接受信息，进行一步计算后向下游节点传递结果。这方式延迟少，但最大吞吐量低（量大的话计算慢，影响下游），且节点不能单独停止。

后者积累小批量数据，然后每批并行计算。可实现高吞吐，且需要更少节点。但有延迟。



#### 3.Spark’s Streaming APIs

包含DStream（纯 micro-batch，declarative API，但不支持event time）和Structured Streaming（在前者基础上增加event time和continuous处理）

DStream的优势在于“high-level API interface and simple exactly-once semantics”。然而它是基于Java/Python对象和函数，与DF相比，难以优化；不支持event time；只能micro-batch

Structured Streaming以Spark的结构化API为基础，支持Spark language API，event time，更多类型的优化，正研发continuous处理（Spark 2.3）。操作跟DF几乎一样，自动转换为累积计算形式，也能导出Spakr SQL所用的表格。

------



### Structured Streaming Basics

#### 1.介绍和概念

Structured Streaming是建立在Spark SQL上的流处理框架，使用结构化API、Catalyst engine。 它确保end-to-end, exactly-once processing as well as fault-tolerance through checkpointing and write-ahead logs.

只需设定batch还是streaming方式，其他代码不用改。

为了整合Spark的其他模块，Structured Streaming可以使用 continous application that is an end-to-end application that reacts to data in real time by combining a variety of tools: streaming jobs, batch jobs, joins between streaming and offline data, and interactive ad-hoc queries. 



**核心概念**

Transformations and Actions：一些查询会受限制（还不能incrementalize），action只有start。

输入源Sources：Kafka，分布式文件系统，A socket source for testing

输出sinks：Kafka，几乎所有文件形式，foreach for computation，console for testing，memory for debugg

输出模式：append，update，complete

Triggers：什么时候输出，固定interval或固定量（在某latency下）

Event-Time处理：Event-time data，Watermarks（等待时间和output结果的时间）



#### 2.Transformations and Actions

Streaming DF基本上可使用所有静态Structured APIs（inference要另外开`spark.sql.streaming.schemaInference` to `true`）

```scala
val static = spark.read.json("/data/activity-data/")
val dataSchema = static.schema
val streaming = spark.readStream.schema(dataSchema)
  .option("maxFilesPerTrigger", 1).json("/data/activity-data")
val activityCounts = streaming.groupBy("gt").count()
spark.conf.set("spark.sql.shuffle.partitions", 5)
val activityQuery = activityCounts.writeStream.queryName("activity_counts")
  .format("memory").outputMode("complete")
  .start()
activityQuery.awaitTermination()//防止driver在查询过程退出

//执行上面的代码，stream已经运行。下面对output到内存中的table进行查询
for( i <- 1 to 5 ) {
    spark.sql("SELECT * FROM activity_counts").show()
    Thread.sleep(1000)
}
```

> `spark.streams.active`查看活动的stream



**transformation细节补充**

限制在不断减少。目前的限制：未进行aggregate的stream不能sort，cannot perform multiple levels of aggregation without using Stateful Processing 

```scala
//所有select和filter都支持
val simpleTransform = streaming.withColumn("stairs", expr("gt like '%stairs%'"))
  .where("stairs")
  .where("gt is not null")
  .select("gt", "model", "arrival_time", "creation_time")
  .writeStream
  .queryName("simple_transform")
  .format("memory")
  .outputMode("append")
  .start()

//支持大部分Aggregations
val deviceModelStats = streaming.cube("gt", "model").avg()
  .drop("avg(Arrival_time)")
  .drop("avg(Creation_Time)")
  .drop("avg(Index)")
  .writeStream.queryName("device_counts").format("memory").outputMode("complete")
  .start()
//限制在于multiple “chained” aggregations (aggregations on streaming aggregations) ，但可以通过output到sink后再aggregate来实现

//支持静态join,不支持outer/ left/ right joins 和stream join
val historicalAgg = static.groupBy("gt", "model").avg()
val deviceModelStats = streaming.drop("Arrival_Time", "Creation_Time", "Index")
  .cube("gt", "model").avg()
  .join(historicalAgg, Seq("gt", "model"))
  .writeStream.queryName("device_counts").format("memory").outputMode("complete")
  .start()
```



#### 3.Input and Output

**Sources and Sinks**

（1）File: 

注意添加file到directory要为原子方式，否则Spark会在file未添加完成时就进行操作。在使用文件系统，如本地files或HDFS时，最好在另外一个目录写文件，完成后再移到input目录。



（2）Kafka: 

一个对流数据进行分布式读写的系统。Kafka lets you store streams of records in categories that are referred to as *topics*. Each *record* in Kafka consists of a key, a value, and a timestamp. 其schema为

- key: binary
- value: binary
- topic: string
- partition: int
- offset: long
- timestamp: long

Topics 有一系列immutable的record组成，record的位置为offset。*subscribing* to a topic为读数据，*publishing* to a topic为写数据

读数据有三个选择：

`assign`：指定topic以及topic的某部分，如  JSON string`{"topicA":[0,1],"topicB":[2,4]}`

 `subscribe` or `subscribePattern`：读取多个topics通过一列topics或一种模式

其他选项

`startingOffsets` and `endingOffsets`：earliest，latest或JSON string（`{"topicA":{"0":23,"1":-1},"topicB":{"0":-2}}`，-1latest，-2earliest）。这仅适用于新的streaming，重新开始时会在结束时的位置开始定位。新发现的分区会在earliest开始，最后位置为查询的范围。

`failOnDataLoss`:a false alarm，默认true

`maxOffsetsPerTrigger`

还有一些Kafka consumer timeouts, fetch retries, and intervals.

```scala
val ds1 = spark.readStream.format("kafka")
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")//连接哪个服务器
  .option("subscribe", "topic1,topic2")//也可("subscribePattern", "topic.*")
  .load()
```

使用使用原生Structured API或UDF对信息进行parse。通常用 JSON or Avro来读写Kafka

```scala
ds1.selectExpr("topic", "CAST(key AS STRING)", "CAST(value AS STRING)")//不加"topic"就在后面加.option("topic", "topic1")
  .writeStream.format("kafka")
  .option("checkpointLocation", "/to/HDFS-compatible/dir")
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
  .start()
```



（3）Foreach sink（`foreachPartitions`）

要实现`ForeachWriter` 接口

```scala
//每当所output的rows trigger，下面三个方法都会被启动（每个executor中）
//方法要Serializable（UDF or a Dataset map）
datasetOfString.write.foreach(new ForeachWriter[String] {
  def open(partitionId: Long, version: Long): Boolean = {
    // 完成所有初始化，如database connection，开始transactionsopen
    //version是单调递增的ID， on a per-trigger basis
    // 返回是否处理这个组rows（比如crash后，如果发现储存系统中已经有对应的version和partitionId，通过返回false来跳过）
  }
  def process(record: String) = {
    // 对每个record的处理
  }
  def close(errorOrNull: Throwable): Unit = {
    //只要open被调用就会启动。
    //如果process时出错，如何处理已完成的部分数据
    //关闭连接
  }
})
```

> 错误示范：如果在open之外初始化，则会在driver进行。



（4）用于测试的sink（不能用于生产，因不提供end-to-end容错）

```scala
//Socket source
val socketDF = spark.readStream.format("socket")
  .option("host", "localhost").option("port", 9999).load()
//如果要写数据给上面这个App，需要在port9999启动一个服务器，Unix用nc -lk 9999后打字。socket源会返回一个文本表，打开每一行。

//Console sink
activityCounts.format("console").write()

//Memory sink
activityCounts.writeStream.format("memory").queryName("my_device_table")
```

> 如果真的想在生产中output为表格，建议用Parquet file sink 



**Output Modes**

Append mode：当使用event-time + watermarks时，只有最终结果会被output到sink

Complete mode：update所有，就像新state of a stream的计算结果。如果只用map，则不能用这个模式。

Update mode：只有row和之前的不同才被update

模式的限定（表，先了解 event-time processing and watermarks）



**Triggers**

默认情况下，前一个trigger完成后马上开始新的。目前只有one periodic trigger type, based on processing time, as well as a “once” trigger to manually run a processing step once. 

```scala
//Processing time trigger，如果Spark在100s内没有完成计算，Spark会等待下一个，而不是计算完成后马上计算下一个
activityCounts.writeStream.trigger(Trigger.ProcessingTime("100 seconds"))//也可以用Duration in Scala or TimeUnit in Java
  .format("console").outputMode("complete").start()

//Once trigger，测试中非常有用，常被用于低频工作（如向总结表格添加新数据）
activityCounts.writeStream.trigger(Trigger.Once())
  .format("console").outputMode("complete").start()
```



#### 4.Streaming Dataset API

除了DF，也可以用Dataset的API

```scala
case class Flight(DEST_COUNTRY_NAME: String, ORIGIN_COUNTRY_NAME: String,
  count: BigInt)
val dataSchema = spark.read
  .parquet("/data/flight-data/parquet/2010-summary.parquet/")
  .schema
val flightsDF = spark.readStream.schema(dataSchema)
  .parquet("/data/flight-data/parquet/2010-summary.parquet/")
val flights = flightsDF.as[Flight]
def originIsDestination(flight_row: Flight): Boolean = {
  return flight_row.ORIGIN_COUNTRY_NAME == flight_row.DEST_COUNTRY_NAME
}
flights.filter(flight_row => originIsDestination(flight_row))
  .groupByKey(x => x.DEST_COUNTRY_NAME).count()
  .writeStream.queryName("device_counts").format("memory").outputMode("complete")
  .start()
```

---



### Event-Time and Stateful Processing

**Event Time**：接收到信息的时间（Processing time）和实际发生时间（Processing time）有差异。网络传输中只尽量保持信息完整（有时还不完整），但是否按顺序，是否重复等不保证。

**Stateful Processing**（前一节所讲的）：通过microbatch or a record-at-a-time 的方式，更新经过一段时间后的中间信息（state）。当执行这一处理时，Spark将中间信息存到*state store*（目前为内存），且通过存到checkpoint directory实现容错。

**Arbitrary Stateful Processing**：自定义储存什么信息，怎样更新和什么时候移除(either explicitly or via a time-out)



#### 1.Event-Time Basics

```scala
spark.conf.set("spark.sql.shuffle.partitions", 5)
val static = spark.read.json("/data/activity-data")
val streaming = spark
  .readStream
  .schema(static.schema)
  .option("maxFilesPerTrigger", 10)
  .json("/data/activity-data")

//转换时间格式
val withEventTime = streaming.selectExpr(
  "*",
  "cast(cast(Creation_Time as double)/1000000000 as timestamp) as event_time")

//Tumbling Windows/ Sliding windows/ Watermarks这部分官方图更好理解
withEventTime
  .withWatermark("event_time", "30 minutes")//某窗口结果为x，但是部分数据在这个窗口的最后一个timestamp过后还没到达，Spark在这会等30min，过后就不再更新x了。
  .dropDuplicates("User", "event_time")
  .groupBy(window(col("event_time"), "10 minutes"),"User").count()//10min后再加一个参数变为Sliding windows，表示每隔多久计算一次
  .writeStream
  .queryName("events_per_window")
  .format("memory")
  .outputMode("complete")
  .start()

spark.sql("SELECT * FROM events_per_window")
```

![Handling Late Data](http://spark.apache.org/docs/2.2.2/img/structured-streaming-late-data.png) 



#### 2.Arbitrary Stateful Processing

- Create window based on counts of a given key
- Emit an alert if there is a number of events within a certain time frame
- Maintain user sessions of an undetermined amount of time and save those sessions to perform some analysis on later.

**Time-Outs**

给每个组配置的一个全局变量，它可以是处理时间（`GroupStateTimeout.ProcessingTimeTimeout`）或者事件时间（GroupStateTimeout.EventTimeTimeout）前者基于system clock，会受时区影响，后者

When using time-outs, check for time-out first before processing the values. You can get this information by checking the `state.hasTimedOut` flag or checking whether the values iterator is empty. You need to set some state (i.e., state must be defined, not removed) for time-outs to be set.



there is a no strict upper bound on when the time-out would occur

GroupState.setTimeoutTimestamp(...)

time-out is that it will never occur before the watermark has exceeded the set time-out.

如果一个正在处理的数据块没有数据，这将没有更新，且不是event-time time-out(未来可能会变)



mapGroupsWithState: one row per key (or group) 

flatMapGroupsWithState:  multiple outputs

**输出模式**

`mapGroupsWithState` 只支持update模式；`flatMapGroupsWithState` 支持append（time-out后，即watermark过后才显示结果）和update



**mapGroupsWithState例子：**

基于user的activity（state）来更新。具体来说，如果收到的信息中，user的activity没变，就检查该信息是否早于或晚于目前所收到的信息，进而得出user某个activity的准确的持续时间（下面设置了GroupStateTimeout.NoTimeout，假设信息传输没丢失，则迟早会收到）。如果activity变了，就更新activity（时间也要重新设置）。如果没有activity，就不变。

```scala
//输入row的格式
case class InputRow(user:String, timestamp:java.sql.Timestamp, activity:String)
//表示user状态的格式(储存目前状态)
case class UserState(user:String,
  var activity:String,
  var start:java.sql.Timestamp,
  var end:java.sql.Timestamp)

//基于某row如何更新
def updateUserStateWithEvent(state:UserState, input:InputRow):UserState = {
  if (Option(input.timestamp).isEmpty) {
    return state
  }
  if (state.activity == input.activity) {

    if (input.timestamp.after(state.end)) {
      state.end = input.timestamp
    }
    if (input.timestamp.before(state.start)) {
      state.start = input.timestamp
    }
  } else {
    if (input.timestamp.after(state.end)) {
      state.start = input.timestamp
      state.end = input.timestamp
      state.activity = input.activity
    }
  }
  state
}

//基于一段时间的row如何更新
def updateAcrossEvents(user:String,
  inputs: Iterator[InputRow],
  oldState: GroupState[UserState]):UserState = {
  var state:UserState = if (oldState.exists) oldState.get else UserState(user,
        "",
        new java.sql.Timestamp(6284160000000L),
        new java.sql.Timestamp(6284160L)
    )
  // we simply specify an old date that we can compare against and
  // immediately update based on the values in our data

  for (input <- inputs) {
    state = updateUserStateWithEvent(state, input)
    oldState.update(state)
  }
  state
}

withEventTime
  .selectExpr("User as user",
    "cast(Creation_Time/1000000000 as timestamp) as timestamp", "gt as activity")
  .as[InputRow]
  .groupByKey(_.user)
  .mapGroupsWithState(GroupStateTimeout.NoTimeout)(updateAcrossEvents)
  .writeStream
  .queryName("events_per_window")
  .format("memory")
  .outputMode("update")
  .start()
```



**flatMapGroupsWithState例子**

基于数量的更新，即某个指标的量达到特定值时更新。下面的代码，DeviceState记录了一段时间（计数未达到500）中，device的名字，数值，和计数。当计算到500时就求均值，并变为输出row，未达到无输出。

```scala
//输入row的格式
case class InputRow(device: String, timestamp: java.sql.Timestamp, x: Double)
//表示DeviceState状态的格式(储存目前状态)
case class DeviceState(device: String, var values: Array[Double], var count: Int)
//输出row的格式
case class OutputRow(device: String, previousAverage: Double)

//基于某row如何更新
def updateWithEvent(state:DeviceState, input:InputRow):DeviceState = {
  state.count += 1
  // maintain an array of the x-axis values
  state.values = state.values ++ Array(input.x)
  state
}

//基于一段时间的row如何更新
def updateAcrossEvents(device:String, inputs: Iterator[InputRow],
  oldState: GroupState[DeviceState]):Iterator[OutputRow] = {//flatMapGroupsWithState要求Iterator？
  inputs.toSeq.sortBy(_.timestamp.getTime).toIterator.flatMap { input =>
    val state = if (oldState.exists) oldState.get
      else DeviceState(device, Array(), 0)
	val newState = updateWithEvent(state, input)
    if (newState.count >= 500) {
      oldState.update(DeviceState(device, Array(), 0))
      Iterator(OutputRow(device,
        newState.values.sum / newState.values.length.toDouble))
    }
    else {
      oldState.update(newState)
      Iterator()//未达到500无输出
    }
  }
}

withEventTime
  .selectExpr("Device as device",
    "cast(Creation_Time/1000000000 as timestamp) as timestamp", "x")
  .as[InputRow]
  .groupByKey(_.device)
  .flatMapGroupsWithState(OutputMode.Append,
    GroupStateTimeout.NoTimeout)(updateAcrossEvents)//这里NoTimeout表示，如果没达到500，则永远没有结果输出
  .writeStream
  .queryName("count_based_device")
  .format("memory")
  .outputMode("append")
  .start()
```

下面这个例子运用了timeout

```scala
case class InputRow(uid:String, timestamp:java.sql.Timestamp, x:Double,
  activity:String)
case class UserSession(val uid:String, var timestamp:java.sql.Timestamp,
  var activities: Array[String], var values: Array[Double])
case class UserSessionOutput(val uid:String, var activities: Array[String],
  var xAvg:Double)

def updateWithEvent(state:UserSession, input:InputRow):UserSession = {
  // handle malformed dates
  if (Option(input.timestamp).isEmpty) {
    return state
  }

  state.timestamp = input.timestamp
  state.values = state.values ++ Array(input.x)
  if (!state.activities.contains(input.activity)) {
    state.activities = state.activities ++ Array(input.activity)
  }
  state
}

def updateAcrossEvents(uid:String,
  inputs: Iterator[InputRow],
  oldState: GroupState[UserSession]):Iterator[UserSessionOutput] = {

  inputs.toSeq.sortBy(_.timestamp.getTime).toIterator.flatMap { input =>
    val state = if (oldState.exists) oldState.get else UserSession(
    uid,
    new java.sql.Timestamp(6284160000000L),
    Array(),
    Array())
    val newState = updateWithEvent(state, input)

    if (oldState.hasTimedOut) {
      val state = oldState.get
      oldState.remove()
      Iterator(UserSessionOutput(uid,
      state.activities,
      newState.values.sum / newState.values.length.toDouble))
    } else if (state.values.length > 1000) {
      val state = oldState.get
      oldState.remove()
      Iterator(UserSessionOutput(uid,
      state.activities,
      newState.values.sum / newState.values.length.toDouble))
    } else {
      oldState.update(newState)
      oldState.setTimeoutTimestamp(newState.timestamp.getTime(), "5 seconds")
      Iterator()
    }
  }
}

withEventTime.where("x is not null")
  .selectExpr("user as uid",
    "cast(Creation_Time/1000000000 as timestamp) as timestamp",
    "x", "gt as activity")
  .as[InputRow]
  .withWatermark("timestamp", "5 seconds")
  .groupByKey(_.uid)
  .flatMapGroupsWithState(OutputMode.Append,
    GroupStateTimeout.EventTimeTimeout)(updateAcrossEvents)
  .writeStream
  .queryName("count_based_device")
  .format("memory")
  .start()
```

---



### Structured Streaming in Production

1.Fault Tolerance and Checkpointing

配置App使用checkpointing and write-ahead logs并配置查询写到checkpoint location（如HDFS）。当遇到failure时，只需重启应用，并保证它指向正确的checkpoint location

checkpoint stores all of the information about what your stream has processed thus far and what the intermediate state it may be storing is

```scala
//在运行App前，在writeStream配置
.option("checkpointLocation", "/some/location/")
```



2.Updating Your Application

增加一个新列或改变UDF不需要新的checkpoint目录，但如果怎加新的聚合键或根本性地改变查询就需要。

infrastructure changes有时只需重启stream，如`spark.sql.shuffle.partitions`，有时则需重启整个App，如Spark application configurations



3.Metrics and Monitoring

`query.status` ， `query.recentProgress`（注意Input rate and processing rate和Batch duration）和Spark UI



4.Alerting

feed the metrics to a monitoring system such as the open source Coda Hale Metrics library or Prometheus, or you may simply log them and use a log aggregation system like Splunk.



5.Advanced Monitoring with the Streaming Listener（详细可能要看其他资料）

allow you to receive asynchronous updates from the streaming query in order to automatically output this information to other systems and implement robust monitoring and alerting mechanisms. 

---

### Dstream

代码编写步骤：定义输入源 -> 定义transformation -> 启动，停止

```scala
val conf = new SparkConf().setAppName(appName).setMaster(master)
val ssc = new StreamingContext(conf, Seconds(1))
val lines = ssc.socketTextStream/ textFileStream //等方法创造streams

val res = lines.flatMap(_.split(" ")).map((_, 1)).reduceByKey(_+_)

ssc.start()
ssc.awaitTermination()
```

> 只有一个context，它停止后就不能再启动。一个StreamingContext只能在一个JVM运行。StreamingContext和SparkContext可以分开停止，例如停止一个StreamingContext后可以开启一个新的StreamingContext。
>
> setMaster中，local[n]要大于receivers的数量，receivers一个占一个线程。
>
> textFileStream中，处理的文件格式要一致，文件的添加是原子性的，即把写好的文件放到一个文件夹中，或把文件重命名为要处理的文件。另外文件只会被处理一次，后续修改不起作用。
