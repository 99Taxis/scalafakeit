package fakeit

import java.time.{ LocalDateTime, OffsetDateTime, ZonedDateTime }
import java.util.Date

import scala.util.Random

import fakeit.ImplicitFakers._
import org.scalatest._

class FakeItSpec extends FreeSpec with MustMatchers {

  case class Person(name: String, age:Int)
  "fake" - {
    "simple" in {
      val person = fake[Person]()
      person.name must not be ""
      person.age must not be 0
    }

    "override faker" in {
      val person = fake[Person](_.name -> "test121")
      person.name must be ("test121")
      person.age must not be 0
    }

    "override string faker" in {
      val person = fake[Person](_.name -> ("fakedName" + next[String]))
      person.name must startWith ("fakedName")
      person.age must not be 0
    }

    "override two fakers" in {
      val person = fake[Person](_.name -> "test121", _.age -> 2)
      person.name must be ("test121")
      person.age must be (2)
    }

    "custom implicit faker" in {
      trait MyTrait {}
      type MyType = Long with MyTrait
      case class Test(name: String, whatever: MyType)
      implicit val myTypeFaker: Faker[MyType] = new Faker[MyType] {
        override def getNext: MyType = next[Long].asInstanceOf[MyType]
      }
      val test = fake[Test]()
      test.whatever must not be 0L
    }

    "wrong format in faker overriders should not compile" in {
      val person1 = fake[Person]()
      """
          fake[Person](x => (person1.name -> "test121"))
      """ mustNot compile
    }

    "type mismatch should not compile" in {
      """
        fake[Person](_.age -> "test")
      """ mustNot compile
    }

    "unknown faker implicit should not compile" in {
      case class Test(name: String, whatever: StringBuilder)
      """
        fake[Test]()
      """ mustNot compile
    }

    "override faker with no implicit should compile" in {
      case class Test(name: String, whatever: StringBuilder)
      val strBuilder = new StringBuilder().append("whatever")
      val t = fake[Test](_.whatever -> strBuilder)
      t.whatever.toString() must be ("whatever")
    }

    "nested case classes with implicit faker" in {
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
    "list" in {
      case class Test(name: String, others: List[String])
      val t = fake[Test]()
      t.name must not be ""
      t.others foreach ( s => s must not be "")
    }
  }
  "next" - {


    "option" in {
      verifyRandom(() => next[Option[Int]].hashCode)
    }
    "iteratable" in {
      verifyRandom(() => next[Iterable[Int]].hashCode)
    }
    "seq" in {
      verifyRandom(() => next[Seq[Int]].hashCode)
    }
    "List" in {
      verifyRandom(() => next[List[Int]].hashCode)
    }
    "int" in {
      verifyRandom(() => next[Int].hashCode)
    }
    "string" in {
      verifyRandom(() => next[String].hashCode)
    }
    "bigDecimal" in {
      verifyRandom(() => next[BigDecimal].hashCode)
    }
    "boolean" in {
      verifyRandom(() => next[Boolean].hashCode)
    }
    "byte" in {
      verifyRandom(() => next[Byte].hashCode)
    }
    "short" in {
      verifyRandom(() => next[Short].hashCode)
    }
    "long" in {
      verifyRandom(() => next[Long].hashCode)
    }
    "float" in {
      verifyRandom(() => next[Float].hashCode)
    }
    "double" in {
      verifyRandom(() => next[Double].hashCode)
    }
    "localDateTime" in {
      verifyRandom(() => next[LocalDateTime].hashCode)
    }
    "offsetDateTime" in {
      verifyRandom(() => next[OffsetDateTime].hashCode)
    }
    "zonedDateTime" in {
      verifyRandom(() => next[ZonedDateTime].hashCode)
    }
    "date" in {
      verifyRandom(() => next[Date].hashCode)
    }

    "explicit faker" in {
      val name = new Faker[String] {
        val names = Array("Peter", "Paul", "Newman", "Amigo")
        override def getNext: String = "name" + names(Random.nextInt(names.length)) + " " + names(Random.nextInt(names.length))
      }
      val nextName = next(name)
      nextName must startWith("name")
    }


  }

  def verifyRandom(f: () => Int) = {
    val v1 = (1 to 100).map(_ => f()).hashCode()
    val v2 = (1 to 100).map(_ => f()).hashCode()
    v1 must not be v2
  }
}


