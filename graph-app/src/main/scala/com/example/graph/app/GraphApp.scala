// --- graph-app/src/main/scala/com/example/graph/app/GraphApp.scala ---
// Application interactive ZIO pour la manipulation de graphes.

package com.example.graph.app

import com.example.graph.model._
import com.example.graph.ops.GraphOperations // Importe l'objet GraphOperations
import com.example.graph.viz._ // Importe toutes les définitions de l'objet GraphViz, y compris les extensions
import com.example.graph.viz.GraphViz.toDotString
import zio._
import zio.json._
import zio.Console._

// Définit l'application ZIO principale.
object GraphApp extends ZIOAppDefault {

  // État de l'application, contenant le graphe actuel et son type.
  // Rendre AppState générique sur le type de graphe concret (G)
  // permet au compilateur de maintenir l'information de type précise.
  case class AppState[G <: Graph[G]](graph: G, graphType: String)

  // Point d'entrée de l'application ZIO.
  override def run: ZIO[Any, Throwable, Unit] =
    // Initialise l'état avec un graphe dirigé vide par défaut.
    for {
      _ <- Console.printLine("Welcome to the Functional Graph Application!")
      _ <- Console.printLine("Choose graph type: (D)irected or (U)ndirected. Default is Directed.")
      initialGraphTypeInput <- Console.readLine.map(_.trim.toLowerCase) // Applique trim et toLowerCase directement sur le String
      // Initialise l'état avec le type de graphe concret choisi
      initialState <- initialGraphTypeInput match {
        case "u" => ZIO.succeed(AppState(UndirectedGraph.empty, "Undirected"))
        case _   => ZIO.succeed(AppState(DirectedGraph.empty, "Directed"))
      }
      // Appelle la boucle principale avec l'état initial, qui est maintenant de type AppState[DirectedGraph] ou AppState[UndirectedGraph]
      _ <- programLoop(initialState)
    } yield ()

  // Boucle principale de l'application.
  // Utilise un paramètre de type abstrait pour AppState pour permettre la flexibilité.
  def programLoop(state: AppState[? <: Graph[?]]): ZIO[Any, Throwable, Unit] =
    for {
      _ <- Console.printLine("\n--- Menu ---")
      _ <- Console.printLine("1. Add Edge")
      _ <- Console.printLine("2. Remove Edge")
      _ <- Console.printLine("3. Show Graph Details")
      _ <- Console.printLine("4. Perform DFS")
      _ <- Console.printLine("5. Perform BFS")
      _ <- Console.printLine("6. Check for Cycle")
      _ <- Console.printLine("7. Run Dijkstra's Algorithm")
      _ <- Console.printLine("8. Export to JSON")
      _ <- Console.printLine("9. Import from JSON")
      _ <- Console.printLine("10. Generate GraphViz DOT")
      _ <- Console.printLine("11. Exit")
      _ <- Console.print("Enter your choice: ")
      choice <- Console.readLine.orDie // Lit l'entrée de l'utilisateur, échoue si erreur.
      _ <- choice match {
        // Pour les opérations qui modifient le graphe, nous devons retourner un nouvel AppState avec le type concret mis à jour.
        // C'est pourquoi nous utilisons flatMap et une nouvelle instanciation de AppState.
        case "1"  => addEdge(state).flatMap(programLoop)
        case "2"  => removeEdge(state).flatMap(programLoop)
        // Pour les opérations qui ne modifient pas le graphe, nous pouvons simplement passer l'état actuel.
        case "3"  => showGraphDetails(state).flatMap(_ => programLoop(state))
        case "4"  => performDFS(state).flatMap(_ => programLoop(state))
        case "5"  => performBFS(state).flatMap(_ => programLoop(state))
        case "6"  => checkForCycle(state).flatMap(_ => programLoop(state))
        case "7"  => runDijkstra(state).flatMap(_ => programLoop(state))
        case "8"  => exportToJson(state).flatMap(_ => programLoop(state))
        case "9"  => importFromJson(state).flatMap(programLoop)
        case "10" => generateGraphViz(state).flatMap(_ => programLoop(state))
        case "11" => Console.printLine("Exiting application. Goodbye!")
        case _    => Console.printLine("Invalid choice. Please try again.").flatMap(_ => programLoop(state))
      }
    } yield ()

