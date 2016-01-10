# shapeless-compat

Backport of various things from the current development version of [shapeless](https://github.com/milessabin/shapeless) (2.3.0-SNAPSHOT) to its current stable version (2.2)

[![Build Status](https://travis-ci.org/alexarchambault/shapeless-compat.svg)](https://travis-ci.org/alexarchambault/shapeless-compat)

Mainly for use by [argonaut-shapeless](https://github.com/alexarchambault/argonaut-shapeless), [scalacheck-shapeless](https://github.com/alexarchambault/scalacheck-shapeless), and [case-app](https://github.com/alexarchambault/case-app).

## Usage

Add to your `build.sbt`
```scala
resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies += "com.github.alexarchambault" %% "shapeless-compat" % "0.1.0-SNAPSHOT"
```

Adds `Lazy` (with `LazyExtensions`), `Strict`, `LowPriority`, `Cached`, `Widen`, `Default`, etc. in the `shapeless.compat` namespace.

Released against both scala 2.10 and 2.11, and against scala JS too.

## License

Released under the Apache 2 license.
