# Spark Introduction(DataFrame/DataSet)

本文代码主要基于Spark2.2，Scala 2.11，Python3

由于用Scala和Python编写的Spark application代码十分类似，所以本文只展示Scala代码，与Python不同的地方会说明。

---

## Part I. Gentle Overview of Big Data and Spark

1. Apache Spark is a **unified** **computing engine** and a set of **libraries** for parallel data processing on computer clusters.  
2. 大数据背景
   硬件散热瓶颈 -> 多核CPU -> 并行计算
   数据储存和收集成本不断下降

---

### Overview

#### 1.基本架构  

Spark driver管理和协调在集群上的“任务”运作。这些集群的资源由三种manager管理：standalone, YARN, or Mesos。他们有自己的master和workers来维护集群运作。Spark driver向它们master请求分配Spark app中executors所需的物理资源（它们workers上的cpu，内存等）并启动executors，然后Spark driver给这些executors分配tasks进行计算。

Application由a driver process and a set of executor processes构成。其中driver进程作为核心，在一个节点上运行`main()`，并负责维护集群的状态、任务信息，规划和分配工作给Executors。Executors负责执行代码和反映情况。每个app有它自己的executor进程。

详细参考“Low-Level APIs, Execution and Tuning”的Spark在集群上运行。

#### 2.基本概念

Spark有两个基础APIs集：非结构化的RDD和结构化的DataFrame/DataSet。

> 模块组成：Spark Core(RDD), SQL(DF/DataSet), Structured Streaming, MLlib/ML等。
>

**Starting Spark**

`spark-shell` (or `pyspark`)直接进行交互式操作（比较少用，一般借助下面的工具），而 `spark-submit` 一般是生成环境向集群提交任务，如上面提到的yarn集群。

交互式操作和调试：可使用jupyter notebook、zeppelin或spark notebook等，方便操作和可视化。

调试的代码量大时用IDEA。

spark-submit的代码详细参考“Low-Level APIs, Execution and Tuning”开发开发Spark App。



**入口**

SparkSession 是一个driver线程。通过它的方法获取操作对象。下面代码调用它的range方法获取操作对象（包含1-1000个数字的Dataset），并转化为DF，列名为number。

`val myRange = spark.range(1000).toDF("number")` 



**对象**（数据结构）

DataFrames (DF)相当于表格，通过schema 定义列及其数据类型。其他对象还有Datasets和RDDs。

这些对象都是被划分为多个partitions并分布式地存储在各个workers节点上。目前不能通过DF/DS设置partition（哪些数据放哪个partition），但RDD可以。另外它们都是 *immutable* 的（相当于final in Java）

> 节点、excutors进程、core线程、partition和block（HDFS的）的关系：
>
> - 一个节点可以有一个或多个excutors进程
> - 一个executor进程可以有n个core线程，m个partition（一个partition不能多个executors）
> - 一个core线程处理一个partition（一个task），partition过多就排队等候core。
> - 一个partition一般由多份block数据合并而成（读取HDFS数据时）
>



**操作**（transformation and action）

1.transformation 分为 narrow 和 wide dependencies。
narrow (pipelining) : each input partition will contribute to only one output partition，即1 to 1（map）或n to 1（coalesce）。
wide (通常shuffle) : input partitions contributing to many output partitions，即 1 to n。

> narrow操作可以预先知道数据分到哪，而不需要根据数据的key值来确定但wide相反，如sort, ByKey等需要repartition的算子。如果Spark已经根据partitioner知道数据按特定方式partitioned，就不一定shuffle。需要shuffle的，Spark会在该RDD上增加ShuffledDependency对象到它的依赖列表中。操作结果会写到磁盘。这里有很多优化的需求。



Lazy evaulation: 上述transformation的代码在运行时不会被马上执行，Spark会在action前对代码的运行计划进行优化后才运行。它能间接减少多次访问数据（对访问到的数据连续执行map和filter，而不是map完后在filter），还能使代码更简洁（相比于MapReduce，如下面代码），Spark自动安排执行计划。

```scala
//考虑StopWordsFilter的wordCount
def withStopWordsFiltered(rdd : RDD[String], illegalTokens : Array[Char],
    stopWords : Set[String]): RDD[(String, Int)] = {
    val separators = illegalTokens ++ Array[Char](' ')
    val tokens: RDD[String] = rdd.flatMap(_.split(separators).
      map(_.trim.toLowerCase))
    val words = tokens.filter(token =>
      !stopWords.contains(token) && (token.length > 0) )
    val wordPairs = words.map((_, 1))
    val wordCounts = wordPairs.reduceByKey(_ + _)
    wordCounts
}
```



2.action(trigger the computation)返回非核心数据结构，分为to view data, to collect data 和 to output data。他们促发scheduler基于RDD的依赖关系，建立DAG。在执行DAG里面的一系列步骤（stages）时，scheduler还能保持每一个分区不丢失数据（丢失部分重算）。

> 有些操作即是transformation又是action，如`sortByKey`
>
> DAG在Spark里是Scheduler，如果连接集群，配置参数或启动job时出错，会是DAG Scheduler errors，因为job是由DAG处理的。DAG为每一个job建立一个stages组成的graph，决定了运行每个task的位置，并将信息传给TaskScheduler。TaskScheduler负责在集群的 running tasks，并创建一个graph，里面有各partitions的依赖。



**UI**

本地模式入口 http://localhost:4040。查看Spark运行的各种情况。



#### 3.例子（可跳过）

CSV半结构化数据

```scala
//DF的header和第一行数据
DEST_COUNTRY_NAME,ORIGIN_COUNTRY_NAME,count
United States,Romania,15

val data = spark//python省去类型val
  .read
  .option("inferSchema", "true")//自动推断schema
  .option("header", "true")
  .csv("path")//可用.format("csv")
//python一样，但每行后加“/”分隔。如上文所述，上面操作并不会马上执行。Spark只通过前面几行推断schema

data.sort("count").explain()//查看explain plan，explain可对DF使用，暂不细说。
//设置spark，它默认输出200个partition，下面设置为5。合理的配置能提高效率。
spark.conf.set("spark.sql.shuffle.partitions", "5")
//一个完整的transformation和action任务
data2015.sort("count").take(2)
```

Spark编程是函数式的，意味它不会改变原始数据，且相同输入会得到相同结果。

上述操作也可以用SQL语法实现，而且效率是一样（一样的explain plain）。`data.createOrReplaceTempView("tableName")` 可以将DF转化为表格或视图。spark.sql还可以直接查询路径中的文件spark.sql("SELECT * FROM parquet.\`path_to_parquet_file`")

> 一些惯例：
>
> - 处理数据时先用take或者sample（数据量大时小心）提取部分数据进行模拟处理，确定怎么处理后才用到整个数据上。当所编辑的内容复杂起来后才转到IDEA
> - count后如果数据不大，可用.cache，（在下一次action时）把数据放到内存，而不是每次action都要Spark重新读数据

```scala
val sqlWay = spark.sql("""
SELECT DEST_COUNTRY_NAME, count(1)
FROM tableName
GROUP BY DEST_COUNTRY_NAME
""")
val dataFrameWay = data
  .groupBy('DEST_COUNTRY_NAME)
  .count()
//对上面两个变量调用explain，所得plan一样

//count列的最值
spark.sql("SELECT max(count) from tableName").take(1)
data.select(max("count")).take(1)

//求count总和前5的国家
maxSql = spark.sql("""
    SELECT DEST_COUNTRY_NAME, sum(count) as destination_total
    FROM flight_data_2015
    GROUP BY DEST_COUNTRY_NAME
    ORDER BY sum(count) DESC
    LIMIT 5
    """)
maxSql.show()

data.groupBy("DEST_COUNTRY_NAME")//产生一个RelationalGroupedDataset，需要指明聚合方法（如下面的sum）才能查看结果
    .sum("count")
    .withColumnRenamed("sum(count)", "destination_total")
    .sort(desc("destination_total"))
    .limit(5)//action
    .show()

//每一步都会产生一个新的immutable DF（groupBy除外），可以通过UI的DAG查看各步骤
```

