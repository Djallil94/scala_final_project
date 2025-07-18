package com.example.graph.model

import zio.json._

// Représente un Graphe Dirigé.
// Il stocke les arêtes directement sous forme d'ensemble, et dérive les sommets de ces arêtes.
// Une liste d'adjacence (Map[Vertex, Set[(Vertex, Int)]]) pourrait également être utilisée pour des recherches de voisins plus rapides,
// mais pour la simplicité et pour mettre en évidence l'immutabilité fonctionnelle, nous gérerons les arêtes directement.
case class DirectedGraph(override val edges: Set[Edge]) extends Graph[DirectedGraph] {

  // Tous les sommets sont dérivés des sources et des destinations des arêtes.
  override val vertices: Set[Vertex] = edges.flatMap(e => Set(e.source, e.destination))

  // Retourne les voisins d'un sommet donné de manière dirigée.
  // Ne considère que les arêtes dont la source correspond au sommet donné.
  override def neighbors(vertex: Vertex): Set[Vertex] =
    edges.filter(_.source == vertex).map(_.destination)

  // Retourne les voisins pondérés pour un sommet donné.
  override def weightedNeighbors(vertex: Vertex): Set[(Vertex, Int)] =
    edges.filter(_.source == vertex).map(e => (e.destination, e.weight))

  // Ajoute une arête au graphe. Retourne une nouvelle instance de DirectedGraph avec l'arête ajoutée.
  // Puisque Set.+(element) gère les doublons avec élégance, c'est simple.
  override def addEdge(edge: Edge): DirectedGraph =
    DirectedGraph(edges + edge)

  // Supprime une arête du graphe. Retourne une nouvelle instance de DirectedGraph avec l'arête supprimée.
  // Si l'arête n'existe pas, elle doit retourner la même instance de graphe.
  override def removeEdge(edge: Edge): DirectedGraph =
    DirectedGraph(edges - edge)
}

object DirectedGraph {
  // Méthode d'usine pour créer un DirectedGraph vide.
  def empty: DirectedGraph = DirectedGraph(Set.empty)

  // Encodeur ZIO-JSON pour DirectedGraph. Il encode l'ensemble des arêtes.
  implicit val encoder: JsonEncoder[DirectedGraph] =
    JsonEncoder[Set[Edge]].contramap(_.edges)

  // Décodeur ZIO-JSON pour DirectedGraph. Il décode un ensemble d'arêtes et construit un DirectedGraph.
  implicit val decoder: JsonDecoder[DirectedGraph] =
    JsonDecoder[Set[Edge]].map(DirectedGraph(_))
}
