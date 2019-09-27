package com.bilal

import scala.concurrent.Future

object LocationServiceMock {

  case class Location(name: String, host:String, port:Int)

  private lazy val data: Map[String, Location] = (1 to 100)
    .map(i => s"service$i" -> Location(s"service$i", "localhost", 8080 + i))
    .toMap

  def resolve(serviceName:String):Future[Option[Location]] = {
    Future.successful(data.get(serviceName))
  }

  def list: Seq[Location] = data.values.toSeq
}
