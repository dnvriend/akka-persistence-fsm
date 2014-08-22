organization := "com.github.dnvriend"

name := "akka-fsm"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.2"

libraryDependencies ++=
  {	val scalaV = scalaVersion.value
    val akkaV = "2.3.5"
    Seq(
      "org.scala-lang" % "scala-library" % scalaV,
      "com.typesafe.akka" %% "akka-actor" %  akkaV,
      "com.typesafe.akka" %% "akka-slf4j" % akkaV,
      "com.typesafe.akka" %% "akka-cluster" % akkaV,
      "com.typesafe.akka" %% "akka-contrib" % akkaV,
      "com.typesafe.akka"   %% "akka-persistence-experimental" % akkaV,
      "com.github.dnvriend" %% "akka-persistence-inmemory"  % "0.0.1" % "test",
      "com.typesafe.akka"   %% "akka-testkit"               % akkaV   % "test" withSources() withJavadoc(),
      "org.scalatest"       %% "scalatest"                  % "2.1.4" % "test" withSources() withJavadoc()
    )
  }

autoCompilerPlugins := true

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

publishMavenStyle := true

publishArtifact in Test := false

parallelExecution in Test := false