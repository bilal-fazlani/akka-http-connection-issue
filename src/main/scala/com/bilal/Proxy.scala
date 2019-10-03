package com.bilal

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{OverflowStrategy, QueueOfferResult}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Random, Success}
object Proxy extends HttpApp with App {

  println()

  val QueueSize = 10000

  def poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpResponse]](
    "localhost",
    8001
  )

  def makeQ = Source.queue[(HttpRequest, Promise[HttpResponse])](QueueSize, OverflowStrategy.dropNew)
    .via(poolClientFlow)
    .to(Sink.foreach({
      case ((Success(resp), p)) => p.success(resp)
      case ((Failure(e), p))    => p.failure(e)
    }))
    .run()

  val qs = (1 to 10).map(_=> makeQ)

  def queueRequest(request: HttpRequest): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    val index = Random.nextInt(9)
     qs(index).offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued    =>
        responsePromise.future
      case QueueOfferResult.Dropped     =>
        val msg = "Queue overflowed. Try again later"
        println(msg)
        Future.failed(new RuntimeException(msg))
      case QueueOfferResult.Failure(ex) =>
        ex.printStackTrace()
        Future.failed(ex)
      case QueueOfferResult.QueueClosed =>
        val msg = "Queue was closed (pool shut down) while running the request. Try again later."
        println(msg)
        Future.failed(new RuntimeException(msg))
    }
  }

  override val routes = path(Segment) { _ =>
    incStart()
    forwardTo(Uri("http://localhost:8001"))
  }

  def forwardTo(uri: Uri) =
    complete(queueRequest(HttpRequest(uri = uri)).map(x=> {
      incEnd()
      x
    }))
  //extractRequest(req => complete(Http().singleRequest(req.copy(uri = uri))))

  this.startServer("0.0.0.0", 5000)
}
