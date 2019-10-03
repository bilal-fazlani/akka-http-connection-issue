package com.example

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.server.directives.RouteDirectives

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Client extends App with RouteDirectives with StartStopMetrics {
  println("client started")

  def makeRequests(number:Int): Future[Done.type] = {
    Future.sequence((1 to number).map{ _=>
      incStart() //request started
      Http()
        .singleRequest(HttpRequest(uri = Uri(s"http://localhost:$serverPort")))
        .map(x => x.entity.discardBytes(mat))
        .map(x =>{
          incEnd() //request ended
          x
        })
    }).map(_=> Done)
  }

  makeRequests(1000)
}
