package shapeless.compat

import scala.language.experimental.macros

import java.util.regex.Pattern

import scala.reflect.macros.{ whitebox, TypecheckException }

/**
 * A utility which ensures that a code fragment does not typecheck.
 *
 * Credit: Stefan Zeiger (@StefanZeiger)
 */
object IllTyped {
  def apply(code: String): Unit = macro IllTypedMacros.applyImplNoExp
  def apply(code: String, expected: String): Unit = macro IllTypedMacros.applyImpl
}

class IllTypedMacros(val c: whitebox.Context) {
  import c.universe._

  def applyImplNoExp(code: Tree): Tree = applyImpl(code, null)

  def applyImpl(code: Tree, expected: Tree): Tree = {
    import c.universe._

    val Literal(Constant(codeStr: String)) = code
    val (expPat, expMsg) = expected match {
      case null => (null, "Expected some error.")
      case Literal(Constant(s: String)) =>
        (Pattern.compile(s, Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "Expected error matching: "+s)
    }

    try {
      val dummy = TermName(c.freshName)
      c.typecheck(c.parse(s"{ val $dummy = { $codeStr } ; () }"))
      c.abort(c.enclosingPosition, "Type-checking succeeded unexpectedly.\n"+expMsg)
    } catch {
      case e: TypecheckException =>
        val msg = e.getMessage
        if((expected ne null) && !(expPat.matcher(msg)).matches)
          c.abort(c.enclosingPosition, "Type-checking failed in an unexpected way.\n"+expMsg+"\nActual error: "+msg)
    }

    q"()"
  }
}
