package ch.makery.desktopPet.entity

class Cat extends Pet {
  override def petID: String = "PSN250"
  override def petName: String = "Pusheen"

  override def imagePaths: Map[String, List[String]] = Map(
    "profile" -> List("/image/catProfile.png"),
    "deco" -> List("/image/interface/cat_status.png"),
    "idle" -> List("/image/cat_idle.jpg"),
    "walking" -> List("/image/cat_walking2.png", "/image/cat_walking3.png"),
    "drag" -> List("/image/cat_drag.png"),
    "sit" -> List("/image/cat_sit.png"),
    "sleep" -> List("/image/cat_preSleep.png", "/image/cat_sleep1.png", "/image/cat_sleep2.png", "/image/cat_sleeping.png"),
    "happy" -> List("/image/cat_happy.png"),
    "yarn" -> List("/image/yarn_play.png"),
    "unhappy" -> List("/image/cat_unhappy.png"),
    "eating" -> List("/image/cat_eating.png"),
    "game" -> List("/image/cat_game.png")
  )

  override def preferences: Map[String, List[Int]] = Map(
    "love" -> List(2, 7, 8),
    "hate" -> List(1, 4, 9),
  )
}