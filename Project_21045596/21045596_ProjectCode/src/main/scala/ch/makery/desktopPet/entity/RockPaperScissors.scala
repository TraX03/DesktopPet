package ch.makery.desktopPet.entity

import ch.makery.desktopPet.entity.GameOutcome.GameOutcome

import scala.util.Random

class RockPaperScissors extends MiniGame {
  override def gameName: String = "Rock Paper Scissors"
  private var userWins = 0
  private var petWins = 0
  private var roundsPlayed = 0
  var petChoice: String = _

  def playRound(userChoice: String): GameOutcome = {
    petChoice = Random.shuffle(Seq("Rock", "Paper", "Scissors")).head
    val roundOutcome = determineOutcome(userChoice, petChoice)
    updateScores(roundOutcome)
    roundsPlayed += 1
    roundOutcome
  }

  private def determineOutcome(userChoice: String, petChoice: String): GameOutcome = {
    (userChoice, petChoice) match {
      case (u, p) if u == p => GameOutcome.Draw
      case ("Rock", "Scissors") | ("Paper", "Rock") | ("Scissors", "Paper") => GameOutcome.Win
      case _ => GameOutcome.Lose
    }
  }

  private def updateScores(outcome: GameOutcome): Unit = {
    outcome match {
      case GameOutcome.Win => userWins += 1
      case GameOutcome.Lose => petWins += 1
      case _ => // Do nothing for a draw
    }
  }

  def isGameOver: Boolean = {
    userWins >= 2 || petWins >= 2 || roundsPlayed >= 3 ||
      (roundsPlayed == 2 && userWins == 0 && petWins == 0)
  }

  override protected def playGame(): GameOutcome = {
    while (!isGameOver) {
      playRound(Random.shuffle(Seq("Rock", "Paper", "Scissors")).head)
    }
    if (userWins >= 2) GameOutcome.Win
    else if (petWins >= 2) GameOutcome.Lose
    else GameOutcome.Draw
  }

  override def reset(): Unit = {
    userWins = 0
    petWins = 0
    roundsPlayed = 0
    petChoice = ""
  }
}