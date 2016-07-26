package com.ferhtaydn.models

import java.io.File

import scala.util.{ Failure, Success, Try }

case class CancerRecord(label: Double, gender: Double, age: Double, weight: Double, height: Double, job: String)

object CancerRecord {

  private val commaRegex: String = "\\,"

  def from(s: String): Option[CancerRecord] = s.split(commaRegex) match {
    case Array(result, gender, age, weight, height, job) ⇒
      Some(CancerRecord(result.toDouble, Gender.id(gender).toDouble, age.toDouble, weight.toDouble, height.toDouble, job))
    case _ ⇒ None
  }

  def read(file: String): Seq[CancerRecord] = Try(scala.io.Source.fromFile(file)) match {
    case Success(bufferedSource) ⇒
      for {
        a ← bufferedSource.getLines().toSeq.map(CancerRecord.from)
        b ← a
      } yield b
    case Failure(exception) ⇒ Seq.empty[CancerRecord]
  }

}