总结：

Spark 是分布式编程模型，用户对它设定transformations和action操作。多步transformations会产生 a directed acyclic graph（指令图，把指令划分为stages和tasks） 。一个action操作在集群启动这些指令。

DataFrames and Datasets 是 transformations和action的操作对象。Transformation会创建新对象，而action还可以将对象转换为native language types。

---

### Spark工具箱

#### 1.Datasets: Type-Safe Structured APIs

用于写特定类型的数据(java和scala)。用户可以通过Dataset API将java/scala的类装进DF（DF里装的是Row类型，它包括各种tabular data）。目前支持JavaBean pattern in Java and case classes in Scala。

Dataset是类型安全的，意味着Spark会记得数据的类型。而DF的row每次提取值都需要getAs来确定类型。

```scala
case class Flight(DEST_COUNTRY_NAME: String,
                  ORIGIN_COUNTRY_NAME: String,
                  count: BigInt)
val flightsDF = spark.read
  .parquet("...fileName.parquet/")
val flights = flightsDF.as[Flight]//df -> dataset，除了case class（Flight），也可以是包含spark type的tuple
//这样就可以对数据进行操作，当用collect或take时，得到的是相应的类Flight，而不是Row

flights
  .filter(flight_row => flight_row.ORIGIN_COUNTRY_NAME != "Canada")//有filterNot 
  .map(fr => Flight(fr.DEST_COUNTRY_NAME, fr.ORIGIN_COUNTRY_NAME, fr.count + 5))
  .take(5)
```



#### 2.Structured Streaming

对输入的数据流分批计算

```scala
//零售商数据，假设按照天分批输入
InvoiceNo,StockCode,Description,Quantity,InvoiceDate,UnitPrice,CustomerID,Country
536365,85123A,WHITE HANGING HEART T-LIGHT HOLDER,6,2010-12-01 08:26:00,2.55

//下面计算一天里每位顾客的消费。（略过spark设置）
//读取数据
val streamingDataFrame = spark.readStream//不同的read
    .schema(staticSchema)//staticSchema自己编写，或从样例中读取数据，转化为视图后调用.schema后获得。有.printSchema方法
    .option("maxFilesPerTrigger", 1)//每次读取文件的个数
    .format("csv")
    .option("header", "true")
    .load(".../*.csv")//某文件夹里所有csv

//计算（全是transformation）
val purchaseByCustomerPerHour = streamingDataFrame
  .selectExpr(
    "CustomerId",
    "(UnitPrice * Quantity) as total_cost",
    "InvoiceDate")
  .groupBy($"CustomerId", window($"InvoiceDate", "1 day"))//scala特色$“”在python中用col("")。注意$只引用其后紧贴的一个String expression，也可用‘InvoiceDate表示
  .sum("total_cost")

//输出
purchaseByCustomerPerHour.writeStream
    .format("memory") // memory 表格存在内存
    .queryName("customer_purchases") // 表格名
    .outputMode("complete") // complete 所有count都在表格里
    .start()

//查看结果
spark.sql("...").show(5)
```



#### 3.Machine Learning and Advanced Analytics

一个Kmean的例子

```scala
//延续上面的数据，使用ML算法时先把DF中各数据类型转换为numerical values vector
staticDataFrame.printSchema()//staticDataFrame是用read获取的DF，非readStream。此处先打印Schema，查看各列的数据类型，是否nullable等
val preppedDataFrame = staticDataFrame
  .na.fill(0)
  .withColumn("day_of_week", date_format($"InvoiceDate", "EEE"))//提取timestamp类的星期，具体查看java.text.SimpleDateFormat
  .coalesce(5)//5个partition，可设置是否shuffle

//分开training和test sets，下面采取手动方式，MLlib有其他APIs来实现。下面的划分方式并非最优
val trainDataFrame = preppedDataFrame
  .where("InvoiceDate < '2011-07-01'")
val testDataFrame = preppedDataFrame
  .where("InvoiceDate >= '2011-07-01'")

//将day_of_week转换为相应的数量值，如Sun -> 7
val indexer = new StringIndexer()
  .setInputCol("day_of_week")
  .setOutputCol("day_of_week_index")
//仅仅用数值表示day_of_week是不合理的，要用OneHotEncoder进一步转化
val encoder = new OneHotEncoder()
  .setInputCol("day_of_week_index")
  .setOutputCol("day_of_week_encoded")
//在使用任何ML算法前，要把col转化为vector（类似与python中pandas转为numpy）
val vectorAssembler = new VectorAssembler()
  .setInputCols(Array("UnitPrice", "Quantity", "day_of_week_encoded"))//python中，Array() -> []
  .setOutputCol("features")
//最后用pipeline把上面三个转换连起来。这样，每次day_of_week有更新，都可调用这个管道
val transformationPipeline = new Pipeline()
  .setStages(Array(indexer, encoder, vectorAssembler))

//确定数据并执行转换
val fittedPipeline = transformationPipeline.fit(trainDataFrame)
val transformedTraining = fittedPipeline.transform(trainDataFrame).cache()//.cache将一份转换后的dataset备份存到内存

//设定和训练ML模型。术语：Algorithm指未训练的模型，AlgorithmModel指训练后的。Estimators可领流程更简单，下面出于step by step展示
val kmeans = new KMeans()
  .setK(20)
  .setSeed(1L)
val kmModel = kmeans.fit(transformedTraining)

//把模型用到test set结果
val transformedTest = fittedPipeline.transform(testDataFrame)
kmModel.computeCost(transformedTest)
```



#### 4.Lower-Level APIs

对于新版的Spark，用户一般只会偶尔运用RDD，例如读取和操作一些非常原始的数据。通常应该用结构化APIs，上面提到的。

```scala
//并行化生成RDD并转化为DF。scala和python中的RDD不一样
spark.sparkContext.parallelize(Seq(1, 2, 3)).toDF()//python：parallelize([Row(1), Row(2), Row(3)])
```



## Part II. Structured APIs—DataFrames, SQL, and Datasets

---

### Structured API Overview

与底层RDD相比，结构化API有schema信息来提供更有效率的储存（Tungsten）和处理（Catalyst），且Spark能够inspect运算的逻辑意义。

结构化APIs可以操作各种数据，包括非结构化log files，半结构化 CSV和结构化的Parquet。这些APIs包括三部分：Datasets, DataFrames, SQL tables and views（这其实和DF一样，用于区分SQL编程）。他们主要应用于batch和streaming计算。这两种计算很容易通过结构化APIs转换。

#### 1.Spark Types

Spark通过*Catalyst*来维护它自身的类型，即Spark type不是其他编程语言中的某种类型，其他语言需要映射成Spark type才能执行。

创建某种类型：`val b = ByteType` （python要加上括号），具体各种数据类型的描述和创建看书中的表格或Spark文档。



**DataFrames and Datasets**

"untyped" DataFrames 指DF在runtime才确定类型。
 "typed" Datasets 则在complie time确定，这是相对于前者的优势，另外就是可以保持数据类型。

简单来说，在Java和Scala中有Datasets，它包含DF和其他非Row类Datasets；在非JVM语言中只有DF。例如：

在Scala中，`spark.range(2).toDF().collect()` 的range产生Datasets，python则直接生成DF

DF是由一系列行组成的，行的类型为Row。这种Row类型是优化过的，避免JVM类型中的garbage-collection and object instantiation消耗。而列代表computation expression，它可被调用于每行。Schemas定义每列的名字和数据类型。

上述讨论仅需知道：使用DF是在享受Spark的内部优化形式。所有的Spark language APIs（适用于Spark的语言）拥有相同的效率。

> garbage-collection会占用更多内存，其产生的序列化时间也会减慢计算

