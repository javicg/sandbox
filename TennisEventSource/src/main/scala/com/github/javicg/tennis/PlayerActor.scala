package com.github.javicg.tennis

import akka.actor.{Actor, ActorLogging}
import com.github.javicg.tennis.Umpire.{Ball, BallLost, BallOverNet}

import scala.util.Random

class PlayerActor(name: String) extends Actor with ActorLogging {
  override def receive: Receive = {
    case Ball =>
      waitForReception()
      playBack()
  }

  private def waitForReception() = {
    log.debug(s"$name is waiting for the ball...")
    Thread.sleep(300)
  }

  private def playBack() = {
    log.debug(s"$name plays back!")
    val r = Random.nextInt(10)
    if (r < 5) {
      sender() ! BallOverNet
    } else {
      sender() ! BallLost
    }
  }
}
