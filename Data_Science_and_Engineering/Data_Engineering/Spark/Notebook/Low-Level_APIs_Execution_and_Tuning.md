## Part III. Low-Level APIs

通常用Part II的APIs就够了，它通常“more efficient, more stable, and more expressive”，还能省内存。

需要底层APIs的情况：

- 高层无法满足，如准确控制数据的物理位置
- 维护一些RDD写的代码
- 自定义一些共享变量

底层APIs包括两类：RDD和共享变量

入口：SparkSession.SparkContext，是集群和Spark App的联系

------

### Resilient Distributed Datasets (RDDs)

RDDs是1.X的主要API，2.X直接用的比较少。

#### 1.介绍

RDD在物理层面就是一批节点上的一批数据（由关联的partition组成），代码中是底层数据结构，是“an immutable, partitioned collection of records”，每个records就是某一编程语言的object，不像DF，每个record被schema限制。然而，这需要自己造轮子，比如自己定义object（属性，方法，接口等），做更多手动调优（因Spark不了解这个object）。通过Java和Scala使用RDD的消耗不大，主要在于处理raw objects（Spark不懂）。python就耗费大了。

正式定义RDD的主要特征：

- partitionlist：`partitions()`返回partition对象的数组，数组的index对应下面partitioner中的`getPartition`
- 计算每个partition的函数：`iterator(p, parentIters)`，p是partition，parentIters是来源RDD的iterator，该函数被action函数调用
- 与其他RDD的依赖list（用于数据恢复）：`dependencies()`返回a Seq of dependency对象，反映依赖关系
- 可选，KV RDD的partitioner，基于hash或range，可自定义（通常使用RDD的原因）：`partitioner()`返回partitioner对象[Scala option类]，如果RDD非tuple，则返回null
- 可选，一个preferred locations list（for HDFS file，尽可能将计算任务分配到其所在的存储位置）：`preferredLocations(p)`返回p在哪个节点的信息a Seq of strings，

RDD遵循Spark的编程范式，如transformations, lazily, actions。其中action包括：reduce, collect, count, first, take, takeSample, takeOrdered, saveAsTextFile, saveAsSequenceFile, saveAsObjectFile, countByKey, foreach等。大部分写到Hadoop的function只适用于pairRDD或NewHadoopRDD

> RDD类似Dataset，互相转化方便(rdd -> df可能需要一点运算)。但Dataset是储存在或利用结构化数据engine的，Spark对它提供更多便利和优化。

RDDs有很多种类，大部分被DF API用于优化physical execution plans。用户一般只会用到泛型RDD或key-value RDD。RDD包裹不同的数据类型决定其所能调用的函数，如果数据类型的信息丢失，相应的函数调用自然会失败。

**细节补充：**

（1）弹性：内存放不下，自动到磁盘
（2）容错：某节点数据丢失后，自动从其来源节点重新计算
（3）位置感知：上面的依赖list

（4）RDD除了上面五个函数实现，还有通用的function。适用于特定类型RDD的函数会定义在特定的function class里，如“PairRDDFunctions, OrderedRDDFunctions, and GroupedRDDFunctions” 通过隐式转使方法可以执行

（5）其他类型RDD，如NewHadoopRDD（通过hdfs创建的rdd），ShuffledRDD（partitioned的rdd）。通过`toDebugString`可得到rdd类型和他的父rdd列表

（4）RDD依赖：

血统（Lineage）：记录RDD的原数据和转换行为，以便恢复丢失数据  

（5）不像高层API会自动优化，需要自己安排操作顺序。



#### 2.RDD代码

```scala
//创建（不改变来源的类型）
//从Dataset[T]到RDD[T]
spark.range(100).rdd
//从DF[Row]到RDD[Row]，所以要转换。也可以toDF转回来
spark.range(10).toDF().rdd.map(rowObject => rowObject.getLong(0))
//从集合
val myCollection = "Spark The Definitive Guide".split(" ")
val words = spark.sparkContext.parallelize(myCollection, 2)//也可以当作是把数据分配到集群的方法
//从数据源
spark.sparkContext.textFile("path")//每个record一行string
spark.sparkContext.wholeTextFiles（）//每个record一个file（文件名为第一个对象，文件值为第二个对象？）
//其他
sequenceFile(Hadoop序列化类型), hadoopRDD, objectFile(针对saveAsObjectFile进行反序列化)

//可为RDD取名，并显示在UI
words.setName("myWords")
words.name

//transformation: distinct，filter, map(MapPartitionsRDD), flatMap, sort
words.randomSplit(Array[Double](0.5, 0.5))//返回RDD数组，第一个参数为比重，第二个为种子

//actions: reduce(f:(T, T) => T), count, countApprox, countApproxDistinct，countByValue，countByValueApprox，first，max，min，mean，stats，take, takeOrdered, top(takeOrdered的倒序)，takeSample
words.countApprox(timeoutMilliseconds, confidence)
words.countApproxDistinct(0.05)//参数为相对精度，至少大于0.000017，越小越占空间
words.countApproxDistinct(p, sp)//p为精度，sp为空间精度，相对精度大概为1.054 / sqrt(2^P)。设置非零的sp>p可以可以减少内存消耗和增加精度
parsed.rdd//把df转为rdd，下面仅作演示，用df的groupBy("is_match").count().show更好
  .map(_.getAs[Boolean]("is_match"))//将rdd中每个row map为Boolean（根据is_match列）
  .countByValue()
//结果Map(true -> 20931, false -> 5728201)，会存到driver的内存，只有当row数量小或distinct的数量小才能用。可用countByValueApprox或者reduceByKey（不会把结果返回到client）
words.countByValueApprox(timeoutMilliseconds, confidence)
words.takeSample(withReplacement, numberToTake, randomSeed)

//Set Operation
"""
union：拼接
intersection：取相同，重复的去掉
subtract：去掉rdd1中与rdd2相同的元素
"""


//Saving Files，只能写到纯文本file。存到一个数据源实际上需要迭代每个partition。
words.saveAsTextFile("file:/tmp/bookTitle")//2partition各产生一个文件
words.saveAsTextFile("file:/tmp/bookTitleCompressed",classOf[BZip2Codec])
words.saveAsObjectFile("/tmp/my/sequenceFilePath")//二进制KV对的flat file，主要Hadoop用？

//Caching，a)计算结果需要重复使用，b)同一个rdd上多个action，c)计算每个partition的成本很高时使用（Spark app结束后会被删除）。如果rdd不再使用，可用unpersist清理。
//详细cache等级看调优部分
words.cache()
words.getStroageLevel
//下面在action之间加persist后，第二个action调用sorted时就不用再执行sortByKey了
val sorted = rddA.sortByKey()
val count = sorted.count()
val sample: Long = count / 10
rddA.persist()
sorted.take(sample.toInt)

//checkpointing，在spark app结束后仍会保存，因保存在更可靠的文件系统中，但血统会被打断（Spark不知道这RDD是怎么得来的）。另外耗费大，记得先persist后checkpoint。
spark.sparkContext.setCheckpointDir("/some/path/for/checkpointing")
words.checkpoint()

//checkpointing和persisting off_heap可以让由于GC或out-of-memory errors而停止的工作完成

//pipe, 即使是空的partition也会被调用
words.pipe("wc -l").collect()//得到两个结果（一个partition一个）的Array

//mapPartitions, f: (Iterator[T]) ⇒ Iterator[U]
words.mapPartitions(part => Iterator[Int](1)).sum()

//mapPartitionsWithIndex, 下面方便debugg，打印出每个元素所在partition
words.mapPartitionsWithIndex((partitionIndex, withinPartIterator) =>
      withinPartIterator.toList.map(
    value => s"Partition: $partitionIndex => $value").iterator
).collect()

//foreachPartition, f: (Iterator[T]) ⇒ Unit

//glom, 将“每一个partition”数据转化为“一个”Array[T],如RDD[Array[String]]
spark.sparkContext.parallelize(Seq("Hello", "World","A"), 2).glom()//两个数组Array(Hello), Array(World, A)
```

#### 3.KV RDD

有两类functions：PairRDDFunctions（任何(K,V)）和OrderedRDDFunctions（K必须为ordering，如numeric 或 strings，其他类型要自定义）

