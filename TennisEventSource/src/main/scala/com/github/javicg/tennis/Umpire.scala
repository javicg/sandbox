package com.github.javicg.tennis

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import com.github.javicg.tennis.Umpire._

class Umpire extends PersistentActor with ActorLogging {
  override val persistenceId: String = "tennis-umpire"

  private var state: State = State()

  override def receiveRecover: Receive = {
    case event: GameStarted =>
      updateState(event)
      self ! ResumeGame

    case event: PlayerScored =>
      updateState(event)
  }

  override def receiveCommand: Receive = {
    case StartGame(name1, name2) =>
      persist(GameStarted(name1, name2)) { event =>
        log.info(s"Game started! ($name1 vs $name2)")
        updateState(event)
        self ! ResumeGame
      }

    case ResumeGame =>
      serve(state.nextServing)

    case EndGame =>
      log.info("Game finished!")
      log.info(s"Player 1 [${state.scores(Player1)}] - Player 2[${state.scores(Player2)}]")
      context.system.terminate()
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
      persist(PlayerScored(player.opponent, serving)) { event =>
        log.info(s"Point for ${player.opponent}")
        updateState(event)
        self ! ResumeGame
        unstashAll()
        context.become(receiveCommand)
      }

    case EndGame =>
      stash()
  }

  private def updateState(event: GameStarted) = {
    state = state.init(
      context.actorOf(Props(classOf[PlayerActor], event.name1), "player1"),
      context.actorOf(Props(classOf[PlayerActor], event.name2), "player2"))
  }

  private def updateState(event: PlayerScored) = {
    state += event
  }

}

object Umpire {
  // Commands
  sealed trait Command
  final case class StartGame(name1: String, name2: String) extends Command
  case object EndGame extends Command

  // Protocol
  case object Ball
  case object BallOverNet
  case object BallLost
  case object ResumeGame

  // Events
  sealed trait Event
  final case class GameStarted(name1: String, name2: String) extends Event
  final case class PlayerScored(player: Player, serving: Player) extends Event
  final case object GameEnded extends Event
}
