package com

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.server.directives.RouteDirectives
import com.bilal.LocationServiceMock.Location

import scala.concurrent.Future

package object bilal extends RouteDirectives {
  implicit val actorSystem: ActorSystem = ActorSystem("default")

  implicit class RichRequest(request: HttpRequest){
    def forwardTo(location: Location): Future[HttpResponse] = {
      val newRequest = request.copy(uri = Uri(s"http://${location.host}:${location.port}"))
      Http().singleRequest(newRequest)
    }
  }
}
