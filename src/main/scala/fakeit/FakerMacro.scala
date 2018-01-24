package fakeit

import scala.collection.immutable.ListMap
import scala.reflect.macros.blackbox

object FakerMacro {

  def fake[T:c.WeakTypeTag](c: blackbox.Context)(args: c.Tree*) = {
    import c.universe._
    val t = c.weakTypeOf[T]
    val fields = getFields(c)(t)
    val defaults = getFieldsDefaultValues(c)(t)
    val caseClassName = getCaseClassTermName(c)(t)

    val overrideFakers = args.map {
      case q"(($i1) => scala.Predef.ArrowAssoc[$t]($i2.${prop: c.TermName}).$arrow[$v]($value))"
        if i1.symbol == i2.symbol => (prop.asInstanceOf[c.TermName], value)
      case t => c.abort(c.enclosingPosition, s"'$t' should be in the form of _.prop -> value")
    }.toMap

    val fakedProps = fields.map {
      case (name, fieldTpe: c.Type) =>
        val fakerTypeTag = fakerType(c)(c.WeakTypeTag(fieldTpe))
        lazy val getImplicitFakerForType = {
          val fakerOpt = c.inferImplicitValue(fakerTypeTag.tpe.asInstanceOf[c.Type])
          fakerOpt match {
            case EmptyTree => None
            case faker => Some(q"$faker.getNext")
          }
        }
        overrideFakers.get(name.asInstanceOf[c.TermName]).orElse(getImplicitFakerForType).orElse(defaults.getOrElse(name.asInstanceOf[c.TermName], None)).getOrElse{
          c.abort(c.enclosingPosition,
            s"""
               | Implicit for value ${fakerTypeTag.tpe.toString} missing. You should import an implicit for
               | type ${fakerTypeTag.tpe.toString} or override it with fake[$t](_.$name -> <your code>)
              """)
        }
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

  private def getFieldsDefaultValues(c: blackbox.Context)( tpe: c.Type ): ListMap[c.universe.TermName, Option[c.universe.Tree]] = {
    import c.universe._
    if(tpe.companion == NoType){
      ListMap()
    } else {
      ListMap( tpe.companion.member(TermName( "apply" )).asTerm.alternatives.find(_.isSynthetic).get.asMethod.paramLists.flatten.zipWithIndex.map {
        case ( field, i ) =>
          (
            field.name.toTermName,
            {
              val method = TermName( s"apply$$default$$${i + 1}" )
              tpe.companion.member( method ) match {
                case NoSymbol => None
                case _        => Some( q"${tpe.typeSymbol.companion}.$method" )
              }
            }
            )
      }: _*)
    }
  }
}