```scala
//创建, map或keyBy
words.map(word => (word, 1))
val keyword = words.keyBy(word => word.toLowerCase.toSeq(0).toString)

//mapValues, flatMapValues, keys, values
keyword.mapValues(word => word.toUpperCase)//Array((s,SPARK), (t,THE),...)
keyword.flatMapValues(word => word.toUpperCase)// Array((s,S), (s,P), (s,A)...)，返回的结果word.toUpperCase会被当作集合拆开，配上当前的key，原本的KV pair会消失。
keyword.lookup("s")//查看key为s的value，没有方法以（K, V）的形式显示

//sampleByKey和sampleByKeyExact，区别在于每个key抽取到的数量是否99.99%等于math.ceil(numItems * samplingRate)。下面根据sampleMap中的概率map对words进行抽样
val distinctChars = words.flatMap(word => word.toLowerCase).distinct
  .collect()
import scala.util.Random
val sampleMap = distinctChars.map(c => (c, new Random().nextDouble())).toMap
words.map(word => (word.toLowerCase.toSeq(0), word))
  .sampleByKey(true, sampleMap, 6L)//6L为种子
  .collect()

//aggregation，定义聚合方式（下面有更复杂的实现，搜索“自定义aggregation”）
val KVcharacters = chars.map(letter => (letter, 1))
def maxFunc(left:Int, right:Int) = math.max(left, right)
def addFunc(left:Int, right:Int) = left + right
val nums = sc.parallelize(1 to 30, 5)
//countByKey,countByKeyApprox(timeout, confidence)结果为map（K -> count）
//groupByKey的工作原理是把相同Key放到相同executor，如果数据量大且数据严重倾斜，某个executor会不够内存。
KVcharacters.groupByKey().map(row => (row._1, row._2.reduce(addFunc)))//groupByKey结果为Map[Char,Long]
//reduceByKey，会先在各个partition上reduce，然后shuffle，再最后reduce
//aggregate，要设置累加器，这里用0。第一个func在partition内执行，第二个跨partition。
nums.aggregate(0)(maxFunc, addFunc)//最后的聚合是在driver上，所以用下面方法更安全
//treeAggregate，一些聚合在executor先完成
nums.treeAggregate(0)(maxFunc, addFunc, depth)
//aggregateByKey，下面先每个partition的同key相加，然后从所有区中选出各key的最值
KVcharacters.aggregateByKey(0)(addFunc, maxFunc).collect()
//combineByKey，设置combiner
val valToCombiner = (value:Int) => List(value)//刚开始如何生成combiner
val mergeValuesFunc = (vals:List[Int], valToAppend:Int) => valToAppend :: vals//值和combiner
val mergeCombinerFunc = (vals1:List[Int], vals2:List[Int]) => vals1 ::: vals2//combiner和combiner
val outputPartitions = 6
KVcharacters
  .combineByKey(
    valToCombiner,
    mergeValuesFunc,
    mergeCombinerFunc,
    outputPartitions)
//foldByKey，设置一个初始值，该初始值会按照Func的规则，如+，作用到每个value上，然后作用后的value再按照Func的规则按key进行聚合。下面结果和aggregateByKey(0)(addFunc, addFunc)一样
KVcharacters.foldByKey(0)(addFunc)//注意，乘法用1

//cogroup，Join的基础。下面结果类似RDD[(Char, (Iterable[Double], Iterable[Double], Iterable[Double]))]，其中Char为Key，一个Iterable对应一个RDD，Double为RDD的value类型
charRDD.cogroup(charRDD2, charRDD3)//比用两次join高效。会有和groupByKey同样的问题，同key要存在同一个分块里

//join，fullOuterJoin，leftOuterJoin，rightOuterJoin，cartesian
KVcharacters.join(keyedChars, outputPartitions)//outputPartitions可选

//zip，合并两个RDD，结果类似RDD[(T, T)]。注意这两个RDD要有相同数量的partition和元素
words.zip(RDD)
```

**KV aggregation的总结**

| Function         | Purpose                                                  | Key restriction                                        | out of memory                                    | Slow when                                                    |
| ---------------- | -------------------------------------------------------- | ------------------------------------------------------ | ------------------------------------------------ | ------------------------------------------------------------ |
| `groupByKey`     | 按keys分组，每个key一个iterator。                        | 默认`HashPartitioner`当keys是array时，用自定义分块器。 | 某些key的数据太多时。                            | 如果没有设定partitioner，会shuffle。distinct的keys越多，或者各keys的数据分布越不均匀，消耗越大。 |
| `combineByKey`   | 同上，但结果可以是各种数据类型。要设置combiner的产生方式 | Same as above.                                         | 看怎么聚合及聚合结果。可能同上，或者结果的GC太多 | 同上，但如果是聚合（看表下说明）运算，会快点。               |
| `aggregateByKey` | 同上，但用的是初始累加器                                 | 同上。                                                 | 同上，但可通过合理设定accumulator来减少GC        | 同上，但更快，因为先在各分块执行一次再跨分块执行。           |
| `reduceByKey`    | 结果为输入类型。                                         | See above.                                             | 一般没问题。且没有累加器的产生（少GC）           | 同上。                                                       |
| `foldByKey`      | 搜索KV RDD代码的说明。                                   | See above.                                             | See above.                                       | 同上。                                                       |

实际上，所有都是建立在`combineByKey`的基础上。上面聚合指SeqOp和combOp都是产生一个空间更小的结果。

**Mapping and Partitioning Functions of PairRDD**

| Function        | Purpose                     | out of memory                            | Slow when                                  | Output partitioner  |
| --------------- | --------------------------- | ---------------------------------------- | ------------------------------------------ | ------------------- |
| `mapValues`     | 只对值操作                  | 取决于map的过程和结果                    | map过程复杂或者map后的record比原来更占空间 | 能保存partitioner   |
| `flatMapValues` | f: (V) ⇒ TraversableOnce[U] | 同上。                                   | 同上                                       | 同上                |
| `keys`          | 返回keys（含重复）          | Almost never                             | Essentially free                           | 同上                |
| `values`        | 返回values                  | Almost never                             | Essentially free                           | 不保留              |
| `sampleByKey`   | 搜索 sampleByKey            | Almost never, 除非map很大（map要被广播） | 没有shuffle                                | 保留                |
| `partitionBy`   | 搜索自定义partition         | 取决于什么partitioner                    | 当每个key有很多值时                        | 看设定的partitioner |

**OrderedRDD Operations**

| Function                                              | Key restriction | Runs out of memory when                      | Slow when                                                    | Output partitioner                      |
| ----------------------------------------------------- | --------------- | -------------------------------------------- | ------------------------------------------------------------ | --------------------------------------- |
| `sortByKey`下面函数和range partition的结合            | 支持tuple2的key | keys分布不均（相同key的位置和某key数量太多） | 多重复key时。数据倾向会导致后期操作慢。                      | Range partitioner.默认分块数和原RDD一样 |
| repartition-And-Sort-Within-Partitions                | Key 为ordering  | 取决于partitoner                             | 某key数量太多时慢，而且还要排序。但比`partitionBy`和`mapPartitions` 的组合快 | 看设定的partitioner                     |
| `filterByRange`选择key落在指定范围的records进行filter | See above.      | Almost never.                                | 通过range partitioning的rdd（如sortByKey后）用这方法比较高效，否则跟普通filter一样 | 保留partitioner                         |



#### 4.RDD Join

三种情况：shuffle, one or both have known partitioners, colocated

三个建议：

join前filter或reduce是个好习惯

如果RDD有重复的keys，最好先用distinct或combineByKey来减少key空间，或者用cogroup来处理重复keys。

如果某key不同时存在于两个RDD，用outerjoin防止数据丢失，之后用filter



针对建议的代码：

