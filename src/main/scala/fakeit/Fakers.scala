package fakeit

import scala.util.Random

object Fakers {
  implicit val intFaker: Faker[Int] =  new Faker[Int] {
    override def getNext = Random.nextInt
  }

  implicit val stringFaker: Faker[String] =  new Faker[String] {
    override def getNext = Random.nextString(10)
  }
}
