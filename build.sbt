// --- build.sbt (root project) ---
// Ce fichier build.sbt est la configuration racine pour la configuration multi-projets.
// Il définit les paramètres généraux du projet et agrège les sous-projets.

lazy val commonSettings = Seq(
  scalaVersion := "3.3.6", // Mise à jour vers Scala 3.3.6
  scalacOptions ++= Seq(
    "-deprecation", // Émet un avertissement et l'emplacement pour les usages d'APIs dépréciées.
    "-feature",      // Émet un avertissement et l'emplacement pour les usages de fonctionnalités qui devraient être importées explicitement.
    "-unchecked",    // Active des avertissements supplémentaires lorsque le code généré dépend d'hypothèses.
    "-Xfatal-warnings", // Fait échouer la compilation s'il y a des avertissements.
    "-Ysafe-init",   // Vérifie l'initialisation sûre.
    "-Ykind-projector:underscores", // Active la syntaxe kind projector pour les lambdas de type.
    // "-Ywarn-unused", // Cette option est ignorée en Scala 3.3.6, nous la supprimons.
    "-Wvalue-discard" // Avertit si une expression pure est ignorée.
  ),
  libraryDependencies ++= Seq(
    // ScalaTest pour les tests unitaires
    "org.scalatest" %% "scalatest" % "3.2.18" % Test,
    // ZIO pour l'application et ZIO-JSON pour l'encodage/décodage JSON
    "dev.zio" %% "zio" % "2.0.22",
    "dev.zio" %% "zio-json" % "0.6.2"
  )
)

lazy val `graph-core` = project
  .in(file("graph-core"))
  .settings(
    name := "graph-core",
    commonSettings,
    // Dépendances spécifiques à graph-core, si elles existent, actuellement couvertes par commonSettings
  )

lazy val `graph-app` = project
  .in(file("graph-app"))
  .settings(
    name := "graph-app",
    commonSettings,
    // Ajoute graph-core comme dépendance pour graph-app
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-streams" % "2.0.22" // ZIO Streams pourrait être utile pour une application interactive
    )
  )
  .dependsOn(`graph-core`) // graph-app dépend de graph-core

lazy val `functional-graphs` = project
  .in(file("."))
  .settings(
    name := "functional-graphs",
    commonSettings,
    publish / skip := true // Ne pas publier le projet racine
  )
  .aggregate(`graph-core`, `graph-app`) // Agrège les sous-projets pour les tâches courantes comme la compilation, les tests, le nettoyage

// Définit la version de SBT pour le projet racine
sbtVersion := "1.11.3" // Mise à jour vers SBT 1.11.3