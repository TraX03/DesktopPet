package ch.makery.desktopPet.view

import ch.makery.desktopPet.MainApp
import ch.makery.desktopPet.model.{DiaryData, InventoryData, ItemData}
import ch.makery.desktopPet.util.CoinManager
import ch.makery.desktopPet.util.ImageUtil.trimBackground
import scalafx.scene.image.{Image, ImageView}
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, Label, Pagination}
import scalafx.scene.layout.{BorderPane, ColumnConstraints, GridPane, HBox, Priority, RowConstraints, StackPane, VBox}
import scalafx.scene.text.Font

import scala.util.{Failure, Success}
import java.time.LocalDateTime


@sfxml
class ShopController(
                      private val itemPage: Pagination,
                      private val foodButton: Button,
                      private val toyButton: Button,
                      private val decoOne: ImageView,
                      private val decoTwo: ImageView
                    ) {

  private var currentItemType: String = "food"

  private def initialize(): Unit = {
    decoTwo.image = trimBackground(new Image(getClass.getResourceAsStream("/image/interface/shop_deco.png")))
    decoOne.image = trimBackground(new Image(getClass.getResourceAsStream("/image/interface/shop_deco2.png")))

    foodButton.onAction = handleClick("food")
    toyButton.onAction = handleClick("toy")

    updateButtonStyle()
    showItems()
  }

  private def handleClick(itemType: String) = (event: ActionEvent) => {
    currentItemType = itemType
    updateButtonStyle()
    showItems()
  }

  private def updateButtonStyle(): Unit = {
    foodButton.styleClass.removeAll("activeButton", "inactiveButton", "menuHover")
    toyButton.styleClass.removeAll("activeButton", "inactiveButton", "menuHover")

    if (currentItemType.contains("food")) {
      foodButton.styleClass += "activeButton"
      toyButton.styleClass.addAll("inactiveButton", "menuHover")
    } else {
      foodButton.styleClass.addAll("inactiveButton", "menuHover")
      toyButton.styleClass += "activeButton"
    }
  }

  private def showItems(): Unit = {
    val items = fetchItems()
    val itemsPerPage = 4
    itemPage.pageCount = (items.size + itemsPerPage - 1) / itemsPerPage
    itemPage.setPageFactory(pageIndex => createPage(items.slice(pageIndex * itemsPerPage, (pageIndex + 1) * itemsPerPage)))
  }

  private def fetchItems(): Seq[ShopItem] = {
    ItemData.getItemDataOrAll(None, Some(currentItemType)) match {
      case Success(data) =>
        data.map { case (itemId, name, imagePath, price, itemType) =>
          ShopItem(itemId, name, imagePath, price, itemType)
        }
      case Failure(_) =>
        Seq.empty
    }
  }

  private def createPage(items: Seq[ShopItem]): GridPane = {
    val columnConstraint = new ColumnConstraints {
      minWidth = 260
      prefWidth = 260
      maxWidth = 260
      hgrow = Priority.Always
    }

    val rowConstraint = new RowConstraints {
      minHeight = 150
      prefHeight = 150
      maxHeight = 150
      vgrow = Priority.Always
    }

    val gridPane = new GridPane {
      hgap = 32
      vgap = 20
      columnConstraints = Seq.fill(2)(columnConstraint)
      rowConstraints = Seq.fill(2)(rowConstraint)
      hgrow = Priority.Always
      vgrow = Priority.Always
    }

    items.zipWithIndex.foreach { case (item, index) =>
      val (col, row) = (index % 2, index / 2)
      gridPane.add(createItemBorderPane(item), col, row)
    }

    gridPane
  }

  private def createItemBorderPane(item: ShopItem): BorderPane = new BorderPane {
  top = new Label(item.name) {
    margin = Insets(0, 0, 10, 15)
    font = Font.font(22)
  }

  left = new StackPane {
    margin = Insets(0, 0, 0, 15)
    children = new HBox {
      alignment = Pos.Center
      children = new ImageView(trimBackground(new Image(item.imagePath))) {
        fitWidth = 80
        fitHeight = 80
        preserveRatio = true
      }
    }
  }

  right = new StackPane {
    margin = Insets(0, 40, 0, 0)
    children = new VBox {
      alignment = Pos.CenterLeft
      spacing = 5
      children = Seq(
        new HBox {
          margin = Insets(0, 0, 10, 0)
          spacing = 5
          children = Seq(
            new Label(s"${item.price}") {
              font = Font.font(22)
            },
            new ImageView(new Image("/image/interface/coin.png")) {
              fitWidth = 30
              fitHeight = 30
              preserveRatio = true
            }
          )
        },
        new Button("Buy") {
          styleClass += "buyButton"
          onAction = buyItem(item)
        }
      )
    }
  }
}

  private def buyItem(item: ShopItem) = (event: ActionEvent) => {
    val userCoins = CoinManager.coinValue.value

    if (userCoins >= item.price) {
      handlePurchase(item)
      CoinManager.subtractCoins(item.price)

      // Log purchase
      val newRecord = DiaryData(
        localDataS = LocalDateTime.now(),
        actionS = "Purchase",
        descriptionS = s"Purchase ${item.name} for ${item.price} coins.",
        coinValueS = CoinManager.coinValue.value
      )
      newRecord.save()

    } else {
      showAlert(AlertType.Warning, "Not enough coins to buy this item.")
    }
  }

  private def handlePurchase(item: ShopItem): Unit = {
    InventoryData.getAllItems.find(_.itemId.value == item.itemId) match {
      case Some(existingItem) =>
        // Increase quantity value if same item is in inventory
        existingItem.quantity.value += 1
        existingItem.save() match {
          case Success(_) => showAlert(AlertType.Information, s"Quantity of item ${item.name} updated.")
          case Failure(e) =>
            e.printStackTrace()
            showAlert(AlertType.Error, "Failed to update item quantity.")
        }

      case None =>
        // Create new record if no this item
        val newItem = InventoryData(
          item.itemId,
          item.name,
          item.itemType,
          quantityI = 1,
          durabilityI = if (item.itemType.equalsIgnoreCase("toy")) Some(100) else None
        )
        newItem.save()
        showAlert(AlertType.Information, s"Item ${item.name} added to inventory.")
    }
  }

  private def showAlert(alertType: AlertType, content: String): Unit = {
    val alert = new Alert(alertType) {
      initOwner(MainApp.stage)
      title = "Purchase Confirmation"
      contentText = content
    }
    alert.setHeaderText(null)
    alert.showAndWait()
  }

  initialize()
}

case class ShopItem(itemId: Int, name: String, imagePath: String, price: Int, itemType: String)

