package ch.makery.desktopPet.model

import ch.makery.desktopPet.util.Database
import scalafx.beans.property.{IntegerProperty, StringProperty}
import scalikejdbc._

import scala.util.{Failure, Success, Try}

class InventoryData(val itemIdS: Int, val itemNameS: String, val itemTypeS: String) extends Database {
  def this() = this(0, null, null)
  val itemId      = IntegerProperty(itemIdS)
  var itemName    = StringProperty(itemNameS)
  var itemType    = StringProperty(itemTypeS)
  var quantity    = IntegerProperty(0)
  var durability  = IntegerProperty(0)

  def save(): Try[Int] = {
    if (!isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
        INSERT INTO inventory (itemId, itemName, itemType, quantity, durability) VALUES
        (${itemId.value}, ${itemName.value}, ${itemType.value}, ${quantity.value}, ${if (durability.value == 0) null else durability.value})
      """.update.apply()
      }).recover({
        case e: Exception =>
          e.printStackTrace()
          0
      })
    } else {
      Try(DB autoCommit { implicit session =>
        sql"""
        UPDATE inventory
        SET
        itemName = ${itemName.value},
        itemType = ${itemType.value},
        quantity = ${quantity.value},
        durability = ${if (durability.value == 0) null else durability.value}
        WHERE itemId = ${itemId.value}
      """.update.apply()
      })
    }
  }

  def delete(): Try[Int] = {
    if (isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
          DELETE FROM inventory WHERE itemId = ${itemId.value}
        """.update.apply()
      })
    } else {
      throw new Exception("Item does not exist in the database")
    }
  }

  def isExist: Boolean = {
    DB readOnly { implicit session =>
      sql"""
        SELECT * FROM inventory WHERE itemId = ${itemId.value}
      """.map(rs => rs.int("itemId")).single.apply()
    } match {
      case Some(_) => true
      case None => false
    }
  }
}

object InventoryData extends Database {
  def apply(
             itemIdS: Int,
             itemNameS: String,
             itemTypeS: String,
             quantityI: Int,
             durabilityI: Option[Int] = None
           ): InventoryData = {
    new InventoryData(itemIdS, itemNameS, itemTypeS) {
      quantity.value = quantityI
      durability.value = durabilityI.getOrElse(0)
    }
  }

  def initializeTable(): Unit = {
    DB autoCommit { implicit session =>
      sql"""
    CREATE TABLE inventory (
      itemId INT PRIMARY KEY,
      itemName VARCHAR(64),
      itemType VARCHAR(64),
      quantity INT,
      durability INT
    )
    """.execute.apply()
    }
  }

  def getAllItems: List[InventoryData] = {
    DB readOnly { implicit session =>
      sql"SELECT * FROM inventory".map { rs =>
        InventoryData(
          rs.int("itemId"),
          rs.string("itemName"),
          rs.string("itemType"),
          rs.int("quantity"),
          Option(rs.intOpt("durability").getOrElse(0))
        )
      }.list.apply()
    }
  }
}