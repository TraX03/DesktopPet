package ch.makery.desktopPet.view

import ch.makery.desktopPet.MainApp
import ch.makery.desktopPet.entity.Pet
import ch.makery.desktopPet.util.AnimationUtil.animateFall
import ch.makery.desktopPet.util.ImageUtil.trimBackground
import scalafx.animation.{FadeTransition, KeyFrame, PauseTransition, Timeline}
import scalafx.scene.control.{ContextMenu, MenuItem}
import scalafx.scene.image.{Image, ImageView, WritableImage}
import scalafx.scene.input.{ContextMenuEvent, MouseEvent}
import scalafx.scene.layout.StackPane
import scalafx.util.Duration
import scalafx.Includes._
import scalafx.scene.Cursor.Hand
import scalafxml.core.macros.sfxml

import scala.util.Random

@sfxml
class PetViewController(
                         private var petView: ImageView,
                         private var mainView: StackPane
                        ) {

  var currentPet: Option[Pet] = None
  var trimmedImages: Map[String, List[WritableImage]] = Map.empty
  var itemImageMap: Map[Int, (ImageView, Int, Boolean)] = Map.empty

  private val contextMenu = new ContextMenu(
    new MenuItem("Pet Menu") { onAction = _ => MainApp.showMainMenu() },
    new MenuItem("Dismiss Pet") { onAction = _ => MainApp.closeAllWindows() }
  )

  def handleMousePressed(event: MouseEvent): Unit = {
    petView.userData = (event.sceneX, event.sceneY, petView.translateX(), petView.translateY())
    currentPet.foreach(_.stopAnimation())
  }

  def handleMouseDragged(event: MouseEvent): Unit = {
    val (startX, startY, startTX, startTY) = petView.userData.asInstanceOf[(Double, Double, Double, Double)]
    val (deltaX, deltaY) = (event.sceneX - startX, event.sceneY - startY)
    petView.translateX = startTX + deltaX
    petView.translateY = startTY + deltaY
    petView.image = trimmedImages("drag").headOption.orNull
    currentPet.foreach(_.behavior.value = "Being drag")
    petView.scaleX = if (currentPet.exists(_.currentDirection != "right")) 1 else -1
  }

  def handleMouseReleased(event: MouseEvent): Unit = {
    val (releaseX, releaseY) = (petView.translateX(), petView.translateY())
    currentPet.foreach { pet =>
      pet.falling(petView, Duration(1100), releaseX, releaseY, trimmedImages)
      new PauseTransition(Duration(1100)) {
        onFinished = _ => pet.walking(petView, trimmedImages)
      }.play()
    }
  }

  def showContextMenu(event: ContextMenuEvent): Unit = {
    contextMenu.show(petView, event.getScreenX, event.getScreenY)
  }

  def initialize(pet: Pet): Unit = {
    // Clear all global variables
    petView.image = null
    currentPet.foreach(_.stopAnimation())
    currentPet = None
    trimmedImages = Map.empty

    // Setup new pet
    currentPet = Some(pet)
    trimmedImages = pet.imagePaths.map { case (state, paths) =>
      state -> paths.map { path =>
        trimBackground(new Image(getClass.getResourceAsStream(path)))
      }
    }

    petView.image = trimmedImages("idle").headOption.orNull
    pet.falling(petView, Duration(1100), 100, -1, trimmedImages)

    // Schedule animations
    new PauseTransition(Duration(2000)) {
      onFinished = _ => {
        pet.walking(petView, trimmedImages)
      }
    }.playFromStart()

    new Timeline {
      cycleCount = Timeline.Indefinite
      keyFrames = Seq(KeyFrame(Duration(10000 + Random.nextInt(20000)), onFinished = _ =>
        currentPet.foreach(_.sitting(petView, trimmedImages))
      ))
    }.playFromStart()
  }

  def addItemImageView(imagePath: String, itemId: Int, durability: Option[Int], isToy: Boolean): Unit = {
    val itemView = new ImageView(trimBackground(new Image(getClass.getResourceAsStream(imagePath))))
    itemView.fitWidth = 70
    itemView.fitHeight = 70
    itemView.preserveRatio = true
    itemView.cursor = Hand

    val initialDurability = durability.getOrElse(100)
    itemImageMap += (itemId -> (itemView, initialDurability, isToy))
    mainView.children.add(itemView)
    animateFall(itemView, Duration(1100), 100, -1, null)

    durability.foreach { _ =>
      new Timeline {
        cycleCount = Timeline.Indefinite
        keyFrames = Seq(
          KeyFrame(Duration(3000), onFinished = _ => {
            itemImageMap.get(itemId).foreach { case (itemView, currentDur, _) =>
              val updatedDur = currentDur - 10
              if (updatedDur <= 0) {
                fadeOutAndRemove(itemView, itemId, isToy)
                stop()
              }
              else itemImageMap += (itemId -> (itemView, updatedDur, isToy))
            }
          })
        )
      }.play()
    }

    itemView.onContextMenuRequested = (event: ContextMenuEvent) => {
      val itemMenu = new ContextMenu(
        new MenuItem("Put Back") {
          onAction = _ => {
            val (_, currentDur, _) = itemImageMap.getOrElse(itemId, (itemView, 100, isToy))
            MainApp.updateInventory(itemId, Some(currentDur), isToy)
            mainView.children.remove(itemView)
          }
        }
      )
      itemMenu.show(itemView, event.getScreenX, event.getScreenY)
    }

    itemView.onMousePressed = (event: MouseEvent) => handleItemMousePressed(event, itemView)
    itemView.onMouseDragged = (event: MouseEvent) => handleItemMouseDragged(event, itemView)
    itemView.onMouseReleased = (event: MouseEvent) => handleItemMouseReleased(event, itemView, itemId, isToy)
  }

  private def fadeOutAndRemove(view: ImageView, itemId: Int, isToy: Boolean): Unit = {
    if (isToy) {
      val fadeTransition = new FadeTransition {
        node = view
        fromValue = 1.0
        toValue = 0.0
        duration = Duration(1000)
      }
      fadeTransition.onFinished = _ => {
        itemImageMap -= itemId
        mainView.children.remove(view)
        MainApp.updateInventory(itemId, Some(0), isToy)
      }
      fadeTransition.play()
    }
  }

  private def handleItemMousePressed(event: MouseEvent, itemView: ImageView): Unit = {
    itemView.userData = (event.sceneX, event.sceneY, itemView.translateX(), itemView.translateY())
  }

  private def handleItemMouseDragged(event: MouseEvent, itemView: ImageView): Unit = {
    val (startX, startY, startTX, startTY) = itemView.userData.asInstanceOf[(Double, Double, Double, Double)]
    val (deltaX, deltaY) = (event.sceneX - startX, event.sceneY - startY)
    itemView.translateX = startTX + deltaX
    itemView.translateY = startTY + deltaY
  }

  private def handleItemMouseReleased(event: MouseEvent, itemView: ImageView, itemId: Int, isToy: Boolean): Unit = {
    val (releaseX, releaseY) = (itemView.translateX(), itemView.translateY())
    animateFall(itemView, Duration(1100), releaseX, releaseY, null)
    if (isItemTouchingPet(itemView)) {
      currentPet.foreach { pet =>
        if (isToy) pet.playing(petView, itemView, itemId, trimmedImages)
        else pet.eating(petView, itemView, itemId, trimmedImages)
      }
    }
  }

  private def isItemTouchingPet(itemView : ImageView): Boolean = {
    petView.boundsInParent.value.intersects(itemView.boundsInParent.value)
  }
}





