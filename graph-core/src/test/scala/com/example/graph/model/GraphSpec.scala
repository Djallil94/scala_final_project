package com.example.graph.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.json._

class GraphSpec extends AnyFlatSpec with Matchers {

  "DirectedGraph" should "être vide lorsqu'il est créé sans arêtes" in {
    val graph = DirectedGraph.empty
    graph.vertices shouldBe empty
    graph.edges shouldBe empty
    graph.numVertices shouldBe 0
    graph.numEdges shouldBe 0
  }

  it should "ajouter une seule arête correctement" in {
    val graph = DirectedGraph.empty
    val edge = Edge("A", "B", 10)
    val newGraph = graph.addEdge(edge)
    newGraph.vertices should contain allOf ("A", "B")
    newGraph.edges should contain (edge)
    newGraph.numVertices shouldBe 2
    newGraph.numEdges shouldBe 1
    newGraph.neighbors("A") should contain ("B")
    newGraph.neighbors("B") shouldBe empty
    newGraph.weightedNeighbors("A") should contain (("B", 10))
    newGraph.getEdgeWeight("A", "B") shouldBe Some(10)
  }

  it should "ajouter plusieurs arêtes correctement" in {
    val graph = DirectedGraph.empty
    val edge1 = Edge("A", "B", 10)
    val edge2 = Edge("B", "C", 20)
    val edge3 = Edge("A", "C", 15)
    val newGraph = graph.addEdge(edge1).addEdge(edge2).addEdge(edge3)

    newGraph.vertices should contain allOf ("A", "B", "C")
    newGraph.edges should contain allOf (edge1, edge2, edge3)
    newGraph.numVertices shouldBe 3
    newGraph.numEdges shouldBe 3
    newGraph.neighbors("A") should contain allOf ("B", "C")
    newGraph.neighbors("B") should contain ("C")
    newGraph.neighbors("C") shouldBe empty
  }

  it should "supprimer une arête correctement" in {
    val edge1 = Edge("A", "B", 10)
    val edge2 = Edge("B", "C", 20)
    val graph = DirectedGraph(Set(edge1, edge2))
    val newGraph = graph.removeEdge(edge1)

    newGraph.vertices should contain allOf ("B", "C") // A n'est plus une source/destination
    newGraph.edges should contain (edge2)
    newGraph.edges should not contain (edge1)
    newGraph.numVertices shouldBe 2
    newGraph.numEdges shouldBe 1
  }

  it should "gérer l'ajout d'arêtes en double avec élégance" in {
    val edge = Edge("A", "B", 10)
    val graph = DirectedGraph.empty.addEdge(edge).addEdge(edge)
    graph.numEdges shouldBe 1
    graph.edges should contain (edge)
  }

  it should "retourner des voisins vides pour un sommet non existant" in {
    val graph = DirectedGraph.empty.addEdge(Edge("A", "B", 1))
    graph.neighbors("X") shouldBe empty
    graph.weightedNeighbors("X") shouldBe empty
  }

  it should "retourner None pour un poids d'arête non existant" in {
    val graph = DirectedGraph.empty.addEdge(Edge("A", "B", 1))
    graph.getEdgeWeight("A", "C") shouldBe None
    graph.getEdgeWeight("C", "A") shouldBe None
  }

  "UndirectedGraph" should "être vide lorsqu'il est créé sans arêtes" in {
    val graph = UndirectedGraph.empty
    graph.vertices shouldBe empty
    graph.edges shouldBe empty
    graph.numVertices shouldBe 0
    graph.numEdges shouldBe 0
  }

  it should "ajouter une seule arête correctement, créant une arête réciproque" in {
    val graph = UndirectedGraph.empty
    val edge = Edge("A", "B", 10)
    val newGraph = graph.addEdge(edge)
    newGraph.vertices should contain allOf ("A", "B")
    newGraph.edges should contain allOf (edge, Edge("B", "A", 10)) // Arête réciproque
    newGraph.numVertices shouldBe 2
    newGraph.numEdges shouldBe 2 // Deux arêtes dirigées pour une non dirigée
    newGraph.neighbors("A") should contain ("B")
    newGraph.neighbors("B") should contain ("A")
    newGraph.weightedNeighbors("A") should contain (("B", 10))
    newGraph.weightedNeighbors("B") should contain (("A", 10))
    newGraph.getEdgeWeight("A", "B") shouldBe Some(10)
    newGraph.getEdgeWeight("B", "A") shouldBe Some(10)
  }

  it should "ajouter plusieurs arêtes correctement" in {
    val graph = UndirectedGraph.empty
    val edge1 = Edge("A", "B", 10)
    val edge2 = Edge("B", "C", 20)
    val edge3 = Edge("A", "C", 15)
    val newGraph = graph.addEdge(edge1).addEdge(edge2).addEdge(edge3)

    newGraph.vertices should contain allOf ("A", "B", "C")
    newGraph.edges.size shouldBe 6 // (A,B), (B,A), (B,C), (C,B), (A,C), (C,A)
    newGraph.neighbors("A") should contain allOf ("B", "C")
    newGraph.neighbors("B") should contain allOf ("A", "C")
    newGraph.neighbors("C") should contain allOf ("A", "B")
  }

