package com.bilal

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.StandardRoute
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import scala.concurrent.duration.DurationLong
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

  private def makeRoute(name: String) = complete(
    Source
    .tick(0.seconds, 2.seconds, NotUsed)
    .map(_ => name)
    .map(ServerSentEvent(_))
    .takeWithin(10.seconds)
  )

  bindingF.onComplete {
    case Failure(exception) => exception.printStackTrace()
    case Success(value)     => println("*** all target servers started ***")
  }
}
