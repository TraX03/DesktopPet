package ch.makery.desktopPet.entity

import ch.makery.desktopPet.MainApp
import ch.makery.desktopPet.model.{DiaryData, ItemData}
import ch.makery.desktopPet.util.{AnimationUtil, CoinManager}
import scalafx.animation.{KeyFrame, PauseTransition, SequentialTransition, Timeline, TranslateTransition}
import scalafx.beans.property.StringProperty
import scalafx.scene.image.{ImageView, WritableImage}
import scalafx.stage.Screen
import scalafx.util.Duration

import java.time.LocalDateTime
import scala.util.{Random, Success, Try}

abstract class Pet {
  private val screenBounds = Screen.primary.bounds
  private var imageCycler: Option[Timeline] = None
  private var walkTransition: Option[TranslateTransition] = None
  private var sitTransition: Option[PauseTransition] = None
  private var continueTransition: Option[PauseTransition] = None
  private var sleepTransition: Option[PauseTransition] = None
  private var animationFrame = 0
  var behavior: StringProperty = new StringProperty(this, "behavior", "Idle")
  var mood: StringProperty = new StringProperty(this, "mood", "Neutral")
  var hunger: StringProperty = new StringProperty(this, "hunger", "Normal")
  var currentDirection: String = "right"

  private val moodAndHungerTimeline = new Timeline {
    cycleCount = Timeline.Indefinite
    keyFrames = Seq(
      KeyFrame(Duration(20000 + Random.nextInt(40000)), onFinished = _ => updateMoodAndHunger())
    )
  }.play()

  def petID: String
  def petName: String
  def imagePaths: Map[String, List[String]]
  def preferences: Map[String, List[Int]]

  def falling(imageView: ImageView, duration: Duration, originX: Double, originY: Double, images: Map[String, List[WritableImage]]): Unit = {
    AnimationUtil.animateFall(imageView, duration, originX, originY, images)
  }

  def walking(imageView: ImageView, images: Map[String, List[WritableImage]]): Unit = {
    stopAnimation()

    val walkingImages = images.getOrElse("walking", List())
    val idleImages = images.getOrElse("idle", List())
    if (walkingImages.isEmpty) return

    imageView.image = images("idle").headOption.orNull
    val imageWidth = imageView.boundsInParent().getWidth
    val screenWidth = screenBounds.maxX - imageWidth

    currentDirection  = {
      if (imageView.translateX.value <= 0) "right"
      else if (imageView.translateX.value >= screenWidth) "left"
      else if (Random.nextBoolean()) "right" else "left"
    }

    val translationLength = Random.nextDouble() * screenWidth
    val targetX = {
      if (currentDirection  == "right")
        math.min(screenWidth, imageView.translateX.value + translationLength)
      else
        math.max(0, imageView.translateX.value - translationLength)
    }

    // Flip the image horizontally according to transition direction
    imageView.scaleX = if (currentDirection  == "right") -1 else 1

    val frameDuration = Duration(200)
    val walkDuration = Duration(5000 + Random.nextInt(2000))
    val idleDuration = Duration(2000 + Random.nextInt(3000))
    val combinedImages = idleImages ++ walkingImages
    behavior.value = "Walking"
    // Walking images cycle
    imageCycler = Some(new Timeline {
      cycleCount = Timeline.Indefinite
      keyFrames = Seq(KeyFrame(frameDuration, onFinished = _ => {
        imageView.image = combinedImages(animationFrame % combinedImages.size)
        animationFrame += 1
      }))
    })

    walkTransition = Some(new TranslateTransition(walkDuration, imageView) {
      fromX = imageView.translateX.value
      toX = targetX
      onFinished = _ => {
        behavior.value = "Idle"
        imageCycler.foreach(_.stop())
        imageView.image = idleImages.headOption.orNull
        imageView.scaleX = if (currentDirection == "right") -1 else 1
        val continueWalk =new PauseTransition(idleDuration) {
          onFinished = _ => {
            walking(imageView, images)
          }
        }
        continueTransition = Some(continueWalk)
        continueWalk.play()
      }
    })

    imageCycler.foreach(_.play())
    walkTransition.foreach(_.play())
  }

  def sitting(imageView: ImageView, images: Map[String, List[WritableImage]]): Unit = {
    // Sit only when walk animation is finished
    val performSitAnimation = () => {
      stopAnimation()
      behavior.value = "Sitting"
      imageView.image = images.getOrElse("sit", List()).headOption.orNull

      val sitDuration = Duration(3000 + Random.nextInt(2000))
      val sitPause = new PauseTransition(sitDuration) {
        onFinished = _ => {
          // 40% probability pet decide to sleep
          if (Random.nextDouble() < 0.4) {
            sleeping(imageView, images)
          } else {
            walking(imageView, images)
          }
        }
      }
      sitTransition = Some(sitPause)
      sitPause.play()
    }

    walkTransition match {
      case Some(transition) =>
        transition.setOnFinished(_ => performSitAnimation())
      case None =>
        performSitAnimation()
    }
  }

