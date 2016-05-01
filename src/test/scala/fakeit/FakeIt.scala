package fakeit

import org.scalatest._
import fakeit.FakeIt._

import scala.util.Random

class FakeItSpec extends FreeSpec with Matchers {
  case class Person(name: String, age:Int)
  "FakeIt" - {
    "simple" in {
      val person = fake[Person]()
      person.name should not be ""
      person.age should not be 0
    }
    "fixed value" in {
      val person = fake[Person](_.name -> "test121")
      person.name should be ("test121")
      person.age should not be 0
    }
    "own string faker" in {
      val nameFaker: Faker[String] = new Faker[String] {
        override def getNext = "fakedName" + Random.nextString(10)
      }
      val person = fake[Person](_.name -> nameFaker.getNext)
      person.name should startWith ("fakedName")
      person.age should not be 0
    }
  }
}
