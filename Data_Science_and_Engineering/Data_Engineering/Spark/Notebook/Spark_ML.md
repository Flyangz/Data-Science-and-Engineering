# Part VI. Advanced Analytics and Machine Learning

---

## Advanced Analytics and Machine Learning Overview

### 1.A Short Primer on Advanced Analytics

**目的**：deriving insights and making predictions or recommendations

**概念**

Supervised Learning：用含有label（因变量）的历史数据训练模型来预测新数据的label。训练过程通常用GD来不断调整参数以完善模型。

Classification：预测一个*categorical* 变量，即一个离散的，有限的value集合。结果只有一个值时，根据*categorical*变量的可取值的数量分为binary和multiclass。结果有多个值为Multilabel Classification

Regression：预测一个连续变量，一个实数。

Recommendation：基于相似客户的喜好或相似商品来推荐

Unsupervised Learning：从数据中寻找规律，没有label。

Graph Analytics：研究*vertices* (objects) 和*edges* (relationships between those objects)组成的结构

The Advanced Analytics Process

- 收集相关数据
- Cleaning and inspecting the data to better understand it.
- 特征工程
- 训练模型
- 比较和评估模型
- 利用模型的结果或模型本身来解决问题

  

### 2.Spark’s Advanced Analytics Toolkit

**介绍**

提供接口完成上述Advanced Analytics Process的模块。和其他ML库相比，Spark的更适合数据量大时使用。

> ml库提供DF接口。本书只介绍它。
>
> mllib库是底层APIs，现在是维护模式，只会修复bug，不会添加新feature。目前如果想进行streaming training，只能用millib。



**概念**

*Transformers*：转换数据的函数，DF => 新DF

*Estimators*： 包含fit和transform的类，根据功能可分为用于初始化数据的transformer和训练算法。 

Low-level data types：`Vector` （类似numpy）包含doubles类型，可以sparse（大部分为0）或dense（很多不同值）

```scala
//创建vector
val denseVec = Vectors.dense(1.0, 2.0, 3.0)
val size = 3
val idx = Array(1,2) // locations of non-zero elements in vector
val values = Array(2.0,3.0)
val sparseVec = Vectors.sparse(size, idx, values)
sparseVec.toDense
denseVec.toSparse
```



### 3.ML in Action

> RFormula: `import org.apache.spark.ml.feature.RFormula`
>
>  “~”分开target和terms；“+0”和“-1”一样，去掉intercept；“: ”数值乘法或二进制分类值; “.”除target的所有列

```scala
//加载数据
var df = spark.read.json("/data/simple-ml")
df.orderBy("value2").show()

//转化数据
//进入训练算法的数据只能是Double(for labels)或Vectro[Double](for features)
val supervised = new RFormula()
  .setFormula("lab ~ . + color:value1 + color:value2")
val fittedRF = supervised.fit(df)
val preparedDF = fittedRF.transform(df)
preparedDF.show()//会在原DF表后添加features和label列，Spark的ml算法的默认列名

//划分训练集和测试集
val Array(train, test) = preparedDF.randomSplit(Array(0.7, 0.3))

//训练和查看模型预测结果（对训练集的）
val lr = new LogisticRegression().setLabelCol("label").setFeaturesCol("features")
println(lr.explainParams())
val fittedLR = lr.fit(train)
fittedLR.transform(train).select("label", "prediction").show()

//建立pipeline
//transformers or models实例是不能在不同pipelines中重复使用的，所以上面的要重新new
val rForm = new RFormula()
val lr = new LogisticRegression().setLabelCol("label").setFeaturesCol("features")
val stages = Array(rForm, lr)
val pipeline = new Pipeline().setStages(stages)//pipeline.stages(0).asInstanceOf[RFormula]可得到该stage的对象。通常是最后取model或stringIndexerModel的inverse

//建立超参网格
val params = new ParamGridBuilder()
  .addGrid(rForm.formula, Array(
    "lab ~ . + color:value1",
    "lab ~ . + color:value1 + color:value2"))
  .addGrid(lr.elasticNetParam, Array(0.0, 0.5, 1.0))
  .addGrid(lr.regParam, Array(0.1, 2.0))
  .build()

//建立评估器
val evaluator = new BinaryClassificationEvaluator()
  .setMetricName("areaUnderROC")
  .setRawPredictionCol("prediction")
  .setLabelCol("label")

//交叉验证
val tvs = new TrainValidationSplit()
  .setTrainRatio(0.75) // also the default.
  .setEstimatorParamMaps(params)
  .setEstimator(pipeline)
  .setEvaluator(evaluator)
val tvsFitted = tvs.fit(train)

//转换测试集并评估areaUnderROC
evaluator.evaluate(tvsFitted.transform(test))

//可查看模型训练历史
val trainedPipeline = tvsFitted.bestModel.asInstanceOf[PipelineModel]
val TrainedLR = trainedPipeline.stages(1).asInstanceOf[LogisticRegressionModel]
val summaryLR = TrainedLR.summary
summaryLR.objectiveHistory
//提取最优模型的参数
.stages.last.extractParamMap

//每个模型的得分和参数
val paramsAndMetrics = tvsFitted.validationMetrics.
  zip(tvsFitted.getEstimatorParamMaps).sortBy(-_._1)
paramsAndMetrics.foreach { case (metric, params) => 
    println(metric)
    println(params)
    println() 
}


//储存模型
tvsFitted.write.overwrite().save("path")
//加载模型，根据模型版本来load，这里load的是CrossValidator得到的模型，所以用CrossValidatorModel（这里TrainValidationSplitModel）。之前手动的LogisticRegression则用LogisticRegressionModel
val model = TrainValidationSplitModel.load("path")
model.transform(test)
//如果想输出PMML格式，可以参考MLeap的github
```



### 4.部署模式

- 离线训练，用于分析（适合Spark）
- 离线训练，储存结果到数据库中，适合recommendation
- 离线训练，储存模型用于服务。（并非低延迟，启动Spark消耗大）
- 将分布式模型转化为运行得更快的单机模式。（Spark可导出PMML）
- 线上训练和使用。（结合Structured Streaming，适合部分ML模型）

---



## Preprocessing and Feature Engineering

