import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

object HttpServer extends App {
  implicit val system = ActorSystem("HttpServer")
  implicit val materializer = ActorMaterializer()

  val route =
    path("hello") {
      get {
        complete("Hi there!")
      }
    }

  Http().bindAndHandle(route, "localhost", 8080)
  println("Server running at http://localhost:8080/")
}
