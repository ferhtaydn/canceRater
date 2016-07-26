package com.ferhtaydn.server

import akka.event.NoLogging
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import com.ferhtaydn.models.{ PatientInfo, PatientResult }
import com.ferhtaydn.rater.RatePredictor
import org.apache.spark.sql.SQLContext
import org.apache.spark.{ SparkConf, SparkContext }
import org.scalamock.scalatest.MockFactory
import org.scalatest._

import scala.concurrent.Future

class ServerSpec extends FlatSpec with Matchers with ScalatestRouteTest with MockFactory with Server {

  override def testConfigSource = "akka.loglevel = WARNING"

  override val logger = NoLogging

  private var sc: SparkContext = _
  var predictor: RatePredictor = _

  override def beforeAll(): Unit = {
    val conf = new SparkConf().setAppName("ServerSpec").setMaster("local")
    sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    class MockableRatePredictor extends RatePredictor(system, sqlContext, null, null, "")
    predictor = mock[MockableRatePredictor]
  }

  override def afterAll(): Unit = {
    system.terminate()
    sc.stop()
  }

  "Server" should "respond OK to index" in {
    Get(s"/cancerater/") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `text/plain(UTF-8)`
      responseAs[String] shouldBe "Welcome to CanceRater"
    }
  }

  "Server" should "respond OK to confusion matrix" in {

    (predictor.confusionMatrixString _).expects().returns(Future.successful("ArrayOfDouble"))

    Get(s"/cancerater/cm") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `text/plain(UTF-8)`
      responseAs[String] shouldBe "ArrayOfDouble"
    }
  }

  "Server" should "respond OK to check request" in {

    val patientInfo = PatientInfo("Male", 30, 100, 170, "Engineer")
    (predictor.predict _).expects(patientInfo).returns(Future(Right(0.13)))

    Post(s"/cancerater/check", patientInfo) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[PatientResult] shouldBe PatientResult(0.13)
    }
  }

  "Server" should "respond with BadRequest to check request with unknown job" in {

    val patientInfo = PatientInfo("Male", 30, 100, 170, "BLABLA")
    (predictor.predict _).expects(patientInfo).returns(Future(Left("WrongJob")))

    Post(s"/cancerater/check", patientInfo) ~> routes ~> check {
      status shouldBe BadRequest
      contentType shouldBe `text/plain(UTF-8)`
      responseAs[String] shouldBe "WrongJob"
    }
  }

}