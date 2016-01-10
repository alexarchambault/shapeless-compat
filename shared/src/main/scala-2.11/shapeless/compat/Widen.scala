package shapeless.compat

import scala.language.experimental.macros

import scala.reflect.macros.whitebox

import shapeless._


trait Widen[T] extends DepFn1[T] { type Out >: T }

object Widen {
  def apply[T](implicit widen: Widen[T]): Aux[T, widen.Out] = widen

  type Aux[T, Out0 >: T] = Widen[T] { type Out = Out0 }

  def instance[T, Out0 >: T](f: T => Out0): Aux[T, Out0] =
    new Widen[T] {
      type Out = Out0
      def apply(t: T) = f(t)
    }

  implicit def materialize[T, Out]: Aux[T, Out] = macro WidenMacros.materializeWiden[T, Out]
}

class WidenMacros(val c: whitebox.Context) extends SingletonTypeUtils {
  import c.universe.{ Symbol => _, _ }

  def symbolTpe = typeOf[Symbol]

  def materializeWiden[T: WeakTypeTag, Out: WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T].normalize

    val widenTpe = tpe match {
      case SingletonSymbolType(s) => symbolTpe
      case _ => tpe.widen
    }

    if (widenTpe =:= tpe)
      c.abort(c.enclosingPosition, s"Don't know how to widen $tpe")
    else
      q"_root_.shapeless.compat.Widen.instance[$tpe, $widenTpe](x => x)"
  }
}

