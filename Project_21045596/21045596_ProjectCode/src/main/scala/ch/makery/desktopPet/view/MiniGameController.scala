package ch.makery.desktopPet.view

import ch.makery.desktopPet.MainApp
import ch.makery.desktopPet.util.ImageUtil.trimBackground
import scalafx.scene.image.{Image, ImageView}
import scalafx.Includes._
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{AnchorPane, Background, BackgroundImage, BackgroundPosition, BackgroundRepeat, BackgroundSize}
import scalafxml.core.macros.sfxml


@sfxml
class MiniGameController(
                           private val gameOne: ImageView,
                           private val gameTwo: ImageView,
                           private val gamePane: AnchorPane
                         ){

  private def initialize(): Unit = {
    gameOne.image = trimBackground(new Image(getClass.getResourceAsStream("/image/interface/tic-tac-toe.png")))
    gameTwo.image = trimBackground(new Image(getClass.getResourceAsStream("/image/interface/rock-paper-scissors.png")))

    val backgroundImage = new Image(getClass.getResourceAsStream("/image/interface/game_background.png"))
    val bgImage = new BackgroundImage(
      backgroundImage,
      BackgroundRepeat.NoRepeat,
      BackgroundRepeat.NoRepeat,
      BackgroundPosition.Default,
      BackgroundSize.Default
    )
    gamePane.background = new Background(Array(bgImage))

    gameOne.onMouseClicked = (e: MouseEvent) => MainApp.showTicTacToe()
    gameTwo.onMouseClicked = (e: MouseEvent) => MainApp.showRockPaperScissors()
  }

  initialize()
}