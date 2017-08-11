package sample

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

// Model
case class Todo(id: Int, text: String)
case class AddTodo(text: String)
case class Message(message: String)

object TodoServer extends App with JsonSupport {
  implicit val system = ActorSystem("HttpServer")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  // State
  var todosById = Map.empty[Int, Todo]
  var todos = Set.empty[Todo]

  // Server
  val route =
    pathPrefix("todos") {
      pathEnd {
        post {
          addTodo()
        } ~
        get {
          listTodos()
        }
      } ~
      path(IntNumber) { id =>
        get {
          listTodoById(id)
        }
      } ~
      path(Segment) { _ =>
        complete(StatusCodes.BadRequest, Message("Invalid id (must be numeric)"))
      }
    }

  private def listTodos(): Route = {
    complete(todos)
  }

  private def listTodoById(id: Int): Route = {
    rejectEmptyResponse(complete(todosById.get(id)))
  }

  private def addTodo(): Route = {
    entity(as[AddTodo]) { toAdd =>
      val todo = Todo(todos.size + 1, toAdd.text)
      todosById += todo.id -> todo
      todos += todo
      complete(StatusCodes.Created, Message(s"Todo ${toAdd.text} added successfully"))
    }
  }

  Http().bindAndHandle(route, "localhost", 8080)
  println("Server running at http://localhost:8080/")
}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val todoFormat: RootJsonFormat[Todo] = jsonFormat2(Todo)
  implicit val addTodoFormat: RootJsonFormat[AddTodo] = jsonFormat1(AddTodo)
  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat1(Message)
}