```scala
//有score(id, score)和student(id, name)的rdd。如果制作一个(id,name,score)的表，且score只要最高分，先用下面的代码去掉多余的score后再join
val bestScore = scores.reduceByKey((x,y) => if (x > y) x else y)
bestScore.join(student)
//join后再filter时注意格式变化[(key,(v1,v2))]
joinedRDD.reduceByKey((x,y) => if (x._1 > y._1) x else y)

//have known partition来避免shuffle
//上面出现在join前的reduceByKey或aggregateByKey会有shuffle，可以执行前加上下面代码，防止shuffle。在reduceByKey的第一个arg加上scoresDataPartitioner即可
val scoresDataPartitioner = scores.partitioner match {
    case (Some(p)) => p
    case (None) => new HashPartitioner(scores.partitions.length)
}

//广播（和DF一样，但要手动设置。实际上就是广播一个map）
import scala.collection.Map
import scala.reflect.ClassTag

def manualBroadCastHashJoin[K : Ordering : ClassTag, V1 : ClassTag,
 V2 : ClassTag](bigRDD : RDD[(K, V1)], smallRDD : RDD[(K, V2)])= {
  val smallRDDMapLocal: Map[K, V2] = smallRDD.collectAsMap()//注意smallRDD不能有相同key不同value，否则只会取一个key-value
  bigRDD.sparkContext.broadcast(smallRDDMapLocal)
  bigRDD.mapPartitions(iter => {
   iter.flatMap{
    case (k,v1) =>
     smallRDDMapLocal.get(k) match {//下面None和Some都是Option的子类
      case None => Seq.empty[(K, (V1, V2))]//没有批对的key就去掉，也可设默认值Seq((k, (v1, 0)))
      case Some(v2) => Seq((k, (v1, v2)))
     }
   }
  }, preservesPartitioning = true)
}
//上面smallRDDMapLocal.get可改为getOrElse
case (k,v1) => {
   val v2 = smallRDDMapLocal.getOrElse(k, 0)
   Seq((k, (v1, v2)))
}

//部分广播，利用countByKeyApprox或者countByKey+sort后取前k个key
```



------



### Advanced RDDs

#### 1.partition

**了解partition**

默认从storage（checkpointing产生的除外）加载的数据是没有partitioner的，会根据数据和分块的大小来分配数据到partitions。

```scala
//下面代码结果为(1,None)，(1,None)，(2,None)...均匀分布在8个partitions（即有两个空了）
////数据刚开始sc.parallelize时是没有partitioner，所以均匀分布
val rdd = sc.parallelize(for {
    x <- 1 to 3
    y <- 1 to 2
} yield (x, None), 8)

//计算每个分块的records数量
rdd.mapPartitions(iter => Iterator(iter.length)).collect()
//显示每个分块各保存了什么数据
df.rdd.mapPartitions(iter => Iterator(iter.map(_.getString(0)).toSet)).collect()

//有了partitioner之后，就会按key来shuffle。如下面重分块为2份，结果是2在一个分块，1，3在另一个分块。Scala RDD的分区根据k.hashCode % numOfPar；DataSets用MurmurHash 3函数，pyspark用其他。
import org.apache.spark.HashPartitioner
val rddTwoP = rdd.partitionBy(new HashPartitioner(2))
rddTwoP.mapPartitions(iter => Iterator(iter.map(_._1).toSet)).collect()
//结果Array(Set(2), Set(1, 3))
```

**控制partition**

重分块后记得persist

```scala
//合并partition，较少partition来减少shuffle
coalesce(numPartitions)
//重partition，伴随shuffle，但增加partitions有时可提高并行效率，使用hash partitioning
repartition(numPartitions)
//重partition并区内排序。用于排序的key可以另外设定
repartitionAndSortWithinPartitions(partitioner)
```

**自定义partition**

其定义partition的目的是让数据较为均匀地分布到各partition。partition需要对数据的业务情况有很好的了解，如果仅仅是为了一个值或一组cols值，这是没必要的。本书建议完成partition后回到结构化API。

Spark有两个内置Partitioners，"HashPartitioner for discrete values and a RangePartitioner for continuous"。结构化APIs就是用他们进行partition的。另外，pairRDD操作用的是hash。

具体来说，range partitioning需要分块数和rdd参数，其中rdd为tuple且key为ordering。它先需要sort，然后通过sample来确定各partition的分配范围，最后才正式分布。key值的不平衡会影响sample的准确性，这样的partitioning可能使后面的计算更低效，甚至out-of-memory。总体来说，比hash消耗大。

```scala
//下面利用HashPartitionerpartition，arg默认值为SparkConf中设置的spark.default.parallelism，如果该值没有设置，就是该rdd血统的存在过的最大值
val keyedRDD = rdd.keyBy(row => row(6).asInstanceOf[Int].toDouble)
keyedRDD.partitionBy(new HashPartitioner(10)).take(10)

//自定义partition。还可以实现equals方法（hashcode也要实现），当partitioner相同时，不再shuffle。hash的是当numPartitions相等时为true。
class DomainPartitioner extends Partitioner {
 def numPartitions = 3
 def getPartition(key: Any): Int = {//返回的数值为0~numPartitions-1
   val customerId = key.asInstanceOf[Double].toInt //Double为key的类型
   if (customerId == 17850.0 || customerId == 12583.0) { //特定为这两个ID分一个区
     return 0
   } else {
     return new java.util.Random().nextInt(2) + 1
   }
 }
}
//下面的代码显示每个partition所得ID的数量
keyedRDD.partitionBy(new DomainPartitioner).map(_._1).glom().map(_.toSet.toSeq.length)
```

部分narrow transformation可以保持partitioner，如mapValues。用map、flatMap等，即便不改变key，还是会失去partitioner。另外，下面mapPartitions设置也可保持partitioner

```scala
prdd.mapPartitions(iter => Iterator(iter.map(v => (v._1, v._2)).toArray), preservesPartitioning = true).partitioner
```

**Co-Located and Co-Partitioned RDDs**

Co-Located指分布在内存的位置相同。如果两个rdd被相同的job分布到内存，且拥有相同partitioner，就会是co-located，能方便CoGroupedRDD函数，如cogroup和join等。

Co-Partitioned指拥有相同partitioner的rdd。

```scala
//下面count执行后，rddA和rddB会是Co-Located。如果把rddA和rddB的注释去掉，则是Co-Partitioned
val rddA = a.partitionBy(partitioner)
rddA.cache()
val rddB = b.partitionBy(partitioner)
rddB.cache()
val rddC = a.cogroup(b)
//rddA.count()
//rddB.count()
rddC.count()
```



#### 2.自定义Aggregation函数

aggregateByKey需要三个参数：`rdd.aggregateByKey( accumulators )( sequence func, combine op )`

下面代码的需求：上面的rdd，其key是教师名，value是report cards。现想知道所有report cards的统计结果，具体是所有成绩单的longestWord，happyMentions（“happy”出现的次数），totalWords.toDouble/numberReportCards平均字数。为此先建立一个累加器MetricsCalculatorReuseObjects（其实累加器可以用array实现，方法通过object类定义，这样更省空间）。

```scala
//tag::calculator[]
class MetricsCalculatorReuseObjects(
  var totalWords : Int,
  var longestWord: Int,
  var happyMentions : Int,
  var numberReportCards: Int) extends Serializable {
  
  //表示把value添加到累加器的函数
  def sequenceOp(reportCardContent : String) : this.type = {//返回还是原来的累加器
    val words = reportCardContent.split(" ")
    totalWords += words.length
    longestWord = Math.max(longestWord, words.map( w => w.length).max)
    happyMentions += words.count(w => w.toLowerCase.equals("happy"))
    numberReportCards +=1
    this
  }

  //表示各累加器加总的函数
  def compOp(other : MetricsCalculatorReuseObjects) : this.type = {
    totalWords += other.totalWords
    longestWord = Math.max(this.longestWord, other.longestWord)
    happyMentions += other.happyMentions
    numberReportCards += other.numberReportCards
    this
  }

  //从最终累加器中导出结果的函数
  def toReportCardMetrics = ReportCardMetrics(
    longestWord,
    happyMentions,
    totalWords.toDouble/numberReportCards)
}
//end::calculator[]

//存放结果的case class
case class ReportCardMetrics(
  longestWord : Int,
  happyMentions : Int,
  averageWords : Double)

//实现上述需求的函数
def calculateReportCardStatisticsReuseObjects(rdd : RDD[(String, String)]
  ): RDD[(String, ReportCardMetrics)] ={

    rdd.aggregateByKey(new MetricsCalculatorReuseObjects(totalWords = 0, longestWord = 0, happyMentions = 0, numberReportCards = 0))(
      seqOp = (reportCardMetrics, reportCardText) =>
        reportCardMetrics.sequenceOp(reportCardText),
      combOp = (x, y) => x.compOp(y))
    .mapValues(_.toReportCardMetrics)
}
//上面代码也可写成这样
    val accumulator = new MetricsCalculatorReuseObjects(totalWords = 0, longestWord = 0, happyMentions = 0, numberReportCards = 0)
    def seqOp = (reportCardMetrics : MetricsCalculatorReuseObjects, reportCardText:String) => reportCardMetrics.sequenceOp(reportCardText)
    def combOp = (x : MetricsCalculatorReuseObjects, y : MetricsCalculatorReuseObjects) => x.compOp(y)    
    rdd.aggregateByKey(accumulator)(seqOp, combOp).mapValues(_.toReportCardMetrics)
```

