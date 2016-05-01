package fakeit

import scala.language.experimental.macros
import scala.util.Random
import scala.reflect.macros.whitebox.Context

trait Faker[T] {
  def getNext: T
}

object FakeIt {

  implicit val intFaker: Faker[Int] =  new Faker[Int] {
    override def getNext = Random.nextInt
  }

  implicit val stringFaker: Faker[String] =  new Faker[String] {
    override def getNext = Random.nextString(10)
  }

  def fake[T](args: (T => (Any, Any))*):T = macro fakeImpl[T]

  def fakeImpl[T:c.WeakTypeTag](c: Context)(args: c.Tree*) = {
    import c.universe._
    val t = implicitly[WeakTypeTag[T]].tpe
    val caseClassName = getCaseClassTermName(c)(t)

    val overrideFakers = args.map {
      case q"(($i1) => scala.this.Predef.ArrowAssoc[$t]($i2.${prop: c.TermName}).$arrow[$v]($value))"
        if i1.symbol == i2.symbol => (prop.asInstanceOf[c.TermName], value)
      case t @ _ => c.abort(c.enclosingPosition, s"'$t' should be in the form of _.prop -> value")
    }.toMap

    val fields = getFields(c)(t)
    val fakedProps = fields.map {
      case (name, fieldTpe: c.Type) =>
        lazy val getImplicitFakerForType = {
          val fakerTypeTag = fakerType(c)(c.WeakTypeTag(fieldTpe))
          val faker = c.inferImplicitValue(fakerTypeTag.tpe.asInstanceOf[c.Type])
          q"$faker.getNext"
        }
        overrideFakers.getOrElse(name.asInstanceOf[c.TermName], getImplicitFakerForType)
    }

    q"""
        $caseClassName(..$fakedProps)
     """
  }

  private def fakerType[T](c: Context)(implicit t: c.WeakTypeTag[T]) = c.weakTypeTag[Faker[T]]

  private def getCaseClassTermName(c:Context)(t:c.Type) = {
    import c.universe._
    val caseClass = t.asInstanceOf[TypeRef].sym.asClass
    caseClass.name.toTermName
  }

  private def getFields(c: Context)(tpe: c.Type) = {
    import c.universe._
    object CaseField {
      def unapply(termSymbol: c.universe.TermSymbol): Option[(c.universe.TermName, c.Type)] = {
        if(termSymbol.isVal && termSymbol.isCaseAccessor) {
          Some((TermName(termSymbol.name.toString.trim), termSymbol.typeSignature))
        } else {
          None
        }
      }
    }
    tpe.decls.collect {
      case CaseField(termName,tpe) =>
        (termName, tpe)
    }
  }
}
