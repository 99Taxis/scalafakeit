package fakeit

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object FakerMacro {

  def fake[T:c.WeakTypeTag](c: blackbox.Context)(args: c.Tree*) = {
    import c.universe._
    val t = implicitly[WeakTypeTag[T]].tpe
    val caseClassName = getCaseClassTermName(c)(t)

    val overrideFakers = args.map {
      case q"(($i1) => scala.this.Predef.ArrowAssoc[$t]($i2.${prop: c.TermName}).$arrow[$v]($value))"
        if i1.symbol == i2.symbol => (prop.asInstanceOf[c.TermName], value)
      case t => c.abort(c.enclosingPosition, s"'$t' should be in the form of _.prop -> value")
    }.toMap

    val fields = getFields(c)(t)
    val fakedProps = fields.map {
      case (name, fieldTpe: c.Type) =>
        lazy val getImplicitFakerForType = {
          val fakerTypeTag = fakerType(c)(c.WeakTypeTag(fieldTpe))
          val fakerOpt = c.inferImplicitValue(fakerTypeTag.tpe.asInstanceOf[c.Type])
          fakerOpt match {
            case EmptyTree => c.abort(c.enclosingPosition,
              s"""
               | Implicit for type ${fakerTypeTag.tpe.toString} missing. You should import an implicit for
               | type ${fakerTypeTag.tpe.toString} or override it with fake[$t](_.$name -> <your code>)
              """)
            case faker => q"$faker.getNext"
          }
        }
        overrideFakers.getOrElse(name.asInstanceOf[c.TermName], getImplicitFakerForType)
    }

    q"$caseClassName(..$fakedProps)"
  }

  private def fakerType[T](c: blackbox.Context)(implicit t: c.WeakTypeTag[T]) = c.weakTypeTag[Faker[T]]

  private def getCaseClassTermName(c:blackbox.Context)(t:c.Type) = {
    if(!t.typeSymbol.isClass || !t.typeSymbol.asClass.isCaseClass) {
      c.abort(c.enclosingPosition, s"${t.typeSymbol.fullName} must be a case class")
    } else {
      val caseClass = t.asInstanceOf[c.universe.TypeRef].sym.asClass
      caseClass.name.toTermName
    }
  }

  private def getFields(c: blackbox.Context)(tpe: c.Type) = {
    import c.universe._
    object CaseField {
      def unapply(termSymbol: c.universe.TermSymbol): Option[(c.universe.TermName, c.Type)] = {
        if((termSymbol.isVal || termSymbol.isVar)  && termSymbol.isCaseAccessor) {
          Some((TermName(termSymbol.name.toString.trim), termSymbol.typeSignature))
        } else {
          None
        }
      }
    }
    tpe.decls.collect {
      case CaseField(termName,fieldTpe) =>
        (termName, fieldTpe)
    }
  }
}
