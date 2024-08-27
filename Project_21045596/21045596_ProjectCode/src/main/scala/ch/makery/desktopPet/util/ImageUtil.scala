package ch.makery.desktopPet.util

import scalafx.scene.image.{Image, PixelReader, PixelWriter, WritableImage}
import scalafx.scene.paint.Color

object ImageUtil {
  // Trims transparent edges, returning only the visible area.
  def trimBackground(image: Image): WritableImage = {
    val (width, height) = (image.width.toInt, image.height.toInt)
    val pixelReader = image.pixelReader

    pixelReader match {
      case Some(pixelReader) =>
        val bounds = for {
          x <- 0 until width
          y <- 0 until height
          if pixelReader.getColor(x, y).opacity > 0
        } yield (x, y)

        bounds match {
          case Seq() => new WritableImage(0, 0)
          case _ =>
            val (minX, maxX) = (bounds.map(_._1).min, bounds.map(_._1).max)
            val (minY, maxY) = (bounds.map(_._2).min, bounds.map(_._2).max)
            val trimmedImage = new WritableImage(maxX - minX + 1, maxY - minY + 1)
            val pixelWriter = trimmedImage.pixelWriter
            for {
              x <- 0 until trimmedImage.width.toInt
              y <- 0 until trimmedImage.height.toInt
            } pixelWriter.setColor(x, y, pixelReader.getColor(minX + x, minY + y))
            trimmedImage
        }

      case None =>
        new WritableImage(0, 0)
    }
  }

  // Recolours the image
  def changeColor(image: Image, colorName: String): Image = {
    val colorMap = Map(
      "white" -> Color.White,
      "blue" -> Color.web("#99c9ea")
    )

    val replaceColor = colorMap.getOrElse(colorName.toLowerCase, Color.Transparent)

    val reader: PixelReader = image.pixelReader.get
    val width = image.width.toInt
    val height = image.height.toInt
    val writableImage = new WritableImage(width, height)
    val writer: PixelWriter = writableImage.pixelWriter

    for (x <- 0 until width; y <- 0 until height) {
      val color = reader.getColor(x, y)
      if (color.opacity > 0) {
        if (replaceColor == Color.Transparent) {
          writer.setColor(x, y, color)
        } else {
          writer.setColor(x, y, replaceColor)
        }
      } else {
        writer.setColor(x, y, color)
      }
    }
    writableImage
  }
}