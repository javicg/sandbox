package com.github.javicg.tennis

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import com.github.javicg.tennis.Umpire.{EndGame, StartGame}

import scala.concurrent.duration.FiniteDuration

object Bootstrap extends App {
  val system = ActorSystem("TennisMatch")

  val umpire = system.actorOf(Props[Umpire], "Umpire")
  umpire ! StartGame("Rafa Nadal", "Roger Federer")

  implicit val context = system.dispatcher
  system.scheduler.scheduleOnce(FiniteDuration(5, TimeUnit.SECONDS)) {
    umpire ! EndGame
  }
}
