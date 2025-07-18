package com.example.graph.viz

import com.example.graph.model._

object GraphViz {

  extension (graph: Graph[?]) {
    def toDotString: String = {
      val edgesStr = graph match {
        case _: DirectedGraph =>
          graph.edges.map(e =>
            s"""  "${e.source}" -> "${e.destination}" [label="${e.weight}"];"""
          ).mkString("\n")

        case _: UndirectedGraph =>
          // Pour éviter la duplication des arêtes A->B et B->A
          val seen = scala.collection.mutable.Set[(Vertex, Vertex)]()
          graph.edges.collect {
            case Edge(src, dst, weight) if !seen((dst, src)) =>
              seen.add((src, dst))
              s"""  "${src}" -- "${dst}" [label="${weight}"];"""
          }.mkString("\n")

        case _ =>
          "// Unknown graph type"
      }

      val graphType = graph match {
        case _: DirectedGraph   => "digraph"
        case _: UndirectedGraph => "graph"
        case _                  => "graph"
      }

      s"""$graphType G {
         |$edgesStr
         |}""".stripMargin
    }
  }
}
