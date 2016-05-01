package fakeit

import org.scalatest._
import fakeit._

import scala.util.Random
import fakeit.ImplicitFakers._

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

    "two parameters" in {
      val person = fake[Person](_.name -> "test121", _.age -> 2)
      person.name must be ("test121")
      person.age must be (2)
    }

    "own implicit for my own type" in {
      trait MyTrait {}
      type MyType = Long with MyTrait
      case class Test(name: String, whatever: MyType)
      implicit val myTypeFaker: Faker[MyType] = new Faker[MyType] {
        override def next: MyType = Random.nextLong().asInstanceOf[MyType]
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

    "nested case classes" in {
      case class Parent(name: String, child: Person)
      implicit val personFaker: Faker[Person] = new Faker[Person] {
        override def next: Person = fake[Person]()
      }
      val parent = fake[Parent]()
      parent must not be ""
    }
  }
  "OptionFaker" - {
    "simple" in {
      case class Test(name: Option[String])
      val t = fake[Test]()
    }
  }
}


