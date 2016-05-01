package fakeit

import org.scalatest._
import fakeit.FakeIt._

import scala.util.Random
import fakeit.Fakers._

class FakeItSpec extends FreeSpec with MustMatchers {

  case class Person(name: String, age:Int)
  "FakeIt" - {
    "simple" in {
      val person = fake[Person]()
      person.name must not be ""
      person.age must not be 0
    }
    "fixed value" in {
      val person = fake[Person](_.name -> "test121")
      person.name must be ("test121")
      person.age must not be 0
    }
    "own string faker" in {
      val person = fake[Person](_.name -> ("fakedName" + Random.nextString(10)))
      person.name must startWith ("fakedName")
      person.age must not be 0
    }
    "own implicit for my own type" in {
      trait MyTrait {}
      type MyType = Long with MyTrait
      case class Test(name: String, whatever: MyType)
      implicit val myTypeFaker: Faker[MyType] = new Faker[MyType] {
        override def getNext: MyType = Random.nextLong().asInstanceOf[MyType]
      }
      val test = fake[Test]()
      test.whatever must not be 0L
    }
    "type mismatch" in {
      """
        fake[Person](, _.age -> "test")
      """ mustNot compile
    }
    "unknown faker implicit" in {
      case class Test(name: String, whatever: StringBuilder)
      """
        | fake[Test]()
      """ mustNot compile
    }
    "override property with no implicit" in {
      case class Test(name: String, whatever: StringBuilder)
      val strBuilder = new StringBuilder().append("whatever")
      val t = fake[Test](_.whatever -> strBuilder)
      t.whatever.toString() must be ("whatever")
    }

  }
}


