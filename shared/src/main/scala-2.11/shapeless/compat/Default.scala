package shapeless.compat

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

import shapeless._
import shapeless.labelled.{ FieldType, field }

/**
 * Provides default values of case class-like types.
 *
 * The `Out` type parameter is an HList type whose length is the number of fields of `T`. Its elements correspond
 * to the fields of `T`, in their original order.  It is made of `None.type` (no default value for this field) and
 * `Some[...]` (default value available for this field, with `...` the type of the field). Note that `None.type` and
 * `Some[...]` are more precise than simply `Option[...]`, so that the availability of default values can be used
 * in type level calculations.
 *
 * The `apply` method returns an HList of type `Out`, with `None` elements corresponding to no default value available,
 * and `Some(defaultValue)` to default value available for the corresponding fields.
 *
 * Use like
 * {{{
 *   case class CC(i: Int, s: String = "b")
 *
 *   val default = Default[CC]
 *
 *   // default.Out is  None.type :: Some[String] :: HNil
 *
 *   // default() returns
 *   //   None :: Some("b") :: HNil,
 *   // typed as default.Out
 * }}}
 *
 * @author Alexandre Archambault
 */
trait Default[T] extends DepFn0 with Serializable {
  type Out <: HList
}

object Default {
  def apply[T](implicit default: Default[T]): Aux[T, default.Out] = default

  def mkDefault[T, Out0 <: HList](defaults: Out0): Aux[T, Out0] =
    new Default[T] {
      type Out = Out0
      def apply() = defaults
    }

  type Aux[T, Out0 <: HList] = Default[T] { type Out = Out0 }

  implicit def materialize[T, L <: HList]: Aux[T, L] = macro DefaultMacros.materialize[T, L]


  /**
   * Provides default values of case class-like types, as a record.
   *
   * Type `Out` is a record type, having one element per field with a default value. Labels
   * come from the available `DefaultSymbolicLabelling[T]`, and values are the default values
   * themselves.
   *
   * Method `apply` provides the record of default values, typed as `Out`.
   *
   * Example
   * {{{
   *   case class CC(i: Int, s: String = "b")
   *
   *   val default = Default.AsRecord[CC]
   *
   *   // default.Out is  Record.`'s -> String`.T
   *   // default() returns Record(s = "b")
   * }}}
   *
   * @author Alexandre Archambault
   */
  trait AsRecord[T] extends DepFn0 with Serializable {
    type Out <: HList
  }

  object AsRecord {
    def apply[T](implicit default: AsRecord[T]): Aux[T, default.Out] = default

    type Aux[T, Out0 <: HList] = AsRecord[T] { type Out = Out0 }

    trait Helper[L <: HList, Labels <: HList] extends DepFn1[L] with Serializable {
      type Out <: HList
    }

    object Helper {
      def apply[L <: HList, Labels <: HList](implicit helper: Helper[L, Labels]): Aux[L, Labels, helper.Out] = helper

      type Aux[L <: HList, Labels <: HList, Out0 <: HList] = Helper[L, Labels] { type Out = Out0 }

      implicit def hnilHelper: Aux[HNil, HNil, HNil] =
        new Helper[HNil, HNil] {
          type Out = HNil
          def apply(l: HNil) = HNil
        }

      implicit def hconsSomeHelper[K <: Symbol, H, T <: HList, LabT <: HList, OutT <: HList]
       (implicit
         tailHelper: Aux[T, LabT, OutT]
       ): Aux[Some[H] :: T, K :: LabT, FieldType[K, H] :: OutT] =
        new Helper[Some[H] :: T, K :: LabT] {
          type Out = FieldType[K, H] :: OutT
          def apply(l: Some[H] :: T) = field[K](l.head.x) :: tailHelper(l.tail)
        }

      implicit def hconsNoneHelper[K <: Symbol, T <: HList, LabT <: HList, OutT <: HList]
       (implicit
         tailHelper: Aux[T, LabT, OutT]
       ): Aux[None.type :: T, K :: LabT, OutT] =
        new Helper[None.type :: T, K :: LabT] {
          type Out = OutT
          def apply(l: None.type :: T) = tailHelper(l.tail)
        }
    }