使用Tungstenl既可以 on-heap (in the JVM) 也可 off-heap storage。如果按后者储存，要留足够空间给 off-heap allocations，可通过UI查看。





#### 2.Structured API Execution

- 写代码（DF/Dataset/SQL）并提交
- Spark得到unresolved logical plan（代码合法但未判断data是否存在，如某列、表）
- 分析对比Catalog（保存数据信息）后得到 resolved logical plan
- 通过Catalyst Optimizer逻辑优化得到optimized logical plan
- 从optimized locical plan中得出不同的物理执行策略，利用Cost Model得出最优physical plan ，它包含一系列RDDs和transformation。（很像编译器了）
- 在集群执行最优Physical Plan（执行过程通过生成本地Java字节码去除整个tasks或者stages来进一步优化）

---

### Basic Structured Operations

本节主要为实践。

#### 1.Schemas

通常推断获取是可行的，但出于谨慎，应用于ETL时，最好通过手动设置Schemas。CSV和JSON等plain-text file 可能读取有点慢，有时甚至完全推断不出正确类型。

DF.schema可查看Schema类型，它是StructType包含多个StructField（列信息）

`StructType(StructField(DEST_COUNTRY_NAME,StringType,true)...)` 在python中多了一层List()来包含所有StructField

根据该结构，我们可以自定义Schema

```scala
val myManualSchema = StructType(Array(//Array() -> []
  StructField("DEST_COUNTRY_NAME", StringType, true),//Spark Type
  StructField("ORIGIN_COUNTRY_NAME", StringType, true),
  StructField("count", LongType, false,
    Metadata.fromJson("{\"hello\":\"world\"}"))//可设置元数据，python: metadata={"hello":"world"}
))
//可以用.printTreeString先检查一下

val df = spark.read.format("json").schema(myManualSchema)
  .load("path") 
```



#### 2.Columns and Expressions

在Spark里，列是表达式，它代表一个基于per-record（即每行）计算的值。所以要得到具体值，我们需要row，而row存在于DF，所以col的内容操作必须在DF框架下。

**创建和引用col**

下面是最简单的两种方式

`col("someColumnName")`或`column("someColumnName")`

显示引用`df.col("someColumnName")`或不加参数

> scala可用$"someColumnName"

**表达式**

它指一系列transformations，对象为每个record里的一个或多个值（map之类）。

通过`expr`创建，最简单情况下，`expr("someCol")`相当于`col("someCol")`。

`expr`可以通过逻辑树对string形式的transformation和col引用等表达式进行分析。如`expr("someCol - 5") `相当于`col("someCol") - 5`或`expr("someCol") - 5`。这也是为什么SQL和DF代码会得到相同效果的原因。



#### 3.Records and Rows

Row对象在内部表示为字节数组。我们只用列表达式来操作他们。

**创建和引用**

```scala
val myRow = Row("Hello", null, 1, false)
//在Scala，use the helper methods or explicitly coerce the values。
myRow(0) // type Any，在Python中可以自动确定类型myRow[0]，不需下面操作
myRow(0).asInstanceOf[String] // String
myRow.getString(0) // String
myRow.getInt(2) // Int
```



#### 4.DataFrame Transformations

**创建DF**

```scala
//创建schema
val myManualSchema =  StructType(Array(
   StructField("some", StringType, true),
   StructField("col", StringType, true),
   StructField("names", LongType, false)))//如果names为null，会对创建后的DF进行操作时出错（因为lazy）
val myRows = Seq(Row("Hello", null, 1L))
val myRDD = spark.sparkContext.parallelize(myRows)
val myDf = spark.createDataFrame(myRDD, myManualSchema)//显式创建，如果myRows是Seq(case class())，就可以不加schema或直接.toDF创建。
//上面三行用python只需两行
myRow = Row("Hello", None, 1)
myDf = spark.createDataFrame([myRow], myManualSchema)

myDf.show()

//如果是text输入的RDD
rdd.map(_split(" ")).map(line => caseclass(line(0).toInt, line(1)....)).toDF

rdd.map(_split(" ")).map(line => Row(line(0).toInt, line(1)....)).toDF //还要创建structType

//创建视图，方便SQL
df.createOrReplaceTempView("dfTable") 

myDf.select("id", "v_2").as[(String, Int)]
.groupByKey{case (user, _) => user}
.flatMapGroups{case (a,b) => Some(a)}

sc.para
  .map(x => (x, x))
  .groupByKey( a => a._1)
  .map(a => a._1)
  .count()
```



**非创建新DF操作**

```scala
//选择
//此处指用于理解，通常用selectExpr
df.select("DEST_COUNTRY_NAME", "ORIGIN_COUNTRY_NAME")//里面的“name”和df.col("name"), col("name")，column("name"),'name, $"name", expr("name")是等价的。部分scala特色。注意Col对象和strings不能混用！！！
df.select(expr("DEST_COUNTRY_NAME AS destination").alias("A"))//将DEST_COUNTRY_NAME改名为destination后并建立一个A的alias，即使最后显示的列名为A。
//用selectExpr更灵活，一个string一个col表达式，里面还可写非聚类的SQL。
df.selectExpr("DEST_COUNTRY_NAME as newColumnName", "DEST_COUNTRY_NAME")//选择以新col name显示的DEST_COUNTRY_NAME，和原名显示的DEST_COUNTRY_NAME
df.selectExpr(
    "*", // include all original columns
    "(DEST_COUNTRY_NAME = ORIGIN_COUNTRY_NAME) as withinCountry")//显示所有列以及一列数据类型为boolean的withinCountry
df.selectExpr("avg(count)", "count(distinct(DEST_COUNTRY_NAME))")//对整份数据进行聚类计算
//literals适用于与某值或对象比较前的准备
df.select(expr("*"), lit(1).as("One"))//python只能alias。相当于创建了一列列名为One，改列所有值为1。实际上并没有创建。

//重partition和合并，对经常filtered的col进行partition能提高效率。重partition会进行full shuffle，所以一般只当“当前partition小于未来partition数”或“想根据一组colspartition”时才用。
df.rdd.getNumPartitions
df.repartition(5)
df.repartition(col("DEST_COUNTRY_NAME"))
df.repartition(5, col("DEST_COUNTRY_NAME")).coalesce(2)//合并为2个partition

//收集行到Driver，目前没有特定的operation，但下面方法能达到效果。
df.first 
df.take(5) // selects the first N rows
df.show(5, false) // prints it out nicely。第二个参数是否truncate
df.collect()//collect所有！
df.toLocalIterator()//按partition提取数据as an iterator，可逐个partition地迭代整份数据
```

> 运用expr时，有dash或space的列名要通过（`）把名字扩起来转义



**创建新DF的操作**

```scala
//添加列
df.withColumn("numberOne", lit(1))//lit是将Scala的int转换为Spark类型
df.withColumn("withinCountry", expr("ORIGIN_COUNTRY_NAME == DEST_COUNTRY_NAME"))

//改列名（两种方式）
df.withColumn("Destination", expr("DEST_COUNTRY_NAME")).columns
df.withColumnRenamed("DEST_COUNTRY_NAME", "dest").columns

//改类型
df.withColumn("count2", col("count").cast("long"))

//过滤filter和where皆可
df.filter(!"count < 2").filter("...")//顺序不重要
df.filter($"count" < 2)
df.filter(col("count") < 2)
df.filter(df("count") < 2)

//抽样（良好的编程习惯）
val seed = 5
val withReplacement = false
val fraction = 0.5
df.sample(withReplacement, fraction, seed).count()

//随机划分（适合ML）
val dataFrames = df.randomSplit(Array(0.25, 0.75), seed)
dataFrames(0) // 第一份，python用[0]

