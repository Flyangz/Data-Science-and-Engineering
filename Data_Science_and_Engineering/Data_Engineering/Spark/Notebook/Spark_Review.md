# Spark Review

---

## 1.基本架构

Spark driver管理和协调在集群上的“任务”运作。这些集群的资源由三种manager管理：standalone, YARN, or Mesos。他们有自己的master和workers来维护集群运作。Spark driver向它们master请求分配Spark app中executors所需的物理资源（它们workers上的cpu，内存等）并启动executors，然后Spark driver给这些executors分配tasks进行计算。

启动spark：spark-shell、spark-submit

入口：SparkSession

对象：DF/DS、RDD

Dataset是类型安全的，意味着Spark会记得数据的类型。而DF的row每次提取值都需要getAs来确定类型。

操作：transformation（narrow、wide，每个操作产生新的rdd数据依赖）、action

>节点、excutors进程、core线程、partition和block（HDFS的）的关系：
>
>- 一个节点可以有一个或多个excutors进程
>- 一个executor进程可以有n个core线程，m个partition（一个partition不能多个executors）
>- 一个core线程处理一个partition（一个task），partition过多就排队等候core。
>- 一个partition一般由多份block数据合并而成（读取HDFS数据时）

Spark编程是函数式的，意味它不会改变原始数据，且相同输入会得到相同结果。



## 2.Structured APIs

理论：

DF默认优化：Tungsten（序列化方式，提高内存利用率，避免garbage-collection and object instantiation消耗）和Catalyst（规划DF代码执行的顺序 和 维护DF所支持的Spark数据类型）

不足：不能自定义partition，操作没有RDD灵活

