package com.ferhtaydn.rater

import org.apache.spark.SparkContext
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.{ StringIndexerModel, VectorAssembler }
import org.apache.spark.ml.tuning.{ CrossValidator, CrossValidatorModel, ParamGridBuilder }
import org.apache.spark.mllib.linalg.Matrix
import org.apache.spark.sql.{ DataFrame, Row, SQLContext }

class LRCV(sc: SparkContext) {

  implicit val sqlContext = new SQLContext(sc)

  val lr = new LogisticRegression().setMaxIter(10).setFeaturesCol("scaledFeatures")

  val paramGrid = new ParamGridBuilder()
    .addGrid(lr.regParam, Array(0.1, 0.01))
    .build()

  val assembler = new VectorAssembler()
    .setInputCols(Array("gender", "age", "weight", "height", "indexedJob"))
    .setOutputCol("features")

  val pipeline = new Pipeline()
    .setStages(Array(assembler, standardScaler("features"), lr))

  val cv = new CrossValidator()
    .setEstimator(pipeline)
    .setEvaluator(new BinaryClassificationEvaluator)
    .setEstimatorParamMaps(paramGrid)
    .setNumFolds(10)

  def train(df: DataFrame): (StringIndexerModel, CrossValidatorModel, Matrix) = {

    // need to index strings on all data to not missing the job fields.
    // other alternative can be manually assign values for each job like gender.
    val indexerModel = stringIndexer("job").fit(df)
    val indexed = indexerModel.transform(df)

    val splits = indexed.randomSplit(Array(0.8, 0.2))
    val training = splits(0).cache()
    val test = splits(1)

    val cvModel = cv.fit(training)

    val predictionAndLabels = cvModel
      .transform(test)
      .select("label", "prediction").map {
        case Row(label: Double, prediction: Double) ⇒
          (prediction, label)
      }

    printBinaryMetrics(predictionAndLabels)

    (indexerModel, cvModel, confusionMatrix(predictionAndLabels))

  }

}
