Functional Programming in Scala 3 - Functional Graphs
This project implements a graph data structure library using functional programming principles in Scala 3, integrates it into a ZIO 2 application, and provides comprehensive documentation.

Project Overview
The project is structured into two main SBT subprojects:

graph-core: A core library providing immutable graph data structures (Directed and Undirected), common graph operations (DFS, BFS, Cycle Detection, Dijkstra's Algorithm), JSON encoding/decoding, and GraphViz DOT language representation.

graph-app: An interactive command-line application based on ZIO 2 that demonstrates the usage of the graph-core library.

How to Build, Test, and Run
Prerequisites
Java Development Kit (JDK): Version 11 or higher (e.g., OpenJDK 17).

SBT (Scala Build Tool): Version 1.11.3 (or compatible).

IntelliJ IDEA (Recommended IDE) or a text editor and terminal.

Building the Project
Navigate to the project root: Open your terminal or command prompt and change your directory to functional-graphs/.

Compile: Run the SBT compile command.
``
sbt compile
``
This will compile both graph-core and graph-app subprojects.

Running Tests
Navigate to the project root (if not already there).

Run all tests:
``
sbt test
``
To run tests for a specific subproject (e.g., graph-core):
``
sbt graph-core/test
``
Running the Application
Navigate to the project root (if not already there).

Run the ZIO application:
``
sbt graph-app/run
``
This will start the interactive terminal application.

Design Decisions
Functional Programming Principles
Immutability: All graph data structures (DirectedGraph, UndirectedGraph, Edge) are implemented as immutable case classes. Operations like addEdge and removeEdge return new instances of the graph rather than modifying existing ones. This promotes referential transparency and simplifies reasoning about the code.

Pure Functions: Graph operations (DFS, BFS, Dijkstra, Cycle Detection) are implemented as pure functions that take a graph and return a result, without causing side effects.

Recursion & Tail Recursion: Algorithms like DFS, BFS, and Cycle Detection are implemented using tail recursion where appropriate to avoid stack overflow errors and maintain a functional style.

Extension Methods: The toDotString method for GraphViz representation is added using Scala 3's extension methods, which is a functional way to extend existing types without modifying them directly.

ZIO for Side Effects: All side-effecting operations (console I/O, potential file I/O for JSON) in graph-app are wrapped in ZIO effects, clearly separating pure logic from impure operations. This ensures that the application's side effects are explicitly managed and composable.

Graph Data Structure
Generic Graph Trait: A Graph[G <: Graph[G]] trait is defined, allowing for different graph implementations to share a common interface while maintaining their specific type information (e.g., DirectedGraph vs. UndirectedGraph). The self-type annotation self: G => ensures that methods returning G return the concrete subtype.

Edge Representation: Edges are simple case class Edge(source: Vertex, destination: Vertex, weight: Int). Weights are mandatory for all edges as per project requirements.

Internal Representation:

DirectedGraph: Stores a Set[Edge] directly. Vertices are derived from the edges.

UndirectedGraph: Stores a Set[Edge] where each conceptual undirected edge (A--B) is represented by two directed edges (A->B and B->A) internally. This simplifies neighbor lookups and reuses the Edge definition. Operations like addEdge and removeEdge automatically handle both directions.

Graph Operations
Decoupling: Graph operations (GraphOperations object) are designed to be independent of the specific graph implementation (DirectedGraph or UndirectedGraph). They operate on the generic Graph trait, promoting reusability by accepting G <: Graph[G] as a parameter to their methods.

Dijkstra's Algorithm: Implemented using mutable vars for distances and unvisited sets within a tailrec loop. While vars are used, the function itself remains pure as it does not modify any external state and produces the same output for the same input. This is a common pragmatic approach for Dijkstra's in a functional context to maintain performance while still encapsulating mutability locally.

ZIO Application
Interactive Menu: The GraphApp provides a simple command-line interface for users to interact with the graph.

State Management: The graph state is passed explicitly between ZIO effects, demonstrating functional state management without mutable global variables. The AppState is now generic to maintain type safety throughout the application.

Error Handling: ZIO's ZIO.attempt and orElseFail are used for handling potential errors from user input (e.g., non-integer weight).

JSON Integration: Uses zio-json for straightforward encoding (toJsonPretty) and decoding (fromJson) of graph objects.

GraphViz Integration: Demonstrates how to generate and display the DOT language representation, which can then be pasted into online tools for visualization.

Usage Examples
The graph-app provides an interactive menu. Here's a typical flow:

Start the app: 
``
sbt graph-app/run
``
Choose graph type: Enter D for Directed or U for Undirected.

Add edges:

Select option 1.

Enter source, destination, and weight (e.g., A, B, 10).

Repeat to build your graph.

To delete edges:

Select 2

Enter source, destination, and weight (e.g., C, D, 11).

Show details: Select option 3 to see current vertices and edges.

Perform operations:

Select 4 for DFS (enter start vertex).

Select 5 for BFS (enter start vertex).

Select 6 to check for cycles.

Select 7 for Dijkstra (enter start vertex).

JSON/GraphViz:

Select 8 to export to JSON.

Select 10 to generate GraphViz DOT string. Copy this output and paste it into an online GraphViz viewer (e.g., GraphvizOnline).

Select 9 to import from JSON (paste a valid JSON string for the current graph type).

Exit: Select option 11.

Tests
Unit tests are implemented using ScalaTest's FlatSpec style, providing clear and readable test specifications.

graph-core/src/test/scala/com/example/graph/model/GraphSpec.scala: Tests the core Graph interface implementations (DirectedGraph, UndirectedGraph), ensuring correct behavior for adding/removing edges, vertex/edge counts, neighbor lookups, and JSON serialization/deserialization.

graph-core/src/test/scala/com/example/graph/ops/GraphOperationsSpec.scala: Tests the graph algorithms (DFS, BFS, hasCycle, dijkstra) on various graph scenarios, including disconnected components and cycles.

Running Tests
As mentioned in the "How to Build, Test, and Run" section, you can run all tests from the project root using ``sbt test`` or specific subproject tests using ``sbt <subproject>/test``.

Test Coverage
Tests cover:

Graph construction and basic properties (empty graph, vertex/edge counts).

Edge addition and removal for both directed and undirected graphs, including handling of reciprocal edges for undirected graphs.

Correct neighbor identification and weighted neighbor retrieval.

JSON encoding and decoding for both graph types.

GraphViz DOT string generation for both graph types, including edge direction and weights.

DFS and BFS traversals from various start points, including disconnected graphs.

Cycle detection in both directed and undirected graphs, including self-loops and complex cycles.

Dijkstra's algorithm for shortest paths, including unreachable nodes and various graph structures.

The test suite aims for comprehensive coverage of the implemented functionalities to ensure correctness and adherence to specifications.
