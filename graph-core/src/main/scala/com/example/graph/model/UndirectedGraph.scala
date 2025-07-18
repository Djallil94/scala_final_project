package com.example.graph.model

import zio.json._

// Représente un Graphe Non Dirigé.
// Pour chaque arête non dirigée conceptuelle (A -- B), nous stockons deux arêtes dirigées (A -> B) et (B -> A) en interne.
// Cela simplifie la recherche de voisins et la cohérence avec la classe de cas Edge.
case class UndirectedGraph(override val edges: Set[Edge]) extends Graph[UndirectedGraph] {

  // Tous les sommets sont dérivés des sources et des destinations des arêtes.
  override val vertices: Set[Vertex] = edges.flatMap(e => Set(e.source, e.destination))

  // Retourne les voisins d'un sommet donné de manière non dirigée.
  // Considere les arêtes où la source est le sommet OU la destination est le sommet.
  override def neighbors(vertex: Vertex): Set[Vertex] =
    edges.filter(e => e.source == vertex || e.destination == vertex)
      .flatMap(e => if (e.source == vertex) Set(e.destination) else Set(e.source))

  // Retourne les voisins pondérés pour un sommet donné.
  // Pour les graphes non dirigés, s'il y a une arête A-B, alors B est un voisin de A avec le poids de l'arête,
  // et A est un voisin de B avec le même poids.
  override def weightedNeighbors(vertex: Vertex): Set[(Vertex, Int)] =
    edges.filter(e => e.source == vertex || e.destination == vertex)
      .map { e =>
        if (e.source == vertex) (e.destination, e.weight)
        else (e.source, e.weight)
      }

  // Ajoute une arête au graphe. Pour un graphe non dirigé, ajouter une arête (A, B, W) signifie
  // ajouter à la fois (A, B, W) et (B, A, W) à l'ensemble interne des arêtes dirigées.
  override def addEdge(edge: Edge): UndirectedGraph = {
    val reciprocalEdge = Edge(edge.destination, edge.source, edge.weight)
    UndirectedGraph(edges + edge + reciprocalEdge)
  }

  // Supprime une arête du graphe. Pour un graphe non dirigé, supprimer une arête (A, B, W) signifie
  // supprimer à la fois (A, B, W) et (B, A, W) de l'ensemble interne des arêtes dirigées.
  override def removeEdge(edge: Edge): UndirectedGraph = {
    val reciprocalEdge = Edge(edge.destination, edge.source, edge.weight)
    UndirectedGraph(edges - edge - reciprocalEdge)
  }
}

object UndirectedGraph {
  // Méthode d'usine pour créer un UndirectedGraph vide.
  def empty: UndirectedGraph = UndirectedGraph(Set.empty)

  // Encodeur ZIO-JSON pour UndirectedGraph. Il encode l'ensemble des arêtes.
  implicit val encoder: JsonEncoder[UndirectedGraph] =
    JsonEncoder[Set[Edge]].contramap(_.edges)

  // Décodeur ZIO-JSON pour UndirectedGraph. Il décode un ensemble d'arêtes et construit un UndirectedGraph.
  implicit val decoder: JsonDecoder[UndirectedGraph] =
    JsonDecoder[Set[Edge]].map(UndirectedGraph(_))
}