**零散的优化建议**

1.重用已存在的objects。如“自定义Aggregation函数”中seqOp和combOp运用this.type方式，而非new

2.使用primitive types比自定义类和tuple更有效率。比如用array比tuple省事，再如“自定义Aggregation函数”中，用array来实现MetricsCalculatorReuseObjects。

3.减少遍历次数（下面实现也有缺陷，看注释）。如修改上面的sequenceOp：先创建一个单例函数，该函数输入Seq[String]，即content.split后的内容，通过toIterator和while loop计算出longestWord和happyMentions。这样避免使用Math.max和words.count对数据进行两次遍历。

```scala
object CollectionRoutines{
  def findWordMetrics[T <:Seq[String]](collection : T ): (Int, Int)={
    val iterator = collection.toIterator//.toIterator需要把collection变为Traversable对象后投影为Iterator，所以创建了两个object
    var mentionsOfHappy = 0
    var longestWordSoFar = 0
    while(iterator.hasNext){
      val n = iterator.next()
      if(n.toLowerCase == "happy"){
        mentionsOfHappy +=1
      }
      val length = n.length
      if(length> longestWordSoFar) {
        longestWordSoFar = length
      }
    }
    (longestWordSoFar, mentionsOfHappy)
}
```

4.减少类型转换。如上面sequenceOp中的.map，它是一个隐式转换；再如上面`val iterator = collection.toIterator`中，.toIterator需要把collection变为Traversable对象。



#### 3.iterator-to-iterator transformation

定义：`f: (Iterator[T]) ⇒ Iterator[U]`，但没有把把原iterator变为collection或者使用“action”（消耗iterator的操作，如size）来建立新的collection。

这种转换的好处是允许Spark选择性地将溢出的数据写到磁盘。换句话说，因为迭代器里每一个元素相当于一个partition，这样处理整份数据时，可逐个partition，暂时用不到的可以写到磁盘。另外，针对iterator的方法还能较少中间数据结果的产生。

> 不能对iterator调用sort，要转换为array后才能调用。

```scala
//下面用了flatMap，每次的结果返回的都是迭代器。这样如果flatMap结果太大，Spark可以把溢出数据写到磁盘。例子背景看6.KV排序例子
pairsWithRanksInThisPart.flatMap{

  case (((value, colIndex), count)) =>

      val total = runningTotals(colIndex)
      val ranksPresent: List[Long] = columnsRelativeIndex(colIndex)
                                     .filter(index => (index <= count + total)
                                       && (index > total))

      val nextElems: Iterator[(Int, Double)] =
        ranksPresent.map(r => (colIndex, value)).toIterator

      runningTotals.update(colIndex, total + count)
      nextElems
}
```





#### 4.Alluxio (nee Tachyon)

Tachyon是Spark外的一个分布式内存存储系统。它可架在存储系统，如HDFS之上，并承载其他计算框架，如Spark。它也像Spark有三种manager管理模式。

它可用于Spark App的数据输入和输出源，或者存储off_heap persistence。运用它来存储可减少GC消耗，且让多个executors享用外部内存池，也不怕executors crash而丢失数据



#### 5.KV排序例子

详细查看《high performance spark》github上的代码。

需求：一个df，一列id，其他为该id的属性列(至少两列，一列不需并行，用loop)。编写一个函数，使得输入该df和一个`List[Long]`(要查看的名次)，产出`Map[Int, Iterable[Double]]`，Int为属性的列index， Iterable[Double]为List包含该名次对应的数值。

```scala
//V1:groupByKey，阅读理解
def findRankStatistics(
    dataFrame: DataFrame,
    ranks: List[Long]): Map[Int, Iterable[Double]] = {
    require(ranks.forall(_ > 0))
    
    val pairRDD: RDD[(Int, Double)] = mapToKeyValuePairs(dataFrame)//函数定义在下面

    val groupColumns: RDD[(Int, Iterable[Double])] = pairRDD.groupByKey()
    groupColumns.mapValues(iter => {
            val sortedIter = iter.toArray.sorted
            sortedIter.toIterable.zipWithIndex.flatMap({
                case (colValue, index) =>
                if (ranks.contains(index + 1)) {
                    Iterator(colValue)
                } else {
                    Iterator.empty
                }
            })
       }).collectAsMap()
}
def mapToKeyValuePairs(dataFrame: DataFrame): RDD[(Int, Double)] = {
    val rowLength = dataFrame.schema.length
    dataFrame.rdd.flatMap(
        row => Range(0, rowLength).map(i => (i, row.getDouble(i)))
    )
}
```

上面的方法虽然用到了collectAsMap（有out-of-memory可能），但是由于结果的大小是可预料的，Map-key数量为columns数，Map-value为、输入参数list中的名次数量，所以问题不大。问题是当groupByKey遇上数据倾斜。如果是groupByKey后聚合的，直接用aggregateByKey or reduceByKey。

下面Version2排序实现了：同key同partition，在同一partition的key按第一个key来排序（最后变倒序了...），然后同key的按value的第一个值排序，并把value合并。优势在于运用了repartitionAndSortWithinPartitions，而不是上面的groupByKey然后sorted。然而这种操作需要保证一个executor能够容纳hashcode相同的key的record。

```scala
//自定义partitioner，由于key是tuple，但只想根据tuple的第一个元素来分块
class PrimaryKeyPartitioner[K, S](partitions: Int) extends Partitioner {
  
  val delegatePartitioner = new HashPartitioner(partitions)

  override def numPartitions = delegatePartitioner.numPartitions

  override def getPartition(key: Any): Int = {
    val k = key.asInstanceOf[(K, S)]
    delegatePartitioner.getPartition(k._1)
  }
}

def sortByTwoKeys[K : Ordering : ClassTag, S : Ordering : ClassTag, V : ClassTag](
    pairRDD : RDD[((K, S), V)], partitions : Int ) = {
    val colValuePartitioner = new PrimaryKeyPartitioner[K, S](partitions)

    //隐式，改变repartitionAndSortWithinPartitions的排序方式
    implicit val ordering: Ordering[(K, S)] = Ordering.Tuple2
    
    val sortedWithinParts = pairRDD.repartitionAndSortWithinPartitions(
        colValuePartitioner)
    sortedWithinParts
}


def groupByKeyAndSortBySecondaryKey[K : Ordering : ClassTag, S : Ordering : ClassTag, V : ClassTag]
    (pairRDD : RDD[((K, S), V)], partitions : Int): RDD[(K, List[(S, V)])] = {
    
    val colValuePartitioner = new PrimaryKeyPartitioner[Double, Int](partitions)

    //隐式，改变repartitionAndSortWithinPartitions的排序方式。如果key是case class，看下面的补充说明。
    implicit val ordering: Ordering[(K, S)] = Ordering.Tuple2

    val sortedWithinParts =
    pairRDD.repartitionAndSortWithinPartitions(colValuePartitioner)

    sortedWithinParts.mapPartitions( iter => groupSorted[K, S, V](iter) )
}

//将相同key的value和为一个list
def groupSorted[K,S,V](
    it: Iterator[((K, S), V)]): Iterator[(K, List[(S, V)])] = {
    val res = List[(K, ArrayBuffer[(S, V)])]() 
    it.foldLeft(res)((list, next) => list match {
        case Nil => //匹配空的List
        val ((firstKey, secondKey), value) = next
        List((firstKey, ArrayBuffer((secondKey, value))))

        case head :: rest => //匹配List的第一个值和剩下的值
        val (curKey, valueBuf) = head
        val ((firstKey, secondKey), value) = next
        if (!firstKey.equals(curKey) ) {
            (firstKey, ArrayBuffer((secondKey, value))) :: list
        } else {
            valueBuf.append((secondKey, value))
            list//此时list的valueBuf已经改变
        }

    }).map { case (key, buf) => (key, buf.toList) }.iterator
}

//注意，下面排序不能保证secondary sort
indexValuePairs.sortByKey.map(_.swap()).sortByKey
```

> 如果Key是某个case class，如下面的PandaKey，则用下面方法自定义排序。这样后面引用到sortByKey方法的方法，如repartitionAndSortWithinPartitions, sortBeyKey都会按照这个方法排序
>
> ```scala
> implicit def orderByLocationAndName[A <: PandaKey]: Ordering[A] = {
>     Ordering.by(pandaKey => (pandaKey.city, pandaKey.zip, pandaKey.name))
> }//PandaKey有4个变量，这里只用其中三个排序
> ```
>