### 1.Formatting Models According to Your Use Case

- 大部分 classification and regression：label和feature
- recommendation：users, items和ratings
- unsupervised learning：features
- graph analytics：vertices DF 和edges DF

```scala
//4个样本数据
val sales = spark.read.format("csv")
  .option("header", "true")
  .option("inferSchema", "true")
  .load("/data/retail-data/by-day/*.csv")
  .coalesce(5)
  .where("Description IS NOT NULL")//Spark对null的处理还在改进
val fakeIntDF = spark.read.parquet("/data/simple-ml-integers")
var simpleDF = spark.read.json("/data/simple-ml")
val scaleDF = spark.read.parquet("/data/simple-ml-scaling")
sales.cache()

//Transformers，转换Description
val tkn = new Tokenizer().setInputCol("Description")
tkn.transform(sales.select("Description")).show(false)
//Estimators 
val ss = new StandardScaler().setInputCol("features")
ss.fit(scaleDF).transform(scaleDF).show(false)

//High-Level Transformers
//RFormula：string默认one-hot（label为Double），numeric默认Double
val supervised = new RFormula()
  .setFormula("lab ~ . + color:value1 + color:value2")
supervised.fit(simpleDF).transform(simpleDF).show()
//SQL Transformers
val basicTransformation = new SQLTransformer()
  .setStatement("""
    SELECT sum(Quantity), count(*), CustomerID
    FROM __THIS__
    GROUP BY CustomerID
  """)
basicTransformation.transform(sales).show()
//VectorAssembler，将所有feature合并为一个大的vector，通常是pipeline的最后一步
val va = new VectorAssembler().setInputCols(Array("int1", "int2", "int3"))
va.transform(fakeIntDF).show()
```



### 2.连续型feature的转换

只适用于Double

```scala
val contDF = spark.range(20).selectExpr("cast(id as double)")

// bucketing，参数为最小值，至少三个中间划分值，最大值（即一共最少6组）。由于已划分，所以不需fit
//下面分组是[-1.0,5.0),[5.0, 10.0),[10.0,250.0)...
val bucketBorders = Array(-1.0, 5.0, 10.0, 250.0, 600.0)//最值也可选择scala.Double.NegativeInfinity/ PositiveInfinity
val bucketer = new Bucketizer().setSplits(bucketBorders).setInputCol("id")
//对于null or NaN 值，需要指定.handleInvalid 参数为某个值，或者keep those values, error or null, or skip those rows

//QuantileDiscretizer
val bucketer = new QuantileDiscretizer().setNumBuckets(5).setInputCol("id")

//Scaling and Normalization
//StandardScaler,.withMean（默认false），对于sparse数据消耗大
val sScaler = new StandardScaler().setInputCol("features")
//MinMaxScaler
val minMax = new MinMaxScaler().setMin(5).setMax(10).setInputCol("features")
//MaxAbsScaler，between −1 and 1
val maScaler = new MaxAbsScaler().setInputCol("features")
//ElementwiseProduct，不需fit
val scaleUpVec = Vectors.dense(10.0, 15.0, 20.0)
val scalingUp = new ElementwiseProduct()
  .setScalingVec(scaleUpVec)
  .setInputCol("features")//如果feature的某row是[1, 0.1, -1]，转换后变为[10, 1.5, -20]
//Normalizer,1,2,3等
val manhattanDistance = new Normalizer().setP(1).setInputCol("features")
```

> 还有一些高级的bucketing，如 locality sensitivity hashing (LSH) 等



### 3.Categorical Features

 **recommend** re-indexing every categorical variable when pre-processing just for consistency’s sake. 所以下面的transform和fit都是传入整个DF，特别提示除外。

```scala
//StringIndexer,一种string to 一个int值。也可对非string使用，但会先转换为string再转为int
val lblIndxr = new StringIndexer().setInputCol("lab").setOutputCol("labelInd")
//如果fit后的transform对象有未见过的，会error，或者通过下面代码设置skip整个row
valIndexer.setHandleInvalid("skip")
valIndexer.fit(simpleDF).setHandleInvalid("skip")

//IndexToString，例如将classification的结果转换回string。由于Spark保留了元数据，所以不需要fit。可能有些没保留，就多加一步.setLabels(Model.labels)
val labelReverse = new IndexToString().setInputCol("labelInd")

//VectorIndexer，设定最大分类量。下面transformer对于unique值多于两个的，不会进行分类。
val indxr = new VectorIndexer().setInputCol("features").setOutputCol("idxed")
  .setMaxCategories(2)

//OneHotEncoder
val lblIndxr = new StringIndexer().setInputCol("color").setOutputCol("colorInd")
val colorLab = lblIndxr.fit(simpleDF).transform(simpleDF.select("color"))
val ohe = new OneHotEncoder().setInputCol("colorInd")
ohe.transform(colorLab).show()

//普通encoder，用when，otherwise
df.select(when(col("happy") === true, 1).otherwise(2).as("encoded")).show

//mapEncoder，下面函数其实与null无关，也可查找“利用map进行数据转换”，没有otherwise可用。
df.na.replace("Description", Map("" -> "UNKNOWN"))
```



### 4.Text Data Transformers

两类：string categorical variables（前面提到的）和free-form text

```scala
//不需fit
//Tokenizer，space分隔
val tkn = new Tokenizer().setInputCol("Description").setOutputCol("DescOut")
val tokenized = tkn.transform(sales.select("Description"))
tokenized.show(false)

//RegexTokenizer，pattern分隔
val rt = new RegexTokenizer()
  .setInputCol("Description")
  .setOutputCol("DescOut")
  .setGaps(false)//设置false就提取pattern
  .setPattern(" ") // simplest expression
  .setToLowercase(true)

//StopWordsRemover
val englishStopWords = StopWordsRemover.loadDefaultStopWords("english")
val stops = new StopWordsRemover()
  .setStopWords(englishStopWords)
  .setInputCol("DescOut")

//NGram，下面代码将[Big, Data, Processing, Made]变为[Big Data, Data Processing, Processing Made]
val bigram = new NGram().setInputCol("DescOut").setN(2)

//词频CountVectorizer
val cv = new CountVectorizer()
  .setInputCol("DescOut")
  .setOutputCol("countVec")
  .setVocabSize(500)
  .setMinTF(1)//所transform的文本里出现的最低频率
  .setMinDF(2)//收入字典的最低频率
//下面结果，后边第二项是单词在字典中的位置（非对应），第三项为在此row的频率
|[rabbit, night, light]  |(500,[150,185,212],[1.0,1.0,1.0]) |

//反词频HashingTF，出现越少评分越高。不同于上面CountVectorizer，知道index不能返回词
val tf = new HashingTF()
  .setInputCol("DescOut")
  .setOutputCol("TFOut")
  .setNumFeatures(10000)
val idf = new IDF()
  .setInputCol("TFOut")
  .setOutputCol("IDFOut")
  .setMinDocFreq(2)
idf.fit(tf.transform(tfIdfIn)).transform(tf.transform(tfIdfIn)).show(false)
//下面结果，第二项为哈希值，第三项为评分
(10000,[2591,4291,4456],[1.0116009116784799,0.0,0.0])

//Word2Vec（深度学习部分，暂略)
```



