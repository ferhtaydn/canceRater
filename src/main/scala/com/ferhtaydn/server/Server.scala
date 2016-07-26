package com.ferhtaydn.server

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.ferhtaydn.models.{ JsonFormats, PatientInfo, PatientResult }
import com.ferhtaydn.rater.RatePredictor
import akka.http.scaladsl.model.StatusCodes._

import scala.concurrent.ExecutionContextExecutor

trait Server extends JsonFormats {

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  val logger: LoggingAdapter

  def predictor: RatePredictor

  protected val routes: Route =
    pathPrefix("cancerater") {
      get {
        pathSingleSlash {
          complete("Welcome to CanceRater")
        }
      } ~
        get {
          path("cm") {
            complete {
              predictor.confusionMatrixString.map[ToResponseMarshallable] {
                case cm ⇒ cm
              }
            }
          }
        } ~
        post {
          path("check") {
            entity(as[PatientInfo]) { patientInfo ⇒
              complete {
                predictor.predict(patientInfo).map[ToResponseMarshallable] {
                  case Right(score) ⇒ PatientResult(score)
                  case Left(error)  ⇒ BadRequest → error
                }
              }
            }
          }
        }
    }
}