Version3：将值作为key，假设value很多样的情况下，sortByKey就不会像以colIndex作为key时会因为某个col值太多而out-of-memory。

```scala
def findRankStatistics(dataFrame: DataFrame, targetRanks: List[Long]): Map[Int, Iterable[Double]] = {
    
    //getValueColumnPairs将df变为rdd，并把每个Row(x,y,z)变为(x,0),(y,1),(z,2)，后面表示colIndex
    val valueColumnPairs: RDD[(Double, Int)] = getValueColumnPairs(dataFrame)
    //排序并persist
    val sortedValueColumnPairs = valueColumnPairs.sortByKey()
    sortedValueColumnPairs.persist(StorageLevel.MEMORY_AND_DISK)

    val numOfColumns = dataFrame.schema.length

  /**
   * getColumnsFreqPerPartition将
   *    P1: (1.5, 0) (1.25, 1) (2.0, 2) (5.25, 0)
   *    P2: (7.5, 1) (9.5, 2)
   * 变为   
   *    [(0, [2, 1, 1]), (1, [0, 1, 1])]
   * 第一个值表示PIndex，第二个值是各colIndex出现的频率
   */
    val partitionColumnsFreq =
    getColumnsFreqPerPartition(sortedValueColumnPairs, numOfColumns)
   
  /**
   * getRanksLocationsWithinEachPart利用上面的频率List找出所查询的targetRanks的在各P的位置，如
   *    targetRanks: 5
   *    partitionColumnsFreq: [(0, [2, 3]), (1, [4, 1]), (2, [5, 2])]
   *    numOfColumns: 2
   * 结果为：
   *    [(0, []), (1, [(0, 3)]), (2, [(1, 1)])]
   * 第一个值表示PIndex，第二个值是一个List[Tuple2]，每个tuple表示一个target的位置，如(0,3)表示第一列(0)的targetranks位置在该P的第3个(0)出现的位置上。
   */
    val ranksLocations = getRanksLocationsWithinEachPart(
        targetRanks, partitionColumnsFreq, numOfColumns)
    
    //根据上面结果取值，结果为RDD(column index, value)
    val targetRanksValues = findTargetRanksIteratively(
        sortedValueColumnPairs, ranksLocations)
    targetRanksValues.groupByKey().collectAsMap()
}
```

Version4：用value排序时，有可能某些value的值很多，比如0。这种情况下，可以对DF作另外一种转换。其他思路与V3一样。

```scala
/**
 * 代码将
 * 1.5, 1.25, 2.0
 * 1.5,  2.5, 2.0
 * 变为
 * ((1.5, 0), 2) ((1.25, 1), 1) ((2.5, 1), 1) ((2.0, 2), 2)
 * 每个value-用统计的方式表示
 */
def getAggregatedValueColumnPairs(dataFrame: DataFrame): RDD[((Double, Int), Long)] = {

  val aggregatedValueColumnRDD = dataFrame.rdd.mapPartitions(rows => {
    val valueColumnMap = new mutable.HashMap[(Double, Int), Long]()
    rows.foreach(row => {
      row.toSeq.zipWithIndex.foreach{ case (value, columnIndex) =>
        val key = (value.toString.toDouble, columnIndex)
        val count = valueColumnMap.getOrElseUpdate(key, 0)
        valueColumnMap.update(key, count + 1)
      }
    })
    valueColumnMap.toIterator
  })
  aggregatedValueColumnRDD
}
```

排序总评：V1的groupByKey和sort可以用V2的repartitionAndSortWithinPartitions来提升效率。V3换了一种思路，不用col作为key，而用value，这能够防止某个col的值过多。V4是当V3有过多重复值时才使用的改进版，用了一个"map-side reduction"，即用统计的方式表示KV后才排序。

V1方案最简单，但效果最差。不同方案的选择要考虑 a）原始数据的records数量 b）列数（分组数） c）每列重复值的占比。V2适合大多数情况，只要每个col的值不算太多。如果多但重复值不多（重复值少于“值”数），就V3，重复多就V4（unique值太多时，hash map有out-of-memory风险）。



------



### Distributed Shared Variables

```scala
//利用mapPartitions，foreachPartition也算共享吧
rdd.mapPartitions{itr =>
    // Only create once RNG per partitions
    val r = new Random()
    itr.filter(x => r.nextInt(10) == 0)
}
```

Spark通过下面两种变量来实现share。

#### Broadcast Variables（immutable）

通常情况下，变量写在函数里就可以，但如果把一个大的lookup表或机器学习模型写到函数里，就会很低效。因每执行一个函数都要重复把变量序列化后传给需要的tasks（一个task一份）。

预先广播这些大变量，让每个executor共享。可序列化的变量才能成为广播变量。  

```scala
val supplementalData = Map("Spark" -> 1000, "Definitive" -> 200,
                           "Big" -> -300, "Simple" -> 100)
val bc1 = spark.sparkContext.broadcast(supplementalData)//Java里改为final
//bc1.value提取值，写在可序列化的函数里
words.map(word => (word, bc1.value.getOrElse(word, 0)))
  .sortBy(wordPair => wordPair._2)
  .collect()

//删除driver和executors的广播变量用.destroy，只删除executors用unpersist

//对于不可序列化的object，用下面代码
class LazyPrng {
    @transient lazy val r = new Random()
  }
def customSampleBroadcast[T: ClassTag](sc: SparkContext, rdd: RDD[T]): RDD[T]= {
    val bcastprng = sc.broadcast(new LazyPrng())
    rdd.filter(x => bcastprng.value.r.nextInt(10) == 0)
}
```

> 广播后的数据被caching为Java对象在每个executor，不需要反序列化给每个task，而且这些数据可跨多jobs，stages和tasks



#### Accumulators（只写）

储存在driver可变的共享变量。用于debugging和聚合，但不适合收集大量数据。

在actions中，重复任务不会继续更新累加器；在transformation中，每个任务的更新可能不止一次，如果tasks stages被重新执行，而且由于lazy evaluation，不是每个transformation都能保证更新。

命名累加器便于在UI查看

caching和累加器一起运用时，即便小数据计算正确，也难保大数据正确。不过本身如果出现重复计算，累计器也不安全？

```scala
//创建命名累加器
val acc = new LongAccumulator
spark.sparkContext.register(acc, "A")
spark.sparkContext.parallelize(1 to 10).foreach(e => ac1.add(1))
ac1.value//10  

//自定义累加器
class EvenAccumulator extends AccumulatorV2[BigInt, BigInt] {
  var num:BigInt = 0
  def reset(): Unit = {  //重置
    this.num = 0
  }
  def add(intValue: BigInt): Unit = {  //偶数才加
    if (intValue % 2 == 0) {
        this.num += intValue
    }
  }
  def merge(other: AccumulatorV2[BigInt,BigInt]): Unit = {//累加器的结合
    this.num += other.value
  }
  def value():BigInt = {
    this.num
  }
  def copy(): AccumulatorV2[BigInt,BigInt] = {
    new EvenAccumulator
    EvenAccumulator.num = num
    EvenAccumulator
  }
  def isZero():Boolean = {
    this.num == 0
  }
}
```

> 广播变量只能写在driver，然后让executors读取；累加器相反，只能写在executors，让driver读取。

------





## Part IV. Production Applications

------

### Spark在集群上运行

紧接着Overview中的基本框架进行扩展。

Cluster Manager：在standalone模式中即为Master主节点，控制整个集群，监控worker。  
Worker Node：负责控制计算节点，启动Executor  
Driver： 运行Application 的main()函数（向Manager申请资源和启动executor后，发送task到Executor）  
Executor：某个Application在Worker Node上的进程  

> 实际上，Manager是在资源协调者，如Yarn中的称呼。在Spark中将这些Manager称为Master。

![Components][1]  

SparkContext can connect to several types of cluster managers (local，standalone，yarn，mesos), which allocate resources across applications.Once connected, Spark acquires executors on nodes in the cluster, which are processes that run computations and store data for your application. Next, it sends your application code to the executors. Finally, SparkContext sends tasks to the executors to run.  

- Each application gets its own executor processes, which stay up for the duration of the whole application and run tasks in multiple threads.    
  isolating applications  
  data cannot be shared without writing it to an external storage system  
- the driver program must be network addressable from the worker nodes. TaskScheduler会根据资源剩余情况分配相应的Task。  
- DAGScheduler: 根据Job构建基于Stage的DAG（Directed Acyclic Graph有向无环图)，并提交Stage给TaskScheduler。  

#### 1.模式

