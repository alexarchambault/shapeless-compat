package shapeless.compat

import org.junit.Test

import shapeless.{ ::, HNil }

import scala.annotation.StaticAnnotation

object AnnotationTestsDefinitions {

  case class First() extends StaticAnnotation
  case class Second(i: Int, s: String) extends StaticAnnotation

  case class Other() extends StaticAnnotation
  case class Last(b: Boolean) extends StaticAnnotation

  case class Unused()

  @Other case class CC(
    @First i: Int,
    s: String,
    @Second(2, "b") ob: Option[Boolean]
  )

  @Last(true) trait Something

  sealed trait Base
  @First case class BaseI(i: Int) extends Base
  @Second(3, "e") case class BaseS(s: String) extends Base

  trait Dummy

}

class AnnotationTests {
  import AnnotationTestsDefinitions._

  def simpleAnnotation {
    {
      val other = Annotation[Other, CC].apply()
      assert(other == Other())

      val last = Annotation[Last, Something].apply()
      assert(last == Last(true))
    }

    {
      val other: Other = Annotation[Other, CC].apply()
      assert(other == Other())

      val last: Last = Annotation[Last, Something].apply()
      assert(last == Last(true))
    }
  }

  @Test
  def invalidAnnotation {
    IllTyped(" Annotation[Other, Dummy] ", "could not find implicit value for parameter annotation: .*")
    IllTyped(" Annotation[Dummy, CC] ", "could not find implicit value for parameter annotation: .*")
  }

  @Test
  def simpleAnnotations {
    {
      val first: Some[First] :: None.type :: None.type :: HNil = Annotations[First, CC].apply()
      assert(first == Some(First()) :: None :: None :: HNil)

      val second: None.type :: None.type :: Some[Second] :: HNil = Annotations[Second, CC].apply()
      assert(second == None :: None :: Some(Second(2, "b")) :: HNil)

      val unused: None.type :: None.type :: None.type :: HNil = Annotations[Unused, CC].apply()
      assert(unused == None :: None :: None :: HNil)

      val firstSum: Some[First] :: None.type :: HNil = Annotations[First, Base].apply()
      assert(firstSum == Some(First()) :: None :: HNil)

      val secondSum: None.type :: Some[Second] :: HNil = Annotations[Second, Base].apply()
      assert(secondSum == None :: Some(Second(3, "e")) :: HNil)
    }

    {
      val first = Annotations[First, CC].apply()
      assert(first == Some(First()) :: None :: None :: HNil)

      val second = Annotations[Second, CC].apply()
      assert(second == None :: None :: Some(Second(2, "b")) :: HNil)

      val unused = Annotations[Unused, CC].apply()
      assert(unused == None :: None :: None :: HNil)

      val firstSum = Annotations[First, Base].apply()
      assert(firstSum == Some(First()) :: None :: HNil)

      val secondSum = Annotations[Second, Base].apply()
      assert(secondSum == None :: Some(Second(3, "e")) :: HNil)
    }
  }

  @Test
  def invalidAnnotations {
    IllTyped(" Annotations[Dummy, CC] ", "could not find implicit value for parameter annotations: .*")
    IllTyped(" Annotations[Dummy, Base] ", "could not find implicit value for parameter annotations: .*")
    IllTyped(" Annotations[Second, Dummy] ", "could not find implicit value for parameter annotations: .*")
  }

}
