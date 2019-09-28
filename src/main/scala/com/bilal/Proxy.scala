package com.bilal

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.server.HttpApp
import scala.concurrent.ExecutionContext.Implicits.global
object Proxy extends HttpApp with App {

  println()

  override val routes = path(Segment) { serviceName =>
    incStart()
    LocationServiceMock.resolve(serviceName) match {
      case Some(target) =>
        forwardTo(target)
      case None         =>
        complete(HttpResponse(StatusCodes.NotFound))
    }
  }

  def forwardTo(uri: Uri) =
    complete(Http().singleRequest(HttpRequest(uri = uri)).map(x=> {
      incEnd()
      x
    }))
  //extractRequest(req => complete(Http().singleRequest(req.copy(uri = uri))))

  this.startServer("0.0.0.0", 5000)
}
