# shapeless-compat

Backport of various things from [shapeless](https://github.com/milessabin/shapeless) 2.3 to shapeless 2.2

[![Build Status](https://travis-ci.org/alexarchambault/shapeless-compat.svg)](https://travis-ci.org/alexarchambault/shapeless-compat)

Mainly for use by [argonaut-shapeless](https://github.com/alexarchambault/argonaut-shapeless), [scalacheck-shapeless](https://github.com/alexarchambault/scalacheck-shapeless), and [case-app](https://github.com/alexarchambault/case-app).

## Usage

Add to your `build.sbt`
```scala
resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies += "com.github.alexarchambault" %% "shapeless-compat" % "1.0.0-M2"
```

Adds `Lazy` (with `LazyExtensions`), `Strict`, `LowPriority` ([#514](https://github.com/milessabin/shapeless/pull/514)), `Cached`, `Widen`, `Default`, etc. in the `shapeless.compat` namespace.

shapeless-compat does not intend to be somehow source-compatible with
shapeless 2.3, it only provides substitutes for some shapeless 2.3 -specific
or -enhanced type classes, that can be used with shapeless 2.2. Beware that
some of them are now a bit out-of-sync with shapeless 2.3, but still
allow the same kind of things
(`Lazy` / `Strict` whose `LazyExtensions` were removed in shapeless 2.3
but not here, `LowPriority` which was made easier to use in shapeless 2.3 in
the mean time).


Released against both scala 2.10 and 2.11, and against scala JS too.

## License

Released under the Apache 2 license.
