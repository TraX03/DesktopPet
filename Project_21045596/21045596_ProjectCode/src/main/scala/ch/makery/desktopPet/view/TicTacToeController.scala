package ch.makery.desktopPet.view

import ch.makery.desktopPet.MainApp
import ch.makery.desktopPet.entity.{GameOutcome, TicTacToe}
import ch.makery.desktopPet.util.ImageUtil._
import scalafx.scene.control.Label
import scalafx.scene.image.Image
import scalafx.scene.image.ImageView
import scalafx.scene.input.MouseEvent
import scalafx.Includes._
import scalafx.animation.PauseTransition
import scalafx.scene.layout.{AnchorPane, Background, BackgroundImage, BackgroundPosition, BackgroundRepeat, BackgroundSize}
import scalafx.util.Duration
import scalafxml.core.macros.sfxml

import scala.util.Random

@sfxml
class TicTacToeController(
                           private val cellOne: ImageView,
                           private val cellTwo: ImageView,
                           private val cellThree: ImageView,
                           private val cellFour: ImageView,
                           private val cellFive: ImageView,
                           private val cellSix: ImageView,
                           private val cellSeven: ImageView,
                           private val cellEight: ImageView,
                           private val cellNine: ImageView,
                           private val petPic: ImageView,
                           private val backIcon: ImageView,
                           private val restartIcon: ImageView,
                           private val statusLabel: Label,
                           private val backPane: AnchorPane,
                           private val restartPane: AnchorPane,
                           private val gamePane: AnchorPane
                         ) {

  private val game = new TicTacToe()
  private val cells = Seq(cellOne, cellTwo, cellThree, cellFour, cellFive, cellSix, cellSeven, cellEight, cellNine)
  private var userSymbol, petSymbol, currentPlayer = ""
  private var gameEnded = false

  private def initialize(): Unit = {
    backIcon.image = trimBackground(new Image(s"/image/interface/return.png"))
    restartIcon.image = trimBackground(new Image(s"/image/interface/restart.png"))

    val bgImage = new BackgroundImage(
      new Image(getClass.getResourceAsStream("/image/interface/room.png")),
      BackgroundRepeat.NoRepeat,
      BackgroundRepeat.NoRepeat,
      BackgroundPosition.Default,
      new BackgroundSize(
        width = BackgroundSize.Auto,
        height = BackgroundSize.Auto,
        widthAsPercentage = false,
        heightAsPercentage = false,
        contain = true,
        cover = true
      )
    )
    gamePane.background = new Background(Array(bgImage))

    petPic.image = MainApp.petViewController.currentPet
      .flatMap(_ => MainApp.petViewController.trimmedImages.get("game").flatMap(_.headOption))
      .getOrElse(null)

    // Initialize game
    userSymbol = if (Random.nextBoolean()) "X" else "O"
    petSymbol = if (userSymbol == "X") "O" else "X"
    currentPlayer = userSymbol

    disableRestart(false, true)

    cells.zipWithIndex.foreach { case (cell, index) =>
      cell.onMouseClicked = (e: MouseEvent) => handleCellClick(index)
    }

    backPane.onMouseClicked = (e: MouseEvent) => {
      MainApp.hidePane()
      MainApp.showGame()
    }
    restartPane.onMouseClicked = (e: MouseEvent) => restartGame()

    updateStatusLabel()
  }

  private def handleCellClick(cellIndex: Int): Unit = {
    if (!gameEnded && game.isCellEmpty(cellIndex) && currentPlayer == userSymbol) {
      playMove(cellIndex, userSymbol)

      // Switch turn to pet
      if (!gameEnded) {
        currentPlayer = petSymbol
        updateStatusLabel()
        new PauseTransition(Duration(1000)) {
          onFinished = _ => petPlay()
        }.play()
      }
    }
  }

  private def petPlay(): Unit = {
    val availableCells = cells.zipWithIndex.filter { case (cell, index) => game.isCellEmpty(index) }
    if (availableCells.nonEmpty) {
      val (_, index) = availableCells(Random.nextInt(availableCells.length))
      playMove(index, petSymbol)

      if (!gameEnded){
        currentPlayer = userSymbol
        updateStatusLabel()
      }
    }
  }

  private def playMove(cellIndex: Int, symbol: String): Unit = {
    game.makeMove(cellIndex, symbol)
    cells(cellIndex).image = trimBackground(new Image(s"/image/interface/$symbol.png"))
    checkGameStatus()
  }

  private def updateStatusLabel(): Unit = {
    val petName = MainApp.petViewController.currentPet.map(_.petName)
      .getOrElse("Unknown Pet")

    statusLabel.text = if (currentPlayer == userSymbol) {
      s"Your turn: You are '$userSymbol'"
    } else {
      s"$petName's turn: $petName is '$petSymbol'"
    }
  }

  private def checkGameStatus(): Unit = {
    val gameResult = game.play()

    gameResult.result match {
      case GameOutcome.Win =>
        gameEnded = true
        if (currentPlayer == userSymbol) {
          statusLabel.text = s"You Win! You are awarded ${gameResult.reward} coins."
        } else {
          statusLabel.text = "You Lose! Better luck next time."
        }
      case GameOutcome.Draw =>
        gameEnded = true
        statusLabel.text = s"Game Over! It's a Draw! You are awarded ${gameResult.reward} coins."
      case _ => // Game ongoing
    }

    if(gameEnded){
      disableRestart(true, false)
    }
  }

  private def restartGame(): Unit = {
    game.reset()
    gameEnded = false
    currentPlayer = userSymbol

    cells.foreach(_.image = null)

    disableRestart(false, true)
    updateStatusLabel()
  }

  private def disableRestart(visible: Boolean, disable: Boolean): Unit = {
    restartPane.visible = visible
    restartPane.disable = disable
  }

  initialize()
}