  // Demande à l'utilisateur les détails d'une arête et l'ajoute au graphe.
  // Cette fonction doit maintenant retourner AppState[? <: Graph[?]] car le type du graphe peut changer.
  def addEdge(state: AppState[? <: Graph[?]]): ZIO[Any, Throwable, AppState[? <: Graph[?]]] =
    for {
      _ <- Console.print("Enter source vertex: ")
      source <- Console.readLine.map(_.trim) // Applique trim directement
      _ <- Console.print("Enter destination vertex: ")
      destination <- Console.readLine.map(_.trim) // Applique trim directement
      _ <- Console.print("Enter weight (integer): ")
      weightStr <- Console.readLine
      weight <- ZIO.attempt(weightStr.toInt).orElseFail(new IllegalArgumentException("Invalid weight. Must be an integer."))
      // On doit s'assurer que le type du graphe est correct pour addEdge
      // Utilise un match pour obtenir le type concret et appeler addEdge, puis crée un nouvel AppState avec le type correct.
      newState <- ZIO.succeed(state.graph match {
        case dg: DirectedGraph =>
          val newDg = dg.addEdge(Edge(source, destination, weight))
          AppState(newDg, "Directed")
        case ug: UndirectedGraph =>
          val newUg = ug.addEdge(Edge(source, destination, weight))
          AppState(newUg, "Undirected")
        case _ => throw new IllegalArgumentException("Unknown graph type for addEdge")
      })
      _ <- Console.printLine(s"Edge ($source -> $destination, weight $weight) added.")
    } yield newState


  // Demande à l'utilisateur les détails d'une arête et la supprime du graphe.
  // Cette fonction doit maintenant retourner AppState[? <: Graph[?]] car le type du graphe peut changer.
  def removeEdge(state: AppState[? <: Graph[?]]): ZIO[Any, Throwable, AppState[? <: Graph[?]]] =
    for {
      _ <- Console.print("Enter source vertex of edge to remove: ")
      source <- Console.readLine.map(_.trim) // Applique trim directement
      _ <- Console.print("Enter destination vertex of edge to remove: ")
      destination <- Console.readLine.map(_.trim) // Applique trim directement
      _ <- Console.print("Enter weight of edge to remove (if known, otherwise any weight will match for undirected): ")
      weightStr <- Console.readLine
      weight <- ZIO.attempt(weightStr.toInt).orElseFail(new IllegalArgumentException("Invalid weight. Must be an integer."))
      edgeToRemove = Edge(source, destination, weight)
      // Utilise un match pour obtenir le type concret et appeler removeEdge, puis crée un nouvel AppState avec le type correct.
      newState <- ZIO.succeed(state.graph match {
        case dg: DirectedGraph =>
          val newDg = dg.removeEdge(edgeToRemove)
          AppState(newDg, "Directed")
        case ug: UndirectedGraph =>
          val newUg = ug.removeEdge(edgeToRemove)
          AppState(newUg, "Undirected")
        case _ => throw new IllegalArgumentException("Unknown graph type for removeEdge")
      })
      _ <- Console.printLine(s"Edge ($source -> $destination, weight $weight) removed (if it existed).")
    } yield newState


  // Affiche les détails actuels du graphe.
  // Le type de AppState peut rester générique ici car il n'est pas modifié.
  def showGraphDetails(state: AppState[? <: Graph[?]]): ZIO[Any, Throwable, Unit] =
    for {
      _ <- Console.printLine(s"Current Graph Type: ${state.graphType}")
      _ <- Console.printLine(s"Vertices (${state.graph.numVertices}): ${state.graph.vertices.mkString(", ")}")
      _ <- Console.printLine(s"Edges (${state.graph.numEdges}): ${state.graph.edges.mkString(", ")}")
      _ <- Console.printLine("Weighted Neighbors:")
      _ <- ZIO.foreach(state.graph.vertices) { v =>
        Console.printLine(s"  $v -> ${state.graph.weightedNeighbors(v).mkString(", ")}")
      }
    } yield ()

  // Effectue un parcours DFS à partir d'un sommet donné.
  def performDFS(state: AppState[? <: Graph[?]]): ZIO[Any, Throwable, Unit] =
    for {
      _ <- Console.print("Enter start vertex for DFS: ")
      startVertex <- Console.readLine.map(_.trim) // Applique trim directement
      _ <- if (state.graph.containsVertex(startVertex)) {
        state.graph match {
          case dg: DirectedGraph =>
            val dfsResult = GraphOperations.dfs(dg, startVertex)
            Console.printLine(s"DFS from $startVertex: ${dfsResult.mkString(" -> ")}")
          case ug: UndirectedGraph =>
            val dfsResult = GraphOperations.dfs(ug, startVertex)
            Console.printLine(s"DFS from $startVertex: ${dfsResult.mkString(" -> ")}")
          case _ =>
            Console.printLine("Cannot perform DFS on unknown graph type.")
        }
      } else {
        Console.printLine(s"Vertex '$startVertex' not found in graph.")
      }
    } yield ()

  // Effectue un parcours BFS à partir d'un sommet donné.
  def performBFS(state: AppState[? <: Graph[?]]): ZIO[Any, Throwable, Unit] =
    for {
      _ <- Console.print("Enter start vertex for BFS: ")
      startVertex <- Console.readLine.map(_.trim) // Applique trim directement
      _ <- if (state.graph.containsVertex(startVertex)) {
        state.graph match {
          case dg: DirectedGraph =>
            val bfsResult = GraphOperations.bfs(dg, startVertex)
            Console.printLine(s"BFS from $startVertex: ${bfsResult.mkString(" -> ")}")
          case ug: UndirectedGraph =>
            val bfsResult = GraphOperations.bfs(ug, startVertex)
            Console.printLine(s"BFS from $startVertex: ${bfsResult.mkString(" -> ")}")
          case _ =>
            Console.printLine("Cannot perform BFS on unknown graph type.")
        }
      } else {
        Console.printLine(s"Vertex '$startVertex' not found in graph.")
      }
    } yield ()

