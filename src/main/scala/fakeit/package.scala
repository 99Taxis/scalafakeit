import scala.language.experimental.macros

package object fakeit {
  def next[T](implicit faker: Faker[T]):T = faker.getNext
  def fake[T](args: (T => (Any, Any))*):T = macro FakerMacro.fake[T]
}
