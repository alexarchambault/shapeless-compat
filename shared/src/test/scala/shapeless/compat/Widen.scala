package shapeless.compat

import org.junit.Test
import org.junit.Assert._

import shapeless._
import shapeless.test._

import shapeless.compat.TestUtil._

package SingletonTypeTestsDefns {
  class ValueTest(val x: Int) extends AnyVal
}

class SingletonTypesTests {
  import SingletonTypeTestsDefns._
  import syntax.singleton._

  val wTrue = Witness(true)
  type True = wTrue.T
  val wFalse = Witness(false)
  type False = wFalse.T

  val w0 = Witness(0)
  type _0 = w0.T
  val w1 = Witness(1)
  type _1 = w1.T
  val w2 = Witness(2)
  type _2 = w2.T
  val w3 = Witness(3)
  type _3 = w3.T

  val wFoo = Witness('foo)
  type Foo = wFoo.T
  val wBar = Witness('bar)
  type Bar = wBar.T

  @Test
  def primitiveWiden {
    {
      val w = Widen[Witness.`2`.T]
      IllTyped(" w(3) ", "type mismatch;.*")
      val n = w(2)
      val n0: Int = n
      IllTyped(" val n1: Witness.`2`.T = n ", "type mismatch;.*")

      assertTypedEquals[Int](2, n)
    }

    {
      val w = Widen[Witness.`true`.T]
      IllTyped(" w(false) ", "type mismatch;.*")
      val b = w(true)
      val b0: Boolean = b
      IllTyped(" val b1: Witness.`true`.T = b ", "type mismatch;.*")

      assertTypedEquals[Boolean](true, b)
    }

    {
      val w = Widen[Witness.`"ab"`.T]
      IllTyped(""" w("s") """, "type mismatch;.*")
      val s = w("ab")
      val s0: String = s
      IllTyped(""" val s1: Witness.`"ab"`.T = s """, "type mismatch;.*")

      assertTypedEquals[String]("ab", s)
    }
  }

  @Test
  def symbolWiden {
    // Masks shapeless.syntax.singleton.narrowSymbol.
    // Having it in scope makes the IllTyped tests fail in an unexpected way.
    def narrowSymbol = ???

    val w = Widen[Witness.`'ab`.T]
    IllTyped(" w('s.narrow) ", "type mismatch;.*")
    val s = w('ab.narrow)
    val s0: Symbol = s
    IllTyped(" val s1: Witness.`'ab`.T = s ", "type mismatch;.*")

    assertTypedEquals[Symbol]('ab, s)
  }

  @Test
  def aliasWiden {
    type T = Witness.`2`.T
    val w = Widen[T]
    IllTyped(" w(3) ", "type mismatch;.*")
    val n = w(2)
    val n0: Int = n
    IllTyped(" val n1: Witness.`2`.T = n ", "type mismatch;.*")

    assertTypedEquals[Int](2, n)
  }


  trait B
  case object A extends B

  @Test
  def singletonWiden {
    IllTyped(" Widen[A.type] ", "could not find implicit value for parameter widen:.*")
  }

}
