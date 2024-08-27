package ch.makery.desktopPet.model

import scalafx.beans.property.StringProperty
import ch.makery.desktopPet.util.Database
import scalikejdbc._
import scala.util.Try

class ItemData(val itemId: Int) extends Database {
  var itemName     = new StringProperty("defaultName")
  var imagePath    = new StringProperty("defaultPath")
  var price        = new StringProperty("0.0")
  var itemType     = new StringProperty("food") // Default type is "food"

  def save(): Try[Int] = {
    if (!isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
        INSERT INTO inventory (itemId, itemName, imagePath, price, itemType) VALUES
        (${itemId}, ${itemName.value}, ${imagePath.value}, ${price.value}, ${itemType.value})
      """.update.apply()
      }).recover({
        case e: Exception =>
          e.printStackTrace()
          0
      })
    } else {
      Try(DB autoCommit { implicit session =>
        sql"""
        UPDATE item
        SET
        itemName  = ${itemName .value},
        imagePath = ${imagePath.value},
        price = ${price.value},
        itemType = ${itemType.value}
        WHERE itemId = $itemId
      """.update.apply()
      })
    }
  }

  def load(): Try[Unit] = {
    Try(DB readOnly { implicit session =>
      sql"""
        SELECT * FROM item WHERE itemId = $itemId
      """.map(rs => {
        itemName.value = rs.string("itemName")
        imagePath.value = rs.string("imagePath")
        price.value = rs.string("price")
        itemType.value = rs.string("itemType")
      }).single.apply()
    }).map(_ => ())
  }

  def delete(): Try[Int] = {
    if (isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
          DELETE FROM inventory WHERE itemId = ${itemId}
        """.update.apply()
      })
    } else {
      throw new Exception("Item does not exist in the database")
    }
  }

  def isExist: Boolean = {
    DB readOnly { implicit session =>
      sql"""
        SELECT * FROM inventory WHERE itemId = ${itemId}
      """.map(rs => rs.int("itemId")).single.apply()
    } match {
      case Some(_) => true
      case None => false
    }
  }
}

object ItemData extends Database {
  def initializeTable(): Unit = {
    DB autoCommit { implicit session =>
      sql"""
        CREATE TABLE item (
          itemId INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
          itemName VARCHAR(64),
          imagePath VARCHAR(256),
          price INT,
          itemType VARCHAR(16) NOT NULL
        )
      """.execute.apply()

      // Insert default records
      sql"""
        INSERT INTO item (itemName, imagePath, price,itemType)
        VALUES
          ('Bone', '/image/item/bone.png', 5, 'food'),
          ('Cookie', '/image/item/cookie.png', 5, 'food'),
          ('Pet Treats', '/image/item/petTreats.png', 10, 'food'),
          ('Dog Food', '/image/item/dogFood.png', 20, 'food'),
          ('Cat Food', '/image/item/catFood.png', 20, 'food'),
          ('Milk', '/image/item/milk.png', 10,'food'),
          ('Fish', '/image/item/fish.png', 15, 'food'),
          ('Yarn', '/image/item/yarnBall.png', 30, 'toy'),
          ('Balls', '/image/item/balls.png', 30, 'toy')
      """.update.apply()
    }
  }

  def getItemDataOrAll(itemIdOpt: Option[Int], itemTypeOpt: Option[String]): Try[Seq[(Int, String, String, Int, String)]] = {
    Try(DB readOnly { implicit session =>
      (itemIdOpt, itemTypeOpt) match {
        case (Some(itemId), Some(itemType)) =>
          // Get data for the specific itemId and itemType
          sql"""
          SELECT itemId, itemName, imagePath, price, itemType
          FROM item
          WHERE itemId = $itemId AND itemType = $itemType
        """.map(rs => (
            rs.int("itemId"),
            rs.string("itemName"),
            rs.string("imagePath"),
            rs.int("price"),
            rs.string("itemType")
          )).single.apply().toSeq

        case (None, Some(itemType)) =>
          // Get data for all items of a specific itemType
          sql"""
          SELECT itemId, itemName, imagePath, price, itemType
          FROM item
          WHERE itemType = $itemType
        """.map(rs => (
            rs.int("itemId"),
            rs.string("itemName"),
            rs.string("imagePath"),
            rs.int("price"),
            rs.string("itemType")
          )).list.apply()

        case (Some(itemId), None) =>
          // Get data for a specific itemId, regardless of type
          sql"""
          SELECT itemId, itemName, imagePath, price, itemType
          FROM item
          WHERE itemId = $itemId
        """.map(rs => (
            rs.int("itemId"),
            rs.string("itemName"),
            rs.string("imagePath"),
            rs.int("price"),
            rs.string("itemType")
          )).single.apply().toSeq

        case (None, None) =>
          // Get data for all items
          sql"""
          SELECT itemId, itemName, imagePath, price, itemType
          FROM item
        """.map(rs => (
            rs.int("itemId"),
            rs.string("itemName"),
            rs.string("imagePath"),
            rs.int("price"),
            rs.string("itemType")
          )).list.apply()
      }
    })
  }
}