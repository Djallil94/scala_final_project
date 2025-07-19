# 📘 Functional Programming in Scala 3 – Functional Graphs

This project implements a **graph data structure library** using functional programming principles in **Scala 3**, integrates it into a **ZIO 2 application**, and provides comprehensive documentation.

---

## 📚 Table of Contents

- [Project Overview](#project-overview)
- [How to Build, Test, and Run](#how-to-build-test-and-run)
  - [Prerequisites](#prerequisites)
  - [Project Structure](#project-structure)
  - [Building the Project](#building-the-project)
  - [Running Tests](#running-tests)
  - [Running the Application](#running-the-application)
- [Design Decisions](#design-decisions)
  - [Functional Programming Principles](#functional-programming-principles)
  - [Graph Data Structure](#graph-data-structure)
  - [Graph Operations](#graph-operations)
  - [ZIO Application](#zio-application)
- [Usage Examples](#usage-examples)
- [Tests](#tests)
  - [Running Tests](#running-tests-1)
  - [Test Coverage](#test-coverage)

---

## 🧩 Project Overview

The project is structured into two main **SBT subprojects**:

- **`graph-core`**:  
  A core library providing:
  - Immutable graph data structures (Directed and Undirected)
  - Common graph operations (DFS, BFS, Cycle Detection, Dijkstra's Algorithm)
  - JSON encoding/decoding
  - GraphViz DOT language representation

- **`graph-app`**:  
  An interactive command-line application based on **ZIO 2** that demonstrates usage of the `graph-core` library.

---

## ⚙️ How to Build, Test, and Run

### 📋 Prerequisites

- **Java Development Kit (JDK)**: Version 11 or higher (e.g., OpenJDK 17)
- **SBT (Scala Build Tool)**: Version 1.11.3 or compatible
- **IDE**: IntelliJ IDEA (recommended) or a terminal + text editor

---

### 🛠️ Building the Project

Navigate to the project root directory (`functional-graphs/`) and run:

```bash
sbt compile
This compiles both graph-core and graph-app subprojects.

✅ Running Tests
From the project root:

bash
Copier
Modifier
sbt test
To run tests for a specific subproject (e.g., graph-core):

bash
Copier
Modifier
sbt graph-core/test
▶️ Running the Application
Run the ZIO-based interactive CLI:

bash
Copier
Modifier
sbt graph-app/run
This will start the terminal application.

🧠 Design Decisions
🔹 Functional Programming Principles
Immutability:
Graph structures (DirectedGraph, UndirectedGraph, Edge) are immutable.
Methods like addEdge and removeEdge return new instances.

Pure Functions:
Algorithms (DFS, BFS, Dijkstra, Cycle Detection) are side-effect free.

Recursion & Tail Recursion:
Used where appropriate to avoid stack overflows and maintain functional style.

Extension Methods:
toDotString is implemented as a Scala 3 extension method.

ZIO for Side Effects:
All side effects (console I/O, file I/O) are wrapped in ZIO effects, separating them from pure logic.

🔹 Graph Data Structure
Generic Graph Trait:
A Graph[G <: Graph[G]] trait defines a shared interface for DirectedGraph and UndirectedGraph, using a self-type annotation (self: G =>).

Edge Representation:
Edges are defined as:

scala
Copier
Modifier
case class Edge(source: Vertex, destination: Vertex, weight: Int)
All edges require weights.

Internal Representation:

DirectedGraph: Stores a Set[Edge]. Vertices are derived from edges.

UndirectedGraph: Stores two directed edges per undirected edge (A→B and B→A). Simplifies neighbor lookups.

🔹 Graph Operations
Decoupling:
All operations are implemented in a GraphOperations object and apply to any G <: Graph[G].

Dijkstra’s Algorithm:
Uses local vars for performance in a tailrec loop. Remains pure by avoiding shared state.

🔹 ZIO Application
Interactive Menu:
The CLI allows users to build and explore graphs interactively.

State Management:
Graph state is passed explicitly between effects — no global mutable state.

Error Handling:
Uses ZIO.attempt and orElseFail to manage input errors safely.

JSON Integration:
Uses zio-json for toJsonPretty and fromJson.

GraphViz Integration:
Exports to DOT language, compatible with tools like GraphvizOnline.

🧪 Usage Examples
Start the app:

bash
Copier
Modifier
sbt graph-app/run
📋 CLI Flow:
Choose graph type: Enter D (Directed) or U (Undirected)

Add edges:

Select option 1

Enter source, destination, and weight (e.g., A, B, 10)

Remove edges:

Select option 2

Enter source, destination, and weight

Show graph:

Select option 3 to view current vertices and edges

Run operations:

4: DFS

5: BFS

6: Cycle detection

7: Dijkstra

Export / Import:

8: Export to JSON

9: Import from JSON

10: Generate DOT string

Exit:

Select option 11

🧪 Tests
Unit tests are implemented using ScalaTest's FlatSpec style.

🗂️ Test Files
graph-core/src/test/scala/com/example/graph/model/GraphSpec.scala
Tests adding/removing edges, vertex/edge counts, neighbors, and JSON handling.

graph-core/src/test/scala/com/example/graph/ops/GraphOperationsSpec.scala
Tests DFS, BFS, Cycle Detection, Dijkstra on various graph configurations.

📈 Test Coverage
Tested features include:

Graph creation and properties (empty, vertex/edge count)

Adding/removing edges (directed + undirected)

Reciprocal edge handling in undirected graphs

Neighbor retrieval (normal and weighted)

JSON encoding/decoding

GraphViz DOT output (directed vs. undirected)

DFS, BFS traversal from multiple nodes

Cycle detection (self-loops, complex cycles)

Dijkstra's algorithm (including disconnected nodes)

The suite aims to provide comprehensive coverage for correctness and reliability.
