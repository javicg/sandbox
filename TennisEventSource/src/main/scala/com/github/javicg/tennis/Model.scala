package com.github.javicg.tennis

import akka.actor.ActorRef
import com.github.javicg.tennis.Umpire.PlayerScored

// Model
sealed trait Player {
  def opponent: Player
}

case object Player1 extends Player {
  val opponent = Player2
}

case object Player2 extends Player {
  val opponent = Player1
}

// State
case class State(scores: Map[Player, Int] = Map.empty,
                 playerRefs: Map[Player, ActorRef] = Map.empty,
                 nextServing: Player = Player1,
                 isFinished: Boolean = false) {

  def init(ref1: ActorRef, ref2: ActorRef): State = {
    copy(
      scores = Map.empty,
      playerRefs = Map(Player1 -> ref1, Player2 -> ref2)
    )
  }

/*
 * Simplification of tennis rules:
 * - players play one after another (alternating)
 * - Points are just summed, no games/sets
 */
  def +(event: PlayerScored): State = {
    copy(
      scores = scores + (event.player -> (scores.getOrElse(event.player, 0) + 1)),
      nextServing = event.serving.opponent
    )
  }

  def end(): State = {
    copy(
      isFinished = true
    )
  }

  def inProgress: Boolean = playerRefs.nonEmpty

  def getRef(player: Player): ActorRef = playerRefs(player)
}