```scala
spark.conf.set("spark.sql.shuffle.partitions", "5")
//DF
//创建方式：spark.read，spark.createDataFrame(RDD, Schema), rdd[caseclass].toDF
val data = spark.read
  .option("inferSchema", "true")
  .option("header", "true")
  .csv("path")//可用.format("csv")

val myManualSchema = StructType(Array(
  StructField("DEST_COUNTRY_NAME", StringType, true),
  StructField("ORIGIN_COUNTRY_NAME", StringType, true),
  StructField("count", LongType, false,)))
val df = spark.read.format("json").schema(myManualSchema)
  .load("path") 

//如果是text输入的RDD
rdd.map(_split(" ")).map(line => caseclass(line(0).toInt, line(1)....)).toDF

//Row
val myRow = Row("Hello", null, 1, false)
myRow(0).asInstanceOf[String] // String
myRow.getString(0) // String
                                
//DS
case class Flight(DEST_COUNTRY_NAME: String,
                  ORIGIN_COUNTRY_NAME: String,
                  count: BigInt)
val flightsDF = spark.read.parquet("...fileName.parquet/")
val flights = flightsDF.as[Flight]


//transformaton例子，看懂即可
df.select("DEST_COUNTRY_NAME", "ORIGIN_COUNTRY_NAME")
df.selectExpr("avg(count)", "count(distinct(DEST_COUNTRY_NAME))")
df.select(expr("*"), lit(1).as("One"))//python只能alias。相当于创建了一列列名为One，改列所有值为1。实际上并没有创建。

data.sort("count").explain()

data2015.sort("count").take(2)

data.groupBy("DEST_COUNTRY_NAME")
    .sum("count")
    .withColumnRenamed("sum(count)", "destination_total")
    .sort(desc("destination_total"))
    .limit(5)
    .show()

data.select(max("count")).take(1)

df.withColumn("numberOne", lit(1))
df.withColumn("count2", col("count").cast("long"))
mapPartitions, foreachPartition

//filter
df.filter($"count" < 2) //filter Boolean类型的col会返回true的row
val priceFilter = col("UnitPrice") > 600
val descripFilter = col("Description").contains("POSTAGE")
df.filter(col("StockCode").isin("DOT")).filter(priceFilter || descripFilter)
  .show()

//sample
df.sample(withReplacement, fraction, seed).count()
val dataFrames = df.randomSplit(Array(0.25, 0.75), seed)
dataFrames(0)

//sort，sortWithinPartitions
df.orderBy(expr("count desc"))//也可orderBy($"count".desc)

//compute，其他mean,stddev,max, count(df.count()即使某行全是null，仍会计算)
val fabricatedQuantity = pow($"Quantity" * $"UnitPrice", 2) + 5
df.select($"CustomerId", fabricatedQuantity.alias("realQuantity"))
round(col("UnitPrice"), 1)
corr("Quantity", "UnitPrice")
df.stat.approxQuantile("UnitPrice", quantileProbs, relError) //select外用sql函数
df.agg(collect_set("Country"), collect_list("Country")).show()

//text
initcap($"col")//其他lower，upper
//trim，ltrim，rtrim，lpad(lit("HELLO"), 6, "a")
val simpleColors = Seq("black", "white", "red", "green", "blue")
val regexString = simpleColors.map(_.toUpperCase).mkString("|") //regexp_extract用("(", "|", ")")
regexp_replace(col("Description"), regexString, "COLOR")//将Description中包含simpleColors中的词变为COLOR。对于“”的空白值（连空格都没有的），要用null中的方法替换。
translate(col("Description"), "LEET", "1337")//Description里的L变为1，E变为3……
regexp_extract(col("Description"), regexString, 1)//提取第一个符合regex的
col("Description").contains("BLACK")
val simpleColors = Seq("black", "white", "red", "green", "blue")
val selectedColumns = simpleColors.map(color => {
   col("text").contains(color).alias(s"is_$color")
}):+expr("*") 
df.select(selectedColumns:_*).select($"is_black" and $"is_white")show(false)

df.select(split(col("Description"), " "))//产生array
  .alias("array_col"))
  .selectExpr("array_col[0]")
size(Array)//length用于非集合对象
array_contains（Array, "A"）
explode(col("array_col"))

//Time
date_sub(col("today"), 5)
datediff(col("week_ago"), col("today"))// months_between($"col1", $"col2")
//根据java SimpleDateFormat
val dateFormat = "yyyy-dd-MM"
to_date(lit("2017-12-11"), dateFormat).alias("date")
//比较
cleanDateDF.filter(col("date2") > lit("2017-12-12")).show()

//null
coalesce($"col1", $"col2")
df.na.drop("any", Seq("col1", "col2"))// all
df.na.fill(5, Seq("col1", "col2"))
val fillColValues = Map("col1" -> 5, "col2" -> "No Value")
df.na.fill(fillColValues)
df.na.replace("Description", Map("" -> "UNKNOWN"))

//json
"""'{"myJSONKey" : {"myJSONValue" : [1, 2, 3]}}' as jsonString"""
jsonDF.select(
    get_json_object(col("jsonString"), "$.myJSONKey.myJSONValue[1]") as "column",
    json_tuple(col("jsonString"), "myJSONKey")).show(false)


df.drop("colName")
//expensive操作
df.dropDuplicates
df.distinct()
df.limit(5) //用建议用rdd -> sortBy -> zipWithIndex -> filter(index < 5) -> key

//partition
df.rdd.getNumPartitions
df.repartition(5)
df.repartition(col("DEST_COUNTRY_NAME"))
df.repartition(5, col("DEST_COUNTRY_NAME")).coalesce(2)

//action
df.first 
df.take(5)
df.show(5, false)
df.collect()
df.toLocalIterator()

//UDF
def power3(number:Double):Double = number * number * number
spark.udf.register("power3", power3(_:Double):Double)//里面只写lambda也可以，此处写法方便看类型而已
udfExampleDF.selectExpr("power3(num)")

//Aggregations，整个DF（上面已有提及）、groupBy、window、grouping set（即多层group by，DF通过rollup和cube实现）
df.groupBy("InvoiceNo", "CustomerId").count()//.agg实现多种聚合
val windowSpec = Window
  .partitionBy("CustomerId", "date")//将Id和date相同的分为一组
  .orderBy(col("Quantity").desc)//Quantity越大，下面的rank排名越低
  .rowsBetween(Window.unboundedPreceding, Window.currentRow)//rank不用设置
sum($"Quantity").over(windowSpec)

dfNoNull.rollup("Date", "Country").agg(sum("Quantity"))
dfNoNull.cube("Date", "Country").agg(sum(col("Quantity")))
dfNoNull.cube("customerId", "stockCode").agg(grouping_id(), sum("Quantity"))
.orderBy(expr("grouping_id()").desc)
.filter($"grouping_id()" === 0)
.show()
//下面两个效果一样，但第一更耗性能
dfNoNull.select("id", "v_2").groupBy("id").pivot("v_2").count().show()
dfNoNull.stat.crosstab("id", "v_2").show

//join
df1.join(df2, joinExpression, joinType)
df1.join(df1,"id").show()
//如果key1在两个表不是唯一的，那么innerjoin也是会crossjoin的。即表1有2个key1，表2有2个key1，则innerjoin后会有2 x 2行
//joinExpression的逻辑可能是一个表的一个row和另一个表的所有row比较，只要返回true就连在一起。所以只要joinExpression包含两表的信息，且返回boolean即可，这样能够实现很多复杂的join判断，比如下面的代码
person.withColumnRenamed("id", "personId")
  .join(sparkStatus, expr("array_contains(spark_status, id)")).show()
+---------------+---+
|   spark_status| id|
+---------------+---+
|          [100]|100|
|[500, 250, 100]|500|
|[500, 250, 100]|250|
|[500, 250, 100]|100|
|     [250, 100]|250|
|     [250, 100]|100|
+---------------+---+
person.join(broadcast(graduateProgram), joinExpr)

//caching，数据重复运用或者某个数据的生成耗费很大
//checkpointing
spark.sparkContext.setCheckpointDir("/some/path/for/checkpointing")
words.checkpoint()
```

