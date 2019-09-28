package com.bilal

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Clients extends App {

  val startTime = System.nanoTime()
  val futures = LocationServiceMock.list
    .take(50)
    .map(_.name)
    .map(name => {
      val streamStartTime = System.nanoTime()
      Http()
        .singleRequest(HttpRequest(uri = Uri(s"http://localhost:5000/$name")))
        .flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
        .flatMap(x => x.runWith(Sink.ignore))
        .map(d => {
          val streamEndTime = System.nanoTime()
          println(s"$name -> ${(streamEndTime - streamStartTime) /1000000000L }s")
          d
        })
    })

  val f = Future.sequence(futures)

  f.onComplete {
    case Failure(exception) =>
      exception.printStackTrace()
    case Success(value) =>
      val endTime = System.nanoTime()
      println("done")
      println(s"total time: ${(endTime - startTime) / 1000000000L}s")
  }
}