  // Vérifie la présence de cycles dans le graphe.
  def checkForCycle(state: AppState[? <: Graph[?]]): ZIO[Any, Throwable, Unit] =
    for {
      _ <- state.graph match {
        case dg: DirectedGraph =>
          val hasCycleResult = GraphOperations.hasCycle(dg)
          Console.printLine(s"Graph has cycle: $hasCycleResult")
        case ug: UndirectedGraph =>
          val hasCycleResult = GraphOperations.hasCycle(ug)
          Console.printLine(s"Graph has cycle: $hasCycleResult")
        case _ =>
          Console.printLine("Cannot check for cycle on unknown graph type.")
      }
    } yield ()

  // Exécute l'algorithme de Dijkstra à partir d'un sommet donné.
  def runDijkstra(state: AppState[? <: Graph[?]]): ZIO[Any, Throwable, Unit] =
    for {
      _ <- Console.print("Enter start vertex for Dijkstra's: ")
      startVertex <- Console.readLine.map(_.trim) // Applique trim directement
      _ <- if (state.graph.containsVertex(startVertex)) {
        state.graph match {
          case dg: DirectedGraph =>
            val distances = GraphOperations.dijkstra(dg, startVertex)
            Console.printLine(s"Shortest distances from $startVertex:")
            ZIO.foreach(distances.toList.sortBy(_._1)) { case (v, d) =>
              Console.printLine(s"  To $v: $d")
            }
          case ug: UndirectedGraph =>
            val distances = GraphOperations.dijkstra(ug, startVertex)
            Console.printLine(s"Shortest distances from $startVertex:")
            ZIO.foreach(distances.toList.sortBy(_._1)) { case (v, d) =>
              Console.printLine(s"  To $v: $d")
            }
          case _ =>
            Console.printLine("Cannot run Dijkstra's on unknown graph type.")
        }
      } else {
        Console.printLine(s"Vertex '$startVertex' not found in graph.")
      }
    } yield ()

  // Exporte le graphe actuel au format JSON.
  def exportToJson(state: AppState[? <: Graph[?]]): ZIO[Any, Throwable, Unit] =
    for {
      jsonString <- ZIO.succeed {
        state.graph match {
          case dg: DirectedGraph => dg.toJsonPretty(using DirectedGraph.encoder)
          case ug: UndirectedGraph => ug.toJsonPretty(using UndirectedGraph.encoder)
          case _ => throw new IllegalArgumentException("Unknown graph type for JSON export")
        }
      }
      _ <- Console.printLine("\n--- Graph JSON Representation ---")
      _ <- Console.printLine(jsonString)
      _ <- Console.printLine("---------------------------------")
    } yield ()

  // Importe un graphe à partir d'une chaîne JSON.
  def importFromJson(state: AppState[? <: Graph[?]]): ZIO[Any, Throwable, AppState[? <: Graph[?]]] =
    for {
      _ <- Console.printLine("Enter JSON string to import (ensure it matches the current graph type):")
      jsonInput <- Console.readLine
      newGraphState <- state.graphType match {
        case "Directed" =>
          jsonInput.fromJson[DirectedGraph](using DirectedGraph.decoder) match {
            case Right(graph) =>
              Console.printLine("Directed Graph imported successfully.").as(state.copy(graph = graph))
            case Left(error) =>
              Console.printLine(s"Failed to decode DirectedGraph from JSON: $error").as(state)
          }
        case "Undirected" =>
          jsonInput.fromJson[UndirectedGraph](using UndirectedGraph.decoder) match {
            case Right(graph) =>
              Console.printLine("Undirected Graph imported successfully.").as(state.copy(graph = graph))
            case Left(error) =>
              Console.printLine(s"Failed to decode UndirectedGraph from JSON: $error").as(state)
          }
        case _ =>
          Console.printLine("Unknown graph type for import.").as(state)
      }
    } yield newGraphState

  // Génère la représentation GraphViz DOT du graphe.
  def generateGraphViz(state: AppState[? <: Graph[?]]): ZIO[Any, Throwable, Unit] =
    for {
      dotString <- ZIO.succeed {
        state.graph match {
          case dg: DirectedGraph => dg.toDotString
          case ug: UndirectedGraph => ug.toDotString
          case _ => throw new IllegalArgumentException("Unknown graph type for GraphViz export")
        }
      }
      _ <- Console.printLine("\n--- GraphViz DOT Representation ---")
      _ <- Console.printLine(dotString)
      _ <- Console.printLine("-----------------------------------")
      _ <- Console.printLine("You can paste this into a GraphViz viewer (e.g., [https://dreampuf.github.io/GraphvizOnline/](https://dreampuf.github.io/GraphvizOnline/)) to visualize your graph.")
    } yield ()
}