**部署模式**

cluster模式：最常用，用户在该模式下提交预编译JAR。然后manager会在一个worker节点启动driver进程，其他worker（同一个也有可能）节点启动executor进程。这意味着manager负责维护所有的Spark app相关进程。

client模式：几乎同上，但spark driver会留在提交app的机器，意味client机器负spark driver，而manager只负责executor进程。这样的client机器被称为“gateway machines or edge nodes”

local模式：整个Spark app都在一台机器上，学习和试验用。

> 集群运行时，尽量减少driver和Executors的距离，如用cluster模式，或driver在worker节点的client模式

**集群模式**

standalone中由standalone master进程和workers进程组成

yarn中由ResourceManager进程和NodeManager进程组成

#### 2.The Life Cycle of Application（外层）

submit jar -> 启动driver -> driver向manager申请资源 -> manager启动executor -> driver发送jar到executors，并分配task给executors让其执行jar中的某些代码

**用户请求**

提交任务，向manager申请，manager允许后把Spark driver放到一个worker节点上，submit结束。

```bash
/bin/spark-submit \
  --class <main-class> \
  --master <master-url> \
  --deploy-mode cluster \
  --conf <key>=<value> \
  ... # other options，如Executor的数量和配置等，书中有表
  <application-jar> \
  [application-arguments]
```

**启动Spark集群**

预编译的JAR中所包含的SparkSession初始化Spark driver，包括DAGScheduler和TaskScheduler。后者的后台进程连接Manager，并注册Application，要求在集群启动Spark executor进程。manager利用资源调度算法启动executor进程（分静态和动态，独立JVM），并把进程的地址发送给Spark driver。

> 在Yarn模式下，submit向Yarn的resourceManager发送请求。后者分配container，根据设定的模式，yarn-cluster或yarn-client在某个nodeManager上启动applicationMaster(Driver)或ExecutorLanucher(非Driver)，此时接上上面的SparkContext的初始化，但yarn-client中，executors向本地Driver注册，而不是向启动ExecutorLanucher的nodeManager。
>
> yarn-cluster和yarn-client，前者用于生产，每个spark app在不同的nodeManager上启动Driver，不会像yarn-client那样都在本地机器，使得流量激增。但后者方便调试，log都返回到本地。



**执行**

DAGScheduler对app进行规划，每个action创建一个job，每个job多个stage，每个stage多个taskSet，后者的每个单位（即每个task）被TaskScheduler分到相应的executor上。 

Executors有一个进程池，每收到一个task，都会用TaskRunner来封装task，然后从线程池里取出一个线程来执行task。

TaskRunner将代码复制和反序列化然后再执行task。后者分为ShuffleMapTask和ResultTask。ResultTask是最后一个stage，其他stages都是ShuffleMapTask。

executors的一个task针对一个partition执行操作，完成一个stage到第二个......期间，executors还会向driver返回结果和自身运行情况

**完成**

不管成功与否，Spark driver都会退出，manager关闭executors。此时用户可咨询manager结果



#### 3.The Life Cycle of Application（内层）

每个app都是由一个或多个Spark jobs构成。Jobs是串行化执行的（除非并行启动action） 。

**SparkSession**

把旧的new SparkContext / SQLContext模式改掉。新的方法更安全（避免context冲突）

```scala
val spark = SparkSession.builder().appName("Name")
  .config("spark.sql.warehouse.dir", "/user/hive/warehouse")
  .getOrCreate()//如果已经存在，会忽略前面的配置
```

**SparkContext**

它代表与Spark集群的连接，使用底层APIs。通常不需要显式实例化它（通过SparkSession调用）。如果创建，用`val sc = SparkContext.getOrCreate()`

> 在过去版本，通过SQLContext and HiveContext（储存为sqlContext之类的）对DF和Spark SQL进行操作，且HiveContext有更完整的SQL parser，故在没有SparkSession的版本中，更推荐。
>
> 在2.X，只要spark.enableHiveSupport()就可以对DF使用HiveQL操作。Spark可对Hive的表格进行SQL查询，并输出让其他软件使用（如Hive本身）  

**代码physical execution**

job：由stages组成。通常来说，一个job一个action。job分解为一系列stages，后者数量取决于shuffle次数。默认FIFO，但也可以设置fair scheduler。

Stage：由一组tasks组成，Spark会将尽可能多的transformation留在相同的stage，但shuffle后（`ShuffleDependencies`）开始新stage。shuffle是数据的repartition，也会把数据写到磁盘。

tasks：一个task由一partition数据（按输出算）和一系列非shuffle的transformation组成，且只能运行在一个executor上。

```scala
//下面一共一个job，collect是唯一的action，6个stages，28个tasks
//预先设置SQLpartition数，默认200。通常设置至少比executor数多
spark.conf.set("spark.sql.shuffle.partitions", 8)
val df1 = spark.range(2, 10000000, 2)//local[*]模式下，有多少个线程就多少个tasks，且range分布数据，要shuffle。此处归为Stage 0或1
val df2 = spark.range(2, 10000000, 4)//Stage 0或1
val step1 = df1.repartition(5)//5partition，5task，重partition要shuffle。Stage2或3
val step22 = df2.repartition(6)//6partition，6task,Stage2或3
val step222 = step22.filter(_ % 2 ==0)
val step2222 = step2222.filter(_ % 3 ==0)
val step11 = step1.selectExpr("id * 5 as id")
val step3 = step11.join(step2222, "id")//shuffle，8tasks。Stage4
val step4 = step3.selectExpr("sum(id)")
step4.collect()//包含action的计划在UI看。shuffle，1task，Stage5
```

一般stages是按顺序的，但上述两个RDD的stage可以并行（不同rdd+join）。即便如此，一有宽依赖计算就要按顺序来。

>shuffle慢的原因：数据移动，潜在的I/O，限制并行（完成shuffle才能下一步）



**执行细节**

pipeline过程在一个node中完成，且不会写到内存或磁盘。

Spark总是让发送数据的任务在执行阶段，把shuffle文件写入磁盘。这有利于后续计算（如果没有足够executors同时执行计算，就先执行I/O再计算）和容错（不需要重新shuffle数据）。



#### 4.源码补充

##### 1.Master

**主备切换**

Spark可以配置两个Manager，一个active，一个Standby。转换机制分为文件系统和ZooKeeper两种，前者手动，后者自动。

Standby读取持久化的storedapp/workers/Drivers，如果都是非空的，则会对这些信息重新注册，储存到自己内部的缓存结构中，将Application和worker的状态改为UNKNOWN，然后向Driver对应的Application以及Worker发送Standby自身的地址。收到信息的Application和Workers向Standby返回信息，没有返回的会被Standby清理掉。然后Standby正式正为active master，用自己的schedule方法调度资源。

**注册机制**

worker向Manager注册，后者过滤掉DEAD的，将UNKNOWN的workers的旧信息更换为新的workers信息，然后将这些新workers加入内存缓存（HashMap）中，用持久化引擎，将Workers信息进行持久化（文件系统或ZooKeeper），Manager调用schedule()

driver向Manager注册，后者将信息存入内存缓存（HashMap）和等待队列（ArrayBuffer）中，用持久化引擎（同上）...

app，backend通过appclient内部的线程，发送registerApplication到Manager中对app进行注册（Master类中的receive匹配RegisterApplication(description, driver)），Manager将信息存入内存缓存（同上）…详细看Master类receive中的RegisterApplication

**状态改变**Master类receive中的DriverStateChanged和ExecutorStateChanged，当任务结束时收到，重新分配资源 。

**资源调度机制**schedule()

worker hashset中取出出alive的workers进行打乱，遍历打乱的集合，看有没有符合启动driver条件的workers(while循环，知道有为止)。然后开始executors的启动startExecutorsOnWorkers，它会先找出还需要调度core的app，然后app对workers的要求进行workers的筛选，对通过筛选的调用scheduleExecutorsOnWorkers，看需要启动多少个executors（spreadout or not），最后启动executors。

##### 2.worker

上面Master调用launchDriver/Executor()时，会向workers发送信息。Worker收到信息后调用LaunchDriver()创建一个DriverRunner，调用DriverRunner的start()，该方法启动一个进程，重写run方法，调用prepareAndRunDriver创建Driver工作目录，下载jar包，runDriver()，该方法初始化和正式启动和执行driver。线程执行的最后（任务结束后）向线程所在worker发送确认信息，后者向master汇报，然后接上上面的状态改变。（Executor类似）

##### 3.scheduler

