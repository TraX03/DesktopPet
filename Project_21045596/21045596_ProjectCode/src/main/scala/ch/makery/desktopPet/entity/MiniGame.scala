package ch.makery.desktopPet.entity

import ch.makery.desktopPet.entity.GameOutcome.GameOutcome
import ch.makery.desktopPet.model.DiaryData
import ch.makery.desktopPet.util.CoinManager

import java.time.LocalDateTime

abstract class MiniGame {
  def gameName: String
  def reset(): Unit

  def play(): GameResult = {
    val result = playGame()
    val reward = rewardCoins(result)

    if (reward!=0){
      CoinManager.addCoins(reward)
      val newRecord = DiaryData(
        localDataS = LocalDateTime.now(),
        actionS = "Earned",
        descriptionS = s"Earned $reward coins from playing $gameName.",
        coinValueS = CoinManager.coinValue.value
      )
      newRecord.save()
    }

    GameResult(result, reward)
  }

  // Game logic
  protected def playGame(): GameOutcome

  private def rewardCoins(result: GameOutcome): Int = result match {
    case GameOutcome.Win  => 10
    case GameOutcome.Lose => 0
    case GameOutcome.Draw => 5
  }
}

case class GameResult(result: GameOutcome, reward: Int)

object GameOutcome extends Enumeration {
  type GameOutcome = Value
  val Win, Lose, Draw = Value
}