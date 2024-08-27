package ch.makery.desktopPet.entity

import ch.makery.desktopPet.entity.GameOutcome.GameOutcome

class TicTacToe extends MiniGame {
  override def gameName:String = "Tic Tac Toe"
  private var board = Array.fill(9)(None: Option[String])

  def isCellEmpty(index: Int): Boolean = board(index).isEmpty

  def makeMove(index: Int, player: String): Unit = {
    if (isCellEmpty(index)) board(index) = Some(player)
  }

  override protected def playGame(): GameOutcome = {
    val winPatterns = Seq(
      Seq(0, 1, 2), // Top row
      Seq(3, 4, 5), // Middle row
      Seq(6, 7, 8), // Bottom row
      Seq(0, 3, 6), // Left column
      Seq(1, 4, 7), // Middle column
      Seq(2, 5, 8), // Right column
      Seq(0, 4, 8), // Diagonal
      Seq(2, 4, 6)  // Diagonal
    )

    winPatterns.find(pattern =>
      board(pattern.head).nonEmpty &&
        board(pattern.head) == board(pattern(1)) &&
        board(pattern(1)) == board(pattern(2))
    ) match {
      case Some(_) => GameOutcome.Win
      case None if board.forall(_.nonEmpty) => GameOutcome.Draw
      case _ => GameOutcome.Lose
    }
  }

  override def reset(): Unit = {
    board = Array.fill(9)(None: Option[String])
  }
}