package com.bilal

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.HttpApp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Proxy extends HttpApp with App {

  override val routes = extractRequest { request =>
    path(Segment) { serviceName =>
      val response = LocationServiceMock
        .resolve(serviceName)
        .flatMap {
          case Some(target) => request.forwardTo(target)
          case None => Future.successful(HttpResponse(StatusCodes.NotFound))
        }
      complete(response)
    }
  }

  this.startServer("0.0.0.0", 9000)
}
