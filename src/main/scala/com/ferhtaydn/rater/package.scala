package com.ferhtaydn

import org.apache.spark.ml.feature.{ StandardScaler, StringIndexer }
import org.apache.spark.mllib.evaluation.{ BinaryClassificationMetrics, MulticlassMetrics }
import org.apache.spark.mllib.linalg.Matrix
import org.apache.spark.rdd.RDD

package object rater {

  def stringIndexer(column: String): StringIndexer = {
    new StringIndexer()
      .setInputCol(column)
      .setOutputCol(s"indexed${column.capitalize}")
      .setHandleInvalid("skip")
  }

  def standardScaler(column: String): StandardScaler = {
    new StandardScaler()
      .setInputCol(column)
      .setOutputCol(s"scaled${column.capitalize}")
      .setWithStd(true)
      .setWithMean(true)
  }

  def confusionMatrix(predictionAndLabels: RDD[(Double, Double)]): Matrix = {
    val metrics = new MulticlassMetrics(predictionAndLabels)
    metrics.confusionMatrix
    println(s"ConfusionMatrix:\n ${metrics.confusionMatrix.toString()}")
    println(s"Precision: ${metrics.precision}")
    println(s"weighted Precision: ${metrics.weightedPrecision}")
    println(s"Recall: ${metrics.recall}")
    println(s"weighted Recall: ${metrics.weightedRecall}")
    println(s"FMeasure: ${metrics.fMeasure}")
    println(s"weighted FMeasure: ${metrics.weightedFMeasure}")
    metrics.confusionMatrix
  }

  def printBinaryMetrics(predictionAndLabels: RDD[(Double, Double)]): Unit = {

    val metrics = new BinaryClassificationMetrics(predictionAndLabels)

    val precision = metrics.precisionByThreshold
    precision.foreach {
      case (t, p) ⇒
        println(s"Threshold: $t, Precision: $p")
    }

    val recall = metrics.recallByThreshold
    recall.foreach {
      case (t, r) ⇒
        println(s"Threshold: $t, Recall: $r")
    }

    val f1Score = metrics.fMeasureByThreshold
    f1Score.foreach {
      case (t, f) ⇒
        println(s"Threshold: $t, F-score: $f, Beta = 1")
    }

    val beta = 0.5
    val fScore = metrics.fMeasureByThreshold(beta)
    fScore.foreach {
      case (t, f) ⇒
        println(s"Threshold: $t, F-score: $f, Beta = 0.5")
    }

    val auPRC = metrics.areaUnderPR
    println("Area under precision-recall curve = " + auPRC)

    val auROC = metrics.areaUnderROC
    println("Area under ROC = " + auROC)
  }
}
