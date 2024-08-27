package ch.makery.desktopPet.util

import ch.makery.desktopPet.model.DiaryData
import scalafx.beans.property.IntegerProperty

object CoinManager {
  private val _coinValue = IntegerProperty(30)

  def coinValue: IntegerProperty = _coinValue

  def setCoinValue(): Unit = {
    val lastRecordOpt = DiaryData.getLastRecord

    lastRecordOpt match {
      case Some(record) =>
        _coinValue.value = record.coinValue.value
      case None =>
    }
  }

  def addCoins(amount: Int): Unit = {
    _coinValue.value += amount
  }

  def subtractCoins(amount: Int): Unit = {
    _coinValue.value -= amount
  }
}