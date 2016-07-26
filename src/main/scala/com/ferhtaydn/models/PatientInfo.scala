package com.ferhtaydn.models

case class PatientInfo(gender: String, age: Int, weight: Int, height: Int, job: String) {
  def toRecord = PatientRecord(Gender.id(gender).toDouble, age.toDouble, weight.toDouble, height.toDouble, job)
}

case class PatientRecord(gender: Double, age: Double, weight: Double, height: Double, job: String)

case class PatientResult(score: Double)