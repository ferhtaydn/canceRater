package com.ferhtaydn.models

object Gender extends Enumeration {

  type Gender = Value
  val Male, Female, Unknown = Value

  def id(s: String): Int = {
    if (s.equalsIgnoreCase("male")) Male.id
    else if (s.equalsIgnoreCase("female")) Female.id
    else Unknown.id
  }
}
