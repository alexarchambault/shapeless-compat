package shapeless.compat

import org.junit.Assert._

object TestUtil {
  def assertTypedEquals[A](expected: A, actual: A): Unit = assertEquals(expected, actual)

  def assertTypedSame[A](expected: A, actual: A): Unit = assertSame(expected, actual)
}
