val scala3Version = "3.4.2"

lazy val root = project
  .in(file("."))
  .settings(
    name                 := "favs",
    version              := "0.1.0-SNAPSHOT",
    scalaVersion         := scala3Version,
    scalacOptions        := Seq("-Ykind-projector"),
    Compile / run / fork := true,
    libraryDependencies ++= Seq(
      "org.tpolecat"          %% "skunk-core"      % "0.6.4",  // Postgres
      "com.lihaoyi"           %% "scalatags"       % "0.13.1", // HTML Tags
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.7", // Config
      "org.atnos"             %% "eff"             % "7.0.4",
      "org.atnos"             %% "eff-cats-effect" % "7.0.4",
    ),
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
  )
