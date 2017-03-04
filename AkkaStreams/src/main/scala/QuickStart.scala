import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.concurrent._
import scala.concurrent.duration._

object QuickStart extends App {
  implicit val system = ActorSystem("SimpleSystem")
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 100)
  val factorials: Source[BigInt, NotUsed] = source.scan(BigInt(1))((acc, next) => acc * next)

  val f = factorials
    .zipWith(Source(0 to 100))((fact, idx) => s"$idx! = $fact")
      .throttle(1, 1.second, 1, ThrottleMode.Shaping)
    .runForeach(println)

  try {
    Await.ready(f, 10.seconds)
  } catch {
    case e: Exception => println(e.getMessage)
  }

  val words: Source[String, NotUsed] = Source(List("Hello", "Hello", "Hi", "Hiya", "Hi", "Hola"))
  val counts: Source[(String, Int), NotUsed] = words
    .groupBy(5, identity)
    .map(_ -> 1)
    .reduce((l, r) => (l._1, l._2 + r._2))
    .mergeSubstreams

  Await.ready(counts.runForeach(println), 10.seconds)

  system.terminate()

  def lineSink(fileName: String): Sink[String, Future[IOResult]] = {
    Flow[String]
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(fileName)))(Keep.right)
  }
}
