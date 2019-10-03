package com.bilal

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Clients extends App {

  val startTime = System.nanoTime()

  val futures =
    (1 to 5000)
    .map(name => {
      incStart()
      Http()
        .singleRequest(HttpRequest(uri = Uri(s"http://localhost:5000/$name")))
        .flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
        .flatMap(x => x.runWith(Sink.ignore))
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
