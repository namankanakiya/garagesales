name := """Garage-Sales-Group-5"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean,
  SbtWeb)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  "org.avaje.ebeanorm" % "avaje-ebeanorm" % "4.6.2",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "com.itextpdf" % "itextpdf" % "5.4.2",
  "com.itextpdf.tool" % "xmlworker" % "5.4.1",
  "commons-io" % "commons-io" % "2.4",
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.0",
  javaWs
)
