# Assignment 4: Smart City / Smart Campus Scheduling

## Overview

This project implements graph algorithms for task scheduling in a smart city/campus scenario:
1. Strongly Connected Components (SCC) detection using Tarjan's algorithm
2. Topological sorting using Kahn's algorithm
3. Shortest and longest paths in Directed Acyclic Graphs (DAGs)

## Project Structure

```
src/
├── Main.java                    # Main entry point
├── data/                        # Dataset directory
│   ├── *.json                  # Graph datasets (9 total)
│   └── DATASETS.md             # Dataset documentation
├── graph/
│   ├── scc/
│   │   ├── TarjanSCC.java     # SCC detection
│   │   └── CondensationGraph.java  # Build DAG from SCCs
│   ├── topo/
│   │   └── KahnTopo.java      # Topological sort
│   └── dagsp/
│       ├── DAGShortestPath.java  # Shortest paths in DAG
│       └── DAGLongestPath.java   # Longest paths (critical path)
├── metrics/
│   ├── Metrics.java            # Metrics interface
│   └── AlgorithmMetrics.java  # Metrics implementation
└── test/java/
    └── GraphAlgorithmsTest.java  # JUnit tests
```

## Algorithms Implemented

### 1. Strongly Connected Components (Tarjan)
- Detects all SCCs in a directed graph
- Outputs list of components and their sizes
- Builds condensation graph (DAG of components)

### 2. Topological Sort (Kahn)
- Computes topological order of condensation DAG
- Outputs valid order of components
- Derives order of original tasks after SCC compression

### 3. Shortest Paths in DAG
- Single-source shortest paths using DP over topological order
- Reconstructs optimal paths

### 4. Longest Paths in DAG (Critical Path)
- Finds longest path using DP over topological order
- Identifies critical path and its length

## Weight Model

The project uses **edge weights** for all path calculations. Edge weights represent task dependencies or costs.

## Datasets

All datasets are stored in `/data/` directory. There are 9 datasets total:

- **Small (6-10 nodes)**: 3 datasets
  - `small_sparse_dag.json` - Sparse DAG
  - `small_dense_cyclic.json` - Dense cyclic graph
  - `small_mixed_cycles.json` - Mixed structure

- **Medium (10-20 nodes)**: 3 datasets
  - `medium_sparse_multiple_scc.json` - Multiple SCCs
  - `medium_dense_dag.json` - Dense DAG
  - `medium_mixed_cyclic.json` - Mixed structure

- **Large (20-50 nodes)**: 3 datasets
  - `large_sparse_dag.json` - Sparse DAG
  - `large_dense_multiple_scc.json` - Multiple SCCs
  - `large_mixed_structure.json` - Mixed structure

See `data/DATASETS.md` for detailed documentation of each dataset.

## Dataset Format

All datasets use JSON format:
```json
{
  "directed": true,
  "n": <number_of_nodes>,
  "edges": [
    {"u": <from>, "v": <to>, "w": <weight>},
    ...
  ],
  "source": <source_node>,
  "weight_model": "edge"
}
```

## Building and Running

### Prerequisites
- Java JDK 8 or higher
- JUnit 4 (for tests)

### Compilation

```bash
cd src
javac -d ../out Main.java graph/**/*.java data/*.java metrics/*.java
```

### Running

```bash
java -cp out Main
```

This will process all JSON datasets in the `data/` directory and write all results to `output.json`.

### Running Tests

```bash
javac -cp ".:junit-4.13.2.jar:hamcrest-core-1.3.jar" test/java/GraphAlgorithmsTest.java
java -cp ".:junit-4.13.2.jar:hamcrest-core-1.3.jar:../out" org.junit.runner.JUnitCore test.java.GraphAlgorithmsTest
```

## Output

The program processes all datasets in the `data/` directory and writes all results to `output.json`. The output file contains:

For each dataset:
1. **Graph Info**: Number of nodes, edges, and source node
2. **SCC Components**: List of strongly connected components with sizes and metrics
3. **Condensation Graph**: DAG structure of components (edges between components)
4. **Topological Sort**: Component order and derived task order with metrics
5. **Shortest Paths**: Distances from source to all components with metrics
6. **Longest Paths**: Distances, critical path, and metrics

All metrics include timing (in milliseconds) and operation counters for each algorithm.

## Instrumentation

The project includes comprehensive metrics:
- **SCC**: DFS visits, edges processed, stack pushes/pops
- **Topological Sort**: Queue pushes/pops
- **Path Algorithms**: Relaxations performed

All metrics include timing via `System.nanoTime()`.

## Code Quality

- **Packages**: Proper package structure (`graph.scc`, `graph.topo`, `graph.dagsp`)
- **Clean Code**: Simple, readable code with good variable/function names
- **No Comments**: Code is self-documenting
- **Tests**: JUnit tests for all algorithms with edge cases

## Example Output

```
============================================================
Processing: data/small_sparse_dag.json
Nodes: 8, Edges: 9
============================================================

1. STRONGLY CONNECTED COMPONENTS (Tarjan)
Number of SCCs: 8
  Component 1: [0] (size: 1)
  Component 2: [1] (size: 1)
  ...
Metrics:
  Time: 0.123 ms
  dfs_visits: 8
  edges_processed: 9

2. CONDENSATION GRAPH (DAG of SCCs)
...

3. TOPOLOGICAL SORT (Kahn)
Component order: [0, 1, 2, 3, 4, 5, 6, 7]
Task order: [0, 1, 2, 3, 4, 5, 6, 7]
Metrics:
  Time: 0.045 ms
  queue_pushes: 8
  queue_pops: 8

4. SHORTEST PATHS IN DAG
...

5. LONGEST PATHS IN DAG (Critical Path)
Critical Path: [0, 1, 4, 5, 6, 7]
Critical Path Length: 21
...
```

## Analysis

The implementation handles:
- **Sparse and dense graphs**: Different density levels tested
- **Cyclic and acyclic structures**: Both types of graphs supported
- **Multiple SCCs**: Graphs with multiple strongly connected components
- **Performance**: Timing and counters for scalability analysis

## License

This project is part of a course assignment.
