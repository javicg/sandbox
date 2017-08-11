package streams

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import streams.Domain.Event

import scala.util.Random

object StreamHttpServer extends App with JsonSupport {
  implicit val system = ActorSystem("HttpServer")
  implicit val materializer = ActorMaterializer()

  var events = Seq.empty[Event]
  val numbers = Source.fromIterator(() => Iterator.continually(Random.nextInt()))

  val route =
    path("events") {
      get {
        complete(events)
      } ~
      post {
        entity(as[Event]) { event =>
          events = events :+ event
          complete(StatusCodes.Created)
        }
      }
    } ~
    path ("random") {
      get {
        complete(
          HttpEntity(
            ContentTypes.`text/plain(UTF-8)`,
            numbers.map(n => {
              println("Another number. Yay!")
              ByteString(s"$n\n")
            })
          ))
      }
    }

  Http().bindAndHandle(route, "localhost", 8080)
  println("Server running at http://localhost:8080/")
}
