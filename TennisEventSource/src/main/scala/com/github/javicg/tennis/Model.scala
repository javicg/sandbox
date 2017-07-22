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

case object Unknown extends Player {
  val opponent = Unknown
}

// State
case class State(scores: Map[Player, Int] = Map.empty,
                 playerRefs: Map[Player, ActorRef] = Map.empty,
                 nextServing: Player = Unknown) {

  def init(ref1: ActorRef, ref2: ActorRef): State = {
    copy(
      Map.empty,
      Map(Player1 -> ref1, Player2 -> ref2),
      Player1
    )
  }

/*
 * Simplification of tennis rules:
 * - players play one after another (alternating)
 * - Points are just summed, no games/sets
 */
  def +(event: PlayerScored): State = {
    copy(
      scores + (event.player -> (scores.getOrElse(event.player, 0) + 1)),
      playerRefs,
      event.serving.opponent
    )
  }

  def getRef(player: Player): ActorRef = playerRefs(player)
}