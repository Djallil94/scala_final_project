// --- graph-core/src/main/scala/com/example/graph/ops/GraphOperations.scala ---
// Implémentations des opérations de parcours de graphe et d'algorithmes.

package com.example.graph.ops

import com.example.graph.model._
import scala.annotation.tailrec
import scala.collection.immutable.{Queue, SortedMap}

// L'objet compagnon GraphOperations contient les méthodes d'opérations sur les graphes.
// Ces méthodes sont définies comme des fonctions génériques qui prennent un graphe de type G.
// Cela permet d'appeler directement GraphOperations.dfs(graph, startVertex)
// sans avoir besoin d'instancier le trait GraphOperations.
object GraphOperations {

  // Parcours en profondeur (DFS - Depth First Search).
  // Explores as far as possible along each branch before backtracking.
  // Returns a list of vertices in DFS order.
  // @param graph: The graph to traverse.
  // @param startVertex: The vertex to start the traversal from.
  // @return A List of vertices in DFS traversal order.
  def dfs[G <: Graph[G]](graph: G, startVertex: Vertex): List[Vertex] = {
    @tailrec
    def loop(stack: List[Vertex], visited: Set[Vertex], result: List[Vertex]): List[Vertex] = {
      stack match {
        case Nil => result.reverse // All reachable vertices visited, reverse to get correct order
        case current :: tail =>
          if (visited.contains(current)) {
            loop(tail, visited, result) // Already visited, skip
          } else {
            // Add current to visited and result, then add its unvisited neighbors to the stack
            val newVisited = visited + current
            val newResult = current :: result
            val unvisitedNeighbors = graph.neighbors(current).diff(newVisited) // Utilisation de diff
            loop(unvisitedNeighbors.toList.sorted ::: tail, newVisited, newResult) // Trie pour un ordre déterministe dans les tests
          }
      }
    }
    if (!graph.containsVertex(startVertex)) List.empty // Start vertex not in graph
    else loop(List(startVertex), Set.empty, List.empty)
  }

  // Parcours en largeur (BFS - Breadth First Search).
  // Explores all neighbor vertices at the present depth before moving on to the next depth level.
  // Returns a list of vertices in BFS order.
  // @param graph: The graph to traverse.
  // @param startVertex: The vertex to start the traversal from.
  // @return A List of vertices in BFS traversal order.
  def bfs[G <: Graph[G]](graph: G, startVertex: Vertex): List[Vertex] = {
    @tailrec
    def loop(queue: Queue[Vertex], visited: Set[Vertex], result: List[Vertex]): List[Vertex] = {
      queue.dequeueOption match {
        case None => result // Queue is empty, all reachable vertices visited
        case Some((current, remainingQueue)) =>
          if (visited.contains(current)) {
            loop(remainingQueue, visited, result) // Already visited, skip
          } else {
            // Add current to visited and result, then add its unvisited neighbors to the queue
            val newVisited = visited + current
            val newResult = result :+ current // Append to result for BFS order
            val unvisitedNeighbors = graph.neighbors(current).diff(newVisited) // Utilisation de diff
            loop(remainingQueue.enqueueAll(unvisitedNeighbors.toList.sorted), newVisited, newResult) // Sort for deterministic order
          }
      }
    }
    if (!graph.containsVertex(startVertex)) List.empty // Start vertex not in graph
    else loop(Queue(startVertex), Set.empty, List.empty)
  }

