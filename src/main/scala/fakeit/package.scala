import scala.language.experimental.macros

package object fakeit {
  def fake[T](args: (T => (Any, Any))*):T = macro FakerMacro.fake[T]
}
