package com.bilal

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.server.HttpApp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Proxy extends HttpApp with App {

  override val routes = extractRequest { req =>
    path(Segment) { serviceName =>

      val response = LocationServiceMock
        .resolve(serviceName)
        .flatMap {
          case Some(location) =>
            val newReq = req.copy(uri = Uri(s"http://${location.host}:${location.port}"))
            Http().singleRequest(newReq)
          case None =>
            Future.successful(HttpResponse(StatusCodes.NotFound))
        }
      complete(
        response
      )
    }
  }

  this.startServer("0.0.0.0", 9000)
}
