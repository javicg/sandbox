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
      state = state.updated(event)
  }

  override def receiveCommand: Receive = {
    case StartGame(name1, name2) =>
      persist(NewGameEvent(name1, name2)) { event =>
        updateState(event)
        self ! ResumeGame
      }

    case ResumeGame =>
      askOpponentToPlay(state.lastPlayer)
  }

  private def askOpponentToPlay(player: Player) = {
    val opponent = player.opponent
    state.getRef(opponent) ! Ball
    context.become(waitingForPlayer(opponent))
  }

  private def waitingForPlayer(player: Player): Receive = {
    case BallOverNet =>
      askOpponentToPlay(player)

    case BallLost =>
      persist(GameScoreEvent(player.opponent)) { event =>
        state = state.updated(event)
        askOpponentToPlay(player)
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
