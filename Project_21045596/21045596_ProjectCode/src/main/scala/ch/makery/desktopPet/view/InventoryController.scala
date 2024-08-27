package ch.makery.desktopPet.view

import ch.makery.desktopPet.MainApp
import ch.makery.desktopPet.model.{InventoryData, ItemData}
import ch.makery.desktopPet.util.ImageUtil._
import scalafx.geometry.Insets
import scalafx.Includes._
import scalafx.scene.Cursor
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{AnchorPane, BorderPane, FlowPane, HBox, Pane, StackPane}
import scalafxml.core.macros.sfxml


@sfxml
class InventoryController(
                           private val bagPane: ScrollPane,
                           private val foodButton: Button,
                           private val toyButton: Button,
                           private val refreshIcon: ImageView,
                           private val inventoryPane: FlowPane,
                           private val refreshPane: AnchorPane,
                           private val decoOne: ImageView,
                         ) {

  private def initialize(itemType: Option[String] = None): Unit = {
    decoOne.image = trimBackground(new Image(getClass.getResourceAsStream("/image/interface/inven_deco.png")))
    refreshIcon.image = trimBackground(new Image(s"/image/interface/refresh.png"))

    inventoryPane.children.clear()
    val filteredItems = InventoryData.getAllItems.filter(item => itemType.forall(_ == item.itemType.value))

    if (filteredItems.isEmpty) {
      val noItemsLabel = new Label("No Items Available") {id = "noItemLabel"}
      inventoryPane.children.add(noItemsLabel)
    } else {
      filteredItems.foreach { item =>
        val borderPane = createItemPane(item)
        inventoryPane.children.add(borderPane)
      }
    }

    bagPane.content = inventoryPane
    bagPane.padding = Insets(13)

    refreshPane.onMouseClicked = (e: MouseEvent) => MainApp.showInventory()
    foodButton.onAction = _ => switchCategory(Some("food"))
    toyButton.onAction = _ => switchCategory(Some("toy"))
  }

  private def createItemPane(item: InventoryData): BorderPane = {
    val itemData = new ItemData(item.itemId.value)
    itemData.load()

    val imageView = new ImageView {
      image = trimBackground(new Image(getClass.getResourceAsStream(itemData.imagePath.value)))
      fitWidth = 50
      fitHeight = 50
      preserveRatio = true
    }

    val itemNameLabel = new Label {
      text = item.itemName.value + (if (item.itemType.value == "toy") s" \nDurability: ${item.durability.value}/100" else "")
      visible = false
      id = "itemNameLabel"
    }

    val quantityLabel = new Label(s"${item.quantity.value}") {
      id = "quantityLabel"
      maxWidth = Double.MaxValue
    }

    item.quantity.onChange { (_, _, newValue) =>
      quantityLabel.text = s"${newValue.intValue()}"
    }

    new BorderPane {
      id = "itemPane"
      margin = Insets(15, 0, 0, 15)

      top = new StackPane {
        children = Seq(
          new HBox {
            id ="imageHolder"
            children = Seq(imageView)
            margin = Insets(15, 15, 5, 15)
          },
          itemNameLabel
        )
      }

      center = quantityLabel

      bottom = new Pane {
        minHeight = 10
      }

      cursor = Cursor.Hand

      onMouseEntered = _ => itemNameLabel.visible = true

      onMousePressed = _ => {
        // Deploy the item to the primary stage
        if (item.quantity.value > 0) {
          item.quantity.value -= 1
          item.save()
          if (item.quantity.value == 0) {
            inventoryPane.children.remove(this)
            item.delete()
          }
        }

        MainApp.petViewController.addItemImageView(itemData.imagePath.value, item.itemId.value,
          Option(item.durability.value), item.itemType.value == "toy")
      }

      onMouseExited = _ => itemNameLabel.visible = false
    }
  }

  def putBackItem(itemId: Int, durability: Option[Int], isToy: Boolean): Unit = {
    val existingItemOpt = InventoryData.getAllItems.find(_.itemId.value == itemId)

    existingItemOpt match {
      case Some(item) =>
        // Update existing item
        durability.foreach { dur =>
          if (isToy) {
            if (dur == 0) {
              item.durability.value = 100
              item.quantity.value -= 1
            } else {
              item.durability.value = dur
              item.quantity.value += 1
            }
          } else {
            item.quantity.value += 1
          }
        }
        item.save()

      case None =>
        // Handle not existing item
        val itemData = new ItemData(itemId)
        itemData.load()

        if (isToy && durability.exists(_ != 0)) {
          val newItem = InventoryData(
            itemId,
            itemData.itemName.value,
            itemData.itemType.value,
            quantityI = 1,
            durabilityI = durability
          )
          newItem.save()

          inventoryPane.children.add(createItemPane(newItem))
        }
        else if(!isToy) {
          val newItem = InventoryData(
            itemId,
            itemData.itemName.value,
            itemData.itemType.value,
            quantityI = 1,
            durabilityI = Some(0)
          )
          newItem.save()
        }
    }
  }

  private def switchCategory(itemType: Option[String]): Unit = {
    initialize(itemType)
    updateButtonStyle(itemType)
  }

  private def updateButtonStyle(activeType: Option[String]): Unit = {
    Seq(foodButton, toyButton).foreach(_.styleClass.removeAll("inventoryActive", "inventoryInactive", "menuHover"))
    activeType match {
      case Some("food") => setButtonStyles(foodButton, toyButton)
      case Some("toy")  => setButtonStyles(toyButton, foodButton)
    }
  }

  private def setButtonStyles(activeButton: Button, inactiveButton: Button): Unit = {
    activeButton.styleClass.add("inventoryActive")
    inactiveButton.styleClass.addAll("inventoryInactive", "menuHover")
  }

  switchCategory(Some("food"))
}

