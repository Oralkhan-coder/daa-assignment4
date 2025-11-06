package graph.topo;

import metrics.AlgorithmMetrics;
import java.util.*;

public class KahnTopo {
    public static List<Integer> topologicalSort(List<List<Integer>> adj) {
        return topologicalSort(adj, null);
    }

    public static List<Integer> topologicalSort(List<List<Integer>> adj, AlgorithmMetrics metrics) {
        int n = adj.size();
        int[] indeg = new int[n];
        for (int u = 0; u < n; u++)
            for (int v : adj.get(u)) indeg[v]++;

        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (indeg[i] == 0) {
                q.add(i);
                if (metrics != null) metrics.count("queue_pushes");
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int u = q.poll();
            order.add(u);
            if (metrics != null) metrics.count("queue_pops");
            for (int v : adj.get(u)) {
                indeg[v]--;
                if (indeg[v] == 0) {
                    q.add(v);
                    if (metrics != null) metrics.count("queue_pushes");
                }
            }
        }
        return order;
    }
}
