package com.example.graph.model

import zio.json._

// Un alias de type pour un sommet (vertex), rendant le code plus lisible.
type Vertex = String

// Représente une arête dans un graphe, connectant deux sommets avec un poids associé.
// Le poids est un Int pour simplifier.
case class Edge(source: Vertex, destination: Vertex, weight: Int)

object Edge {
  // Encodeur ZIO-JSON pour la classe de cas Edge.
  implicit val encoder: JsonEncoder[Edge] = DeriveJsonEncoder.gen[Edge]
  // Décodeur ZIO-JSON pour la classe de cas Edge.
  implicit val decoder: JsonDecoder[Edge] = DeriveJsonDecoder.gen[Edge]
}