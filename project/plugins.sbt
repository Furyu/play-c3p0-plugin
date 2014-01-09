import sbt._

import Defaults._

ivyLoggingLevel := UpdateLogging.Full

// Comment to get more information during initialization
logLevel := Level.Info

resolvers ++= Seq(
    DefaultMavenRepository,
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    Classpaths.typesafeResolver
)

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0-SNAPSHOT")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.0.1")

// The repository for sbt-pgp plugin
resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.7")
