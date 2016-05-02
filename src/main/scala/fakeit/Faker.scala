package fakeit

trait Faker[T] {
  def getNext: T
}