DF输出：Parquet，gzip，partition，bucketing，maxRecordsPerFile

## 3.RDD

优点：灵活，可以控制数据的分块，广播变量

缺点：麻烦

理论：

RDD的属性：partitionlist，计算每个partition的函数，与其他RDD的依赖list，partitioner，preferred locations list。另外还有写通用的function。

这些属性实现了rdd数据的弹性、容错、位置感知等。

action：reduce, collect, count, first, take, takeSample, takeOrdered, saveAsxxx, countByKey, foreach

partition：默认从storage（checkpointing产生的除外）加载的数据是没有partitioner的，会根据数据和分块的大小来分配数据到partitions。

shuffle慢的原因：数据移动，潜在的I/O，限制并行（完成shuffle才能下一步）

```scala
//创建
DS.rdd
DF.rdd后取值还要map(....getLong(0))
spark.sparkContext.parallelize(collection)
spark.sparkContext.textFile("path")

glom//容易OOM，因为把弹性的rdd变为非弹性的集合了
countApprox, countApproxDistinct，countByValue，countByValueApprox

//KV RDD
mapValues, flatMapValues
lookup("s")//查看key为s的value
sampleByKey(true, sampleMap, 6L)//是否替代实际上是选择Poisson还是Bernoulli来抽样，sampleMap映射每个distinct样本的期望抽取数量和占比，6L为种子
groupByKey //如果没有设定partitioner，会shuffle。distinct的keys越多，或者各keys的数据分布越不均匀，消耗越大。
//combineByKey，更复杂，更灵活，可设置是否mapSideCombine。
combineByKey(valToCombiner, mergeValuesFunc, mergeCombinerFunc, outputPartitions)
//aggregate，第一个func在partition内执行，第二个跨partition，最后聚合到driver上。一般比combineByKey快，因为可通过重复运用accumulator来减少object的创建，规定mapSideCombine。
aggregate(0)(maxFunc, addFunc)
treeAggregate(0)(maxFunc, addFunc, depth)
aggregateByKey(0)(addFunc, maxFunc)
//比上面更快，但限定了输出类型，没有累加器产生。
reduceByKey

//foldByKey，设置一个初始值，该初始值会按照Func的规则，如+，作用到每个value上，然后作用后的value再按照Func的规则按key进行聚合。
foldByKey(0)(addFunc)
charRDD.cogroup(charRDD2, charRDD3)
KVrdd1.join(KVrdd2, outputPartitions)
KVrdd1.zip(KVrdd2)

sortByKey
repartitionAndSortWithinPartitions(partitioner)
filterByRange（sortByKey后用）

//用于join
def manualBroadCastHashJoin[K : Ordering : ClassTag, V1 : ClassTag,
 V2 : ClassTag](bigRDD : RDD[(K, V1)], smallRDD : RDD[(K, V2)])= {
  val smallRDDMapLocal: Map[K, V2] = smallRDD.collectAsMap()//注意smallRDD不能有相同key不同value，否则只会取一个key-value
  bigRDD.sparkContext.broadcast(smallRDDMapLocal)
  bigRDD.mapPartitions(iter => {
   iter.flatMap{
    case (k,v1) => {
      val v2 = smallRDDMapLocal.getOrElse(k, 0)
      Seq((k, (v1, v2)))
    }
   }
  }, preservesPartitioning = true)
}
//部分广播，利用countByKeyApprox或者countByKey+sort后取前k个key

//partition
coalesce
repartition

//Broadcast Variables
val supplementalData = Map(...)
val bc1 = spark.sparkContext.broadcast(supplementalData)
bc1.value.getOrElse(word, 0)
//non-serialized object
@transient lazy val
```



## 4.Structured Streaming

理论：

DStream的优势在于“high-level API interface and simple exactly-once semantics”。然而它是基于Java/Python对象和函数，与DF相比，难以优化；不支持event time；只能micro-batch

Structured Streaming以Spark的结构化API为基础，支持Spark language API，event time，更多类型的优化，正研发continuous处理（Spark 2.3）

2.2为止，支持静态join,不支持outer/ left/ right joins 和stream join，限制在于multiple “chained” aggregations

