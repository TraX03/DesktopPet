package ch.makery.desktopPet.model

import scalafx.beans.property.StringProperty
import ch.makery.desktopPet.util.Database
import scalikejdbc._
import scala.util.Try

class PetData(val petId: String) extends Database {
  var name       = new StringProperty("defaultName")
  var mood       = new StringProperty("Neutral")
  var hunger     = new StringProperty("Normal")
  var behavior   = new StringProperty("Idle")

  def copy(
            name: String = this.name.value,
            mood: String = this.mood.value,
            hunger: String = this.hunger.value,
            behavior: String = this.behavior.value
          ): PetData = {
    this.name.value = name
    this.mood.value = mood
    this.hunger.value = hunger
    this.behavior.value = behavior
    this
  }

  def save(): Try[Int] = {
    Try(DB autoCommit { implicit session =>
      sql"""
        UPDATE pet
        SET
        mood = ${mood.value},
        hunger = ${hunger.value},
        behavior = ${behavior.value}
        WHERE petId = $petId
      """.update.apply()
    })
  }

  def load(): Try[Unit] = {
    Try(DB readOnly { implicit session =>
      sql"""
        SELECT * FROM pet WHERE petId = $petId
      """.map(rs => {
        name.value = rs.string("name")
        mood.value = rs.string("mood")
        hunger.value = rs.string("hunger")
        behavior.value = rs.string("behavior")
      }).single.apply()
    }).map(_ => ())
  }
}

object PetData extends Database {
  def initializeTable(): Unit = {
    DB autoCommit { implicit session =>
      sql"""
        CREATE TABLE pet (
          petId VARCHAR(64) NOT NULL PRIMARY KEY,
          name VARCHAR(64),
          mood VARCHAR(64),
          hunger VARCHAR(64),
          behavior VARCHAR(100)
        )
      """.execute.apply()

      // Insert default records
      sql"""
        INSERT INTO pet (petId, name, mood, hunger, behavior)
        VALUES
          ('PSN250', 'Pusheen', 'Neutral', 'Normal', 'Idle'),
          ('SHN235', 'Shiba Inu', 'Neutral', 'Normal', 'Idle')
      """.update.apply()
    }
  }

  def getAllPetData(petId: String): Try[Option[(String, String, String, String, String)]] = {
    Try(DB readOnly { implicit session =>
      sql"""
        SELECT petId, name, mood, hunger, behavior FROM pet WHERE petId = $petId
      """.map(rs => (
        rs.string("petId"),
        rs.string("name"),
        rs.string("mood"),
        rs.string("hunger"),
        rs.string("behavior")
      )).single.apply()
    })
  }
}