//不能append(会改变DF），只能union（创建新DF，目前基于地址而非schema，所以union结果有可能与预料的不一样）
df.union(newDF)
  .where("count = 1")//此处直接解释SQL，下面则需要把"United States"变为lit("United States")后比较
  .where($"ORIGIN_COUNTRY_NAME" =!= "United States")//scala在中Spark中用=!=表不等于，===表等于（上面==是在“”里面的），也可用equalTo,not,leq等

//sort和orderBy，两者一样。有asc_nulls_first，sortWithinPartitions之类的。
df.orderBy(expr("count desc"))//也可orderBy($"count".desc)
df.orderBy(desc("count"), asc("DEST_COUNTRY_NAME"))

//其他简单操作
df.drop("colName")
df.dropDuplicates
df.distinct()
df.limit(5) //其实当数据量大时，limit效率很低，因为它会shuffle，且将所有符合条件的数据存到一个partition中。建议用rdd -> sortBy -> zipWithIndex -> filter(index < n) -> key
```

> Spark默认不是case sensitive，可更改`set spark.sql.caseSensitive true`
>
> String表达式的boolean运算符还可以是=、<>

---

### Working with Different Types of Data

**APIs资料（都在sql模块）**

DF方法：通过DataFrame 或 Dataset 类找。它有两个模块DataFrameStatFunctions（静态方法）和DataFrameNaFunctions（处理null数据）

column方法

其他语言中的类型转换为Spark Types`df.select(lit(5), lit("five"), lit(5.0))`

#### 1.Working with Booleans

```scala
//要将Boolean写成链式，Spark会flatten所有filters成单独statement并返回and的结果。思路是先弄几个filters，然后在where或withColumn里面用and或or把他们连在一起
val priceFilter = col("UnitPrice") > 600
val descripFilter = col("Description").contains("POSTAGE")
df.filter(col("StockCode").isin("DOT")).filter(priceFilter.or(descripFilter))
  .show()
//python中第二三行
descripFilter = instr(df.Description, "POSTAGE") >= 1
df.where(df.StockCode.isin("DOT")).where(priceFilter | descripFilter).show()

//也可以直接创建列
val DOTCodeFilter = col("StockCode") === "DOT"//上面第一个where的filter
df.withColumn("isExpensive", DOTCodeFilter.and(priceFilter.or(descripFilter)))
  .where("isExpensive")//返回该列为true的rows
  .select("unitPrice", "isExpensive").show(5)
```

> 用eqNullSafe("hello")类方法更安全



#### 2.Working with Numbers

```scala
//计算指数
val fabricatedQuantity = pow(col("Quantity") * col("UnitPrice"), 2) + 5
df.select(expr("CustomerId"), fabricatedQuantity.alias("realQuantity")).show(2)
//也可以直接在df.selectExpr()写SQL
df.selectExpr(
  "CustomerId",
  "(POWER((Quantity * UnitPrice), 2.0) + 5) as realQuantity").show(2)

//round默认为up，bound是down
df.select(round(col("UnitPrice"), 1).alias("rounded"), col("UnitPrice"))//保留一位

//相关系数
df.stat.corr("Quantity", "UnitPrice")
df.select(corr("Quantity", "UnitPrice")).show()

//统计，如果代码中需要describe中的数，用mean,stddev,max等来取值
df.describe().select().show()

//计算某分位的值
val colName = "UnitPrice"
val quantileProbs = Array(0.5)
val relError = 0.05//设置0的话耗费大
df.stat.approxQuantile("UnitPrice", quantileProbs, relError) 

//计算各个组合的频率
df.stat.crosstab("StockCode", "Quantity").show()//交叉表显示，所以仅限两个col
df.stat.freqItems(Seq("StockCode", "Quantity")).show()

//添加id
df.select(monotonically_increasing_id()).show(2)
```



#### 3.Working with Strings

```scala
//首字母大写，其他lower，upper
df.select(initcap(col("Description")))

//trim，ltrim，rtrim
lpad(lit("HELLO"), 6, "a")//结果|aHELLO|，3时为|HEL|，3位整体长度，HELLO长度大于3，会从右边截掉部分

//正则表达
//replace
val simpleColors = Seq("black", "white", "red", "green", "blue")
val regexString = simpleColors.map(_.toUpperCase).mkString("|")
//python就直接写“BLACK|WHITE|RED|GREEN|BLUE”
df.select(
  regexp_replace(col("Description"), regexString, "COLOR").alias("color_clean"),
  col("Description")).show(2)//把所有符合regexString的string变为COLOR。对于“”的空白值（连空格都没有的），要用null中的方法替换。
//字母水平的替代
df.select(translate(col("Description"), "LEET", "1337"), col("Description"))
//Description里的L变为1，E变为3……

//提取第一个符合regex的
val regexString = simpleColors.map(_.toUpperCase).mkString("(", "|", ")")
//python就直接写“(BLACK|WHITE|RED|GREEN|BLUE)”，下面就是替换成regexp_extract，并提取第几个符合的
df.select(
     regexp_extract(col("Description"), regexString, 1).alias("color_clean"),
     col("Description")).show(2)

//含有单个
val containsBlack = col("Description").contains("BLACK")//python: containsBlack = instr(col("Description"), "BLACK") >= 1
//含有多个
val simpleColors = Seq("black", "white", "red", "green", "blue")
val selectedColumns = simpleColors.map(color => {
   col("Description").contains(color.toUpperCase).alias(s"is_$color")
}):+expr("*") // 不加:+expr("*")就不能用下面的:_*
df.select(selectedColumns:_*).where($"is_white" || $"is_red")
  .select("Description").show(3, false)
//python实现
simpleColors = ["black", "white", "red", "green", "blue"]
def color_locator(column, color_string):
  return locate(color_string.upper(), column)\
          .cast("boolean")\
		  .alias("is_" + c)
selectedColumns = [color_locator(df.Description, c) for c in simpleColors]
selectedColumns.append(expr("*")) // has to a be Column type

df.select(*selectedColumns).where(expr("is_white OR is_red"))\
  .select("Description").show(3, False)
```



#### 4.Working with Dates and Timestamps

两种类型：datas日期，timestamps日期和时间

> 可在SQL里设置时区`spark.conf.sessionLocalTimeZone`
>
> `TimestampType`支持二级准度，如果使用毫秒或微秒，需要把他们作为longs才能解决问题。

```scala
//下面得到一个10*3的表，第一列为id
val dateDF = spark.range(10)
  .withColumn("today", current_date())
  .withColumn("now", current_timestamp())

|2018-08-24|2018-08-24 02:17:18.861|

//加减数值
dateDF.select(date_sub(col("today"), 5), date_add(col("today"), 5))
//日期差
dateDF.withColumn("week_ago", date_sub(col("today"), 7))
  .select(datediff(col("week_ago"), col("today"))).show(1)
dateDF.select(
    to_date(lit("2016-01-01")).alias("start"),//to_date的格式是根据java SimpleDateFormat的。spark不能转化时用null填充，如yyyy-dd-mm格式不能转
    to_date(lit("2017-05-22")).alias("end"))
  .select(months_between(col("start"), col("end"))).show(1)

//转化为date或datestamp（为解决上面注释的问题，在to_date中加上format），to_timestamp参数一样
val dateFormat = "yyyy-dd-MM"
val cleanDateDF = spark.range(1).select(
    to_date(lit("2017-12-11"), dateFormat).alias("date"))
//比较
cleanDateDF.filter(col("date2") > lit("2017-12-12")).show()
```



#### 5.Working with Nulls in Data

在Spark，缺失值用null比空白要好。DF中的schema表示not nullable时，spark依然会让null进入？

```scala
//Coalesce得到第一个没有null的列，如果“Description”某row为null，该null会被下一列“CustomerId”的同一row位置的非null值替代
df.select(coalesce(col("Description"), col("CustomerId"))).show()
//其他null情况
ifnull(null, 'return_value')
nullif('value', 'value')//相等时null，不等于时返回第二个value
nvl(null, 'return_value')
nvl2('not_null', 'return_value', "else_value")

