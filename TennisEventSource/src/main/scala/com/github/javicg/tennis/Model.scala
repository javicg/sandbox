package com.github.javicg.tennis

import akka.actor.ActorRef

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

// Commands
case class StartGame(name1: String, name2: String)
case object Ball
case object BallOverNet
case object BallLost
case object ResumeGame

// Events
case class NewGameEvent(name1: String, name2: String)
case class GameScoreEvent(player: Player, serving: Player)

// State
case class State(score: Map[Player, Int] = Map.empty,
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
  def updated(event: GameScoreEvent): State = {
    copy(
      score + (event.player -> (score.getOrElse(event.player, 0) + 1)),
      playerRefs,
      event.serving.opponent
    )
  }

  def getRef(player: Player): ActorRef = playerRefs(player)
}