  private def sleeping(imageView: ImageView, images: Map[String, List[WritableImage]]): Unit = {
    stopAnimation()

    behavior.value = "Sleeping"
    val sleepingImages = images.getOrElse("sleep", List())
    if (sleepingImages.isEmpty) return
    imageView.image = sleepingImages.headOption.orNull

    imageCycler = Some(new Timeline {
      cycleCount = Timeline.Indefinite
      keyFrames = Seq(KeyFrame(Duration(500), onFinished = _ => {
        imageView.image = sleepingImages.drop(1)(animationFrame % (sleepingImages.size-1))
        animationFrame += 1
      }))
    })
    imageCycler.foreach(_.play())

    val sleepDuration = Duration(6000 + Random.nextInt(5000))
    val sleepPause = new PauseTransition(sleepDuration) {
      onFinished = _ => {
        sitting(imageView, images)
      }
    }

    sleepTransition = Some(sleepPause)
    sleepPause.play()
  }

  def playing(petImage: ImageView, itemImage: ImageView, itemId: Int, images: Map[String, List[WritableImage]]): Unit = {
    itemImage.visible = false
    stopAnimation()

    val toyType = Map(8 -> "yarn", 9 -> "ball").getOrElse(itemId, "unknown")
    mood.value = determineMood(itemId)
    saveDiaryRecord("Played", s"Played with ${petName} with $toyType.")

    mood.value match {
      case "Joyful" =>
        val happyImage = images.getOrElse("happy", List())
        petImage.image = happyImage.headOption.orNull

      case "Angry" =>
        val unhappyImage = images.getOrElse("unhappy", List())
        petImage.image = unhappyImage.headOption.orNull
        behavior.value =  "Angy"
        // Pet refuse to play
        new PauseTransition(Duration(1000)) {
          onFinished = _ => {
            itemImage.visible = true
            walking(petImage, images)
          }
        }
        return

      case _ =>
        val neutralImage = images.getOrElse("sit", List())
        petImage.image = neutralImage.headOption.orNull
    }

    behavior.value = "Playing"

    val showPlayImage = new PauseTransition(Duration(1000)) {
      onFinished = _ => {
        val toyImages = images.getOrElse(toyType, List())
        petImage.image = toyImages.headOption.orNull
      }
    }

    val donePlay = new PauseTransition(Duration(3000)) {
      onFinished = _ => {
        itemImage.visible = true
        walking(petImage, images)
      }
    }

    val sequence = new SequentialTransition()
    sequence.getChildren.addAll(showPlayImage, donePlay)
    sequence.play()
  }

  def eating(petImage: ImageView, itemImage: ImageView, itemId: Int, images: Map[String, List[WritableImage]]): Unit = {
    itemImage.visible = false
    stopAnimation()

    val itemData: Try[Seq[(Int, String, String, Int, String)]] = ItemData.getItemDataOrAll(Some(itemId), None)
    val itemName = itemData match {
      case Success(itemDataSeq) if itemDataSeq.nonEmpty =>
        itemDataSeq.head._2
      case _ =>
    }

    mood.value = determineMood(itemId)
    saveDiaryRecord("Fed", s"Fed ${petName} with $itemName.")

    mood.value match {
      case "Joyful" =>
        val happyImage = images.getOrElse("happy", List())
        petImage.image = happyImage.headOption.orNull

      case "Angry" =>
        val unhappyImage = images.getOrElse("unhappy", List())
        petImage.image = unhappyImage.headOption.orNull
        behavior.value = "Angry"
        // Pet refuse to eat
        new PauseTransition(Duration(1000)) {
          onFinished = _ => {
            MainApp.petViewController.itemImageMap -= itemId
            walking(petImage, images)
          }
        }.play()
        return

      case _ =>
        val neutralImage = images.getOrElse("sit", List())
        petImage.image = neutralImage.headOption.orNull
    }

    behavior.value = "Eating"
    hunger.value = "Full"

    val showEatImage = new PauseTransition(Duration(1000)) {
      onFinished = _ => {
        petImage.image = images.getOrElse("eating", List()).headOption.orNull
      }
    }

    val doneEat = new PauseTransition(Duration(1000)) {
      onFinished = _ => {
        MainApp.petViewController.itemImageMap -= itemId
        walking(petImage, images)
      }
    }

    val sequence = new SequentialTransition()
    sequence.getChildren.addAll(showEatImage, doneEat)
    sequence.play()
  }

  private def updateMoodAndHunger(): Unit = {
    val newMood = if (Random.nextBoolean()) "Sad" else "Neutral"
    mood.value = newMood
    hunger.value = "Normal"

    new PauseTransition(Duration(30000)) {
      onFinished = _ => {
        hunger.value = "Hungry"
        mood.value = "Sad"
      }
    }.playFromStart()
  }

  private def determineMood(itemId: Int): String = {
    preferences.collectFirst {
      case ("love", ids) if ids.contains(itemId) => "Joyful"
      case ("hate", ids) if ids.contains(itemId) => "Angry"
    }.getOrElse("Happy")
  }

  private def saveDiaryRecord(action: String, description: String): Unit = {
    DiaryData(
      localDataS = LocalDateTime.now(),
      actionS = action,
      descriptionS = description,
      coinValueS = CoinManager.coinValue.value
    ).save()
  }

  def stopAnimation(): Unit = {
    imageCycler.foreach(_.stop())
    walkTransition.foreach(_.stop())
    sitTransition.foreach(_.stop())
    sleepTransition.foreach(_.stop())
    continueTransition.foreach(_.stop())
    imageCycler = None
    walkTransition = None
    sitTransition = None
    sleepTransition = None
    continueTransition = None
    animationFrame = 0
    behavior.value = "Idle"
  }
}




