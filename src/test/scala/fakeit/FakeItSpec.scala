package fakeit

import org.scalatest._
import fakeit._

import scala.util.Random
import fakeit.ImplicitFakers._

class FakeItSpec extends FreeSpec with MustMatchers {

  case class Person(name: String, age:Int)
  "fake" - {
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
      val person = fake[Person](_.name -> ("fakedName" + next[String]))
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
        override def getNext: MyType = next[Long].asInstanceOf[MyType]
      }
      val test = fake[Test]()
      test.whatever must not be 0L
    }

    "type mismatch" in {
      """
        fake[Person](_.age -> "test")
      """ mustNot compile
    }

    "unknown faker implicit" in {
      case class Test(name: String, whatever: StringBuilder)
      """
        fake[Test]()
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
        override def getNext: Person = fake[Person]()
      }
      val parent = fake[Parent]()
      parent must not be ""
    }

    "non case class should not compile" in {
      class Test(name: String)
      """
        fake[Test]()
      """ mustNot compile
    }
    "option" in {
      case class Test(name: Option[String])
      val t = fake[Test]()
    }
    "var" in {
      case class Test(var name: Option[String])
      val t = fake[Test](_.name -> Some("testVar"))
      t.name must be (Some("testVar"))
    }
  }
  "next" - {
    "next String" in {
      val str = next[String]
      str must not be ""
    }
    "next Int" in {
      val i = next[Int]
      i must not be 0
    }
    "next Name" in {
      val name = new Faker[String] {
        val names = Array("Peter", "Paul", "Newman", "Amigo")
        override def getNext: String = "name" + names(Random.nextInt(names.length)) + " " + names(Random.nextInt(names.length))
      }
      val nextName = next(name)
      nextName must startWith("name")
    }


  }
}


