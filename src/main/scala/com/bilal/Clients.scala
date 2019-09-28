package com.bilal

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Clients extends App {

  val startTime = System.nanoTime()
  val futures = LocationServiceMock.list
    .take(150)
    .map(_.name)
    .map(name => {
      incStart()
      Http()
        .singleRequest(HttpRequest(uri = Uri(s"http://localhost:5000/$name")))
        .map(x=>x.entity.discardBytes(mat))
        .map(d => {
          incEnd()
          d
        })
    })

  val f = Future.sequence(futures)

  f.onComplete {
    case Failure(exception) =>
      exception.printStackTrace()
    case Success(_) =>
      val endTime = System.nanoTime()
      Thread.sleep(100)
      println()
      println("done")
      println(s"total time: ${(endTime - startTime) / 1000000000L}s")
  }
}