  // Detects if a cycle exists in the graph using DFS.
  // @param graph: The graph to check for cycles.
  // @return True if a cycle is detected, false otherwise.
  def hasCycle[G <: Graph[G]](graph: G): Boolean = {
    // allVisitedGlobal: Garde une trace de tous les nœuds visités globalement à travers tous les appels DFS.
    // recursionStack: Garde une trace des nœuds actuellement dans la pile de récursion du DFS en cours.
    // Cette fonction interne est récursive terminale.
    @tailrec
    def dfsVisit(
                  stack: List[Vertex], // Pile des sommets à visiter dans le chemin DFS actuel
                  visitedInPath: Set[Vertex], // Nœuds visités dans le chemin DFS actuel
                  recursionStack: Set[Vertex], // Nœuds actuellement dans la pile de récursion
                  allVisitedGlobal: Set[Vertex] // Tous les nœuds visités globalement
                ): (Boolean, Set[Vertex]) = {
      stack match {
        case Nil => (false, allVisitedGlobal) // Chemin DFS terminé, pas de cycle trouvé dans ce chemin
        case currentVertex :: tailStack =>
          // Si le sommet actuel est déjà dans la pile de récursion, un cycle est détecté.
          if (recursionStack.contains(currentVertex)) (true, allVisitedGlobal)
          // Si le sommet a déjà été visité globalement, pas besoin de le revisiter.
          else if (allVisitedGlobal.contains(currentVertex)) {
            dfsVisit(tailStack, visitedInPath, recursionStack, allVisitedGlobal) // Passe au prochain sommet dans la pile
          } else {
            // Marque le sommet comme visité dans le chemin et l'ajoute à la pile de récursion.
            val newVisitedInPath = visitedInPath + currentVertex
            val newRecursionStack = recursionStack + currentVertex
            val newAllVisitedGlobal = allVisitedGlobal + currentVertex

            // Ajoute les voisins non visités à la pile pour exploration.
            val unvisitedNeighbors = graph.neighbors(currentVertex).diff(newVisitedInPath).toList.sorted // Trie pour un ordre déterministe

            // L'appel récursif est la dernière opération, rendant la fonction tail-recursive.
            dfsVisit(unvisitedNeighbors ::: tailStack, newVisitedInPath, newRecursionStack, newAllVisitedGlobal)
          }
      }
    }

    // Itère sur tous les sommets pour s'assurer que les composants déconnectés sont également vérifiés.
    // C'est la boucle externe qui lance le DFS pour chaque composant non visité.
    graph.vertices.foldLeft((false, Set.empty[Vertex])) { case ((cycleDetected, allVisited), vertex) =>
      if (cycleDetected) (true, allVisited) // Si un cycle est déjà trouvé, pas besoin de continuer.
      else if (allVisited.contains(vertex)) (false, allVisited) // Déjà visité par une exécution DFS précédente, ignorer.
      else {
        val (foundCycle, updatedAllVisited) = dfsVisit(
          List(vertex), // Commence le DFS avec le sommet actuel
          Set.empty, // visitedInPath commence vide pour un nouveau parcours DFS
          Set.empty, // recursionStack commence vide pour un nouveau parcours DFS
          allVisited // Passe l'ensemble global des visités
        )
        (foundCycle, updatedAllVisited)
      }
    }._1 // Retourne seulement le booléen indiquant si un cycle a été trouvé.
  }


  // Algorithme de Dijkstra pour trouver les plus courts chemins d'un sommet source à tous les autres sommets.
  // @param graph: Le graphe sur lequel appliquer Dijkstra.
  // @param startVertex: Le sommet source.
  // @return Une Map où les clés sont les sommets de destination et les valeurs sont leurs plus courtes distances depuis le startVertex.
  //         Retourne une map vide si le startVertex n'est pas dans le graphe.
  def dijkstra[G <: Graph[G]](graph: G, startVertex: Vertex): Map[Vertex, Int] = {
    if (!graph.containsVertex(startVertex)) return Map.empty

    // distances: Map du sommet à sa distance la plus courte actuelle depuis startVertex.
    // Initialisé avec 0 pour startVertex et Int.MaxValue pour les autres.
    // Utilisation de SortedMap pour un ordre déterministe dans les tests, bien que ce ne soit pas strictement nécessaire pour la correction de l'algorithme.
    var distances: SortedMap[Vertex, Int] = SortedMap(startVertex -> 0) ++
      graph.vertices.diff(Set(startVertex)).map(_ -> Int.MaxValue) // Utilisation de diff

    // unvisited: Ensemble de sommets qui n'ont pas encore été inclus dans l'arbre des plus courts chemins.
    var unvisited: Set[Vertex] = graph.vertices

    @tailrec
    def loop(): Unit = {
      // Trouve le sommet non visité avec la plus petite distance.
      val currentOption = unvisited.filter(distances.contains).minByOption(distances(_))

      currentOption match {
        case None => // Tous les sommets atteignables ont été visités ou aucun chemin vers les non visités restants.
        case Some(currentVertex) =>
          // Supprime currentVertex de l'ensemble non visité.
          unvisited = unvisited - currentVertex

          // Si la plus petite distance est Int.MaxValue, cela signifie que les sommets non visités restants sont inatteignables.
          if (distances(currentVertex) == Int.MaxValue) {
            // Aucun chemin vers les sommets non visités restants, terminer.
            unvisited = Set.empty // Efface les non visités pour arrêter la boucle
          } else {
            // Itère sur les voisins du sommet actuel.
            graph.weightedNeighbors(currentVertex).foreach { case (neighbor, weight) =>
              if (unvisited.contains(neighbor)) { // Ne considère que les voisins non visités
                val newDistance = distances(currentVertex) + weight
                if (newDistance < distances.getOrElse(neighbor, Int.MaxValue)) {
                  distances = distances.updated(neighbor, newDistance)
                }
              }
            }
            loop() // Recurse pour le prochain sommet avec la plus petite distance
          }
      }
    }

    loop() // Démarre l'algorithme

    // Filtre les sommets inatteignables (ceux qui sont toujours à Int.MaxValue)
    distances.filter(_._2 != Int.MaxValue).toMap
  }
}
