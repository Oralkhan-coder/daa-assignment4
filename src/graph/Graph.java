package graph;

import java.util.*;

public class Graph {
    private int n;
    private List<List<Edge>> adj;

    public Graph(int n) {
        this.n = n;
        adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int from, int to, int weight) {
        adj.get(from).add(new Edge(from, to, weight));
    }

    public List<List<Edge>> getAdj() { return adj; }
    public int size() { return n; }
}
