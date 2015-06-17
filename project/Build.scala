/*
 * Copyright (C) 2013 FURYU CORPORATION
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
import sbt._
import Keys._

object ApplicationBuild extends Build {

  val appOrganization = "jp.furyu"
  val appName         = "play-c3p0-plugin"
  val appVersion      = "0.3"
  val appScalaVersion = "2.11.1"
  val appScalaCrossVersions = Seq(appScalaVersion, "2.10.4")

  val main = Project(appName, base = file(".")).settings(
    organization := appOrganization,
    version := appVersion,
    scalaVersion := appScalaVersion,
    crossScalaVersions := appScalaCrossVersions,
    resolvers ++= Seq(
      Resolver.typesafeRepo("releases")
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-jdbc" % "2.3.9",
      "com.mchange" % "c3p0" % "0.9.5"
    ),
    publishMavenStyle := true,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) {
        Some("snapshots" at nexus + "content/repositories/snapshots")
      } else {
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      }
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra := (
      <url>https://github.com/Furyu/play-c3p0-plugin</url>
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:Furyu/play-c3p0-plugin.git</url>
        <connection>scm:git:git@github.com:Furyu/play-c3p0-plugin.git</connection>
      </scm>
      <developers>
        <developer>
          <id>flysheep1980</id>
          <name>flysheep1980</name>
          <url>https://github.com/flysheep1980</url>
        </developer>
      </developers>
    )
  ).settings(appScalariformSettings)

  private lazy val appScalariformSettings = {
    import com.typesafe.sbt.SbtScalariform
    import scalariform.formatter.preferences._

    SbtScalariform.scalariformSettings ++ Seq(
      SbtScalariform.ScalariformKeys.preferences := FormattingPreferences()
        .setPreference(IndentWithTabs, false)
        .setPreference(DoubleIndentClassDeclaration, true)
        .setPreference(PreserveDanglingCloseParenthesis, true)
    )
  }

}
