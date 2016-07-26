package com.ferhtaydn.rater

import akka.actor.ActorSystem
import com.ferhtaydn.models.PatientInfo
import org.apache.spark.ml.feature.StringIndexerModel
import org.apache.spark.ml.tuning.CrossValidatorModel
import org.apache.spark.mllib.linalg.{ Matrix, Vector }
import org.apache.spark.sql.{ Row, SQLContext }

import scala.concurrent.{ ExecutionContextExecutor, Future }

class RatePredictor(system: ActorSystem, sqlContext: SQLContext,
    indexModel: StringIndexerModel, cvModel: CrossValidatorModel,
    confusionMatrix: String) {

  private val decimalFormatter = new java.text.DecimalFormat("##.##")
  private val blockingDispatcher: ExecutionContextExecutor = system.dispatchers.lookup("ml.predictor.dispatcher")

  def confusionMatrixString: Future[String] = {
    Future {
      confusionMatrix
    }(blockingDispatcher)
  }

  def predict(patientInfo: PatientInfo): Future[Either[String, Double]] = {

    Future {

      val df = sqlContext.createDataFrame(Seq(patientInfo.toRecord))
      val indexedJobDF = indexModel.transform(df)

      val result = cvModel
        .transform(indexedJobDF)
        .select("prediction", "probability").map {
          case Row(prediction: Double, probability: Vector) ⇒
            (probability, prediction)
        }

      result.collect().headOption match {
        case Some((prob, _)) ⇒ Right(decimalFormatter.format(prob(1)).toDouble)
        case None            ⇒ Left(s"No result can be predicted for the patient")
      }

    }(blockingDispatcher)
  }

}