### 5.Feature Manipulation and Selection

```scala
//PCA
val pca = new PCA().setInputCol("features").setK(2)
pca.fit(scaleDF).transform(scaleDF).show(false)

//Interaction，用RFormula 

//Polynomial Expansion
val pe = new PolynomialExpansion().setInputCol("features").setDegree(2)

//ChiSqSelector
val chisq = new ChiSqSelector()
  .setFeaturesCol("countVec")
  .setLabelCol("CustomerId")
  .setNumTopFeatures(2)//百分比
```



### 6.Advanced Topics

```scala
//Persisting Transformers
val fittedPCA = pca.fit(scaleDF)
fittedPCA.write.overwrite().save("/tmp/fittedPCA")
val loadedPCA = PCAModel.load("/tmp/fittedPCA")
loadedPCA.transform(scaleDF).show()

//自定义转换
class MyTokenizer(override val uid: String)
  extends UnaryTransformer[String, Seq[String],
    MyTokenizer] with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("myTokenizer"))

  val maxWords: IntParam = new IntParam(this, "maxWords",
    "The max number of words to return.",
  ParamValidators.gtEq(0))

  def setMaxWords(value: Int): this.type = set(maxWords, value)

  def getMaxWords: Integer = $(maxWords)

  override protected def createTransformFunc: String => Seq[String] = (
    inputString: String) => {
      inputString.split("\\s").take($(maxWords))
  }

  override protected def validateInputType(inputType: DataType): Unit = {
    require(
      inputType == StringType, s"Bad input type: $inputType. Requires String.")
  }

  override protected def outputDataType: DataType = new ArrayType(StringType,
    true)
}

// this will allow you to read it back in by using this object.
object MyTokenizer extends DefaultParamsReadable[MyTokenizer]
  val myT = new MyTokenizer().setInputCol("someCol").setMaxWords(2)
  myT.transform(Seq("hello world. This text won't show.").toDF("someCol")).show()

//另外一个自定义转换
class ConfigurableWordCount(override val uid: String) extends Transformer {
  final val inputCol= new Param[String](this, "inputCol", "The input column")
  final val outputCol = new Param[String](this, "outputCol", "The output column")

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)
  //构造器
  def this() = this(Identifiable.randomUID("configurablewordcount"))
  //current stage的copy，一般用defaultCopy就可以了
  def copy(extra: ParamMap): HardCodedWordCountStage = {
    defaultCopy(extra)
  }

  //修改返回的schema: StructType。记得先检查输入类型
  override def transformSchema(schema: StructType): StructType = {
    // Check that the input type is a string
    val idx = schema.fieldIndex($(inputCol))
    val field = schema.fields(idx)
    if (field.dataType != StringType) {
      throw new Exception(
        s"Input type ${field.dataType} did not match input type StringType")
    }
    // Add the return field
    schema.add(StructField($(outputCol), IntegerType, false))
  }

  def transform(df: Dataset[_]): DataFrame = {
    val wordcount = udf { in: String => in.split(" ").size }
    df.select(col("*"), wordcount(df.col($(inputCol))).as($(outputCol)))
  }
}
```

关于estimator和model的自定义，参考《high performance spark》的CustomPipeline.scala。可以加些caching代码。另外org.apache.spark.ml.Predictor 和 org.apache.spark.ml.classificationClassifier 有时更方便，因为它们能自动处理schema transformation。后者多了rawPredictionColumn和getNumClasses。而回归跟聚类就只能用estimator接口了。

---

### 7.例子linkage(C2)

#### 1.transpose summary table

```scala
//parsed为某DF
val summary = parsed.describe()//summary表第一行为列名，第一列为metric名
val schema = summary.schema//StructType(StructField(summary,StringType,true)...)全部都是StringType
val longDF = summary.flatMap(row => {
  val metric = row.getString(0) 
  (1 until row.size).map(i => {
    (metric, schema(i).name, row.getString(i).toDouble)
    })
}).toDF("metric", "field", "value")

//前5行结果为，下面还有其他metric
+------+------------+---------+
|metric|       field|    value|
+------+------------+---------+
| count|        id_1|5749132.0|
| count|        id_2|5749132.0|
| count|cmp_fname_c1|5748125.0|
| count|cmp_fname_c2| 103698.0|
| count|cmp_lname_c1|5749132.0|
+------+------------+---------+

//进行透视
val wideDF = longDF
  .groupBy("field")
  .pivot("metric")//2.2以下要加，Seq("count", "mean", "stddev", "min", "max")？
  .sum()//每对组合都是唯一的，所以sum()其实只有一个数

//可以将上面两步封装为一个function。打开IDEA（下面代码忽略了import DF，sql.functions之类），写一个Pivot.scala，然后load这个类，就可以对任何.describe()表使用了。
//要对上面的summary改为desc
def pivotSummary(desc: DataFrame): DataFrame = { 
    val schema = desc.schema
    import desc.sparkSession.implicits._

//查看结果
wideDF.select("field", "count", "mean").show()
```

> 上面练习中，row.getString(i)得到的是java.lang.String类，其本身没有toDouble方法，是通过Scala的隐式转换实现的。这种转换把String变为了StringOps（Scala类），然后调用该类的toDouble。隐式转换让我们增加核心类的功能，但有时让人难以弄清功能的来源。



