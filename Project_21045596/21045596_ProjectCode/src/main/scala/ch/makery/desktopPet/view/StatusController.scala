package ch.makery.desktopPet.view

import ch.makery.desktopPet.MainApp
import ch.makery.desktopPet.entity.Pet
import ch.makery.desktopPet.model.PetData
import ch.makery.desktopPet.util.ImageUtil.trimBackground
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import scala.util.{Failure, Success}


@sfxml
class StatusController(
                        private val blobDeco: ImageView,
                        private val profilePic: ImageView,
                        private val statusDeco: ImageView,
                        private val idLabel: Label,
                        private val nameLabel: Label,
                        private val moodLabel: Label,
                        private val hungerLabel: Label,
                        private val behaviorLabel: Label,
                        private val switchPet: Button
                      ) {

  private var pet: Pet = _

  def initialize(pet: Pet): Unit = {
    this.pet = pet
    blobDeco.image = new Image(getClass.getResourceAsStream("/image/interface/blob.png"))
    statusDeco.image = trimBackground(new Image(getClass.getResourceAsStream(pet.imagePaths("deco").head)))
    profilePic.image = new Image(getClass.getResourceAsStream(pet.imagePaths("profile").head))

    pet.behavior.onChange { (_, _, newValue) =>
      behaviorLabel.text = newValue
      updateDatabase()
    }

    pet.mood.onChange { (_, _, newValue) =>
      moodLabel.text = newValue
      updateDatabase()
    }

    pet.hunger.onChange { (_, _, newValue) =>
      hungerLabel.text = newValue
      updateDatabase()
    }

    showStatusDetails()
    switchPet.onAction = (e: ActionEvent) => MainApp.showCompanion()
  }

  private def showStatusDetails(): Unit = {
    PetData.getAllPetData(pet.petID) match {
      case Success(Some((id, name, mood, hunger, behavior))) =>
        idLabel.text = id
        nameLabel.text = name
        moodLabel.text = mood
        hungerLabel.text = hunger
        behaviorLabel.text = behavior
      case Success(None) =>
        idLabel.text = "Unknown"
        nameLabel.text = "Unknown"
        moodLabel.text = "Unknown"
        hungerLabel.text = "Unknown"
        behaviorLabel.text = "Unknown"
      case Failure(_) =>
        idLabel.text = "Error"
        nameLabel.text = "Error"
        moodLabel.text = "Error"
        hungerLabel.text = "Error"
        behaviorLabel.text = "Error"
        println("Failed to get data.")
    }
  }

  private def updateDatabase(): Unit = {
    new PetData(pet.petID).copy(
      hunger = hungerLabel.text.value,
      mood = moodLabel.text.value,
      behavior = behaviorLabel.text.value
    ).save()
  }
}