    implicit def asRecord[T, Labels <: HList, Options <: HList, Rec <: HList]
     (implicit
       default: Default.Aux[T, Options],
       labelling: DefaultSymbolicLabelling.Aux[T, Labels],
       helper: Helper.Aux[Options, Labels, Rec]
     ): Aux[T, Rec] =
      new AsRecord[T] {
        type Out = Rec
        def apply() = helper(default())
      }
  }


  /**
   * Provides default values of case class-like types, as a HList of options.
   *
   * Unlike `Default`, `Out` is made of elements like `Option[...]` instead of `None.type` and `Some[...]`.
   * Thus, the availability of default values cannot be checked through types, only through values (via the `apply`
   * method).
   *
   * This representation can be more convenient to deal with when one only check the default values at run-time.
   *
   * Method `apply` provides the HList of default values, typed as `Out`.
   *
   * Example
   * {{{
   *   case class CC(i: Int, s: String = "b")
   *
   *   val default = Default.AsOptions[CC]
   *
   *   // default.Out is  Option[Int] :: Option[String] :: HNil
   *   // default() returns
   *   //   None :: Some("b") :: HNil
   *   // typed as default.Out
   * }}}
   *
   * @author Alexandre Archambault
   */
  trait AsOptions[T] extends DepFn0 with Serializable {
    type Out <: HList
  }

  object AsOptions {
    def apply[T](implicit default: AsOptions[T]): Aux[T, default.Out] = default

    type Aux[T, Out0 <: HList] = AsOptions[T] { type Out = Out0 }

    trait Helper[L <: HList, Repr <: HList] extends DepFn1[L] with Serializable {
      type Out <: HList
    }

    object Helper {
      def apply[L <: HList, Repr <: HList](implicit helper: Helper[L, Repr]): Aux[L, Repr, helper.Out] = helper

      type Aux[L <: HList, Repr <: HList, Out0 <: HList] = Helper[L, Repr] { type Out = Out0 }

      implicit def hnilHelper: Aux[HNil, HNil, HNil] =
        new Helper[HNil, HNil] {
          type Out = HNil
          def apply(l: HNil) = HNil
        }

      implicit def hconsSomeHelper[H, T <: HList, ReprT <: HList, OutT <: HList]
       (implicit
         tailHelper: Aux[T, ReprT, OutT]
       ): Aux[Some[H] :: T, H :: ReprT, Option[H] :: OutT] =
        new Helper[Some[H] :: T, H :: ReprT] {
          type Out = Option[H] :: OutT
          def apply(l: Some[H] :: T) = l.head :: tailHelper(l.tail)
        }

      implicit def hconsNoneHelper[H, T <: HList, ReprT <: HList, OutT <: HList]
       (implicit
         tailHelper: Aux[T, ReprT, OutT]
       ): Aux[None.type :: T, H :: ReprT, Option[H] :: OutT] =
        new Helper[None.type :: T, H :: ReprT] {
          type Out = Option[H] :: OutT
          def apply(l: None.type :: T) = None :: tailHelper(l.tail)
        }
    }

    implicit def asOption[T, Repr <: HList, Options <: HList, Out0 <: HList]
     (implicit
       default: Default.Aux[T, Options],
       gen: Generic.Aux[T, Repr],
       helper: Helper.Aux[Options, Repr, Out0]
     ): Aux[T, Out0] =
      new AsOptions[T] {
        type Out = Out0
        def apply() = helper(default())
      }
  }
}

class DefaultMacros(val c: whitebox.Context) extends CaseClassMacros {
  import c.universe._

  def someTpe = typeOf[Some[_]].typeConstructor
  def noneTpe = typeOf[None.type]

  def anyRefTpe = typeOf[AnyRef]