action实际上会调用DAGscheduler的runjob() -> submitStage() -> submitMissingTasks() -> taskScheduler.submitTasks()（默认情况下，standalone的taskScheduler使用TaskSchedulerImpl） -> backend.reviveOffers() -> driverendpoint发送ReviveOffers -> makeOffers -> resourceOffers遍历sortedTaskSets，根据tasks和partition的位置（看partition所在位置是否符合task所需的资源），选出最佳task的location。resourceOffers接着launchTasks。

##### 4.executor

CoarseGrainedExecutorBackend（进程）向Driver询问RegisterExecutorx信息，Driver返回信息后创建executor对象。当Driver的TaskScheduler向ExecutorBackend发送LaunchTask时，调用executor的LaunchTask，新建TaskRunner线程，把它放到线程池执行。

executor的run()，反序列化，updateDependencies -> task.run()创建TaskContextImpl（保持task的全局信息）后调用runTask（根据不同task类有不同效果）。executor的run()后面还计算各种UI中出现的信息

##### 5.shuffle

SortShuffleManager有两种运行机制：普通和bypass。

普通机制：数据先写入内存结构中，不同shuffle算子采用不同的数据结构。当该数据结构占用空间达到阈值时，将该数据写入磁盘，并清空该数据的内存。写入磁盘前先按key排序，然后分批溢出（底层是Java的BufferedOutputStream）。多次的益处会产生多个临时文件，但SortShuffleManager会在最后将他们合并成一个，另外生成一个索引文件，方便下游task拉取数据，即拉取一个文件的哪个部分。

bypass机制：促发条件是shuffle read task的数量小于等于spark.shuffle.sort.bypassMergeThreshold参数的值时（默认为200）以及采用非聚合类算子。此时每个task为每个下游task创建一个临时文件，将数据按key的hashcode写入相应的磁盘文件中（写入过程同样是利用BufferedOutputStream），最后合并和生成索引文件。

##### 6.storage

Driver上的DAGScheduler中有BlockManagerMaster，里面存放有BlockManagerInfo/Status（节点上的BlockManager创建并注册后创建），负责记录和管理各节点上的BlockManager的元数据。节点上的BlockManager功能由DiskStore（磁盘读写）, MemoryStore, BlockTransferService（远程读写）等组成。

##### 7.persist

rdd.iterator如果有persist过，即StorageLevel不是NONE，则rdd.getOrCompute -> blockManager.getOrElseUpdate取数据或者重新计算并persist，如果不能persist，就返回iter。

------



### 开发Spark App

App由集群和代码构成

```scala
object DataFrameExample extends Serializable {//序列化
  def main(args: Array[String]) = {
      
    if (args.length != 2) {
      println("Usage: logCleanYarn <inputPath> <outputPath>")
      System.exit(1)
    }
	val Array(inputPath, outputPath) = args

    val spark = SparkSession.builder().appName("Example")
      .config("spark.sql.warehouse.dir", "/user/hive/warehouse")
      .getOrCreate()//appName，config等在生产环境中，去掉，在submit时设定。

    val df = spark.read.json(inputPath)
    
      df.write.format("parquet").partitionBy("day").save(outputPath)
  }
}
```

利用maven进行编译`mvn clean package -DskipTests`，当要将不同路径的jar都打包到一个jar包中时，在pom.xml中配置maven-assembly-plugin，然后`mvn assembly:assembly`

用`scp .jar hadoop@hadoop001:~/lib/`传到hadoop的master上。如果项目的resource中有文件，则需要另外scp到远程。

一般把下面的代码写到脚本中，以yarn为例

```bash
export YARN_CONF_DIR=path/of/hadoop #也可以在spark的conf文件设置
$SPARK_HOME/bin/spark-submit \
   --class com.databricks.example.DataFrameExample \ #写到类名，在idea中直接右键类名copy reference
   --name xxx\ #生产环境加上
   --master yarn \ #或yarn-cluster
   --conf spark.xxx.ss.xx=xx
   --files path/file1, path/file2 \ #额外用到的resource中的文件，此处为上面传到hadoop中的位置
   --jars xxx.jar \ #有额外jar时，比如用Hive时，加上mysql-connector-java的jar包
  #其他还有--num-executors/ --driver-memory/ --executor-cores --conf 等
   path/xxx.jar \ #jar的位置，一般为上面传到hadoop中的位置
   path/xxx.json  #数据源位置，即上面的inputPath
   path  #输出位置
```

**分开启动master和workers**

```bash
start-master.sh
start-slave.sh <master-spark-URL>

#配置
-h #在哪里启动，默认本机。也可以在spark-submit、spark-shell、代码中设置
-p #启动后使用哪个端口对外提供服务，master默认7077，workers随机
--webui-port #UI端口，master默认8080，worker 80801
-c #worker的core数量，默认当前机器所有cores
-m #worker的内存，默认1g
-d #worker工作目录，默认SPARK_HOME/work
```



#### App其他相关

**开发策略**

- 顾及边缘类型数据。
- 从真实的数据中做逻辑测试，保证你写的代码不是仅仅符合Spark功能，还要是符合业务的。
- 输出结构，确保下游用户理解数据的情况（更新频率，是否已完成等）

**测试**：JUnit or Scala Test

**开发流程**：一个临时空间，如交互式notebook。当完成一个关键部分，保存为库或者包。



#### 配置App

SparkConf：

```scala
val conf = new SparkConf().setMaster("local[2]").setAppName("DefinitiveGuide")//在UI中的名字
  .set("some.conf", "to.some.value")”
```

也可以在spark-submit中配置

硬编码配置：如通过conf/spark-env.sh设置环境变量（IP地址等），log4j.properties配置登录

其他可配置的还有运行时属性、执行属性、内存管理（和终端用户关系不大，2.X自动管理）、shuffle行为、环境变量、job Scheduling

------



### 部署Spark（略）

**位置**

内部：完全控制，但集群空间固定（难以根据数据大小调节），要应付数据修复等。推荐可以允许运行多个Spark App和动态分配资源的manager，YARN和Mesos更好支持动态分配，且能运行非Spark任务。

云：容易给每一个App一个自己的集群“ with the required size for just the duration of that job”

------



### Monitoring and Debugging

Spark UI和Spark logs，JVM，OS，Cluster

监测：processes（CPU、内存等）和query execution（jobs和tasks）

**常见问题**

启动、执行前、执行中。

Driver OutOfMemoryError or Driver Unresponsive：用了collect，broadcast数据太大，长时间运行APP产生大量数据，JVM的jmap

Executor OutOfMemoryError or Executor Unresponsive：少用UDF，JVM的jmap

Unexpected Nulls in Results：利用累加器计算raw data是否转化成功

磁盘空间：设置log配置（logs保持的时间）

其他包括：序列化出错，unexpected null



**Unit test**

常规的Spark：分为单个函数和整个流程（即所有函数组合起来）。前者看如何分解函数，后者可参考《high performance spark》的模版，编写输入和期望输出，然后在本地比较期望输出和实际输出。如果结果太大，可用toLocalIterator。

Streaming：输入用file(maxFilesPerTrigger)或socket来模拟数据源，输出用console或memory sink

DF：测试collect后的List(Row(...))。如果是ByteArrays，则要比较值。对于浮点结果，可用“assertDataFrameApproximateEquals(expectedDf, resultDF, 1E-5)”或者“assert(r1.getDouble(1) === (r2.getDouble(1) +- 0.001))”，查看spark-testing-base的equalDataFrames和approxEqualDataFrames

RDD：(order matters or not)：如果两个RDD经过sortByKey（partitioner相同时），可以zip两个rdd后直接比较，否则其中一个需要repartition后再比较。详细《high performance spark》中的代码。



**测试数据**

自编：纯手动，RandomRDDs，RandomDataGenerator

```scala
def generateGoldilocks(sc: SparkContext, rows: Long, numCols: Int):
    RDD[RawPanda] = {
  val zipRDD = RandomRDDs.exponentialRDD(sc, mean = 1000,  size = rows)
    .map(_.toInt.toString)
  val valuesRDD = RandomRDDs.normalVectorRDD(
    sc, numRows = rows, numCols = numCols)
  zipRDD.zip(valuesRDD).map{case (z, v) =>
    RawPanda(1, z, "giant", v(0) > 0.5, v.toArray)
  }
}
```

sample：

常规抽样：sample根据是否能replacement会创造poisson 或 bernoulli的PartitionwiseSampledRDD，也可以自定义sampler，例如为了sample一份样本和一份inverse样本。

