import sbt._
import Keys._
import play.sbt.PlayImport._
import com.typesafe.sbt.web.SbtWeb.autoImport.{Assets, pipelineStages}
import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.rjs.Import.{rjs, RjsKeys}
import com.typesafe.sbt.digest.Import.digest
import com.typesafe.sbt.gzip.Import.gzip

object Common {
	def appName = "play-multidomain-auth"
	
	// Common settings for every project
	def settings (theName: String) = Seq(
		name := theName,
		organization := "com.myweb",
		version := "1.0-SNAPSHOT",
		scalaVersion := "2.11.6",
		doc in Compile <<= target.map(_ / "none"),
		scalacOptions ++= Seq(
			"-feature",
			"-deprecation",
			"-unchecked",
			"-language:reflectiveCalls",
			"-language:postfixOps",
			"-language:implicitConversions"
		)
	)
	// Settings for the app, i.e. the root project
	val appSettings = settings(appName) ++: Seq(
		resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
	)
	// Settings for every module, i.e. for every subproject
	def moduleSettings (module: String) = settings(module) ++: Seq(
		javaOptions in Test += s"-Dconfig.resource=application.conf",
		resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
	)
	// Settings for every service, i.e. for admin and web subprojects
	def serviceSettings (module: String) = moduleSettings(module) ++: Seq(
		includeFilter in (Assets, LessKeys.less) := "*.less",
		excludeFilter in (Assets, LessKeys.less) := "_*.less",
		pipelineStages := Seq(rjs, digest, gzip),
		RjsKeys.mainModule := s"main-$module"
	)
	
	val commonDependencies = Seq(
		cache,
		ws,
    specs2 % Test,
    "com.typesafe.play" %% "play-mailer" % "3.0.1",
		"org.webjars" % "requirejs" % "2.1.19",
		"com.mohiva" %% "play-silhouette" % "3.0.0",
		"com.adrianhurt" %% "play-bootstrap3" % "0.4",	// Add bootstrap3 helpers and field constructors (http://play-bootstrap3.herokuapp.com/)
	  "net.codingwell" %% "scala-guice" % "4.0.0",
    "net.ceedubs" %% "ficus" % "1.1.2"
		// Add here more common dependencies:
		// jdbc,
		// anorm,
		// ...
	)
}