  private def patchedCompanionSymbolOf(original: Symbol): Symbol = {
    // see https://github.com/scalamacros/paradise/issues/7
    // also see https://github.com/scalamacros/paradise/issues/64

    val global = c.universe.asInstanceOf[scala.tools.nsc.Global]
    val typer = c.asInstanceOf[reflect.macros.runtime.Context].callsiteTyper.asInstanceOf[global.analyzer.Typer]
    val ctx = typer.context
    val owner = original.owner

    import global.analyzer.Context

    original.companionSymbol.orElse {
      import global.{ abort => aabort, _ }
      implicit class PatchedContext(ctx: Context) {
        trait PatchedLookupResult { def suchThat(criterion: Symbol => Boolean): Symbol }
        def patchedLookup(name: Name, expectedOwner: Symbol) = new PatchedLookupResult {
          override def suchThat(criterion: Symbol => Boolean): Symbol = {
            var res: Symbol = NoSymbol
            var ctx = PatchedContext.this.ctx
            while (res == NoSymbol && ctx.outer != ctx) {
              // NOTE: original implementation says `val s = ctx.scope lookup name`
              // but we can't use it, because Scope.lookup returns wrong results when the lookup is ambiguous
              // and that triggers https://github.com/scalamacros/paradise/issues/64
              val s = {
                val lookupResult = ctx.scope.lookupAll(name).filter(criterion).toList
                lookupResult match {
                  case Nil => NoSymbol
                  case List(unique) => unique
                  case _ => aabort(s"unexpected multiple results for a companion symbol lookup for $original#{$original.id}")
                }
              }
              if (s != NoSymbol && s.owner == expectedOwner)
                res = s
              else
                ctx = ctx.outer
            }
            res
          }
        }
      }
      ctx.patchedLookup(original.asInstanceOf[global.Symbol].name.companionName, owner.asInstanceOf[global.Symbol]).suchThat(sym =>
        (original.isTerm || sym.hasModuleFlag) &&
          (sym isCoDefinedWith original.asInstanceOf[global.Symbol])
      ).asInstanceOf[c.universe.Symbol]
    }
  }

  private def patchedCompanionRef(tpe: Type): Tree = {
    val global = c.universe.asInstanceOf[scala.tools.nsc.Global]
    val gTpe = tpe.asInstanceOf[global.Type]
    val pre = gTpe.prefix
    val cSym = patchedCompanionSymbolOf(tpe.typeSymbol).asInstanceOf[global.Symbol]
    if(cSym != NoSymbol)
      global.gen.mkAttributedRef(pre, cSym).asInstanceOf[Tree]
    else
      Ident(tpe.typeSymbol.name.toTermName) // Attempt to refer to local companion
  }


  def materialize[T: WeakTypeTag, L: WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]

    if (tpe =:= anyRefTpe || !isCaseClassLike(classSym(tpe)))
      abort(s"$tpe is not a case class or case class like")

    lazy val companion = patchedCompanionRef(tpe)

    // Fixes https://github.com/milessabin/shapeless/issues/474
    tpe.typeSymbol.companion.info.members

    def methodFrom(tpe: Type, name: String): Option[Symbol] = {
      val m = tpe.member(TermName(name))
      if (m == NoSymbol)
        None
      else
        Some(m)
    }

    def wrapTpeTree(idx: Int, argTpe: Type) = {
      val methodOpt = methodFrom(tpe.companion, s"apply$$default$$${idx + 1}")
        .orElse(methodFrom(companion.symbol.info, s"$$lessinit$$greater$$default$$${idx + 1}"))

      methodOpt match {
        case Some(method) =>
          (appliedType(someTpe, argTpe), q"_root_.scala.Some($companion.$method)")
        case None =>
          (noneTpe, q"_root_.scala.None")
      }
    }

    val wrapTpeTrees = fieldsOf(tpe).zipWithIndex.map {case ((_, argTpe), idx) =>
      wrapTpeTree(idx, devarargify(argTpe))
    }

    val resultTpe = mkHListTpe(wrapTpeTrees.map { case (wrapTpe, _) => wrapTpe })

    val resultTree = wrapTpeTrees.foldRight(q"_root_.shapeless.HNil": Tree) { case ((_, value), acc) =>
      q"_root_.shapeless.::($value, $acc)"
    }

    q"_root_.shapeless.compat.Default.mkDefault[$tpe, $resultTpe]($resultTree)"
  }
}
