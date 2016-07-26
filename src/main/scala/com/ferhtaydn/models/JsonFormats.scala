package com.ferhtaydn.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val patientInfoFormat = jsonFormat5(PatientInfo)
  implicit val patientResultFormat = jsonFormat1(PatientResult)
}