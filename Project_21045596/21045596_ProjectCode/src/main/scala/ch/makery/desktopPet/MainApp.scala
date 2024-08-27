package ch.makery.desktopPet

import ch.makery.desktopPet.entity.Cat
import ch.makery.desktopPet.util.Database
import ch.makery.desktopPet.view.{InventoryController, MenuController, PetViewController, StatusController}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.stage.{Screen, Stage, StageStyle}
import javafx.{scene => jfxs}
import scalafxml.core.{FXMLLoader, NoDependencyResolver}
import scalafx.Includes._
import scalafx.animation.TranslateTransition
import scalafx.scene.text.Font
import scalafx.util.Duration


object MainApp extends JFXApp {
  Database.setupDB()

  // Setup fonts
  Font.loadFont(getClass.getResourceAsStream("/font/RobotoCondensed-SemiBold.ttf"), 0)
  Font.loadFont(getClass.getResourceAsStream("/font/Sniglet-Regular.ttf"), 0)
  Font.loadFont(getClass.getResourceAsStream("/font/Marykate-Regular.ttf"), 0)

  // Standard fxml files
  val petViewResource = getClass.getResource("view/PetView.fxml")
  val petViewLoader  = new FXMLLoader(petViewResource, NoDependencyResolver)
  petViewLoader.load();
  val petViewRoot  = petViewLoader.getRoot[javafx.scene.layout.StackPane]
  val petViewController = petViewLoader.getController[PetViewController#Controller]

  val menuResource = getClass.getResource("view/Menu.fxml")
  val menuLoader = new FXMLLoader(menuResource, NoDependencyResolver)
  menuLoader.load();
  val menuRoot = menuLoader.getRoot[javafx.scene.control.SplitPane]
  val menuController = menuLoader.getController[MenuController#Controller]

  val rightPane = menuRoot.getItems.get(1).asInstanceOf[javafx.scene.layout.AnchorPane]


  stage = new PrimaryStage {
    initStyle(StageStyle.Transparent)
    alwaysOnTop = true
    width = Screen.primary.bounds.width
    height = Screen.primary.bounds.height
    x = 0
    y = 0

    scene = new Scene {
      root = petViewRoot
      fill = null
      petViewController.initialize(new Cat())
      showStatus()
    }
  }

  val mainMenuStage = new Stage {
    title = "Desktop Pet"
    alwaysOnTop = true
    scene = new Scene {
      root = menuRoot
      resizable = false
    }
  }

  def showMainMenu(): Unit = {
    mainMenuStage.show()
  }

  def closeAllWindows(): Unit = {
    stage.close()
    mainMenuStage.close()
  }

  def showStatus() = {
    val resource = getClass.getResourceAsStream("view/Status.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource)
    val root = loader.getRoot[jfxs.Parent]
    rightPane.children.clear()
    rightPane.getChildren.setAll(root)

    val controller = loader.getController[StatusController#Controller]
    petViewController.currentPet.foreach(controller.initialize)
    menuController.updateUI(menuRoot, "#statusBox")
  }

  def showCompanion() = {
    val resource = getClass.getResourceAsStream("view/Companion.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource)
    val root = loader.getRoot[jfxs.Parent]
    rightPane.children.clear()
    rightPane.getChildren.setAll(root)

    root.translateX = rightPane.width.value
    val slideTransition = new TranslateTransition(Duration(500), root)
    slideTransition.fromX = rightPane.width.value
    slideTransition.toX = 0
    slideTransition.play()
  }

  def hidePane(): Unit = {
    val root = rightPane.getChildren.get(0)
    val slideTransition = new TranslateTransition(Duration(500), root)
    slideTransition.fromX = 0
    slideTransition.toX = rightPane.width.value
    slideTransition.onFinished = _ => {
      rightPane.children.remove(root)
    }
    slideTransition.play()
  }

  def showDiary() = {
    val resource = getClass.getResourceAsStream("view/Diary.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource)
    val root = loader.getRoot[jfxs.Parent]
    rightPane.children.clear()
    rightPane.getChildren.setAll(root)

    menuController.updateUI(menuRoot, "#diaryBox")
  }

  def showInventory() = {
    val resource = getClass.getResourceAsStream("view/Inventory.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource)
    val root = loader.getRoot[jfxs.Parent]
    rightPane.children.clear()
    rightPane.getChildren.setAll(root)

    menuController.updateUI(menuRoot, "#invenBox")
  }

  def showShop() = {
    val resource = getClass.getResourceAsStream("view/Shop.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource)
    val root = loader.getRoot[jfxs.Parent]
    rightPane.children.clear()
    rightPane.getChildren.setAll(root)

    menuController.updateUI(menuRoot, "#shopBox")
  }

  def updateInventory(id: Int, durability: Option[Int], isToy: Boolean): Unit = {
    val resource = getClass.getResource("view/Inventory.fxml")
    val inventoryLoader = new FXMLLoader(resource, NoDependencyResolver)
    inventoryLoader.load()
    val inventoryController = inventoryLoader.getController[InventoryController#Controller]

    inventoryController.putBackItem(id, durability, isToy)
  }

  def showGame() = {
    val resource = getClass.getResourceAsStream("view/MiniGame.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource)
    val root = loader.getRoot[jfxs.Parent]
    rightPane.children.clear()
    rightPane.getChildren.setAll(root)

    menuController.updateUI(menuRoot, "#gameBox")
  }

  def showTicTacToe() = {
    val resource = getClass.getResourceAsStream("view/TicTacToe.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource)
    val root = loader.getRoot[jfxs.Parent]
    rightPane.children.clear()
    rightPane.getChildren.setAll(root)

    root.translateX = rightPane.width.value
    val slideTransition = new TranslateTransition(Duration(500), root)
    slideTransition.fromX = rightPane.width.value
    slideTransition.toX = 0
    slideTransition.play()
  }

  def showRockPaperScissors() = {
    val resource = getClass.getResourceAsStream("view/RockPaperScissors.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource)
    val root = loader.getRoot[jfxs.Parent]
    rightPane.children.clear()
    rightPane.getChildren.setAll(root)

    root.translateX = rightPane.width.value
    val slideTransition = new TranslateTransition(Duration(500), root)
    slideTransition.fromX = rightPane.width.value
    slideTransition.toX = 0
    slideTransition.play()
  }

  stage.show()
}

