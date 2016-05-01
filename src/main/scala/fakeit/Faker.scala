package fakeit

trait Faker[T] {
  def next: T
}
