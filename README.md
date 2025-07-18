Programmation Fonctionnelle en Scala 3 - Graphes Fonctionnels
Ce projet implémente une bibliothèque de structures de données de graphes en utilisant les principes de la programmation fonctionnelle en Scala 3, l'intègre dans une application ZIO 2 et fournit une documentation complète.

Aperçu du Projet
Le projet est structuré en deux sous-projets SBT principaux :

graph-core : Une bibliothèque de base fournissant des structures de données de graphes immutables (Dirigés et Non-dirigés), des opérations de graphes courantes (DFS, BFS, Détection de Cycle, Algorithme de Dijkstra), l'encodage/décodage JSON, et la représentation en langage DOT de GraphViz.

graph-app : Une application interactive en ligne de commande basée sur ZIO 2 qui démontre l'utilisation de la bibliothèque graph-core.

Comment Construire, Tester et Exécuter
Prérequis
Java Development Kit (JDK) : Version 11 ou supérieure (par exemple, OpenJDK 17).

SBT (Scala Build Tool) : Version 1.11.3 (ou compatible).

IntelliJ IDEA (IDE Recommandé) ou un éditeur de texte et un terminal.
``
Structure du Projet
functional-graphs/
├── build.sbt
├── graph-core/
│   ├── src/
│   │   ├── main/
│   │   │   └── scala/
│   │   │       └── com/
│   │   │           └── example/
│   │   │               └── graph/
│   │   │                   ├── model/
│   │   │                   │   ├── Edge.scala            (Classe de cas et Objet Compagnon)
│   │   │                   │   ├── Graph.scala           (Trait)
│   │   │                   │   ├── DirectedGraph.scala   (Classe de cas et Objet Compagnon)
│   │   │                   │   └── UndirectedGraph.scala (Classe de cas et Objet Compagnon)
│   │   │                   ├── ops/
│   │   │                   │   └── GraphOperations.scala (Objet avec méthodes statiques)
│   │   │                   └── viz/
│   │   │                       └── GraphViz.scala        (Objet avec méthode d'extension)
│   │   └── test/
│   │       └── scala/
│   │           └── com/
│   │               └── example/
│   │                   └── graph/
│   │                       ├── model/
│   │                       │   └── GraphSpec.scala       (Classe ScalaTest)
│   │                       └── ops/
│   │                           └── GraphOperationsSpec.scala (Classe ScalaTest)
├── graph-app/
│   ├── src/
│   │   └── main/
│   │       └── scala/
│   │           └── com/
│   │               └── example/
│   │                   └── graph/
│   │                       └── app/
│   │                           └── GraphApp.scala        (Objet d'Application ZIO)
└── README.md
``
Construction du Projet
Naviguer vers la racine du projet : Ouvrez votre terminal ou invite de commande et changez votre répertoire pour functional-graphs/.

Compiler : Exécutez la commande compile de SBT.

``
sbt compile
``

Cela compilera les deux sous-projets graph-core et graph-app.

Exécution des Tests
Naviguer vers la racine du projet (si ce n'est pas déjà fait).

Exécuter tous les tests :

``
sbt test
``

Pour exécuter les tests d'un sous-projet spécifique (par exemple, graph-core) :

``
sbt graph-core/test
``

Exécution de l'Application
Naviguer vers la racine du projet (si ce n'est pas déjà fait).

Exécuter l'application ZIO :

``
sbt graph-app/run
``

Cela démarrera l'application interactive en terminal.

Décisions de Conception
Principes de Programmation Fonctionnelle
Immutabilité : Toutes les structures de données de graphes (DirectedGraph, UndirectedGraph, Edge) sont implémentées comme des case classes immutables. Les opérations comme addEdge et removeEdge retournent de nouvelles instances du graphe plutôt que de modifier celles existantes. Cela favorise la transparence référentielle et simplifie le raisonnement sur le code.

Fonctions Pures : Les opérations de graphes (DFS, BFS, Dijkstra, Détection de Cycle) sont implémentées comme des fonctions pures qui prennent un graphe et retournent un résultat, sans provoquer d'effets de bord.

Récursion et Récursion Terminale : Les algorithmes comme DFS, BFS et la Détection de Cycle sont implémentés en utilisant la récursion terminale lorsque cela est approprié pour éviter les erreurs de débordement de pile (StackOverflowError) et maintenir un style fonctionnel.

Méthodes d'Extension : La méthode toDotString pour la représentation GraphViz est ajoutée en utilisant les méthodes extension de Scala 3, ce qui est une manière fonctionnelle d'étendre les types existants sans les modifier directement.

ZIO pour les Effets de Bord : Toutes les opérations à effets de bord (E/S console, E/S fichier potentielle pour JSON) dans graph-app sont encapsulées dans des effets ZIO, séparant clairement la logique pure des opérations impures. Cela garantit que les effets de bord de l'application sont explicitement gérés et composables.

Structure de Données du Graphe
Trait Graph Générique : Un trait Graph[G <: Graph[G]] est défini, permettant à différentes implémentations de graphes de partager une interface commune tout en conservant leurs informations de type spécifiques (par exemple, DirectedGraph vs. UndirectedGraph). L'annotation de type self: G => garantit que les méthodes retournant G retournent le sous-type concret.

Représentation des Arêtes : Les arêtes sont de simples case class Edge(source: Vertex, destination: Vertex, weight: Int). Les poids sont obligatoires pour toutes les arêtes selon les exigences du projet.

Représentation Interne :

DirectedGraph : Stocke un Set[Edge] directement. Les sommets sont dérivés des arêtes.

UndirectedGraph : Stocke un Set[Edge] où chaque arête non dirigée conceptuelle (A--B) est représentée par deux arêtes dirigées (A->B et B->A) en interne. Cela simplifie les recherches de voisins et réutilise la définition de Edge. Les opérations comme addEdge et removeEdge gèrent automatiquement les deux directions.

Opérations de Graphe
Découplage : Les opérations de graphes (objet GraphOperations) sont conçues pour être indépendantes de l'implémentation spécifique du graphe (DirectedGraph ou UndirectedGraph). Elles opèrent sur le trait Graph générique, favorisant la réutilisabilité en acceptant G <: Graph[G] comme paramètre de leurs méthodes.

Algorithme de Dijkstra : Implémenté en utilisant des var mutables pour les ensembles distances et unvisited dans une boucle tailrec. Bien que des var soient utilisées, la fonction elle-même reste pure car elle ne modifie aucun état externe et produit la même sortie pour la même entrée. C'est une approche pragmatique courante pour Dijkstra dans un contexte fonctionnel afin de maintenir les performances tout en encapsulant la mutabilité localement.

Application ZIO
Menu Interactif : L'application GraphApp fournit une interface en ligne de commande simple permettant aux utilisateurs d'interagir avec le graphe.

Gestion de l'État : L'état du graphe est passé explicitement entre les effets ZIO, démontrant une gestion de l'état fonctionnelle sans variables globales mutables. L'AppState est maintenant générique pour maintenir la sûreté des types tout au long de l'application.

Gestion des Erreurs : Les méthodes ZIO.attempt et orElseFail de ZIO sont utilisées pour gérer les erreurs potentielles provenant des entrées utilisateur (par exemple, un poids non entier).

Intégration JSON : Utilise zio-json pour un encodage (toJsonPretty) et un décodage (fromJson) simples des objets graphes.

Intégration GraphViz : Démontre comment générer et afficher la représentation en langage DOT, qui peut ensuite être collée dans des outils en ligne pour la visualisation.

Exemples d'Utilisation
L'application graph-app fournit un menu interactif. Voici un flux typique :

Démarrer l'application : 

``
sbt graph-app/run
``

Choisir le type de graphe : Entrez D pour Dirigé ou U pour Non-dirigé.

Ajouter des arêtes :

Sélectionnez l'option 1.

Entrez le sommet source, le sommet de destination et le poids (par exemple, A, B, 10).

Répétez pour construire votre graphe.

Afficher les détails : Sélectionnez l'option 3 pour voir les sommets et les arêtes actuels.

Effectuer des opérations :

Sélectionnez 4 pour DFS (entrez le sommet de départ).

Sélectionnez 5 pour BFS (entrez le sommet de départ).

Sélectionnez 6 pour vérifier les cycles.

Sélectionnez 7 pour Dijkstra (entrez le sommet de départ).

JSON/GraphViz :

Sélectionnez 8 pour exporter en JSON.

Sélectionnez 10 pour générer la chaîne DOT de GraphViz. Copiez cette sortie et collez-la dans un visualiseur GraphViz en ligne (par exemple, GraphvizOnline).

Sélectionnez 9 pour importer depuis JSON (collez une chaîne JSON valide pour le type de graphe actuel).

Quitter : Sélectionnez l'option 11.

Tests
Les tests unitaires sont implémentés en utilisant le style FlatSpec de ScalaTest, offrant des spécifications de test claires et lisibles.

graph-core/src/test/scala/com/example/graph/model/GraphSpec.scala : Teste les implémentations de l'interface Graph (DirectedGraph, UndirectedGraph), assurant un comportement correct pour l'ajout/suppression d'arêtes, le comptage des sommets/arêtes, les recherches de voisins et la sérialisation/désérialisation JSON.

graph-core/src/test/scala/com/example/graph/ops/GraphOperationsSpec.scala : Teste les algorithmes de graphes (DFS, BFS, hasCycle, dijkstra) sur divers scénarios de graphes, y compris les composants déconnectés et les cycles.

Exécution des Tests
Comme mentionné dans la section "Comment Construire, Tester et Exécuter", vous pouvez exécuter tous les tests depuis la racine du projet en utilisant sbt test ou les tests d'un sous-projet spécifique en utilisant sbt <sous-projet>/test.

Couverture des Tests
Les tests couvrent :

La construction du graphe et les propriétés de base (graphe vide, nombre de sommets/arêtes).

L'ajout et la suppression d'arêtes pour les graphes dirigés et non dirigés, y compris la gestion des arêtes réciproques pour les graphes non dirigés.

L'identification correcte des voisins et la récupération des voisins pondérés.

L'encodage et le décodage JSON pour les deux types de graphes.

La génération de la chaîne DOT de GraphViz pour les deux types de graphes, y compris la direction des arêtes et les poids.

Les parcours DFS et BFS à partir de divers points de départ, y compris les graphes déconnectés.

La détection de cycles dans les graphes dirigés et non dirigés, y compris les boucles sur soi-même et les cycles complexes.

L'algorithme de Dijkstra pour les plus courts chemins, y compris les nœuds inatteignables et diverses structures de graphes.

La suite de tests vise une couverture complète des fonctionnalités implémentées pour assurer la correction et la conformité aux spécifications.
