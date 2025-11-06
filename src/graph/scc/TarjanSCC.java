package graph.scc;

import metrics.AlgorithmMetrics;
import java.util.*;

public class TarjanSCC {
    private List<List<Integer>> adj;
    private int time = 0;
    private int[] low, disc;
    private boolean[] stackMember;
    private Deque<Integer> stack;
    private List<List<Integer>> components = new ArrayList<>();
    private AlgorithmMetrics metrics;

    public TarjanSCC(List<List<Integer>> adj) {
        this(adj, null);
    }

    public TarjanSCC(List<List<Integer>> adj, AlgorithmMetrics metrics) {
        this.adj = adj;
        this.metrics = metrics;
        int n = adj.size();
        low = new int[n];
        disc = new int[n];
        stackMember = new boolean[n];
        stack = new ArrayDeque<>();
        Arrays.fill(disc, -1);
        for (int i = 0; i < n; i++)
            if (disc[i] == -1) dfs(i);
    }

    private void dfs(int u) {
        if (metrics != null) metrics.count("dfs_visits");
        disc[u] = low[u] = time++;
        stack.push(u);
        stackMember[u] = true;
        if (metrics != null) metrics.count("stack_pushes");

        for (int v : adj.get(u)) {
            if (metrics != null) metrics.count("edges_processed");
            if (disc[v] == -1) {
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (stackMember[v]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }

        if (low[u] == disc[u]) {
            List<Integer> comp = new ArrayList<>();
            int v;
            do {
                v = stack.pop();
                stackMember[v] = false;
                comp.add(v);
                if (metrics != null) metrics.count("stack_pops");
            } while (v != u);
            components.add(comp);
        }
    }

    public List<List<Integer>> getComponents() {
        return components;
    }
}
