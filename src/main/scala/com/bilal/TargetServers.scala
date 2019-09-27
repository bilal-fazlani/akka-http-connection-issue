package com.bilal

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationLong
import scala.util.{Failure, Success}

object TargetServers extends App {
  implicit val mat: ActorMaterializer = ActorMaterializer()

  val bindingFutures = LocationServiceMock.list
    .map(location => {
      Http()
        .bindAndHandle(
          makeRoute(location.name),
          "0.0.0.0",
          location.uri.effectivePort
        )
        .map(_ => ())
        .recover {
          case _ =>
            println(s"${location.uri.effectivePort} is already in use")
        }
    })

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
    case Success(_)     => println("*** all target servers started ***")
  }
}
