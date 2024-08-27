package ch.makery.desktopPet.util

import scalikejdbc._
import ch.makery.desktopPet.model.{DiaryData, InventoryData, ItemData, PetData}

trait Database {
  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
  val dbURL = "jdbc:derby:petDB;create=true;"
  Class.forName(derbyDriverClassname)
  ConnectionPool.singleton(dbURL, "traZe", "avenstelle")
  implicit val session = AutoSession
}

object Database extends Database {
  def setupDB(): Unit = {
    if (!hasDBInitialize) {
      PetData.initializeTable()
      ItemData.initializeTable()
      InventoryData.initializeTable()
      DiaryData.initializeTable()
    }
  }

  def hasDBInitialize: Boolean = {
    DB.getTable("pet").isDefined &&
      DB.getTable("item").isDefined &&
      DB.getTable("inventory").isDefined &&
      DB.getTable("diary").isDefined
  }
}