#### 2.feature 选择

其实简单join用SQL可能更清晰，下面有点附加功能，当作阅读理解。它主要目的是比较两个.describe()表（一个match表，一个miss表）的差异

```scala
matchSummaryT.createOrReplaceTempView("match_desc")
missSummaryT.createOrReplaceTempView("miss_desc")
spark.sql("""
  SELECT a.field, a.count + b.count total, a.mean - b.mean delta
  FROM match_desc a INNER JOIN miss_desc b ON a.field = b.field
  WHERE a.field NOT IN ("id_1", "id_2")
  ORDER BY delta DESC, total DESC
""").show()
//结果前两row，field中每项的值域为0～1
+------------+---------+--------------------+
|       field|    total|               delta|
+------------+---------+--------------------+
|     cmp_plz|5736289.0|  0.9563812499852176|
|cmp_lname_c2|   2464.0|  0.8064147192926264|
+------------+---------+--------------------+
//上表中，total看数据缺失情况，delta看差异情况
```



#### 3.交叉表分析

例子数据比较简单，所以书中方法只是为了展示，最后有简便的方法实现。

创建case class并把DF[row] -> Dataset[MatchData]，这样对结构化表格中的元素的操作更灵活，但会放弃DF的部分效率。

```scala
//1.创建转化需要的类
case class MatchData(//下面删去了部分变量
    id_1: Int,
    cmp_fname_c1: Option[Double], 
    cmp_plz: Option[Int], 
    is_match: Boolean
)
val matchData = parsed.as[MatchData]

//2.创建case class拥有+方法
case class Score(value: Double) { 
    def +(oi: Option[Int]) = {
		Score(value + oi.getOrElse(0)) 
    }
}
//3.把认为合适的feature加总（分析来自Spark Join1.实践），得出评分
def scoreMatchData(md: MatchData): Double = {
    (Score(md.cmp_lname_c1.getOrElse(0.0)) + md.cmp_plz +
     	md.cmp_by + md.cmp_bd + md.cmp_bm).value
}
//4.把评分和label抽出来
val scored = matchData.map { 
    md => (scoreMatchData(md), md.is_match) 
}.toDF("score", "is_match")

//5.创建crosstab
def crossTabs(scored: DataFrame, t: Double): DataFrame = { 
    scored.selectExpr(s"score >= $t as above", "is_match")
          .groupBy("above")
          .pivot("is_match")
          .count()
}

crossTabs(scored, 2.0).show()
+-----+-----+-------+ 
|above| true|  false| 
+-----+-----+-------+
| true|20931| 596414| 
|false| null|5131787| 
+-----+-----+-------+

//上面可以直接在DF实现，而不需要转为Dataset。
val scored = parsed
    .na.fill(0, Seq("cmp_lname_c1",...))//如果不fill，下面列中有null行的结果为null
    .withColumn("score", expr("cmp_lname_c1 + ..."))
    .select("score", "is_match")
```

------



## 

## Classification

#### 1.四个最常用模型

**模型Scalability**

| Model                  | Features count  | Training examples | Output classes                  |
| ---------------------- | --------------- | ----------------- | ------------------------------- |
| Logistic regression    | 1 to 10 million | No limit          | Features x Classes < 10 million |
| Decision trees         | 1,000s          | No limit          | Features x Classes < 10,000s    |
| Random forest          | 10,000s         | No limit          | Features x Classes < 100,000s   |
| Gradient-boosted trees | 1,000s          | No limit          | Features x Classes < 10,000s    |

**模型参数**

| Logistic regression                              | Decision trees                                               | Random forest                                            |     GDBT（目前只能binary）     |
| ------------------------------------------------ | ------------------------------------------------------------ | -------------------------------------------------------- | :----------------------------: |
| family: multinomial or binary                    | maxDepth: 默认5                                              | numTrees                                                 | lossType: 只支持 logistic loss |
| elasticNetParam: 0~1, 0为纯L2，1为纯L1           | maxBins: 对某feature的分类数，默认32                         | featureSubsetStrategy特征考虑数: auto, all, sqrt, log2等 |         maxIter:  100          |
| fitIntercept: boolean 如果没有normalized通常会设 | impurity:   “entropy” or “gini” (default)                    |                                                          |     stepSize: 0~1，默认0.1     |
| regParam: >=0                                    | minInfoGain: 最小 information gain，默认0                    |                                                          |                                |
| standardization: boolean                         | minInstancePerNode：默认1                                    |                                                          |                                |
|                                                  |                                                              |                                                          |                                |
| maxIter: 默认100，不应该第一个调                 | checkpointInterval: -1取消，10表示每10次迭代记录一次。还要设置 `checkpointDir和useNodeIdCache=true` | checkpointInterval                                       |       checkpointInterval       |
| tol: 默认1.0E-6，不应该第一个调                  |                                                              |                                                          |                                |
| weightCol                                        |                                                              |                                                          |                                |
|                                                  |                                                              |                                                          |                                |
| threshold: 0~1，用于惩罚错误                     |                                                              |                                                          |                                |
| thresholds: 同上，但适用于 multiclass            | thresholds                                                   | thresholds                                               |           thresholds           |



```scala
//一些补充，下面主要是Logistic regression
val lr = new LogisticRegression().setMaxIter(10).setRegParam(0.3)
  .setElasticNetParam(0.8)
//查看参数
println(lr.explainParams())
//查看结果，对于multiclass，用lrModel.coefficientMatrix and lrModel.interceptVector
println(lrModel.coefficients)
println(lrModel.intercept)
//查看summary，暂时不适用逻辑回归的multiclass，其他模型也可以尝试调用summary后看有什么方法。不会重新计算
val summary = lrModel.summary
summary.residuals.show()//显示feature的权重
summary.rootMeanSquaredError
summary.r2
val bSummary = summary.asInstanceOf[BinaryLogisticRegressionSummary]
println(bSummary.areaUnderROC)
bSummary.roc.show()
bSummary.pr.show()
summary.objectiveHistory//看每次迭代的效果
```



#### 2.其他

**Naive Bayes**:

indicator variables represent the existence of a term in a document; or the *multinomial model*, where the total counts of terms are used.

