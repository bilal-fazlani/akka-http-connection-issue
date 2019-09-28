package com

import akka.actor.ActorSystem
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.stream.ActorMaterializer

package object bilal extends RouteDirectives {
  implicit val actorSystem: ActorSystem = ActorSystem("default")
  implicit val mat = ActorMaterializer()
}
