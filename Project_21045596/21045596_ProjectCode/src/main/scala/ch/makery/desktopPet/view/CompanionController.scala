package ch.makery.desktopPet.view

import ch.makery.desktopPet.MainApp
import ch.makery.desktopPet.entity.{Cat, Dog}
import ch.makery.desktopPet.util.ImageUtil.trimBackground
import scalafx.scene.image.{Image, ImageView}
import scalafx.Includes._
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{AnchorPane, Background, BackgroundImage, BackgroundPosition, BackgroundRepeat, BackgroundSize}
import scalafxml.core.macros.sfxml


@sfxml
class CompanionController(
                           private val petOne: ImageView,
                           private val petTwo: ImageView,
                           private val companionPane: AnchorPane
                         ){

  private def initialize(): Unit = {
    petOne.image = trimBackground(new Image(getClass.getResourceAsStream("/image/interface/cat_display.png")))
    petTwo.image = trimBackground(new Image(getClass.getResourceAsStream("/image/interface/shiba_display.png")))

    val bgImage = new BackgroundImage(
      new Image(getClass.getResourceAsStream("/image/interface/companion_background.png")),
      BackgroundRepeat.NoRepeat,
      BackgroundRepeat.NoRepeat,
      BackgroundPosition.Default,
      BackgroundSize.Default
    )
    companionPane.background = new Background(Array(bgImage))

    setAction(petOne, () => MainApp.petViewController.initialize(new Cat()))
    setAction(petTwo, () => MainApp.petViewController.initialize(new Dog()))
  }

  private def setAction(imageView: ImageView, action: () => Unit): Unit = {
    imageView.onMouseClicked = (_: MouseEvent) => {
      action()
      MainApp.hidePane()
      MainApp.showStatus()
    }
  }

  initialize()
}