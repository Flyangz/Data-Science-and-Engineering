package ML.Titanic

import org.apache.spark.SparkContext
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.ml.feature.Bucketizer
import org.apache.spark.ml.feature.QuantileDiscretizer
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.OneHotEncoder
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.classification.GBTClassifier
import org.apache.spark.ml.tuning.ParamGridBuilder
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.tuning.{TrainValidationSplit, TrainValidationSplitModel}
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.types._


/**
  * GBTClassifier for predicting survival in the Titanic ship
  */
object TitanicChallenge {

  def main(args: Array[String]) {

    val spark = SparkSession.builder.
      master("local[*]")
      .appName("example")
      .config("spark.sql.shuffle.partitions", 8)
      .getOrCreate()
    val sc = spark.sparkContext

    spark.sparkContext.setLogLevel("ERROR")

    val schemaArray = StructType(Array(
      StructField("PassengerId", IntegerType, true),
      StructField("Survived", IntegerType, true),
      StructField("Pclass", IntegerType, true),
      StructField("Name", StringType, true),
      StructField("Sex", StringType, true),
      StructField("Age", FloatType, true),
      StructField("SibSp", IntegerType, true),
      StructField("Parch", IntegerType, true),
      StructField("Ticket", StringType, true),
      StructField("Fare", FloatType, true),
      StructField("Cabin", StringType, true),
      StructField("Embarked", StringType, true)
    ))

    val path = "Titanic/"
    val df = spark.read
      .option("header", "true")
      .schema(schemaArray)
      .csv(path + "train.csv")
      .drop("PassengerId")
//    df.cache()

    val utils = new TitanicChallenge(spark)
    val df2 = utils.transCabin(df)
    val df3 = utils.transTicket(sc, df2)
    val df4 = utils.transEmbarked(df3)
    val df5 = utils.extractTitle(sc, df4)
    val df6 = utils.transAge(sc, df5)
    val df7 = utils.categorizeAge(df6)
    val df8 = utils.createFellow(df7)
    val df9 = utils.categorizeFellow(df8)
    val df10 = utils.extractFName(df9)
    val df11 = utils.transFare(df10)

    val prePipelineDF = df11.select("Survived", "Pclass", "Sex",
      "Age_categorized", "fellow_type", "Fare_categorized",
      "Embarked", "Cabin", "Ticket",
      "Title", "family_type")

//    prePipelineDF.show(1)
//    +--------+------+----+---------------+-----------+----------------+--------+-----+------+-----+-----------+
//    |Survived|Pclass| Sex|Age_categorized|fellow_type|Fare_categorized|Embarked|Cabin|Ticket|Title|family_type|
//    +--------+------+----+---------------+-----------+----------------+--------+-----+------+-----+-----------+
//    |       0|     3|male|            3.0|      Small|             0.0|       S|    U|     0|   Mr|          0|
//    +--------+------+----+---------------+-----------+----------------+--------+-----+------+-----+-----------+

    val (df_indexed, colsTrain) = utils.index_onehot(prePipelineDF)
    df_indexed.cache()

    //训练模型
    val validatorModel = utils.trainData(df_indexed, colsTrain)

    //打印最优模型的参数
    val bestModel = validatorModel.bestModel
    println(bestModel.asInstanceOf[PipelineModel].stages.last.extractParamMap)

    //打印各模型的成绩和参数
    val paramsAndMetrics = validatorModel.validationMetrics
      .zip(validatorModel.getEstimatorParamMaps)
      .sortBy(-_._1)
    paramsAndMetrics.foreach { case (metric, params) =>
      println(metric)
      println(params)
      println()
    }

    validatorModel.write.overwrite().save(path + "Titanic_gbtc")

    spark.stop()
  }
}

class TitanicChallenge(private val spark: SparkSession) extends Serializable {

  import spark.implicits._

  //Cabin，用“U”填充null，并提取Cabin的首字母
  def transCabin(df: Dataset[Row]): Dataset[Row] = {
    df.na.fill("U", Seq("Cabin"))
      .withColumn("Cabin", substring($"Cabin", 0, 1))
  }