所有input features要非负

一些参数说明：

modelType: “bernoulli” or “multinomial"

weightCol

smoothing: 默认1

thresholds



**Evaluators for Classification and Automating Model Tuning**

`BinaryClassificationEvaluator`: “areaUnderROC” and areaUnderPR"

`MulticlassClassificationEvaluator:  “f1”, “weightedPrecision”, “weightedRecall”, and “accuracy”



**Detailed Evaluation Metrics**

```scala
//三种classification相似
val out = model.transform(bInput)
  .select("prediction", "label")
  .rdd.map(x => (x(0).asInstanceOf[Double], x(1).asInstanceOf[Double]))
val metrics = new BinaryClassificationMetrics(out)
metrics.areaUnderPR
metrics.areaUnderROC
println("Receiver Operating Characteristic")
metrics.roc.toDF().show()
```



**One-vs-Rest Classifier**

查看Spark documentation，有例子



### 例子Predicting Forest Cover (C4)

决策树对异常值（一些极端或错误值）很稳健。

树的建立很耗费内存。

one-hot能使模型对特征逐个考虑（当然更占内存），如果一列categorical特征，模型就可能通过把部分特征分组，而不会深入考虑，但准确率不一定更差。

```scala
//创建schema
val colNames = Seq(
    "Elevation", "Aspect", "Slope",
    ...    
  )++(
    (0 until 4).map(i => s"Wilderness_Area_$i")//注意这种创建collection的方式(IndexedSeq)
  ) ++ Seq("Cover_Type")
val data = dataWithoutHeader.toDF(colNames:_*)//添加schema的方式
    .withColumn("Cover_Type", $"Cover_Type".cast("double"))

//生成Vector
val inputCols = trainData.columns.filter(_ != "Cover_Type") 
val assembler = new VectorAssembler().
      setInputCols(inputCols).
      setOutputCol("featureVector")
val assembledTrainData = assembler.transform(trainData)

//模型
//查看树的逻辑
model.toDebugString
//打印featureImportances
model.featureImportances.toArray.zip(inputCols).
      sorted.reverse.foreach(println)

//用DF来计算confusionMatrix，Spark内置的要用rdd
val confusionMatrix = predictions
  .groupBy("Cover_Type")
  .pivot("prediction", (1 to 7))//1 to 7是对应prediction里面的内容的。如果prediction里面没有7，也可以，但该列全是null
  .count()
  .na.fill(0.0)
  .orderBy("Cover_Type")

//设计一个随机classifier
def classProbabilities(data: DataFrame): Array[Double] = { 
    val total = data.count()
    data.groupBy("Cover_Type").count()
      .orderBy("Cover_Type")
      .select("count").as[Double]
      .map(_ / total).collect() 
}
```



#### 2.将one-hot转化为一列类型特征

```scala
def unencodeOneHot(data: DataFrame): DataFrame = {
  //要转换的列名
  val wildernessCols = (0 until 4).map(i => s"Wilderness_Area_$i").toArray
  //将上面的列名上的数值合并为一个vector
  val wildernessAssembler = new VectorAssembler()
    .setInputCols(wildernessCols)
    .setOutputCol("wilderness")
  
  //提取1.0的index来将one-hot转化categorical number
  val unhotUDF = udf((vec: Vector) => vec.toArray.indexOf(1.0).toDouble)
  
  //转化数据
  val withWilderness = wildernessAssembler.transform(data)
    .drop(wildernessCols:_*)
    .withColumn("wilderness", unhotUDF($"wilderness"))
}

//转化后的数据在pipeline中添加一步indexer，这样能使Spark将这些能被划分的列视为categorical feature。注意这里假设所处理的数据中4个种类至少出现一次。
val indexer = new VectorIndexer()
  .setMaxCategories(4)
  .setInputCol("featureVector")
  .setOutputCol("indexedVector")
val pipeline = new Pipeline().setStages(Array(assembler, indexer, classifier))
```



---



## Regression

**模型Scalability**

| Model                         | Number features | Training examples |
| ----------------------------- | --------------- | ----------------- |
| Linear regression             | 1 to 10 million | No limit          |
| Generalized linear regression | 4,096           | No limit          |
| Isotonic regression           | N/A             | Millions          |
| Decision trees                | 1,000s          | No limit          |
| Random forest                 | 10,000s         | No limit          |
| Gradient-boosted trees        | 1,000s          | No limit          |
| Survival regression           | 1 to 10 million | No limit          |

**Generalized Linear Regression**

下面Supported links指定线性预测变量与分布函数的均值之间的关系

| Family   | Response type            | Supported links         |
| -------- | ------------------------ | ----------------------- |
| Gaussian | Continuous               | Identity*, Log, Inverse |
| Binomial | Binary                   | Logit*, Probit, CLogLog |
| Poisson  | Count                    | Log*, Identity, Sqrt    |
| Gamma    | Continuous               | Inverse*, Idenity, Log  |
| Tweedie  | Zero-inflated continuous | Power link function     |

参数：

family和link参考上表

 solver：目前只支持irls（iteratively reweighted least squares）

variancePower：0~1，0默认，1表无穷。表示分布方差和均值的关系，只适用Tweedie 

linkPower：Tweedie 

linkPredictionCol：boolean



**Advanced Methods**（略）

Survival Regression (Accelerated Failure Time)

Isotonic Regression



**Evaluators and Automating Model Tuning**

```scala
//Evaluators 
val glr = new GeneralizedLinearRegression()
  .setFamily("gaussian")
  .setLink("identity")
val pipeline = new Pipeline().setStages(Array(glr))
val params = new ParamGridBuilder().addGrid(glr.regParam, Array(0, 0.5, 1))
  .build()
val evaluator = new RegressionEvaluator()
  .setMetricName("rmse")
  .setPredictionCol("prediction")
  .setLabelCol("label")
val cv = new CrossValidator()//大量数据时用得不多，太耗时
  .setEstimator(pipeline)
  .setEvaluator(evaluator)
  .setEstimatorParamMaps(params)
  .setNumFolds(2) // should always be 3 or more but this dataset is small
val model = cv.fit(df)

