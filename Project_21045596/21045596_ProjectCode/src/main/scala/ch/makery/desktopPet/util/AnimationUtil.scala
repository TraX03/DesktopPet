package ch.makery.desktopPet.util

import scalafx.animation.TranslateTransition
import scalafx.scene.image.{ImageView, WritableImage}
import scalafx.util.Duration
import scalafx.stage.Screen

object AnimationUtil {
  def calcLanding(imageView: ImageView): Double = {
    Screen.primary.visualBounds.maxY - imageView.boundsInParent().getHeight
  }

  def animateFall(imageView: ImageView, duration: Duration, originX: Double, originY: Double, images: Map[String, List[WritableImage]]): Unit = {
    val fallTransition = new TranslateTransition(duration, imageView) {
      fromX = originX
      fromY = originY
      toY = calcLanding(imageView)
    }

    if (images != null) {
      fallTransition.onFinished = _ => {
        imageView.image = images("idle").headOption.orNull
        imageView.translateY = calcLanding(imageView)
      }
    }

    fallTransition.play()
  }
}