//drop null
df.na.drop("any", Seq("StockCode", "InvoiceNo"))//默认any，是null就删。all是当所有都为null时删

//fill null（里面可以放map）
df.na.fill(5, Seq("StockCode", "InvoiceNo"))//用5来填充
val fillColValues = Map("StockCode" -> 5, "Description" -> "No Value")
df.na.fill(fillColValues)//python用自己的map形式
```

**其他操作**

```scala
//replace
df.na.replace("Description", Map("" -> "UNKNOWN"))
//python如下
df.na.replace([""], ["UNKNOWN"], "Description")
```



#### 6.Working with Complex Types（structs, arrays and maps）

Struct相当于一组col

```scala
//创建
//方式1
df.selectExpr("(Description, InvoiceNo) as complex", "*")
//方式2
df.selectExpr("struct(Description, InvoiceNo) as complex", "*")
//方式3
val complexDF = df.select(struct("Description", "InvoiceNo").alias("complex"))
//创建Struct后可以通过dot或者getField来引用
complexDF.select("complex.Description")//可以.*，相当于把struct拆分回来
complexDF.select(col("complex").getField("Description"))
```

**Arrays**

```scala
//split
df.select(split(col("Description"), " ")//产生array
  .alias("array_col"))
  .selectExpr("array_col[0]")//可用类似python的语法提取第一个元素
//length
size(Array)//col里面的数据类型是Array
//array_contains
array_contains（Array, "A"）//Array里是否有A
//explode，参数是数据类型为Array的col，为参数列里面的Array里每一个元素创建一行数据，除该元素外，其他列都是原元素所处行的其他列的复制。
df.withColumn("splitted", split(col("Description"), " "))
  .withColumn("exploded", explode(col("splitted")))
  .select("Description", "InvoiceNo", "exploded").show(2)
```

**Map**

```scala
df.select(map(col("Description"), col("InvoiceNo")).alias("complex_map"))//Description列作key，另一个作value
  .selectExpr("complex_map['WHITE METAL LANTERN']")//提取值，key值不匹配的row显示null

//对map用explode可将它们转换回cols
```



#### 7.Working with JSON

```scala
//创建
val jsonDF = spark.range(1).selectExpr("""
  '{"myJSONKey" : {"myJSONValue" : [1, 2, 3]}}' as jsonString""")

jsonDF.select(
    get_json_object(col("jsonString"), "$.myJSONKey.myJSONValue[1]") as "column"// 2,
    json_tuple(col("jsonString"), "myJSONKey")//{"myJSONValue" : [1, 2, 3]}
).show(2)

//to_json参数为StructType或Map，from_json操作相反，不过要创建schama
val parseSchema = StructType(Array(
  StructField("InvoiceNo",StringType,true),
  StructField("Description",StringType,true)))
df.selectExpr("(InvoiceNo, Description) as myStruct")
  .select(to_json(col("myStruct")).alias("newJSON"))
  .select(from_json(col("newJSON"), parseSchema), col("newJSON")).show(2)
```



#### 8.User-Defined Functions

定义的函数会被序列化后发送到所有executors。用Scala或Java写的function除了发送外没有其他额外消耗。但是python会有，Spark需要在worker节点上启动python进程，将数据转化为该进程理解的形式，得出结果后还要转换回来。

```scala
//从创建到使用
val udfExampleDF = spark.range(5).toDF("num")
def power3(number:Double):Double = number * number * number
val power3udf = udf(power3(_:Double):Double)
udfExampleDF.select(power3udf(col("num")))

//登记后，可由DF函数变为Spark SQL函数，且可以跨语言使用。但还不能用在string表达式
spark.udf.register("power3", power3(_:Double):Double)//python要多加DoubleType()的类型参数
udfExampleDF.selectExpr("power3(num)").show(2)
```

> 通过Hive写的UDF或SQL，在创建SparkSession要加上`.enableHiveSupport()`，这只支持预编译的Scala和Java包，需要加依赖（Maven就spark-hive_2.11之类的）。下面TEMPORARY可删，如果想登记为永久function在Hive Metastore上
>
> ```mysql
> CREATE TEMPORARY FUNCTION myFunc AS 'com.organization.hive.udf.FunctionName
> ```

---



### Aggregations

聚类需要key或grouping和一个聚类函数。该函数需要每个group产生一个结果。

groupings形式：整个DF（如select statement）、group by（n键，n聚类函数）、window（和group by 一样，但rows分批）、grouping set（即多层group by，SQL原生，DF则通过rollup和cube）

grouping产生RelationalGroupedDataset类

> - 下面会有一些approximation functions，毕竟大量数据聚合会很费时间，使用这些函数也有利于交互和临时分析
> - 加载大量小文件（总量也小）可以在.load后.coalesce减少partition并cache

#### 1.Aggregation Functions

基本都在` org.apache.spark.sql.functions`

```scala
//count
df.count()//这个action既可了解数量，也可触发cache。另外要注意，在使用count(*)时，即使某行全是null，仍会计算。而单独列的count会忽略null，即df.select(count(col("xxx")))

//countDistinct
df.select(countDistinct("StockCode")).show() # 4070
approx_count_distinct("StockCode", 0.1) # 3364

//各种一看就懂的函数
first, last, min, max, sum, sumDistinct
var_pop, var_samp, stddev_pop
skewness, kurtosis//偏斜和峰度
corr, covar_pop, covar_samp//当然，这需要两个列

//平时计算完习惯用alias，下面有很多灵活的实现方式
df.select(
    count("Quantity").alias("total_transactions"),
    sum("Quantity").alias("total_purchases"),
    avg("Quantity").alias("avg_purchases"),
    expr("mean(Quantity)").alias("mean_purchases"))
  .selectExpr(
    "total_purchases/total_transactions",
    "avg_purchases",
    "mean_purchases").show()

//聚合为复合类型
df.agg(collect_set("Country"), collect_list("Country")).show()
```



#### 2.Grouping

```scala
//grouping with count
df.groupBy("InvoiceNo", "CustomerId").count().show()

//在groupby的基础上进行多个聚类计算
df.groupBy("InvoiceNo").agg(//类似select，但只放聚合方法
  count("Quantity").alias("quan"),//count既是method又是expression，但我们很少把count作为后者使用，如下一行。
  expr("count(Quantity)")).show()

//grouping with Maps
df.groupBy("InvoiceNo").agg("Quantity"->"avg", "Quantity"->"stddev_pop")
```



#### 3.Window Functions

支持三类函数：ranking, analytic and aggregate

分组聚合，与其他组无关

```scala
val dfWithDate = df.withColumn("date", to_date(col("InvoiceDate"),
  "MM/d/yyyy H:mm"))

val windowSpec = Window
  .partitionBy("CustomerId", "date")//将Id和date相同的分为一组
  .orderBy(col("Quantity").desc)//Quantity越大，下面的rank排名越低
  .rowsBetween(Window.unboundedPreceding, Window.currentRow)//all previous rows up to the current row，也有rangeBetween，即按隔多少个值来划分，如rangeBetween(Window.currentRow, 1)中，下面不管有多少个1和2，1的统计值都会是1和2所有值的总和
+---+--------+---+
| id|category|sum|
+---+--------+---+
|  1|       a|  7|
|  1|       a|  7|
|  1|       a|  7|
|  2|       a|  4|
|  2|       a|  4|
+---+--------+---+

//直接用
df.withColumn("sum", sum(col("Quantity")).over(windowSpec)).show()

//聚合方法.over产生expression/col，可用于select
val maxPurchaseQuantity = max(col("Quantity")).over(windowSpec)
val purchaseDenseRank = dense_rank().over(windowSpec)// 排名没有gap，和下面一样，window只能用.unboundedPreceding和.currentRow
val purchaseRank = rank().over(windowSpec)

