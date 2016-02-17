import com.typesafe.sbt.pgp.PgpKeys

lazy val root = project.in(file("."))
  .aggregate(shapelessCompatJVM, shapelessCompatJS)
  .settings(
    name := "shapeless-compat-root"
  )
  .settings(commonSettings)
  .settings(compileSettings)
  .settings(noPublishSettings)

lazy val shapelessCompat = crossProject.in(file("."))
  .settings(
    name := "shapeless-compat"
  )
  .settings(commonSettings: _*)
  .settings(compileSettings: _*)
  .jsSettings(scalaJSStage in Test := FastOptStage)

lazy val shapelessCompatJVM = shapelessCompat.jvm
lazy val shapelessCompatJS = shapelessCompat.js

lazy val commonSettings = Seq(
  organization := "com.github.alexarchambault"
) ++ publishSettings

lazy val compileSettings = Seq(
  scalaVersion := "2.11.7",
  crossScalaVersions := Seq("2.10.6", "2.11.7"),
  unmanagedSourceDirectories in Compile += (baseDirectory in Compile).value / ".." / "shared" / "src" / "main" / s"scala-${scalaBinaryVersion.value}",
  libraryDependencies ++= Seq(
    "com.chuusai" %%% "shapeless" % "2.2.5",
    "com.novocode" % "junit-interface" % "0.7" % "test",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
  ),
  libraryDependencies ++= {
    if (scalaVersion.value.startsWith("2.10.")) Seq(
      compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
    ) else Nil
  },
  scalacOptions += "-target:jvm-1.6"
)

lazy val publishSettings = Seq(
  homepage := Some(url("https://github.com/alexarchambault/shapeless-compat")),
  licenses := Seq(
    "Apache 2.0" -> url("http://opensource.org/licenses/Apache-2.0")
  ),
  scmInfo := Some(ScmInfo(
    url("https://github.com/alexarchambault/shapeless-compat.git"),
    "scm:git:github.com/alexarchambault/shapeless-compat.git",
    Some("scm:git:git@github.com:alexarchambault/shapeless-compat.git")
  )),
  developers := List(Developer(
    "alexarchambault",
    "Alexandre Archambault",
    "",
    url("https://github.com/alexarchambault")
  )),
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  publishTo := Some {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      "snapshots" at nexus + "content/repositories/snapshots"
    else
      "releases" at nexus + "service/local/staging/deploy/maven2"
  },
  credentials += {
    Seq("SONATYPE_USER", "SONATYPE_PASS").map(sys.env.get) match {
      case Seq(Some(user), Some(pass)) =>
        Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)
      case _ =>
        Credentials(Path.userHome / ".ivy2" / ".credentials")
    }
  }
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)
