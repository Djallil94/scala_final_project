package com.example.graph.ops

import com.example.graph.model._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GraphOperationsSpec extends AnyFlatSpec with Matchers {

  // Crée une instance concrète de GraphOperations pour les tests.
  // Nous pouvons utiliser un type de graphe spécifique pour les tests, ou créer une aide pour passer un G générique.
  // Pour simplifier, nous allons créer des instances pour DirectedGraph et UndirectedGraph séparément.
  val directedOps = new GraphOperations[DirectedGraph] {}
  val undirectedOps = new GraphOperations[UndirectedGraph] {}

  // --- Tests DFS ---
  "DFS" should "parcourir un graphe dirigé simple correctement" in {
    val graph = DirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("A", "C", 1))
      .addEdge(Edge("B", "D", 1))
      .addEdge(Edge("C", "D", 1))
      .addEdge(Edge("D", "E", 1))

    // Remarque : l'ordre DFS peut varier en fonction de l'ordre d'itération des voisins.
    // Notre implémentation trie les voisins pour une sortie déterministe dans les tests.
    directedOps.dfs(graph, "A") should contain inOrderOnly ("A", "C", "D", "E", "B")
  }

  it should "parcourir un graphe non dirigé simple correctement" in {
    val graph = UndirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("A", "C", 1))
      .addEdge(Edge("B", "D", 1))
      .addEdge(Edge("C", "D", 1))

    // Les voisins triés conduisent à des chemins déterministes
    undirectedOps.dfs(graph, "A") should contain inOrderOnly ("A", "C", "D", "B")
  }

  it should "gérer les composants déconnectés en DFS (en partant d'un sommet dans un composant)" in {
    val graph = DirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("X", "Y", 1))

    directedOps.dfs(graph, "A") should contain inOrderOnly ("A", "B")
    directedOps.dfs(graph, "X") should contain inOrderOnly ("X", "Y")
  }

  it should "retourner une liste vide pour un sommet de départ non existant en DFS" in {
    val graph = DirectedGraph.empty.addEdge(Edge("A", "B", 1))
    directedOps.dfs(graph, "Z") shouldBe empty
  }

  it should "gérer un graphe avec un seul sommet en DFS" in {
    val graph = DirectedGraph(Set.empty).addEdge(Edge("A", "A", 0)) // Boucle sur soi-même pour s'assurer que A est un sommet
    directedOps.dfs(graph, "A") should contain inOrderOnly ("A")
  }

  // --- Tests BFS ---
  "BFS" should "parcourir un graphe dirigé simple correctement" in {
    val graph = DirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("A", "C", 1))
      .addEdge(Edge("B", "D", 1))
      .addEdge(Edge("C", "D", 1))
      .addEdge(Edge("D", "E", 1))

    // Les voisins triés conduisent à des chemins déterministes
    directedOps.bfs(graph, "A") should contain inOrderOnly ("A", "B", "C", "D", "E")
  }

  it should "parcourir un graphe non dirigé simple correctement" in {
    val graph = UndirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("A", "C", 1))
      .addEdge(Edge("B", "D", 1))
      .addEdge(Edge("C", "D", 1))

    undirectedOps.bfs(graph, "A") should contain inOrderOnly ("A", "B", "C", "D")
  }

  it should "gérer les composants déconnectés en BFS (en partant d'un sommet dans un composant)" in {
    val graph = DirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("X", "Y", 1))

    directedOps.bfs(graph, "A") should contain inOrderOnly ("A", "B")
    directedOps.bfs(graph, "X") should contain inOrderOnly ("X", "Y")
  }

  it should "retourner une liste vide pour un sommet de départ non existant en BFS" in {
    val graph = DirectedGraph.empty.addEdge(Edge("A", "B", 1))
    directedOps.bfs(graph, "Z") shouldBe empty
  }

  it should "handle a graph with a single vertex in BFS" in {
    val graph = DirectedGraph(Set.empty).addEdge(Edge("A", "A", 0))
    directedOps.bfs(graph, "A") should contain inOrderOnly ("A")
  }

  // --- Tests de Détection de Cycle ---
  "Détection de Cycle" should "détecter un cycle dans un graphe dirigé" in {
    val graph = DirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("B", "C", 1))
      .addEdge(Edge("C", "A", 1)) // Cycle A -> B -> C -> A

    directedOps.hasCycle(graph) shouldBe true
  }

  it should "détecter une boucle sur soi-même comme un cycle dans un graphe dirigé" in {
    val graph = DirectedGraph.empty.addEdge(Edge("A", "A", 1))
    directedOps.hasCycle(graph) shouldBe true
  }

  it should "détecter un cycle dans un graphe non dirigé" in {
    val graph = UndirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("B", "C", 1))
      .addEdge(Edge("C", "A", 1)) // Cycle A -- B -- C -- A

    undirectedOps.hasCycle(graph) shouldBe true
  }

  it should "ne pas détecter de cycle dans un Graphe Dirigé Acyclique (DAG)" in {
    val graph = DirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("A", "C", 1))
      .addEdge(Edge("B", "D", 1))
      .addEdge(Edge("C", "D", 1))

    directedOps.hasCycle(graph) shouldBe false
  }

  it should "ne pas détecter de cycle dans un graphe non dirigé de type arbre" in {
    val graph = UndirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("A", "C", 1))
      .addEdge(Edge("B", "D", 1))

    undirectedOps.hasCycle(graph) shouldBe false
  }

  it should "gérer les composants déconnectés avec et sans cycles" in {
    val graphWithCycle = DirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("B", "A", 1)) // Cycle
      .addEdge(Edge("X", "Y", 1)) // Pas de cycle ici

    directedOps.hasCycle(graphWithCycle) shouldBe true

    val graphWithoutCycle = DirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("X", "Y", 1))

    directedOps.hasCycle(graphWithoutCycle) shouldBe false
  }

  it should "retourner faux pour un graphe vide" in {
    directedOps.hasCycle(DirectedGraph.empty) shouldBe false
    undirectedOps.hasCycle(UndirectedGraph.empty) shouldBe false
  }

  // --- Tests de l'Algorithme de Dijkstra ---
  "L'algorithme de Dijkstra" should "trouver les plus courts chemins dans un graphe dirigé simple" in {
    val graph = DirectedGraph.empty
      .addEdge(Edge("A", "B", 4))
      .addEdge(Edge("A", "C", 2))
      .addEdge(Edge("B", "E", 3))
      .addEdge(Edge("C", "D", 2))
      .addEdge(Edge("C", "F", 4))
      .addEdge(Edge("D", "E", 3))
      .addEdge(Edge("D", "F", 1))
      .addEdge(Edge("E", "Z", 1))
      .addEdge(Edge("F", "Z", 3))

    val expectedDistances = Map(
      "A" -> 0,
      "C" -> 2,
      "D" -> 4,
      "F" -> 5,
      "Z" -> 6,
      "B" -> 4,
      "E" -> 7
    )

    directedOps.dijkstra(graph, "A") should contain theSameElementsAs expectedDistances
  }

  it should "trouver les plus courts chemins dans un graphe non dirigé simple" in {
    val graph = UndirectedGraph.empty
      .addEdge(Edge("A", "B", 4))
      .addEdge(Edge("A", "C", 2))
      .addEdge(Edge("B", "E", 3))
      .addEdge(Edge("C", "D", 2))
      .addEdge(Edge("C", "F", 4))
      .addEdge(Edge("D", "E", 3))
      .addEdge(Edge("D", "F", 1))
      .addEdge(Edge("E", "Z", 1))
      .addEdge(Edge("F", "Z", 3))

    val expectedDistances = Map(
      "A" -> 0,
      "C" -> 2,
      "D" -> 4, // A->C(2)->D(2) = 4
      "F" -> 5, // A->C(2)->D(2)->F(1) = 5
      "Z" -> 6, // A->C(2)->D(2)->F(1)->Z(3) = 8 OU A->C(2)->D(2)->E(3)->Z(1) = 8 OU A->C(2)->D(2)->E(3)->Z(1) = 8
      "B" -> 4,
      "E" -> 7 // A->C(2)->D(2)->E(3) = 7
    )

    undirectedOps.dijkstra(graph, "A") should contain theSameElementsAs expectedDistances
  }

  it should "retourner une map vide pour un sommet de départ non existant dans Dijkstra" in {
    val graph = DirectedGraph.empty.addEdge(Edge("A", "B", 1))
    directedOps.dijkstra(graph, "Z") shouldBe empty
  }

  it should "gérer les composants déconnectés dans Dijkstra" in {
    val graph = DirectedGraph.empty
      .addEdge(Edge("A", "B", 1))
      .addEdge(Edge("X", "Y", 10))

    val resultFromA = directedOps.dijkstra(graph, "A")
    resultFromA should contain theSameElementsAs Map("A" -> 0, "B" -> 1)

    val resultFromX = directedOps.dijkstra(graph, "X")
    resultFromX should contain theSameElementsAs Map("X" -> 0, "Y" -> 10)
  }

  it should "gérer un graphe avec un seul sommet dans Dijkstra" in {
    val graph = DirectedGraph(Set.empty).addEdge(Edge("A", "A", 0))
    directedOps.dijkstra(graph, "A") should contain theSameElementsAs Map("A" -> 0)
  }

  it should "gérer un graphe vide dans Dijkstra" in {
    directedOps.dijkstra(DirectedGraph.empty, "A") shouldBe empty
  }

  it should "calculer correctement le plus court chemin lorsque le chemin direct est plus long que l'indirect" in {
    val graph = DirectedGraph.empty
      .addEdge(Edge("A", "B", 10))
      .addEdge(Edge("A", "C", 1))
      .addEdge(Edge("C", "B", 2))

    val expectedDistances = Map("A" -> 0, "C" -> 1, "B" -> 3) // A -> C (1) -> B (2) = 3

    directedOps.dijkstra(graph, "A") should contain theSameElementsAs expectedDistances
  }
}