fWithDate.where("CustomerId IS NOT NULL").orderBy("CustomerId")
  .select(
    col("CustomerId"),
    col("date"),
    col("Quantity"),
    purchaseRank.alias("quantityRank"),
    purchaseDenseRank.alias("quantityDenseRank"),
    maxPurchaseQuantity.alias("maxPurchaseQuantity")).show()
```



#### 4.Grouping Sets

跨组聚合。操作前先删除null值！

```scala
//rollup，下面方法其实直接用SQL更方便。结果中的null表示总计。下面代码的结果包括：
//（1）所有日期和国家的Quantity总数
//（2）每个日期所有国家的Quantity总数
//（3）每个国家在每个日期的Quantity总数
val rolledUpDF = dfNoNull.rollup("Date", "Country").agg(sum("Quantity"))
  .selectExpr("Date", "Country", "`sum(Quantity)` as total_quantity")
  .orderBy("Date")

//cube是更深层的聚合。下面代码的结果比上面多了：
//（4）每个国家所有日期的Quantity总数
dfNoNull.cube("Date", "Country").agg(sum(col("Quantity")))
  .select("Date", "Country", "sum(Quantity)").orderBy("Date").show()

//grouping_id可以提取cube的某个层次。下面代码中，id数字对应的层次：3-(1)，2-(2), 1-(4), 0-(3)
dfNoNull.cube("customerId", "stockCode").agg(grouping_id(), sum("Quantity"))
.orderBy(expr("grouping_id()").desc)
.filter($"grouping_id()" === 0)
.show()

//Pivot透视表
val pivoted = dfWithDate.groupBy("date").pivot("Country").sum()
```



#### 5.UDAFs

自定义聚类函数，麻烦，但比ds的mapGroups和rdd的aggregateByKey高效。

```scala
//先要创建类
class BoolAnd extends UserDefinedAggregateFunction {
  def inputSchema: org.apache.spark.sql.types.StructType =
    StructType(StructField("value", BooleanType) :: Nil)//加Nil转换为List包裹的StructField
  
  //一个容器，存放计算时临时产生的结果
  def bufferSchema: StructType = StructType(
    StructField("result", BooleanType) :: Nil
  )
  
  //返回类型
  def dataType: DataType = BooleanType
  
  //相同输入是否返回相同结果
  def deterministic: Boolean = true
  
  //初始化上面bufferSchema设定的内容
  def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = true
  }
  
  //根据当前row如何更新buffer。对于两个arg，要通过.getAs[T](index or colName)来得相应的值  
  def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    buffer(0) = buffer.getAs[Boolean](0) && input.getAs[Boolean](0)
  }
  def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    buffer1(0) = buffer1.getAs[Boolean](0) && buffer2.getAs[Boolean](0)
  }
  
  //结果
  def evaluate(buffer: Row): Any = {
    buffer(0)
  }
}

//使用
val ba = new BoolAnd
spark.udf.register("booland", ba)//登记名字
import org.apache.spark.sql.functions._
spark.range(1)
  .selectExpr("explode(array(TRUE, TRUE, TRUE)) as t")
  .selectExpr("explode(array(TRUE, FALSE, TRUE)) as f", "t")
  .select(ba(col("t")), expr("booland(f)"))
  .show()
```

---



### Join

#### 1.实践

```scala
//至少要有joinExpression，joinType可选
var joinType = "inner"//默认inner，所以这里是多余的
val joinExpression = person.col("graduate_program") === graduateProgram.col("id")
//然后才能join
person.join(graduateProgram, joinExpression, joinType).show()

//self joins
df1.join(df1,"id").show()
```

> **joinType**: outer, left_outer, right_outer, left_semi(这个本质上不是join，而是filter。对应的右侧数据存在，那左侧rows保留), left_anti(存在则去掉)，cross笛卡尔（最危险）
>
> 如果key1在两个表不是唯一的，那么innerjoin也是会crossjoin的。即表1有2个key1，表2有2个key1，则innerjoin后会有2 x 2行

```scala
//复杂类型join，joinExpression的逻辑可能是一个表的一个row和另一个表的所有row比较，只要返回true就连在一起。所以只要joinExpression包含两表的信息，且返回boolean即可，这样能够实现很多复杂的join判断
//下面的join的判断为person表中id列的值是否存在于sparkStatus表中的spark_status数组中
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
//重名列。假设person和gradPrograDupe有同名列graduate_program。此时join后选择同名列自然会出错，可在select(person.col("graduate_program"))来指明
//下面join时去掉右边组的同名列
person.join(gradProgramDupe,"graduate_program").select("graduate_program").show()
```



#### 2.Spark如何实现Joins

node-to-node communication strategy  和 per node computation strategy 对应下面两种方式

shuffle join和broadcast join。前者的情况是，两个表都很大，不能单独存到一个worker节点上，并有剩余空间存另一个表的一部分，这时worker就必须communicate了。后者情况是，如果其中一个表足够小，可以把该表广播到各个worker节点上，广播后就不需交流了。当然，广播的过程也是有消耗的。

当两个表都很小时，最好让Spark自己决定。如果发现什么奇怪情况，也可以通过下面方式设置。

```scala
person.join(broadcast(graduateProgram), joinExpr).explain()//可查看计划，看Spark采取哪种策略。这里已经设置了broadcast了
```

---



### 数据源Data Sources

六大核心数据源CSV, JSON, Parquet, ORC, JSBC/ODBC connections, Plain-text files，更多去spark-packages.org下载。

将不同数据源，经过混合处理，写到特定的系统上去

#### 1.Reader and Writer

**Read API**

```scala
spark.read.format(...).option("key", "value").schema(...).load()
```

format(Parquet默认)，option和schema都是可选，不会改变reader类型

`option("mode", "x")`中x可填下面三种：

- permissive默认，遇到损坏的records时将所有字段设置为null，并将所有损坏的记录放在名为“_corrupt_record”的字符串列中
- dropMalformed删除有问题的records
- failFast有问题直接fail

> 由于lazy的原因，即便是找不到文件的问题，也要等Spark真正action才会发现

**Write API**

```scala
df.write.format(...).option(...).partitionBy(...).bucketBy(...).sortBy(...).save(folder)
```

format(parquet默认)

mode有append，overwrite，errorIfExists（默认），ignore



#### 2.各种数据格式的介绍

**CSV Files**

选项（只是常用）sep, header, escape（转义符）, inferSchema, ignore(Leading/Trail)WhiteSpace, nullValue(null值的形式), nanValue, positiveInf(正无穷),  negativeInf, compression or codec, dateFormat, timestampFormat, maxColumns(20480), maxCharsPerColumn, escapeQuotes（是否转义）, maxMalformedLogPerPartition(错误记录长度10), quoteAll, multiLine



**JSON Files**

在Spark, 一般指的是单行分隔JSON。（可以通过multiLine设定多行）

选项compression or codec, dateFormat, timestampFormat, primitiveAsString, allowComments, allowUnquotedFieldNames, allowSingleQuotes, allowNumbericLeadingZeros, allowBackslashEscapingAnyCharacter, columnNameOfCorruptRecord, multiLine



**Parquet Files**

 column-oriented，可以提取特定列。支持复杂类型（即array等）。读取方便，file本身有schema。

> 旧版本Spark写出的Parquet和新版的兼容不太好？

选项compression.codec, mergeSchema



**ORC Files**

和Parquet很相似，前者更适合Hive，后者更适合Spark。

没有选项。

读取hive table时，spark会改变元数据并cache结果。如果元数据被改变了，可以用`sqlContext.refreshTable("tablename") `来更新。可以取消cache: ` spark.sql.parquet.cacheMetadata = false`



**SQL Databases**

选项有很多，如numPartitions，此处略过。

```scala
//读写数据库需要两样东西JDBC driver和数据源
//测试链接
val connection = DriverManager.getConnection(url)
connection.isClosed()
connection.close()

