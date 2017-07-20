package com.github.javicg.tennis

import akka.actor.Props
import akka.persistence.PersistentActor

class Umpire extends PersistentActor {
  override def persistenceId: String = "tennis-umpire"

  private var state: State = State()

  override def receiveRecover: Receive = {
    case event: NewGameEvent =>
      updateState(event)
      self ! ResumeGame

    case event: GameScoreEvent =>
      updateState(event)
  }

  override def receiveCommand: Receive = {
    case StartGame(name1, name2) =>
      persist(NewGameEvent(name1, name2)) { event =>
        updateState(event)
        self ! ResumeGame
      }

    case ResumeGame =>
      serve(state.nextServing)
  }

  private def serve(player: Player) = {
    rally(player, player)
  }

  private def rally(player: Player, serving: Player) = {
    state.getRef(player) ! Ball
    context.become(waitingForPlayer(player, serving))
  }

  private def waitingForPlayer(player: Player, serving: Player): Receive = {
    case BallOverNet =>
      rally(player.opponent, serving)

    case BallLost =>
      persist(GameScoreEvent(player.opponent, serving)) { event =>
        updateState(event)
        serve(serving.opponent)
      }
  }

  private def updateState(event: NewGameEvent) = {
    state = state.init(
      context.actorOf(Props[PlayerActor], event.name1),
      context.actorOf(Props[PlayerActor], event.name2))
  }

  private def updateState(event: GameScoreEvent) = {
    state = state.updated(event)
  }

}
