package com.github.javicg.tennis

import akka.actor.{ActorSystem, Props}

object Bootstrap extends App {
  val system = ActorSystem("TennisMatch")
  val umpire = system.actorOf(Props[Umpire], "Umpire")
  umpire ! StartGame("Rafa Nadal", "Roger Federer")
}