//Metrics
val out = model.transform(df)
  .select("prediction", "label")
  .rdd.map(x => (x(0).asInstanceOf[Double], x(1).asInstanceOf[Double]))
val metrics = new RegressionMetrics(out)
println(s"MSE = ${metrics.meanSquaredError}")
println(s"RMSE = ${metrics.rootMeanSquaredError}")
println(s"R-squared = ${metrics.r2}")
println(s"MAE = ${metrics.meanAbsoluteError}")
println(s"Explained variance = ${metrics.explainedVariance}")
```

---



## Recommendation

### 1.Collaborative Filtering with Alternating Least Squares

仅根据用户过去和商品的interaction情况，而非用户或商品的attributes，来估计用户心中的商品排名。需要三列：user ID , item ID, and rating 。其中rating可显式（user自己的评分）可隐式（user和item的interaction程度）。

该算法会倾向于大众商品和具有很多说明信息的商品。对于新商品或客户有cold start问题。

在实际生产当中，一般会预先计算所有用户的推荐，然后存到NoSQL来实现实时推荐，但这很浪费存储空间（大部分人在当天不一定需要推荐）。而单独计算需要几秒钟的时间。[Oryx 2](https://github.com/OryxProject/oryx) 可能是一个解决方法。



> 将一些数值转化为Integer或者Int更有效率。

参数：

rank：latent factors数量，默认10

alpha：implicit feedback时，被观察和未被观察的互动的相对比重，越高说明越看重已记录的，默认1，40也是个不错的选择

regParam：默认0.1

implicitPrefs：boolean，是否implicit，默认true

nonnegative：默认false，non-negative constraints on the least-squares problem

numUserBlocks：默认10

numItemBlocks：默认10

maxIter：默认10

checkpointInterval

seed

coldStartStrategy：设定模型如何预测新客户或商品，只可选`drop` and `nan`

> blocks一般是one to five million ratings per block，如果少于这个数，更多的block也不会提升效率

```scala
val ratings = spark.read.textFile("/data/sample_movielens_ratings.txt")
  .selectExpr("split(value , '::') as col")
  .selectExpr(
    "cast(col[0] as int) as userId",
    "cast(col[1] as int) as movieId",
    "cast(col[2] as float) as rating",
    "cast(col[3] as long) as timestamp")
val Array(training, test) = ratings.randomSplit(Array(0.8, 0.2))
val als = new ALS()
  .setMaxIter(5)
  .setRegParam(0.01)
  .setUserCol("userId")
  .setItemCol("movieId")
  .setRatingCol("rating")
println(als.explainParams())
val alsModel = als.fit(training)
val predictions = alsModel.transform(test)

//结果，查看排名前十的
alsModel.recommendForAllUsers(10)
  .selectExpr("userId", "explode(recommendations)").show()
alsModel.recommendForAllItems(10)
  .selectExpr("movieId", "explode(recommendations)").show()

//评估，先将cold-start strategy设为drop
val evaluator = new RegressionEvaluator()
  .setMetricName("rmse")
  .setLabelCol("rating")
  .setPredictionCol("prediction")
val rmse = evaluator.evaluate(predictions)
println(s"Root-mean-square error = $rmse")

//Regression Metrics
val regComparison = predictions.select("rating", "prediction")
  .rdd.map(x => (x.getFloat(0).toDouble,x.getFloat(1).toDouble))
val metrics = new RegressionMetrics(regComparison)
metrics.rootMeanSquaredError//和上面Root-mean-square error一样

//Ranking Metrics，下面评估不关注值，而是ALS是否会推荐某个值以上的商品
//下面将rating大于2.5的定为好的商品
val perUserActual = predictions
  .where("rating > 2.5")
  .groupBy("userId")
  .agg(expr("collect_set(movieId) as movies"))
//下面提取ALS推荐的商品
val perUserPredictions = predictions
  .orderBy(col("userId"), col("prediction").desc)
  .groupBy("userId")
  .agg(expr("collect_list(movieId) as movies"))
//合并上面两个数据，并截取推荐中的前15个
val perUserActualvPred = perUserActual.join(perUserPredictions, Seq("userId"))
  .map(row => (
    row(1).asInstanceOf[Seq[Integer]].toArray,
    row(2).asInstanceOf[Seq[Integer]].toArray.take(15)
  ))
//判断推荐的平均正确率
val ranks = new RankingMetrics(perUserActualvPred.rdd)
ranks.meanAveragePrecision
ranks.precisionAt(5)//看具体排第5的准确率
```

> latent-factor models：通过相对少数量的unobserved, underlying reasons来解释客户和商品之间大量的interactions。
>
> matrix factorization model： 假设一个网格，row代表用户，col代表商品，如果某格子为1，该格子对应的用户和商品有interaction。这可以是一个x * y的矩阵A。利用latent-factor model的思想，把客户和产品通过产品类型（如电子，图书，零食等k种）来联系，那么矩阵可分解为x * k和 y * k（其中一个乘以另一个的转置就能大概还原，但不可能真正还原）。但是如果这两个被分解出来的矩阵rank太低，对原本a*b的还原就很差。
>
> 现在目的是通过已知的A（所记录到的用户和商品的interaction）和Y（商品的y * k，实质也未知，通常随机生成）求X（用户的x * k）。由于不能真正解出，所以只能较少差别，这就是叫Least Squares的原因。alternating 来源于既可通过AY求X，也可通过AX求Y。
>
> 本模型的 user-feature 和 product-feature 的矩阵相当大（用户数 x feature数，商品数 x feature数）

### 例子Recommending Music(C3)

### 1.text文件的切割

```scala
//rawUserArtistData为RDD[String]，某row：“1000002 1 55”
val userArtistDF = rawUserArtistData.map { line => 
    val Array(user, artist, _*) = line.split(' ') //利用_*可以使Array接收多于3个的参数
    (user.toInt, artist.toInt)//如果数值不大，用Int更有效率。转换后看min和max确定没有超过限度，以及是否有负数
}.toDF("user", "artist")

//分离ID和对应的作品名，某row：“122	app”
//下面代码不完善，不能适应没有tab分隔的row或者空白row
rawArtistData.map { line =>
    val (id, name) = line.span(_ != '\t') //在第一个tab处前断开（保留tab），如果没有tab就在末尾断开（返回(String, String)，但最后一个是null）。也可以用val Array(..) =.split("\t",2)，2分为两份，0为全分
    (id.toInt, name.trim)
}.count()

