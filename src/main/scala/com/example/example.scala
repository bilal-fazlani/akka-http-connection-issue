package com

import akka.actor.ActorSystem
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.stream.ActorMaterializer

package object example extends RouteDirectives {
  implicit val actorSystem: ActorSystem = ActorSystem("my_actor_system")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  val serverPort = 9005
}
