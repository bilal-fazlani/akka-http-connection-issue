package com.bilal

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.server.HttpApp

object Proxy extends HttpApp with App {

  override val routes = path(Segment) { serviceName =>
    LocationServiceMock.resolve(serviceName) match {
      case Some(target) => forwardTo(target)
      case None         => complete(HttpResponse(StatusCodes.NotFound))
    }
  }

  def forwardTo(uri: Uri) =
    extractRequest(req => complete(Http().singleRequest(req.copy(uri = uri))))

  this.startServer("0.0.0.0", 5000)
}
