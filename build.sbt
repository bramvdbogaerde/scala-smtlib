enablePlugins(GitVersioning)

lazy val root = project.in(file(".")).
   aggregate(smtlib.js, smtlib.jvm).
   settings(
      scalaVersion := "3.0.2",
      publish := {},
      publishLocal := {}
   )

lazy val smtlib = crossProject(JSPlatform, JVMPlatform)
   .in(file("code"))
   .settings(
     organization := "com.regblanc",
     name := "scala-smtlib",
     scalaVersion := "3.0.2",
     crossScalaVersions := Seq(),
     scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
     scalacOptions ++= {
        val Seq(_, major, minor) = (scalaVersion in ThisBuild).value.split("\\.").toSeq.map(_.toInt)
        if (major <= 10 || (major == 11 && minor < 5)) Seq.empty
        else Seq("-Ypatmat-exhaust-depth", "40")
     },
     libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11" % "test"
   ).jsSettings(
      Compile / npmDependencies ++= Seq(
       "z3-solver" -> "4.8.14-pre" 
     )
   ).jsConfigure(_.enablePlugins(ScalablyTypedConverterPlugin))


git.useGitDescribe := true



//javaOptions in IntegrationTest ++= Seq("-Xss128M")
//
//fork in IntegrationTest := true


//logBuffered in IntegrationTest := false
//
//parallelExecution in Test := true
//
//publishMavenStyle := true
//
//publishArtifact in Test := false
//
//publishTo := {
//  val nexus = "https://oss.sonatype.org/"
//  if(version.value.trim.endsWith("SNAPSHOT"))
//    Some("snapshots" at nexus + "content/repositories/snapshots")
//  else
//    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
//}
//
//pomIncludeRepository := { _ => false }
//
//licenses := Seq("MIT-style" -> url("https://opensource.org/licenses/MIT"))
//
//pomExtra := (
//  <url>https://github.com/regb/scala-smtlib</url>
//    <developers>
//      <developer>
//        <id>reg</id>
//        <name>Regis Blanc</name>
//        <url>http://regblanc.com</url>
//      </developer>
//    </developers>
//)