//这个更好（下面可以直接用Exception省去一个if，但可能没那么安全）
val artistByID = rawArtistData.flatMap { line => 
    val (id, name) = line.span(_ != '\t')
    if (name.isEmpty) {
        None
    }else{ 
        try {
            Some(id.toInt, name.trim)//加一个以上的括号结果一样
        } catch {
            case _: NumberFormatException => None 
        }
    }
}.toDF("id", "name")

//把ID的alias（别名）和正确的ID转化为Map，某row：“123	22”
val artistAlias = rawArtistAlias.flatMap { line =>
    val Array(artist, alias) = line.split('\t') 
    if (artist.isEmpty) {
        None
    }else{
        Some((artist.toInt, alias.toInt))
    }
}.collect().toMap

 artistByID.filter($"id" isin (1208690, 1003926)).show()
+-------+----------------+ 
|     id|            name| 
+-------+----------------+ 
|1208690|Collective Souls| 
|1003926| Collective Soul| 
+-------+----------------+
```



### 2.利用map进行数据转换

```scala
//下面代码把有别名ID的作品统一成唯一ID
def buildCounts(
    rawUserArtistData: Dataset[String],
    bArtistAlias: Broadcast[Map[Int,Int]]): DataFrame = {
        rawUserArtistData.map { line =>
    val Array(userID, artistID, count) = line.split(' ').map(_.toInt) 
    val finalArtistID =
        bArtistAlias.value.getOrElse(artistID, artistID)
    (userID, finalArtistID, count)
    }.toDF("user", "artist", "count")
}

val bArtistAlias = spark.sparkContext.broadcast(artistAlias) 
val trainData = buildCounts(rawUserArtistData, bArtistAlias)
trainData.cache()//转换后将用于训练的数据存到内存，否则ALS每次用到数据时都要重新算
```



### 3.ALS模型的实现以及推断用户偏好

```scala
val model = new ALS()
    .setSeed(Random.nextLong())//不设的话会是相同的默认seed，其他Spark MLlib算法一样
    .setImplicitPrefs(true)
    .setRank(10)
    .setRegParam(0.01)
    .setAlpha(1.0)
    .setMaxIter(5)
    .setUserCol("user")
    .setItemCol("artist")
    .setRatingCol("count")
    .setPredictionCol("prediction")
    .fit(trainData)

//看结果，下面两段代码当作阅读理解
//1.查看某用户接触过的商品
val userID = 2093760
val existingArtistIDs = trainData
    .filter($"user" === userID)
    .select("artist").as[Int].collect()
artistByID.filter($"id" isin (existingArtistIDs:_*)).show()//注意这个_*用法
//2.将推荐的商品排名，并取前howMany个。并没有把用户接触过的商品过滤掉
def makeRecommendations( 
    model: ALSModel,
    userID: Int,
    howMany: Int): DataFrame = {
   
   val toRecommend = model.itemFactors
    .select($"id".as("artist"))
    .withColumn("user", lit(userID))
   
   model.transform(toRecommend)
    .select("artist", "prediction")
    .orderBy($"prediction".desc)
    .limit(howMany)
}
val topRecommendations = makeRecommendations(model, userID, 5) 


//在2.2中，直接用下面代码可以得到全部前10。
alsModel.recommendForAllUsers(10)
  .selectExpr("userId", "explode(recommendations)").show()

//根据结果推测用户的偏好（根据所推荐商品的名字）
//提取被推荐的商品的ID
val recommendedArtistIDs = 
    topRecommendations.select("artist").as[Int].collect()
//查看ID对应的商品名
artistByID.filter($"id" isin (recommendedArtistIDs:_*)).show()
```



### 4.ALS模型的评估

假设interaction越多，越喜欢。尽管用户之前的一些interaction没有被记录，而且少interaction并不一定是坏的推荐。

```scala
//书中利用自己编写的mean AUC进行评估
//先划分训练集和测试集，并cache。下一节的CrossValidator结合pipeline更实用。
val Array(trainData, cvData) = allData.randomSplit(Array(0.9, 0.1)) trainData.cache()
cvData.cache()
//计算量大而体积不大的变量也要broadcast？
val allArtistIDs = allData.select("artist").as[Int].distinct().collect() 
val bAllArtistIDs = spark.sparkContext.broadcast(allArtistIDs)
//重新执行3中的模型

//下面是书中自定义的方法。areaUnderCurve在其GitHub中。
areaUnderCurve(cvData, bAllArtistIDs, model.transform)
```



### 5.调参

```scala
//由于没有合适的evaluator，没有连成pipelines，这里就手动写grid调超参数
val evaluations =
    for (rank     <- Seq(5,  30);
         regParam <- Seq(1.0, 0.0001);
         alpha    <- Seq(1.0, 40.0))
    yield {
        val model = new ALS().
        ...//参数略

        val auc = areaUnderCurve(cvData, bAllArtistIDs, model.transform)

        model.userFactors.unpersist()//测试完后马上清空
        model.itemFactors.unpersist()

        (auc, (rank, regParam, alpha))
    }
//打印结果
evaluations.sorted.reverse.foreach(println)
```

```scala
println(s"$userID -> ${recommendedArtists.mkString(", ")}")
```

> 例子的一些补充：
>
> 没有考察数值范围的合理性，例如一些播放时长超过现实可能（听某个artist的作品33年时间）
>
> 没有处理缺失或者无意义值，例如unknown artist

------





#### 2.Frequent Pattern Mining（需要查看官方例子）

---



## Unsupervised Learning

**模型Scalability**

| Model               | Statistical recommendation | Computation limits               | Training examples |
| ------------------- | -------------------------- | -------------------------------- | ----------------- |
| *k*-means           | 50 to 100 maximum          | Features x clusters < 10 million | No limit          |
| Bisecting *k*-means | 50 to 100 maximum          | Features x clusters < 10 million | No limit          |
| GMM                 | 50 to 100 maximum          | Features x clusters < 10 million | No limit          |
| LDA                 | An interpretable number    | 1,000s of topics                 | No limit          |

| k-means                                    | Bisecting *k*-means            | GMM                            | LDA（暂略） |
| ------------------------------------------ | ------------------------------ | ------------------------------ | ----------- |
| k                                          | 𝗄                              | 𝗄                              | 𝗄：默认10   |
| initMode：random and 默认𝘬-means\|\|       | minDivisibleClusterSize：默认1 |                                |             |
| initSteps：默认2，𝘬-means\|\| 初始化的步数 |                                |                                |             |
| maxIter：默认20                            | maxIter：默认20                | maxIter：默认100               |             |
| tol：默认0.001（越小可以移动得越多）       |                                | tol：默认0.01，会受maxIter限制 |             |

### Anomaly Detection in Network Traffic (C5)

```scala
//查看cluster对label的分组情况
val withCluster = pipelineModel.transform(numericOnly)
withCluster.select("cluster", "label").
    groupBy("cluster", "label").count().
    orderBy($"cluster", $"count".desc).
    show(25)
