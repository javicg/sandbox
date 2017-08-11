package streams

import Domain.Event
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val eventFormat: RootJsonFormat[Event] = jsonFormat2(Event)
}
