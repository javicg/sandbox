package com.github.javicg.tennis.model

import akka.actor.ActorRef
import com.github.javicg.tennis.Umpire.PlayerScored

final case class Match(scores: Map[Player, Int], players: Map[Player, ActorRef], nextServe: Player, isFinished: Boolean) {

  def init(ref1: ActorRef, ref2: ActorRef): Match = {
    copy(
      scores = Map.empty,
      players = Map(Player1 -> ref1, Player2 -> ref2)
    )
  }

  /*
   * Simplification of tennis rules:
   * - players play one after another (alternating)
   * - Points are just summed, no games/sets
   */
  def +(event: PlayerScored): Match = {
    copy(
      scores = scores + (event.player -> (scores.getOrElse(event.player, 0) + 1)),
      nextServe = event.serving.opponent
    )
  }

  def end(): Match = {
    copy(
      isFinished = true
    )
  }

  def inProgress: Boolean = players.nonEmpty

  def getRef(player: Player): ActorRef = players(player)
}

object Match {
  def apply(): Match = Match(scores = Map.empty, players = Map.empty, nextServe = Player1, isFinished = false)
}