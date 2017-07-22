name := "TennisEventSource"

version := "1.0"

scalaVersion := "2.12.1"

val mainDependencies = Seq(
  "com.typesafe.akka" %% "akka-persistence" % "2.5.3"
)

val eventStoreDependencies = Seq(
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
)

libraryDependencies ++= mainDependencies ++ eventStoreDependencies