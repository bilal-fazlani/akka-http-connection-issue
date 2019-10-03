package com.example

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps
trait StartStopMetrics {
  private sealed trait Msg
  private case object IncStart extends Msg
  private case object IncEnd extends Msg
  private case class CountingState(started:Int, ended:Int)

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

  private val startEndCounter: ActorRef[Msg] = actorSystem.spawn(countingStartEnd(CountingState(0,0)), "startEndCounter")

  def incStart(): Unit = startEndCounter ! IncStart
  def incEnd(): Unit = startEndCounter ! IncEnd
}
