package ch.makery.desktopPet.view

import ch.makery.desktopPet.model.DiaryData
import ch.makery.desktopPet.util.ImageUtil.trimBackground
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{TableColumn, TableView}
import scalafx.beans.property.StringProperty
import scalafx.scene.image.{Image, ImageView}
import scalafxml.core.macros.sfxml

import java.time.format.DateTimeFormatter


@sfxml
class DiaryController (
                        private val diaryTable: TableView[DiaryEntry],
                        private val decoOne: ImageView,
                      ){

  private def initialize(): Unit = {
    decoOne.image = trimBackground(new Image(getClass.getResourceAsStream("/image/interface/diary_deco.png")))
    configureTable()
  }

  private def configureTable(): Unit = {
    diaryTable.items = ObservableBuffer(DiaryData.getAllEntries.map(convertToDiaryEntry): _*)
    diaryTable.columns ++= Seq(
      createColumn("DateTime", _.dateTime, 105),
      createColumn("Action", _.action, 90),
      createColumn("Description", _.description, 350)
    )
  }

  private def createColumn(header: String, valueExtractor: DiaryEntry => String, colWidth: Double): TableColumn[DiaryEntry, String] = {
    new TableColumn[DiaryEntry, String] {
      text = header
      cellValueFactory = cellData => StringProperty(valueExtractor(cellData.value))
      prefWidth = colWidth
    }
  }

  private def convertToDiaryEntry(diaryData: DiaryData): DiaryEntry = {
    val formatter = DateTimeFormatter.ofPattern("dd-MM HH:mm")
    val formattedDateTime = diaryData.localData.value.format(formatter)
    DiaryEntry(formattedDateTime, diaryData.action.value, diaryData.description.value)
  }

  initialize()
}

case class DiaryEntry(dateTime: String, action: String, description: String)