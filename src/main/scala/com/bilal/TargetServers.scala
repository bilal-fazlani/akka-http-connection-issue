package com.bilal

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.StandardRoute
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object TargetServers extends App {
  implicit val mat: ActorMaterializer = ActorMaterializer()

  val bindingFutures = LocationServiceMock.list
    .map(
      location =>
        Http().bindAndHandle(makeRoute(location.name), "0.0.0.0", location.port)
    )

  val bindingF = Future.sequence(bindingFutures)

  def makeRoute(name: String): StandardRoute = complete(s"OK from $name")

  bindingF.onComplete {
    case Failure(exception) => exception.printStackTrace()
    case Success(value)     => println("*** all target servers started ***")
  }
}