```scala
val df = spark
  .readStream
  .format("kafka")
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
  .option("subscribe", "topic1,topic2")
  .load()
df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
  .as[(String, String)]

val purchaseByCustomerPerHour = streamingDataFrame
  .selectExpr(
    "CustomerId",
    "(UnitPrice * Quantity) as total_cost",
    "InvoiceDate")
  .groupBy($"CustomerId", window($"InvoiceDate", "1 day"))
  .sum("total_cost")

//Event Time
purchaseByCustomerPerHour
    .withWatermark("event_time", "30 minutes")
    .dropDuplicates("User", "event_time")
    .groupBy(window(col("event_time"), "10 minutes", "5 minutes(optional)"),"User").count()
    .writeStream
    .trigger(Trigger.ProcessingTime("100 seconds"))
    .format("memory") 
    .queryName("customer_purchases") // 表格名
    .outputMode("complete") 
    .start()
             
//Arbitrary Stateful Processing, 自定义储存什么信息，怎样更新和什么时候移除
//mapGroupsWithState，基于user的activity（state）来更新
//设置两个case class，一个存储state，一个存储event；写两个方法，一个updateUserStateWithEvent（基于一条row event如何更新state），一个updateAcrossEvents（根据组名，属于该组的新信息和该组目前的state，调用updateUserStateWithEvent来更新state。）
//main代码，select需要的列，cast为存储state的case class，分组后mapGroupsWithState，传入updateAcrossEvents
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

//flatMapGroupsWithState，基于数量的更新，即某个指标的量达到特定值时更新
//设置三个case class，一个存InputRow，一个存状态State，一个存OutputRow；两个函数updateWithEvent和updateAcrossEvents，前者和mapGroupsWithState例子一个思路，后者取得state并更新后，要判断数值是否达到输出要求。
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
//运用timeout的例子，具体代码看《Structured_Streaming_and_DStreams.md》
.withWatermark("timestamp", "5 seconds")
  .groupByKey(_.uid)
  .flatMapGroupsWithState(OutputMode.Append,
    GroupStateTimeout.EventTimeTimeout)(updateAcrossEvents)
```

ML

```scala
//使用ML算法时先把DF中各数据类型转换为numerical values vector
staticDataFrame.printSchema()//查看各列的数据类型，是否nullable等

val preppedDataFrame = staticDataFrame
  .na.fill(0)
  .withColumn("day_of_week", date_format($"InvoiceDate", "EEE"))//提取timestamp类的星期，具体查看java.text.SimpleDateFormat
  .coalesce(5)

val trainDataFrame = preppedDataFrame
  .where("InvoiceDate < '2011-07-01'")
val testDataFrame = preppedDataFrame
  .where("InvoiceDate >= '2011-07-01'")

//将day_of_week转换为相应的数量值，如Sun -> 7
val indexer = new StringIndexer()
  .setInputCol("day_of_week")
  .setOutputCol("day_of_week_index")

val encoder = new OneHotEncoder()
  .setInputCol("day_of_week_index")
  .setOutputCol("day_of_week_encoded")

val vectorAssembler = new VectorAssembler()
  .setInputCols(Array("UnitPrice", "Quantity", "day_of_week_encoded"))
  .setOutputCol("features")

val transformationPipeline = new Pipeline()
  .setStages(Array(indexer, encoder, vectorAssembler))

val fittedPipeline = transformationPipeline.fit(trainDataFrame)
val transformedTraining = fittedPipeline.transform(trainDataFrame).cache()

//设定和训练ML模型。术语：Algorithm指未训练的模型，AlgorithmModel指训练后的。Estimators可领流程更简单，下面出于step by step展示
val kmeans = new KMeans()
  .setK(20)
  .setSeed(1L)
val kmModel = kmeans.fit(transformedTraining)

val transformedTest = fittedPipeline.transform(testDataFrame)
kmModel.computeCost(transformedTest)
```



## 习惯：

- 处理数据时先用take或者sample（数据量大时小心）提取部分数据进行模拟处理，确定怎么处理后才用到整个数据上。当所编辑的内容复杂起来后才转到IDEA

- count后如果数据不大，可用.cache，（在下一次action时）把数据放到内存，而不是每次action都要Spark重新读数据

- 加载大量小文件（总量也小）可以在.load后.coalesce减少partition并cache

- 通常的做法是先用SQL查询数据库，然后Spark读取该查询的数据。`spark.read.jdbc(url, tablename, predicates, props).option("numPartitions", 10)`

- join前filter或reduce，比如有score(id, score)和student(id, name)的rdd。如果制作一个(id,name,score)的表，且score只要最高分，先用下面的代码去掉多余的score后再join
  val bestScore = scores.reduceByKey((x,y) => if (x > y) x else y)

- 如果RDD有重复的keys，最好先用distinct或combineByKey来减少key空间，或者用cogroup来处理重复keys

- shuffle操作前加上下面代码：

  val partitioner = rdd.partitioner match {
        case (Some(p)) => p
        case (None) => new HashPartitioner(scores.partitions.length)
  }

- 重用已存在的objects。如“自定义Aggregation函数”中seqOp和combOp运用this.type方式，而非new

- 使用mapPartitions算子时，用iterator-to-iterator transformation