package ch.makery.desktopPet.view

import ch.makery.desktopPet.MainApp
import ch.makery.desktopPet.util.CoinManager
import ch.makery.desktopPet.util.ImageUtil.changeColor
import scalafx.scene.image.{Image, ImageView}
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import scalafx.scene.control.{Label, SplitPane}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.HBox


@sfxml
class MenuController(
                      private val statusBox: HBox,
                      private val diaryBox: HBox,
                      private val shopBox: HBox,
                      private val invenBox: HBox,
                      private val gameBox: HBox,
                      private val tornDecor: ImageView,
                      private val coinIcon: ImageView,
                      private val coinLabel: Label
                    ) {

  def initialize(): Unit = {
    initializeIcons()
    CoinManager.setCoinValue()  // Retrieve last coin value

    val hboxActionMap = Map[HBox, () => Unit](
      statusBox -> MainApp.showStatus,
      diaryBox -> MainApp.showDiary,
      shopBox -> MainApp.showShop,
      invenBox -> MainApp.showInventory,
      gameBox -> MainApp.showGame
    )

    coinLabel.text = s"Coins: ${CoinManager.coinValue.value}"
    CoinManager.coinValue.onChange((_, _, newValue) => coinLabel.text = s"Coins: ${newValue.intValue()}")

    hboxActionMap.foreach { case (box, action) =>
      box.onMouseClicked = (_: MouseEvent) => action()
    }
  }

  private def initializeIcons(): Unit = {
    val icons = Map[HBox, String](
      statusBox -> "/image/interface/pawprint.png",
      diaryBox -> "/image/interface/diary.png",
      shopBox -> "/image/interface/shop.png",
      invenBox -> "/image/interface/bag.png",
      gameBox -> "/image/interface/game.png"
    )

    icons.foreach { case (box, path) =>
      box.getChildren.get(0).asInstanceOf[javafx.scene.image.ImageView].image = changeColor(new Image(getClass.getResourceAsStream(path)), "white")
    }

    tornDecor.image = new Image(getClass.getResourceAsStream("/image/interface/torn.png"))
    coinIcon.image = new Image(getClass.getResourceAsStream("/image/interface/coin.png"))
  }

  def updateUI(root : SplitPane, activeMenuID: String): Unit = {
    val menuIDList = List("#statusBox", "#diaryBox", "#invenBox", "#shopBox", "#gameBox")

    val vbox = root.getItems.get(0)
      .asInstanceOf[javafx.scene.layout.AnchorPane]
      .getChildren.get(0)
      .asInstanceOf[javafx.scene.layout.BorderPane]
      .getCenter
      .asInstanceOf[javafx.scene.layout.VBox]

    menuIDList.foreach { menuID =>
      vbox.lookup(menuID) match {
        case box: javafx.scene.layout.HBox =>
          box.getStyleClass.setAll("menuHover", "menu")
          if (menuID == activeMenuID) {
            box.getStyleClass.setAll("activeMenu")
          }
          val imageView = box.getChildren.get(0).asInstanceOf[javafx.scene.image.ImageView]
          imageView.image = changeColor(imageView.image.getValue, if (menuID == activeMenuID) "blue" else "white")
        case _ => println("HBox not found.")
      }
    }
  }

  initialize()
}