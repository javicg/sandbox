package com.github.javicg.tennis

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import com.github.javicg.tennis.Umpire._
import com.github.javicg.tennis.model._

class Umpire extends PersistentActor with ActorLogging {
  override val persistenceId: String = "tennis-umpire"

  private var _match = Match()

  override def receiveCommand: Receive = {
    case StartGame(name1, name2) =>
      if (_match.isFinished) {
        log.warning("Game can't be started. It's finished!")
      } else if (_match.inProgress) {
        log.warning("Game can't be started. It's already in progress! Resuming game instead...")
        resumeGame()
      } else {
        persist(GameStarted(name1, name2)) { event =>
          log.info(s"Game started! ($name1 vs $name2)")
          updateState(event)
          resumeGame()
        }
      }

    case ResumeGame =>
      if (_match.isFinished) {
        log.warning("Game can't be resumed. It's finished!")
      } else {
        serve(_match.nextServe)
      }

    case EndGame =>
      persist(GameEnded) { _ =>
        endGame()
      }

    case FinishMatch =>
      context.system.terminate()
  }

  override def receiveRecover: Receive = {
    case event: GameStarted =>
      updateState(event)

    case event: PlayerScored =>
      updateState(event)

    case GameEnded =>
      endGame()
  }

  private def serve(player: Player) = {
    rally(player, player)
  }

  private def rally(player: Player, serving: Player) = {
    _match.getRef(player) ! Ball
    context.become(waitingForPlayer(player, serving))
  }

  private def waitingForPlayer(player: Player, serving: Player): Receive = {
    case BallOverNet =>
      rally(player.opponent, serving)

    case BallLost =>
      persist(PlayerScored(player.opponent, serving)) { event =>
        log.info(s"Point for ${player.opponent}")
        updateState(event)
        unstashAll()
        resumeGame()
        context.become(receiveCommand)
      }

    case EndGame =>
      stash()
  }

  private def resumeGame() = {
    self ! ResumeGame
  }

  private def updateState(event: GameStarted) = {
    _match = _match.init(
      context.actorOf(Props(classOf[PlayerActor], event.name1), "player1"),
      context.actorOf(Props(classOf[PlayerActor], event.name2), "player2"))
  }

  private def updateState(event: PlayerScored) = {
    _match += event
  }

  private def endGame(): Unit = {
    log.info("Game finished!")
    log.info(s"Player 1 [${_match.scores(Player1)}] - Player 2[${_match.scores(Player2)}]")
    _match = _match.end()
    self ! FinishMatch
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
  case object FinishMatch

  // Events
  sealed trait Event
  final case class GameStarted(name1: String, name2: String) extends Event
  final case class PlayerScored(player: Player, serving: Player) extends Event
  final case object GameEnded extends Event
}