val dbDataFrame = spark.read.format("jdbc").option("url", url)
  .option("dbtable", tablename).option("driver",  driver).load()//书中有PostgreSQL的读取
```

Spark在对数据库数据进行操作时，会用各种filter以提高load效率。比如查询某列，Spark只会load该列；如果用filter，spark会直接在数据库filter再load。

但通常的做法是先用SQL查询，然后Spark读取该查询的数据。例如想下面写一个SQL查询，然后放到.option("dbtable", pushdownQuery)

```scala
val pushdownQuery = """(SELECT DISTINCT(DEST_COUNTRY_NAME) FROM flight_info)
  AS sub_flight_info""” 
```

可以将一份数据放到不同的partitions，也可多份一partition。`.option("numPartitions", 10)`

```scala
val props = new java.util.Properties
props.setProperty("driver", "org.sqlite.JDBC")
val predicates = Array(
  "DEST_COUNTRY_NAME = 'Sweden' OR ORIGIN_COUNTRY_NAME = 'Sweden'",
  "DEST_COUNTRY_NAME = 'Anguilla' OR ORIGIN_COUNTRY_NAME = 'Anguilla'")//这两个结果是不相交的，如果相交，会有重复项出现
spark.read.jdbc(url, tablename, predicates, props).rdd.getNumPartitions // 另一种读取数据库的方法。
```

可以设定读取窗口

```scala
//下面根据tablename中的一个列colName进行partition，并设置了窗口的最值。数据会被平均分到numPartitions个partition里
spark.read.jdbc(url,tablename,colName,lowerBound,upperBound,numPartitions,props)
```

写入数据库

```scala
val newPath = "jdbc:sqlite://tmp/my-sqlite.db"
csvFile.write.mode("overwrite").jdbc(newPath, tablename, props)
```



**Text Files**

读写

```scala
spark.read.textFile("/data/flight-data/csv/2010-summary.csv")
  .selectExpr("split(value, ',') as rows").show()
//写的时候要保证只有一col的string，而非partition写，但partition写就不会是单个文件。
csvFile.select("DEST_COUNTRY_NAME").write.text("path/file")
```



#### 3.Advanced I/O Concepts

**可分隔的文件类和压缩**

把文件复制多份存到HDFS中，可以提高I/O效率（并行读写）。但同时要管理好压缩。本书推荐Parquet和gzip。

**partition输出**（Bucketing可能更好）

`csvFile.write.mode("overwrite").partitionBy("DEST_COUNTRY_NAME").save()`

这样每个country都有一个folder。当用户经常filter某个对象，可以用这种方式输出

**Bucketing**

控制数据写到制定的每个file，这样在读取，join或agg时，就可以避免一些shuffle。

```scala
csvFile.write.format("parquet").mode("overwrite")
  .bucketBy(numberBuckets, columnToBucketBy).saveAsTable("bucketedFiles")
```

**控制文件输出大小**

在option里设置maxRecordsPerFile

---



### Spark SQL

连接Hive metastore有几个步骤：设定`spark.sql.hive.metastore.version`。如果要改变HiveMetastoreClient的初始化，还要设置`spark.sql.hive.metastore.jars`。合适的类前缀`spark.sql.hive.metastore.sharedPrefixes`

#### 1.运行Spark SQL 查询的三种方式

**设置服务器**

通过Spark SQL CLI实现，但它不能与Thrift JDBC server通信。`./bin/spark-sql`

配置Hive在conf中的三个文件hive-site.xml, core-site.xml, and hdfs-site.xml。完整的option查`spark-sql --help`



**通过语言接口**

SQL和DF转换：load后.createOrReplaceTempView("tableName")就得到SQL所用的table。然后通过SparkSession.sql("")便可写sql语法查询该表，它返回DF。这个方法很有用，因为有些转换代码在SQL中比在DF中更容易写。



**SparkSQL Thrift JDBC/ODBC Server**

一些软件，如Tableau可通过Java Database Connectivity (JDBC) interface 连接Spark driver去执行Spark SQL查询。通过`./sbin/start-thriftserver.sh`启动服务，该脚本接受所有spark-submit命令行选项，例如`--master local[2] --jars xx/mysql-connector-java-xxx.jar`。服务默认在localhost:10000，可通过环境变量或系统特征来修改，如`--hiveconf hive.server2.thrift.port=xxxx`。之后通过beeline测试连接，即`beeline -u jdbc:hive2:localhost:10000 -n hadoop`其中n是用户名。

这种方式不管有多少个客户端，都是一个spark application，使得多个客户端共享数据。



#### 2.Catalog

Spark SQL的最顶层抽象，它关系到元数据、databases、table、function和views。

**table**

它在逻辑上等价于DF，可以对它进行前几节提到的操作，不同在于后者定义在编程语言中，前者定义在database中。这意味着创建一个table就是一个default database。

在2.X中没有temporary table，只有views（不存数据），所有tables都会有数据。这意味着drop a table会丢失数据。

managed 和 unmanaged tables，前者为通过saveAsTable实现的table（Spark记录所有相关信息），后者为其他方法定义的table。

**创建和插入tables**

语法和之前的I/O类似

```mysql
COMMENT "managed table"
CREATE TABLE flights_csv (
  DEST_COUNTRY_NAME STRING,
  ORIGIN_COUNTRY_NAME STRING COMMENT "remember, the US will be most prevalent",
  count LONG)
USING csv OPTIONS (header true, path '/data/flight-data/csv/2015-summary.csv')

COMMENT "也可以通过query创建。加上IF NOT EXISTS更好，如果去掉USING parquet，就会默认创建Hive兼容表"
CREATE TABLE IF NOT EXISTS flights_from_select USING parquet PARTITIONED BY (DEST_COUNTRY_NAME) AS SELECT * FROM flights LIMIT 5

COMMENT "unmanaged"
CREATE EXTERNAL TABLE hive_flights_2
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
LOCATION '/data/flight-data-hive/' AS SELECT * FROM flights

COMMENT "插入，Partition可选"
INSERT INTO partitioned_flights
  PARTITION (DEST_COUNTRY_NAME="UNITED STATES")
  SELECT count, ORIGIN_COUNTRY_NAME FROM flights
  WHERE DEST_COUNTRY_NAME='UNITED STATES' LIMIT 12
```

**元数据**

```mysql
DESCRIBE TABLE flights_csv # 查看comment信息
SHOW PARTITIONS partitioned_flights # 查看partitioned table的信息

# REFRESH TABLE刷新与表关联的所有缓存条目（实质上是文件）。 如果该表先前已被缓存，则下次扫描时它会lazily缓存
REFRESH table partitioned_flights
# repair主要是收集新partition信息
MSCK REPAIR TABLE partitioned_flights
```

**drop tables（数据会丢失，谨慎）**

```mysql
#不能删，只能drop
DROP TABLE IF EXISTS flights_csv;
```

如果drop的是unmanaged table，数据不会丢失，只是不能引用该table name

> 可以CACHE或UNCACHE TABLE



**View**

```mysql
# 创建。在VIEW前可加TEMP，创建仅在当前会话期间可用但未注册到数据库的临时视图。还可以再加GLOBAL，这样可以在整个Spark app中可见，但会在会话结束时被删除。有
CREATE OR REPLACE GLOBAL VIEW just_usa_view AS
  SELECT * FROM flights WHERE dest_country_name = 'United States'

# Drop
DROP VIEW IF EXISTS just_usa_view;
```



**Databeases**

组织tables的工具。

```mysql
SHOW DATABASES
CREATE DATABASE some_db
USE some_db #USE default直接到默认
SHOW tables IN databaseName # IN可选
SELECT * FROM default.flights #可以查询其他库的表
SELECT current_database() #查看当前数据库
DROP DATABASE IF EXISTS some_db;
```



#### 3.Advanced Topics

**复杂类型**（Structs, Lists, Maps）

```mysql
# Structs
CREATE VIEW IF NOT EXISTS nested_data AS
	SELECT (DEST_COUNTRY_NAME, ORIGIN_COUNTRY_NAME) as country, count FROM flights
	
SELECT country.DEST_COUNTRY_NAME, count FROM nested_data

# List，分collect_list and collect_set
# 下面代码由于groupby，所以同组的数据会放到一个分collect_list或分collect_set里。可用[0]提取第一个数
SELECT DEST_COUNTRY_NAME as new_name, collect_list(count) as flight_counts,
  collect_set(ORIGIN_COUNTRY_NAME) as origin_set
FROM flights GROUP BY DEST_COUNTRY_NAME
# 下面会给每个row添加一列，该列每个元素为ARRAY(1, 2, 3) 
SELECT DEST_COUNTRY_NAME, ARRAY(1, 2, 3) FROM flights
# 同样可以用explode拆开
SELECT explode(collected_counts), DEST_COUNTRY_NAME FROM flights_agg
```



**SQL Functions**

```mysql
SHOW FUNCTIONS "s*";
SHOW USER FUNCTIONS
SHOW SYSTEM FUNCTIONS
DESCRIBE #查functions描述
```



**Subqueries**

```mysql
#Uncorrelated predicate subqueries
SELECT * FROM flights
WHERE origin_country_name IN (SELECT dest_country_name FROM flights
      GROUP BY dest_country_name ORDER BY sum(count) DESC LIMIT 5)

#Correlated predicate subqueries 下面查询有往返的航线
SELECT * FROM aa f1
WHERE EXISTS (SELECT 1 FROM aa f2
            WHERE f1.dest_country_name = f2.origin_country_name)
AND EXISTS (SELECT 1 FROM aa f2
            WHERE f2.dest_country_name = f1.origin_country_name)

#Uncorrelated scalar queries 添加辅助信息，下面就是添加一列maximum
SELECT *, (SELECT max(count) FROM flights) AS maximum FROM flights
```



#### 4.Miscellaneous Features

**配置SQL**

```mysql
SET
spark.sql.inMemoryColumnarStorage.compressed //true
spark.sql.inMemoryColumnarStorage.batchSize //10000
spark.sql.files.maxPartitionBytes //128MB
spark.sql.files.openCostInBytes //4MB高估比较好
spark.sql.broadcastTimeout // 300s，超时就不再broadcast了
spark.sql.autoBroadcastJoinThreshold //10M，文件小于多少会自动广播到所有workers
spark.sql.shuffle.partitions//200
```

---



### Datasets

编码器指示Spark生成代码去序列化T对象。当使用DF或标准Structured APIs时，二进制结构会变成Row类型。如果用Dataset API的话，二进制结构就会变回T。当Row转化为T类时，会影响效率，但并没有python用UDF时影响大。

使用Datasets一般有三个理由：DF操作无法满足， 需要type-safety，某些情况比较方便。

Datasets的一些代码，主要是展示如何使用

```scala
//创建，你定义schema
//Java
public class Flight implements Serializable{
  String DEST_COUNTRY_NAME;
  String ORIGIN_COUNTRY_NAME;
  Long DEST_COUNTRY_NAME;
}

Dataset<Flight> flights = spark.read
  .parquet("path")
  .as(Encoders.bean(Flight.class));

//Scala要创建的是单例类，该类的特征为：immutable，模式匹配时可解构，基于值比较而非引用。
case class Flight(DEST_COUNTRY_NAME: String,
                  ORIGIN_COUNTRY_NAME: String, count: BigInt)
val flightsDF = spark.read.parquet("path")
val flights = flightsDF.as[Flight]
//也可以直接对对象创建dataset，下面pandaPlace是一个case class的实例。也可以直接toDS()
df = spark.createDataFrame(Seq(pandaPlace))
//多层次schema，StructType构造器有很多，加上Array来具体确定其中一个；ArrayType(Type, Boolean)，其中Boolean默认true
val pandasType = ArrayType(StructType(Array(
    StructField("id",LongType,false),
    StructField("zip",StringType,true),
    StructField("pt",StringType,true),
    StructField("happy",BooleanType,false),
    StructField("attributes",ArrayType(DoubleType,false),true))),
  true)
val tschema = StructType(Array(
  StructField("name",StringType,true),
  StructField("pandas",pandasType,true)))

//action现在通过action方法返回某个record就能直接通过.valueName来get值
flights.first.DEST_COUNTRY_NAME

//transformation，SQL有的就不要自定义，麻烦且效率会降低。下面不是UDF，是泛型函数
//filter
def originIsDestination(flight_row: Flight): Boolean = {
  return flight_row.ORIGIN_COUNTRY_NAME == flight_row.DEST_COUNTRY_NAME
}
flights.filter(flight_row => originIsDestination(flight_row))
//mapping
val destinations = flights.map(f => f.DEST_COUNTRY_NAME)

//Joins
case class FlightMetadata(count: BigInt, randomData: BigInt)

val flightsMeta = spark.range(500).map(x => (x, scala.util.Random.nextLong))//自动变为两列
  .withColumnRenamed("_1", "count").withColumnRenamed("_2", "randomData")
  .as[FlightMetadata]
//下面join的结果是两个cols，里面各自每row存放一个相应的类实例
val flights2 = flights
  .joinWith(flightsMeta, flights.col("count") === flightsMeta.col("count"))
//提取某列的值
flights2.selectExpr("_1.DEST_COUNTRY_NAME")

//如果直接用join，就会拆开原来的type，每个变量一列，合并成一个Row类DF。下面toDF可加可不加，指用于表示DF可与Dataset合并
val flights2 = flights.join(flightsMeta.toDF(), Seq("count"))

//grouping和aggregations
//groupBy，rollup和cube都可用，但会返回DF（即失去所定义类型的信息）
//如果想保留信息，用groupByKey
flights.groupByKey(x => x.DEST_COUNTRY_NAME).count()//结果是Dataset[]里面的key是原类型变量的类型string
//其他聚类用agg加聚类函数加as把col转化为TypedColumn
ds.groupByKey(row => row.ID).agg(max("V2").as[Int]).show

flights.groupBy("DEST_COUNTRY_NAME").count()//结果是DF，里面的类型是Spark Tpye
//groupByKey后，我们得到KeyValueGroupedDataset，即（key,原对象），可根据它可调用的方法自定义函数。如果调用flatMapGroups的话，形式如下，其中countryName是key，Flight是各组中的对象的类
def grpSum(countryName:String, values: Iterator[Flight]) = {
  values.dropWhile(_.count < 5).map(x => (countryName, x))//并没有任何合并
}

flights.groupByKey(x => x.DEST_COUNTRY_NAME).flatMapGroups(grpSum).show(2)
+--------+---------------------------+
|_1      |_2                         |
+--------+---------------------------+
|Anguilla|[Anguilla,United States,21]|
|Paraguay|[Paraguay,United States,90]|
+--------+---------------------------+
//mapGroups
ds.groupByKey(row => row.ID).mapGroups{ case (g, iter) => (g, iter.map(_.V3).reduce(_+_))}.show

//调用mapValues
def grpSum2(f:Flight):Integer = {
  1
}
flights.groupByKey(x => x.DEST_COUNTRY_NAME).mapValues(grpSum2).count().take(5)
//调用reduceGroups
def sum2(left:Flight, right:Flight) = {
  Flight(left.DEST_COUNTRY_NAME, null, left.count + right.count)
}
flights.groupByKey(x => x.DEST_COUNTRY_NAME).reduceGroups((l, r) => sum2(l, r))
  .take(5)
```

---

参考：
书籍：
Spark: The Definitive Guide
包含数据科学家和工程师所需的基础内容
不深入ML，但会介绍如何用
重点关注应用开发而非运行管理，但还是会有监测、debygging和配置的内容。

High Performance Spark

网站：
http://spark.apache.org/docs/2.1.2/cluster-overview.html  

