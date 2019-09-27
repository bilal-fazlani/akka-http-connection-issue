package com

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.server.directives.RouteDirectives

package object bilal extends RouteDirectives {
  implicit val actorSystem: ActorSystem = ActorSystem("default")
}
