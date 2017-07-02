package com.github.javicg.tennis

import akka.actor.Actor

import scala.util.Random

class PlayerActor extends Actor {
  override def receive: Receive = {
    case Ball =>
      val r = Random.nextInt(10)
      if (r < 9) {
        sender() ! BallOverNet
      } else {
        sender() ! BallLost
      }
  }
}
