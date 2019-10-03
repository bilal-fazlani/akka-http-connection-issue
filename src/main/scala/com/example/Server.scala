package com.example

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.stream.scaladsl.Source
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import scala.concurrent.Await
import scala.concurrent.duration.DurationLong

object Server extends App with RouteDirectives with SingleMetrics {

  val route = complete({
    inc() // record request hit count
    Source
      .tick(0.seconds, 5.seconds, NotUsed) // a tick every 5 seconds
      .take(2) // take only 2 ticks (stream will run for 10 seconds)
      .map(_ => ServerSentEvent("tick")) // convert to SSE stream
  })

  //start server
  Await.result(Http().bindAndHandle(route, "0.0.0.0", serverPort), 5.seconds)
  println("server started")
}
