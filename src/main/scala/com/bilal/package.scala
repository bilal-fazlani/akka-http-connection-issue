package com

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.stream.ActorMaterializer

package object bilal extends RouteDirectives {
  implicit val actorSystem: ActorSystem = ActorSystem("my_actor_system")
  implicit val mat: ActorMaterializer = ActorMaterializer()

  case object Inc

  trait Msg
  case object IncStart extends Msg
  case object IncEnd extends Msg

  case class CountingState(started:Int, ended:Int)

  private def counting(count: Int): Behavior[Inc.type] = Behaviors.receiveMessage[Inc.type] {
    case Inc =>
      val result = count + 1
      print(s"\rrequests hit: $result")
      counting(result)
  }

  private def countingStartEnd(state: CountingState): Behavior[Msg] = Behaviors.receiveMessage[Msg] {
    case IncStart =>
      val result = state.copy(started = state.started + 1)
      print(s"\rprogress: ${result.ended} / ${result.started}")
      countingStartEnd(result)
    case IncEnd =>
      val result = state.copy(ended = state.ended + 1)
      print(s"\rprogress: ${result.ended} / ${result.started}")
      countingStartEnd(result)
  }

  private val counter: ActorRef[Inc.type] = actorSystem.spawn(counting(0), "counter")

  private val startEndCounter: ActorRef[Msg] = actorSystem.spawn(countingStartEnd(CountingState(0,0)), "startEndCounter")

  def incStart(): Unit = startEndCounter ! IncStart
  def incEnd(): Unit = startEndCounter ! IncEnd
  def inc(): Unit = counter ! Inc
}
