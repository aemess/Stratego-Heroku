

name := "stratego-backend"

version := "5.0.0"

herokuAppName in Compile := "stratego-backend"
herokuJdkVersion in Compile := "1.8"

scalaVersion := "2.12.7"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += guice

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

libraryDependencies += "com.h2database" % "h2" % "1.4.196"

libraryDependencies += "org.scala-lang.modules" % "scala-swing_2.12" % "2.0.3"

libraryDependencies += "com.google.inject" % "guice" % "4.2.3"

libraryDependencies += "net.codingwell" %% "scala-guice" % "4.2.10"

libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.12" % "1.0.6"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.6"

libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.3.0"


lazy val root = (project in file(".")).enablePlugins(PlayScala)

enablePlugins(JavaAppPackaging)