分层抽样：sampleByKeyExact 和 sampleByKey的fraction用Map来确定各key的比率（占原数据的）

抽取多份样本：randomSplit

DF不支持分层抽样

**Property Checking**

sscheck和spark-testing-base。可以设定outputs类型，让testing library自动生成test input。



------



### Performance Tuning

#### 1.间接调优

**语言推荐**：

如果是ETL并进行单节点机器学习，SparkR或Python。方便

如果用到自定义transformations或自定义类，Scala或Java。性能

**数据结构**：

尽量用DF，默认通常比自定义有更多优化，更少工作量，且不同版本不需改变。

另外，优先用原始数据结构，次之为String和Array而非其他集合和自定义对象。真的要用集合或自定义类时，把这些类用String的形式来表示，即用逗号，竖线等隔开每个成员变量，又或者用json字符串存储。

**Kryo序列化**：

Spark涉及序列化的场景：闭包变量、广播变量、自定义类、持久化。

Kryo不是所有类型都能序列化，默认情况下，simple types, arrays of simple types, or string type都是Kryo序列化。

```scala
val conf = new SparkConf().setMaster(...).setAppName(...)
conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
conf.registerKryoClasses(Array(classOf[MyClass1], classOf[MyClass2]))
val sc = new SparkContext(conf)
```

优化缓存大小spark.kryoserializer.buffr.mb（默认2M）

**数据本地化**：调节任务等待时间 ，通过spark.locality.xxx，可以根据不同级别设置等待时间。

**集群配置**：

sizing和sharing：16和17节的配置

动态分配：多个App共享资源`spark.dynamicAllocation.enabled `为` true`，官方文件有很多参数可设置

**规划**：

资源均分：`spark.scheduler.mode`为 `FAIR `

限制：`--max-executor-cores`或修改默认`spark.cores.max`

**数据储存**：

存储格式：Parquet首选

压缩格式：选择splittable文件如 zip（在文件范围内）, bzip2（压缩慢，其他都很好）,  LZO（压缩速度最快）。上传数据分开几个文件，每个最好不超几百MB，用`maxRecordsPerFile`控制。文件不算太大，用gzip（各方面都好，但不能分割）。在conf中通过`spark.sql.partuet.compression.codec`设置。

**shuffle:**

reduceByKey、join、distinct、repartition等操作。

spark.reducer.maxSizeInFlight: ResultTask每次拉取数据的大小，如果内存够大，可以调高来减少拉取次数，默认48m，可尝试96m。

spark.shuffle.memoryFraction

spark.shuffle.file.buffer：所产生准备shuffle的文件的大小，调大可减少溢出磁盘的次数，默认32k，可尝试64k。

maxRetries * retryWait两个参数：有可能CG时间长导致拉取不到数据

**executor内存压力和Garbage Collection**

收集统计数据：在Spark的JVM添加`-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps`。这之后能在worker的logs里看到信息。也可以在UI查看。

如果full garbage collection被触发多次，则表明该task的内存不够用，要减少Spark用于caching的内存（`sc.set("spark.memory.fraction", "0.5")`）；如果很多minor collections而不是大的，分配更多内存给Eden（-Xmn=4/3*E）。

尝试`-XX:+UseG1GC`和`-XX:G1HeapRegionSize`等

> spark.executor.memory是不包含overhead的，所以实际上用到的内存比申请的多。在executor内存中，默认60% execution（用于计算和caching） and 40% storage（Spark内部元数据和用户的数据结构） memory。前者过小会有很多的磁盘溢出和cache丢失，后者过小会有很多GC。前者可赶走后者，R代表保留storage memory的占比，通过`spark.memory.storageFraction` 调节。更深一层可对Java Heap space区的比例进行调节（最好不用）。Java Heap space区可大致分为Eden，survival1，survival2，老年区等。当Eden区满后会促发minor GC，存活的放到survival1，第二次满就将Eden和survival1存活的存到survival2，重复，多次存活放到老年区，老年区满了会促发GC。但如果某次存放到survival时满了，数据会直接到老年区，使得老年区更快满载而频繁GC。

RDD（空间M），40%存task执行期间创建的对象（Java Heap space），通过memoryFraction调优。



#### 2.直接调优

**并行**：

num-executors * executor-cores的2~3倍`spark.default.parallelism`和`spark.sql.shuffle.partitions`

**filter**：

尽早filter，有时还能避免读取部分数据

**partition**：

减少partition用coalesce不会产生shuffle（把同节点的partition合并），例如filter后数据减少了不少时可以考虑减少分块

repartition能尽量均匀分布data，在join或cache前用比较合适。

自定义partition（很少用）

**UDFs**：多用内置的，而非UDFs

**Caching**：

cache不同操作逻辑都需要用到的RDD，如Web Log Analysis中的commonDF。

会占用空间。RDD的cache是字节，结构化API的cache是physical plan，比前者快

持久化RDD时，节点上的每个partition都会保存操内存中,以备在其他操作中进行重用  
cache()是persist(MEMORY_ONLY)，首选。内存不够部分（整个partition）在下次action时会重新计算  
action第一次计算时会发生persist()  
cache要在每一步操作中连用，不能对被赋值的变量调用  
persist可以使用不同的存储级别进行持久化  
在不同阶段，用`rdd.persist(StortageLevel.DISK_ONLY)` 例如在map后cache，那么第二次重新运行时就不会执行map，而是直接reduce。有rdd.unpersist 

能不存disk尽量不存，有时比重新算还慢
MEMORY_AND_DISK
MEMORY_ONLY_SER（序列化存，节省内存（小2到5倍），但要反序列化效率稍低，第二种选择），MEMORY_AND_DISK_SER
DISK_ONLY
MEMORY_ONLY_2, MEMORY_AND_DISK_2 备2份

> 当persist RDD时，默认会“evicts the least recently used partition”。不同persist选项有不同操作，比如memory_only时，重用溢出的persisted RDD会重新计算，而memory and disk会将溢出部分写到磁盘。默认算法LRU可通过`persistencePriority`控制。
>
> 当rdd被shuffle后，Spark会写一些数据到磁盘。当该rdd被重用时，就可避免shuffle了，类似于disk_only。但目前没有方法检查rdd是否有shuffle文件，除非通过UI看是否skipped



**join**：

空值null而非其他形式，选择合适的join，先完成筛选量大的join，做好filter，join前partition，广播

**aggregations**：

聚合前，空值null而非其他形式，前增加partition，增加内存。聚合后repartition。

**groupByKey/ reduceByKey:**

对于RDD，少用前者，因为它不会在map-side进行聚合。但注意不要与DF的groupBy + agg和DS的groupByKey + reduceGroups混淆，它们的行为会经过Catalyst优化，会有map-side聚合。

**mapPartitions替代map（foreachPartitions同理）：**

该算子比map更高效，但注意算子内运用iterator-to-iterator转换，而非一次性将iterator转换为一个集合（容易OOM）。详情看high performance spark的“Iterator-to-Iterator Transformations with mapPartitions”

**I/O**：

开启speculation（有些storage system会产生重复写入），HDFS系统和Spark在同一个节点

将数据写到数据库中时，使用foreachPartition，分batch提交等。具体查看Web log analysis。

**广播变量：**

比如一个函数要调用一个大的闭包变量时。

**其他**：

partition不够、倾斜或硬件（某台机器内存不够）

**数据倾斜**：参考美团的《Spark性能优化指南》：

shuffle，如distinct、Bykey、join、cogroup和repartition等，后可能产生，

后果：大部分task很快完成，小部分要很久。平时正常执行，某天出现OOM（也有可能时异常数据的问题，需要完善代码）。

分析：用sample + countBykey -> /count判断key的分布情况。

解决方法：

1.Hive预先处理：先在hive中聚合key或join表。但只是把任务交给hive处理而已，而且处理速度还慢。

2.当少数key数量巨大时，先估计分布，把量大的几个key过滤出来。

3.提高并行度

4.采用map-side聚合的算子

5.join类倾斜：其中一个表小时，广播join表；两个大表中的数据倾斜就自定义分类器。

其他思路：如果输入数据的总量确定，那么通过估计各key的占比应该就能估计出各key所占用的空间大小，从而对特定占比的key进行特定的改造，比如把key 1随机变为1_0或1_1。

---

[1]: http://spark.apache.org/docs/2.1.2/img/cluster-overview.png

参考：
书籍：
Spark: The Definitive Guide
High Performance Spark

文章：

美团的《Spark性能优化指南》

