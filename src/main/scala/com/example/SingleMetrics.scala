package com.example

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps

trait SingleMetrics {
  private case object Inc
  private def counting(count: Int): Behavior[Inc.type] = Behaviors.receiveMessage[Inc.type] {
    case Inc =>
      val result = count + 1
      print(s"\rrequests hit: $result")
      counting(result)
  }

  private val counter: ActorRef[Inc.type] = actorSystem.spawn(counting(0), "counter")
  def inc(): Unit = counter ! Inc
}
