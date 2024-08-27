package ch.makery.desktopPet.entity

class Dog extends Pet {
  override def petID: String = "SHN235"
  override def petName: String = "Shiba Inu"

  override def imagePaths: Map[String, List[String]] = Map(
    "profile" -> List("/image/dog_profile.jpg"),
    "deco" -> List("/image/interface/dog_deco.png"),
    "idle" -> List("/image/dog_idle.png"),
    "walking" -> List("/image/dog_walking3.png"),
    "drag" -> List("/image/dog_drag.png"),
    "sit" -> List("/image/dog_sit.png"),
    "sleep" -> List("/image/dog_preSleep.png", "/image/dog_sleep1.png", "/image/dog_sleep2.png", "/image/dog_sleeping.png"),
    "ball" -> List("/image/dog_play.png"),
    "unhappy" -> List("/image/dog_sad.png"),
    "eating" -> List("/image/dog_eating.png"),
    "happy" -> List("/image/dog_happy.png"),
    "game" -> List("/image/dog_game.png")
    )

  override def preferences: Map[String, List[Int]] = Map(
    "love" -> List(1, 3, 4, 9),
    "hate" -> List(5, 7),
  )
}