package com.bilal

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.scaladsl.Source

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationLong
import scala.util.{Failure, Success}

object TargetServers extends App {

  val bindingFutures = LocationServiceMock.list
      .take(1)
    .map(location => {
      Http()
        .bindAndHandle(
          makeRoute(location.name),
          "0.0.0.0",
          location.uri.effectivePort
        )
        .map(_.localAddress.getPort)
        .recover {
          case _ =>
            println(s"${location.uri.effectivePort} is already in use")
            0
        }
    })
  val bindingF = Future.sequence(bindingFutures)

  private def makeRoute(name: String) = {
    complete({
      inc()
      Source
        .tick(0.seconds, 5.seconds, NotUsed)
        .map(_ => name)
        .map(ServerSentEvent(_))
        .takeWithin(5.seconds)
    })
  }

  bindingF.onComplete {
    case Failure(exception) =>
      exception.printStackTrace()
    case Success(s) =>
      println()
      println(s"*** ${s.length} target servers started ***")
      println(s"$s ")
  }
}
