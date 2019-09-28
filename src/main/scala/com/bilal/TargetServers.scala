package com.bilal

import akka.Done
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object TargetServers extends App {

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

  private def makeRoute(name: String) = {
    complete({
      inc()
      Done
    })
  }

  bindingF.onComplete {
    case Failure(exception) =>
      exception.printStackTrace()
    case Success(s) =>
      println()
      println(s"*** ${s.length} target servers started ***")
  }
}