```

#### 1.评估KMean（拐点或Entropy）

```scala
//由于非监督学习缺乏evaluator，所以网格也只能手动算
def clusteringScore0(data: DataFrame, k: Int): Double = { 
    val assembler = new VectorAssembler()
       .setInputCols(data.columns.filter(_ != "label"))
       .setOutputCol("featureVector") 
    val kmeans = new KMeans()
       .setSeed(Random.nextLong())
       .setK(k)
       .setPredictionCol("cluster")
       .setFeaturesCol("featureVector")
    val pipeline = new Pipeline().setStages(Array(assembler, kmeans))
    val kmeansModel = pipeline.fit(data).stages.last.asInstanceOf[KMeansModel]   
    kmeansModel.computeCost(assembler.transform(data)) / data.count()
}

//在拐点找出合适的K
(20 to 100 by 20).map(k => (k, clusteringScore0(numericOnly, k))).
      foreach(println)

//定义entropy的计算方法
def entropy(counts: Iterable[Int]): Double = { 
    val values = counts.filter(_ > 0)
    val n = values.map(_.toDouble).sum 
    values.map { v => 
        val p=v/n
        -p * math.log(p)
    }.sum
}

//计算Entropy评分
val clusterLabel = pipelineModel.transform(data). 
    select("cluster", "label").as[(Int, String)]
val weightedClusterEntropy = clusterLabel. 
    groupByKey { case (cluster, _) => cluster }. 
    mapGroups { case (_, clusterLabels) =>
        val labels = clusterLabels.map { case (_, label) => label }.toSeq 
        val labelCounts = labels.groupBy(identity).values.map(_.size)    
        labels.size * entropy(labelCounts)
    }.collect()

weightedClusterEntropy.sum / data.count()
```



#### 2.分类变量转为one-hot

```scala
//nonnumeric features 不能用于KMean，可将categorical features转为one-hot
//可以直接在函数上加col参数，而不需要指明表...
def oneHotPipeline(inputCol: String): (Pipeline, String) = { 
    val indexer = new StringIndexer().
        setInputCol(inputCol).
        setOutputCol(inputCol + "_indexed") 
    val encoder = new OneHotEncoder().
        setInputCol(inputCol + "_indexed").
        setOutputCol(inputCol + "_vec")
    val pipeline = new Pipeline().setStages(Array(indexer, encoder))
    (pipeline, inputCol + "_vec")
}
```



#### 3.找出异常

```scala
val clustered = pipelineModel.transform(data) 
val threshold = clustered.
    select("cluster", "scaledFeatureVector").as[(Int, Vector)].
    map { case (cluster, vec) => Vectors.sqdist(centroids(cluster), vec) }. 
    orderBy($"value".desc).take(100).last

val originalCols = data.columns
val anomalies = clustered.filter { row => 
    val cluster = row.getAs[Int]("cluster")
    val vec = row.getAs[Vector]("scaledFeatureVector")
    Vectors.sqdist(centroids(cluster), vec) >= threshold }.select(originalCols.head, originalCols.tail:_*)//select("*")
anomalies.first()
```

> 本例子的距离函数可以改为Mahalanobis distance，但Spark暂时没有
>
> 运用Gaussian mixture model 或 DBSCAN （未实现）也是一个选择。



## MLlib（了解）

Mllib的supervised用labeled points，而unsupervised用vectors。注意它们不同于Scala和spark ml的同名类。fromML可以将ml的vectors转换为mllib的

```scala
import com.github.fommil.netlib.BLAS.{getInstance => blas}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS,
  LogisticRegressionModel}
import org.apache.spark.mllib.linalg.{Vector => SparkVector}//改名，避免混淆
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.feature._
```

### feature encoding 和 data preparation

Mllib 提供feature selection和scaling

```scala
//numeric数据
Vectors.dense()/.dense()

//对于文本数据
//HashingTF，如果需要HashingTF处理结果外的信息，应像下面那样用。
def toVectorPerserving(rdd: RDD[RawPanda]): RDD[(RawPanda, SparkVector)] = {
  val ht = new HashingTF()
  rdd.map{panda =>
    val textField = panda.pt
    val tokenizedTextField = textField.split(" ").toIterable
    (panda, ht.transform(tokenizedTextField))
  }
}
//Word2Vec
def word2vecTrain(rdd: RDD[String]): Word2VecModel = {
  // Tokenize our data
  val tokenized = rdd.map(_.split(" ").toIterable)
  val wv = new Word2Vec()
  wv.fit(tokenized)
}

//准备训练数据
//Supervised，LabeledPoint接收double和vectors
LabeledPoint(booleanToDouble(rp.happy), Vectors.dense(combined))
//文字数据创建map
val distinctLabels: Array[T] = rdd.distinct().collect()
 distinctLabels.zipWithIndex.map{case (label, x) => (label, x.toDouble)}.toMap
//scaling and selection
//training and prediction
//保存
//Saveable(internal format)
model.save()
Spercific_Model.load()
//PMMLExportable，Spark能产出，但不能直接读取
model.toPMML()
```

## 补充说明

1.参数最好全部显式设置，不同版本的默认值可能变。

## 参考书籍：

OReilly Spark: The Definitive Guide

OReilly Advanced Analytics with Spark 2nd Edition