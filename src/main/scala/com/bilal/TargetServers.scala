package com.bilal

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.scaladsl.Source

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationLong
import scala.util.{Failure, Success}

object TargetServers extends App {

  val ref = actorSystem.spawn(beh(0), "counter")

  println()

  val bindingFutures = LocationServiceMock.list
    .map(location => {
      Http()
        .bindAndHandle(
          makeRoute(location.name),
          "0.0.0.0",
          location.uri.effectivePort
        )
        .map(_ => ())
        .recover {
          case _ =>
            println(s"${location.uri.effectivePort} is already in use")
        }
    })
  val bindingF = Future.sequence(bindingFutures)

  def beh(count: Int): Behavior[Msg] = Behaviors.receiveMessage[Msg] {
    case Inc =>
      val result = count + 1
      print(s"\rrequest count: $result")
      beh(result)
    case GetCount(replyTo) =>
      replyTo ! count
      Behaviors.same
  }

  private def makeRoute(name: String) = {
    complete(
      {
        ref ! Inc
        Source
        .tick(0.seconds, 5.seconds, NotUsed)
        .map(_ => name)
        .map(ServerSentEvent(_))
        .takeWithin(5.seconds)
      }
    )
  }

  trait Msg

  case class GetCount(replyTo: ActorRef[Int]) extends Msg

  case object Inc extends Msg

  bindingF.onComplete {
    case Failure(exception) => exception.printStackTrace()
    case Success(_)         =>
      println()
      println("*** all target servers started ***")
  }
}
