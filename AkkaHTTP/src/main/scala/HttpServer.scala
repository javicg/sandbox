import Domain.Event
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

object HttpServer extends App with JsonSupport {
  implicit val system = ActorSystem("HttpServer")
  implicit val materializer = ActorMaterializer()

  val route =
    path("events") {
      get {
        complete(Seq(
          Event(123, "ApplicationStarted"),
          Event(124, "ServerStarted")
        ))
      }
    }

  Http().bindAndHandle(route, "localhost", 8080)
  println("Server running at http://localhost:8080/")
}