  //
  def transTicket(sc: SparkContext, df: Dataset[Row]): Dataset[Row] = {

    ////提取船票的号码，如“A/5 21171”中的21171
    val medDF1 = df.withColumn("Ticket", split($"Ticket", " "))
      .withColumn("Ticket", $"Ticket"(size($"Ticket").minus(1)))
      .filter($"Ticket" =!= "LINE")//去掉某种特殊的船票

    //对船票号进行分类，小于四位号码的为“1”，四位号码的以第一个数字开头，后面接上“0”，大于4位号码的，取前三个数字开头。如21171变为211
    val ticketTransUdf = udf((ticket: String) => {
      if (ticket.length < 4) {
        "1"
      } else if (ticket.length == 4){
        ticket(0)+"0"
      } else {
        ticket.slice(0, 3)
      }
    })
    val medDF2 = medDF1.withColumn("Ticket", ticketTransUdf($"Ticket"))

    //将数量小于等于5的类别统一归为“0”。先统计小于5的名单，然后用udf进行转换。
    val filterList = medDF2.groupBy($"Ticket").count()
      .filter($"count" <= 5)
      .map(row => row.getString(0))
      .collect.toList

    val filterList_bc = sc.broadcast(filterList)

    val ticketTransAdjustUdf = udf((subticket: String) => {
      if (filterList_bc.value.contains(subticket)) "0"
      else subticket
    })

    medDF2.withColumn("Ticket", ticketTransAdjustUdf($"Ticket"))
  }

  //用“S”填充null
  def transEmbarked(df: Dataset[Row]): Dataset[Row] = {
    df.na.fill("S", Seq("Embarked"))
  }

  def extractTitle(sc: SparkContext, df: Dataset[Row]): Dataset[Row] = {
    val regex = ".*, (.*?)\\..*"

    //对头衔进行归类
    val titlesMap = Map(
      "Capt"-> "Officer",
      "Col"-> "Officer",
      "Major"-> "Officer",
      "Jonkheer"-> "Royalty",
      "Don"-> "Royalty",
      "Sir" -> "Royalty",
      "Dr"-> "Officer",
      "Rev"-> "Officer",
      "the Countess"->"Royalty",
      "Mme"-> "Mrs",
      "Mlle"-> "Miss",
      "Ms"-> "Mrs",
      "Mr" -> "Mr",
      "Mrs" -> "Mrs",
      "Miss" -> "Miss",
      "Master" -> "Master",
      "Lady" -> "Royalty"
    )

    val titlesMap_bc = sc.broadcast(titlesMap)

    df.withColumn("Title", regexp_extract(($"Name"), regex, 1))
      .na.replace("Title", titlesMap_bc.value)
  }

  //根据null age的records对应的Pclass和Name_final分组后的平均来填充缺失age。
  // 首先，生成分组key，并获取分组后的平均年龄map。然后广播map,当Age为null时，用udf返回需要填充的值。
  def transAge(sc: SparkContext, df: Dataset[Row]): Dataset[Row] = {
    val medDF = df.withColumn("Pclass_Title_key", concat($"Title", $"Pclass"))
    val meanAgeMap = medDF.groupBy("Pclass_Title_key")
      .mean("Age")
      .map(row => (row.getString(0), row.getDouble(1)))
      .collect().toMap

    val meanAgeMap_bc = sc.broadcast(meanAgeMap)

    val fillAgeUdf = udf((comb_key: String) => meanAgeMap_bc.value.getOrElse(comb_key, 0.0))

    medDF.withColumn("Age", when($"Age".isNull, fillAgeUdf($"Pclass_Title_key")).otherwise($"Age"))
  }

  //对Age进行分类
  def categorizeAge(df: Dataset[Row]): Dataset[Row] = {
    val ageBucketBorders = 0.0 +: (10.0 to 60.0 by 5.0).toArray :+ 150.0
    val ageBucketer = new Bucketizer().setSplits(ageBucketBorders).setInputCol("Age").setOutputCol("Age_categorized")
    ageBucketer.transform(df).drop("Pclass_Title_key")
  }

  //将SibSp和Parch相加，得出同行人数
  def createFellow(df: Dataset[Row]): Dataset[Row] = {
    df.withColumn("fellow", $"SibSp" + $"Parch")
  }

  //fellow_type, 对fellow进行分类。此处其实可以留到pipeline部分一次性完成。
  def categorizeFellow(df: Dataset[Row]): Dataset[Row] = {
    df.withColumn("fellow_type", when($"fellow" === 0, "Alone")
      .when($"fellow" <= 3, "Small")
      .otherwise("Large"))
  }