  it should "supprimer une arête correctement, supprimant également l'arête réciproque" in {
    val edge1 = Edge("A", "B", 10)
    val edge2 = Edge("B", "C", 20)
    val graph = UndirectedGraph.empty.addEdge(edge1).addEdge(edge2) // Cela crée 4 arêtes internes
    val newGraph = graph.removeEdge(edge1)

    newGraph.vertices should contain allOf ("B", "C")
    newGraph.edges should contain allOf (edge2, Edge("C", "B", 20))
    newGraph.edges should not contain (edge1)
    newGraph.edges should not contain (Edge("B", "A", 10))
    newGraph.numEdges shouldBe 2
  }

  it should "gérer l'ajout d'arêtes en double avec élégance dans UndirectedGraph" in {
    val edge = Edge("A", "B", 10)
    val graph = UndirectedGraph.empty.addEdge(edge).addEdge(edge)
    graph.numEdges shouldBe 2 // (A,B,10) et (B,A,10)
    graph.edges should contain allOf (edge, Edge("B", "A", 10))
  }

  "Encodage/Décodage JSON de Graphe" should "encoder et décoder DirectedGraph correctement" in {
    val edge1 = Edge("A", "B", 10)
    val edge2 = Edge("B", "C", 20)
    val originalGraph = DirectedGraph.empty.addEdge(edge1).addEdge(edge2)

    val jsonString = originalGraph.toJson
    val decodedGraph = jsonString.fromJson[DirectedGraph]

    decodedGraph match {
      case Right(graph) =>
        graph.vertices should contain allOf ("A", "B", "C")
        graph.edges should contain allOf (edge1, edge2)
        graph.numEdges shouldBe 2
      case Left(error) => fail(s"Le décodage a échoué : $error")
    }
  }

  it should "encode et décoder UndirectedGraph correctement" in {
    val edge1 = Edge("X", "Y", 5)
    val edge2 = Edge("Y", "Z", 8)
    val originalGraph = UndirectedGraph.empty.addEdge(edge1).addEdge(edge2)

    val jsonString = originalGraph.toJson
    val decodedGraph = jsonString.fromJson[UndirectedGraph]

    decodedGraph match {
      case Right(graph) =>
        graph.vertices should contain allOf ("X", "Y", "Z")
        // Le graphe non dirigé stocke les arêtes réciproques, donc 4 arêtes pour 2 conceptuelles
        graph.edges.size shouldBe 4
        graph.edges should contain allOf (edge1, Edge("Y", "X", 5), edge2, Edge("Z", "Y", 8))
      case Left(error) => fail(s"Le décodage a échoué : $error")
    }
  }

  "Représentation GraphViz" should "générer une chaîne DOT correcte pour DirectedGraph" in {
    import com.example.graph.viz.toDotString // Importe la méthode d'extension

    val graph = DirectedGraph.empty
      .addEdge(Edge("A", "B", 10))
      .addEdge(Edge("B", "C", 20))
      .addEdge(Edge("A", "C", 15))
      .addEdge(Edge("D", "E", 5)) // Composant déconnecté

    val expectedDot =
      """digraph G {
        |  "A";
        |  "B";
        |  "C";
        |  "D";
        |  "E";
        |  "A" -> "B" [label="10"];
        |  "B" -> "C" [label="20"];
        |  "A" -> "C" [label="15"];
        |  "D" -> "E" [label="5"];
        |}""".stripMargin.split("\n").map(_.trim).filter(_.nonEmpty).toSet

    val actualDotLines = graph.toDotString.split("\n").map(_.trim).filter(_.nonEmpty).toSet
    actualDotLines should contain allElementsOf expectedDot
    actualDotLines.size shouldBe expectedDot.size
  }

  it should "générer une chaîne DOT correcte pour UndirectedGraph" in {
    import com.example.graph.viz.toDotString

    val graph = UndirectedGraph.empty
      .addEdge(Edge("X", "Y", 5))
      .addEdge(Edge("Y", "Z", 8))
      .addEdge(Edge("X", "Z", 12))

    val expectedDot =
      """graph G {
        |  "X";
        |  "Y";
        |  "Z";
        |  "X" -- "Y" [label="5"];
        |  "Y" -- "Z" [label="8"];
        |  "X" -- "Z" [label="12"];
        |}""".stripMargin.split("\n").map(_.trim).filter(_.nonEmpty).toSet

    val actualDotLines = graph.toDotString.split("\n").map(_.trim).filter(_.nonEmpty).toSet
    actualDotLines should contain allElementsOf expectedDot
    actualDotLines.size shouldBe expectedDot.size
  }

  it should "gérer un graphe vide pour la chaîne DOT" in {
    import com.example.graph.viz.toDotString
    val graph = DirectedGraph.empty
    val expectedDot =
      """digraph G {
        |
        |}""".stripMargin.split("\n").map(_.trim).filter(_.nonEmpty).toSet
    val actualDotLines = graph.toDotString.split("\n").map(_.trim).filter(_.nonEmpty).toSet
    actualDotLines should contain allElementsOf expectedDot
    actualDotLines.size shouldBe expectedDot.size
  }
}