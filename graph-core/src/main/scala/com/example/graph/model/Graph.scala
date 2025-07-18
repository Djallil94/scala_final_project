package com.example.graph.model

import zio.json._
import com.example.graph.model.{Vertex, Edge} // Importe Vertex et Edge depuis le même package model

// Trait représentant un graphe générique. Il est paramétré par le type du graphe lui-même,
// permettant à différentes implémentations concrètes (par exemple, DirectedGraph, UndirectedGraph)
// d'être traitées uniformément tout en préservant leurs informations de type spécifiques.
trait Graph[G <: Graph[G]] { self: G => // Annotation de type self pour s'assurer que G est un sous-type de Graph[G]

  // Retourne un ensemble immuable de tous les sommets du graphe.
  def vertices: Set[Vertex]

  // Retourne un ensemble immuable de toutes les arêtes du graphe.
  def edges: Set[Edge]

  // Retourne un ensemble immuable de voisins pour un sommet donné.
  // Un voisin est un sommet atteignable par une arête depuis le sommet donné.
  // Retourne un ensemble vide si le sommet n'est pas dans le graphe.
  def neighbors(vertex: Vertex): Set[Vertex]

  // Retourne un ensemble de tuples (voisin, poids) pour un sommet donné.
  // Ceci est utile pour des algorithmes comme Dijkstra.
  def weightedNeighbors(vertex: Vertex): Set[(Vertex, Int)]

  // Ajoute une nouvelle arête au graphe. Retourne une nouvelle instance de graphe avec l'arête ajoutée.
  // Si l'arête existe déjà, elle doit retourner la même instance de graphe ou une nouvelle
  // qui est effectivement identique.
  def addEdge(edge: Edge): G

  // Supprime une arête du graphe. Retourne une nouvelle instance de graphe avec l'arête supprimée.
  // Si l'arête n'existe pas, elle doit retourner la même instance de graphe.
  def removeEdge(edge: Edge): G

  // Vérifie si un sommet existe dans le graphe.
  def containsVertex(vertex: Vertex): Boolean = vertices.contains(vertex)

  // Vérifie si une arête existe dans le graphe.
  def containsEdge(edge: Edge): Boolean = edges.contains(edge)

  // Retourne le poids d'une arête si elle existe, sinon retourne None.
  def getEdgeWeight(source: Vertex, destination: Vertex): Option[Int] =
    edges.find(e => e.source == source && e.destination == destination).map(_.weight)

  // Retourne le nombre de sommets dans le graphe.
  def numVertices: Int = vertices.size

  // Retourne le nombre d'arêtes dans le graphe.
  def numEdges: Int = edges.size
}