package com.ferhtaydn.server

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.ferhtaydn.models.CancerRecord
import com.ferhtaydn.rater.{ LRCV, RatePredictor }
import com.ferhtaydn.settings.Settings
import org.apache.spark.sql.SQLContext
import org.apache.spark.{ SparkConf, SparkContext }

import scala.io.StdIn

object CanceRaterServer extends App with Server {

  implicit val system = ActorSystem("canceRater")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  private val settings = Settings(system)

  val logger = Logging(system, getClass)

  val conf = new SparkConf().setAppName("canceRater").setMaster("local")
  val sc = new SparkContext(conf)
  sc.setLogLevel("WARN")
  val sqlContext = new SQLContext(sc)

  private val cancerRecordRDD = sc.parallelize(CancerRecord.read(settings.corpus))
  private val cancerRecordDF = sqlContext.createDataFrame(cancerRecordRDD)
  private val (indexModel, cvModel, confusionMatrix) = new LRCV(sc).train(cancerRecordDF)

  val predictor: RatePredictor = new RatePredictor(system, sqlContext, indexModel, cvModel, confusionMatrix.toString)

  val bindingFuture = Http().bindAndHandle(routes, settings.interface, settings.port)

  println(s"Server online at ${settings.interface}:${settings.port}\nPress RETURN to stop...")

  StdIn.readLine()

  bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ { system.terminate(); sc.stop() })
}
