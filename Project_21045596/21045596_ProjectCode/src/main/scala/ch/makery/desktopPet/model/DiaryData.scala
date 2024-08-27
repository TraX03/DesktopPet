package ch.makery.desktopPet.model

import ch.makery.desktopPet.util.Database
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.time.LocalDateTime
import scala.util.Try

class DiaryData(val localDataS: LocalDateTime, val actionS: String, val descriptionS: String, val coinValueS: Int) extends Database {
  def this() = this(LocalDateTime.now(), "", "", 0)
  var localData   = ObjectProperty(localDataS)
  var action      = StringProperty(actionS)
  var description = StringProperty(descriptionS)
  var coinValue   = IntegerProperty(coinValueS)

  def save(): Try[Int] = {
    Try(DB autoCommit { implicit session =>
      sql"""
        INSERT INTO diary (localData, action, description, coinValue) VALUES
        (${localData.value}, ${action.value}, ${description.value}, ${coinValue.value})
      """.update.apply()
    }).recover({
      case e: Exception =>
        e.printStackTrace()
        0
    })
  }

  def delete(id: Int): Try[Int] = {
    if (isExist(id)) {
      Try(DB autoCommit { implicit session =>
        sql"""
        DELETE FROM diary WHERE id = $id
      """.update.apply()
      })
    } else
      throw new Exception("Record not found.")
  }

  def isExist(id: Int): Boolean = {
    DB readOnly { implicit session =>
      sql"""
        SELECT * FROM diary WHERE id = $id
      """.map(rs => rs.int("id")).single.apply()
    } match {
      case Some(_) => true
      case None => false
    }
  }
}

object DiaryData extends Database {
  def apply(
             localDataS: LocalDateTime,
             actionS: String,
             descriptionS: String,
             coinValueS: Int
           ): DiaryData = {
    new DiaryData(localDataS, actionS, descriptionS, coinValueS)
  }

  def initializeTable(): Unit = {
    DB autoCommit { implicit session =>
      sql"""
    CREATE TABLE diary (
      id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
      localData TIMESTAMP,
      action VARCHAR(64),
      description VARCHAR(64),
      coinValue INT
    )
    """.execute.apply()
    }
  }

  def getAllEntries: List[DiaryData] = {
    DB readOnly { implicit session =>
      sql"SELECT * FROM diary".map { rs =>
        DiaryData(
          rs.localDateTime("localData"),
          rs.string("action"),
          rs.string("description"),
          rs.int("coinValue")
        )
      }.list.apply()
    }
  }

  def getLastRecord: Option[DiaryData] = {
    DB readOnly { implicit session =>
      sql"""
        SELECT * FROM (
          SELECT * FROM diary ORDER BY localData DESC
        ) AS subquery
        FETCH FIRST ROW ONLY
      """.map { rs =>
        new DiaryData(
          localDataS = rs.localDateTime("localData"),
          actionS = rs.string("action"),
          descriptionS = rs.string("description"),
          coinValueS = rs.int("coinValue")
        )
      }.single.apply()
    }
  }
}