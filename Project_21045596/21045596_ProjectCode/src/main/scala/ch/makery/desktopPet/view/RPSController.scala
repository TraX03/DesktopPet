package ch.makery.desktopPet.view

import ch.makery.desktopPet.MainApp
import ch.makery.desktopPet.entity.GameOutcome.GameOutcome
import ch.makery.desktopPet.entity.{GameOutcome, RockPaperScissors}
import ch.makery.desktopPet.util.ImageUtil._
import scalafx.animation.PauseTransition
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{AnchorPane, Background, BackgroundImage, BackgroundPosition, BackgroundRepeat, BackgroundSize}
import scalafx.util.Duration
import scalafxml.core.macros.sfxml


@sfxml
class RPSController(
                     private val rockIcon: ImageView,
                     private val paperIcon: ImageView,
                     private val scissorsIcon: ImageView,
                     private val petPick: ImageView,
                     private val petPic: ImageView,
                     private val bubble: ImageView,
                     private val backIcon: ImageView,
                     private val restartIcon: ImageView,
                     private val statusLabel: Label,
                     private val backPane: AnchorPane,
                     private val restartPane: AnchorPane,
                     private val gamePane: AnchorPane
                   ) {

  private val game = new RockPaperScissors()
  private var currentRound = 1

  private def initialize(): Unit = {
    loadImages()
    setupEventHandlers()
    disableRestart(false, true)
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
  }

  private def loadImages(): Unit = {
    val images = Map[ImageView, String](
      rockIcon -> "/image/interface/Rock.png",
      paperIcon -> "/image/interface/Paper.png",
      scissorsIcon -> "/image/interface/Scissors.png",
      bubble -> "/image/interface/thinkBubble.png",
      backIcon -> "/image/interface/return.png",
      restartIcon -> "/image/interface/restart.png",
    )

    images.foreach { case (imageView, path) =>
      imageView.image = trimBackground(new Image(getClass.getResourceAsStream(path)))
    }
  }

  private def setupEventHandlers(): Unit = {
    backPane.onMouseClicked = _ => {
      MainApp.hidePane()
      MainApp.showGame()
    }
    restartPane.onMouseClicked = _ => restart()
    rockIcon.onMouseClicked = _ => handleButtonPress("Rock")
    paperIcon.onMouseClicked = _ => handleButtonPress("Paper")
    scissorsIcon.onMouseClicked = _ => handleButtonPress("Scissors")
  }

  private def handleButtonPress(userChoice: String): Unit = {
    disableIcons(true)

    if (currentRound <= 3 && !game.isGameOver) {
      val roundOutcome = game.playRound(userChoice)
      petPick.image = trimBackground(new Image(s"/image/interface/${game.petChoice}.png"))

      if (game.isGameOver) {
        displayFinalOutcome()
      } else {
        updateStatusLabel(roundOutcome)
        currentRound += 1
        scheduleNextRound()
      }
    }
  }

  private def updateStatusLabel(roundOutcome: GameOutcome): Unit = {
    statusLabel.text = s"Round $currentRound: ${roundOutcome match {
      case GameOutcome.Win => "You Win!"
      case GameOutcome.Lose => "You Lose!"
      case GameOutcome.Draw => "It's a Draw!"
    }}"
  }

  private def displayFinalOutcome(): Unit = {
    val gameResult = game.play()

    statusLabel.text = gameResult.result match {
      case GameOutcome.Win => s"Congratulations! You won the match and earned ${gameResult.reward} coins!"
      case GameOutcome.Lose => s"Sorry! You lost the match. Better luck next time!"
      case GameOutcome.Draw => s"It's a Draw! Well played! You are awarded ${gameResult.reward} coins."
    }

    disableIcons(true)
    disableRestart(true, false)
  }

  private def scheduleNextRound(): Unit = {
    new PauseTransition(Duration(1000)) {
      onFinished = _ => {
        petPick.image = null
        statusLabel.text = s"Round $currentRound: Make your choice!"
        disableIcons(false)
      }
    }.play()
  }

  private def disableIcons(disable: Boolean): Unit = {
    rockIcon.disable = disable
    paperIcon.disable = disable
    scissorsIcon.disable = disable
  }

  private def disableRestart(visible: Boolean, disable: Boolean): Unit = {
    restartPane.visible = visible
    restartPane.disable = disable
  }

  private def restart(): Unit = {
    game.reset()
    currentRound = 1
    statusLabel.text = "Round 1: Make your choice!"
    disableIcons(false)
    disableRestart(false, true)
    petPick.image = null
  }

  initialize()
}