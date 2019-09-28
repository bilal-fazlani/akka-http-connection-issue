package com.bilal

import akka.http.scaladsl.model.Uri

object LocationServiceMock {

  private lazy val data: Map[String, Location] = (1 to 200)
    .map(
      i =>
        s"service$i" -> Location(
          s"service$i",
          Uri(s"http://localhost:${8000 + i}")
      )
    )
    .toMap

  def resolve(serviceName: String): Option[Uri] = data.get(serviceName).map(_.uri)

  def list: Seq[Location] = data.values.toSeq

  case class Location(name: String, uri: Uri)
}
