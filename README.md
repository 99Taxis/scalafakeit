# Scala FakeIt
Scala FakeIt is a library for generating test data without boilerplate code.

Quite simple to use:

```scala
import fakeit._
import fakeit.ImplicitFakers._

case class Person(name: String, age: Int)

val fakedPerson = fake[Person]()
```

Also, you can override some properties with your own random generator.
```scala
import fakeit._
import fakeit.ImplicitFakers._

case class Person(name: String, age: Int)

val fakedPerson = fake[Person](_.name -> ("fakedName" + next[String]))
```

## License

`Scala FakeIt` is open source software released under the Apache 2.0 License.

See the [LICENSE](https://github.com/gustavoamigo/scalafakeit/blob/master/LICENSE) file for details.