  def extractFName(df: Dataset[Row]): Dataset[Row] = {

    //检查df是否有Survived和fellow列
    if (!df.columns.contains("Survived") || !df.columns.contains("fellow")){
      throw new IllegalArgumentException(
        """
          |Check if the argument is a training set or if this training set contains column named \"fellow\"
        """.stripMargin)
    }

    //FName，提取家庭名称。例如："Johnston, Miss. Catherine Helen ""Carrie""" 提取出Johnston
    // 由于spark的读取csv时，如果有引号，读取就会出现多余的引号，所以除了split逗号，还要再split一次引号。
    val medDF = df
      .withColumn("FArray", split($"Name", ","))
      .withColumn("FName", expr("FArray[0]"))
      .withColumn("FArray", split($"FName", "\""))
      .withColumn("FName", $"FArray"(size($"FArray").minus(1)))

    //family_type，分为三类，第一类是60岁以下女性遇难的家庭，第二类是18岁以上男性存活的家庭，第三类其他。
    val femaleDiedFamily_filter = $"Sex" === "female" and $"Age" < 60 and $"Survived" === 0 and $"fellow" > 0

    val maleSurvivedFamily_filter = $"Sex" === "male" and $"Age" >= 18 and $"Survived" === 1 and $"fellow" > 1

    val resDF = medDF.withColumn("family_type", when(femaleDiedFamily_filter, 1)
      .when(maleSurvivedFamily_filter, 2).otherwise(0))

    //familyTable，家庭分类名单，用于后续test集的转化。此处用${FName}_${family_type}的形式保存。
    resDF.filter($"family_type".isin(1,2))
      .select(concat($"FName", lit("_"), $"family_type"))
      .dropDuplicates()
      .write.format("text").mode("overwrite").save("familyTable")

    //如果需要直接收集成Map的话，可用下面代码。
    // 此代码先利用mapPartitions对各分块的数据进行聚合，降低直接调用count而使driver挂掉的风险。
    //另外新建一个默认Set是为了防止某个partition并没有数据的情况（出现概率可能比较少），
    // 从而使得Set的类型变为Set[_>:Tuple]而不能直接flatten

    // val familyMap = df10
    //   .filter($"family_type" === 1 || $"family_type" === 2)
    //   .select("FName", "family_type")
    //   .rdd
    //   .mapPartitions{iter => {
    //       if (!iter.isEmpty) {
    //       Iterator(iter.map(row => (row.getString(0), row.getInt(1))).toSet)}
    //       else Iterator(Set(("defualt", 9)))}
    //                 }
    //   .collect()
    //   .flatten
    //   .toMap

    resDF
  }

  //Fare。首先去掉缺失的（test集合中有一个，如果量多的话，也可以像Age那样通过头衔，年龄等因数来推断）
  //然后对Fare进行分类
  def transFare(df: Dataset[Row]): Dataset[Row] = {

    val medDF = df.na.drop("any", Seq("Fare"))
    val fareBucketer = new QuantileDiscretizer()
      .setInputCol("Fare")
      .setOutputCol("Fare_categorized")
      .setNumBuckets(4)

    fareBucketer.fit(medDF).transform(medDF)
  }

  def index_onehot(df: Dataset[Row]): Tuple2[Dataset[Row], Array[String]] = {
    val stringCols = Array("Sex","fellow_type", "Embarked", "Cabin", "Ticket", "Title")
    val subOneHotCols = stringCols.map(cname => s"${cname}_index")
    val index_transformers: Array[org.apache.spark.ml.PipelineStage] = stringCols.map(
      cname => new StringIndexer()
        .setInputCol(cname)
        .setOutputCol(s"${cname}_index")
        .setHandleInvalid("skip")
    )


    val oneHotCols = subOneHotCols ++ Array("Pclass", "Age_categorized", "Fare_categorized", "family_type")
    val vectorCols = oneHotCols.map(cname => s"${cname}_encoded")
    val encode_transformers: Array[org.apache.spark.ml.PipelineStage] = oneHotCols.map(
      cname => new OneHotEncoder()
        .setInputCol(cname)
        .setOutputCol(s"${cname}_encoded")
    )

    val pipelineStage = index_transformers ++ encode_transformers
    val index_onehot_pipeline = new Pipeline().setStages(pipelineStage)
    val index_onehot_pipelineModel = index_onehot_pipeline.fit(df)

    val resDF = index_onehot_pipelineModel.transform(df).drop(stringCols:_*).drop(subOneHotCols:_*)
    println(resDF.columns.size)
    (resDF, vectorCols)
  }

  def trainData(df: Dataset[Row], vectorCols: Array[String]): TrainValidationSplitModel = {
    //separate and model pipeline，包含划分label和features，机器学习模型的pipeline
    val vectorAssembler = new VectorAssembler()
      .setInputCols(vectorCols)
      .setOutputCol("features")

    val gbtc = new GBTClassifier()
      .setLabelCol("Survived")
      .setFeaturesCol("features")
      .setPredictionCol("prediction")

    val pipeline = new Pipeline().setStages(Array(vectorAssembler, gbtc))

    val paramGrid = new ParamGridBuilder()
      .addGrid(gbtc.stepSize, Seq(0.1))
      .addGrid(gbtc.maxDepth, Seq(5))
      .addGrid(gbtc.maxIter, Seq(20))
      .build()

    val multiclassEval = new MulticlassClassificationEvaluator()
      .setLabelCol("Survived")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")

    val tvs = new TrainValidationSplit()
      .setTrainRatio(0.75)
      .setEstimatorParamMaps(paramGrid)
      .setEstimator(pipeline)
      .setEvaluator(multiclassEval)

    tvs.fit(df)
  }
}