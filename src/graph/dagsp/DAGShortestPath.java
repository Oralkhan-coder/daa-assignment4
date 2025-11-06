package graph.dagsp;

import metrics.AlgorithmMetrics;
import java.util.*;

public class DAGShortestPath {
    public static int[] shortestPath(int src, List<List<int[]>> adj, List<Integer> topo) {
        return shortestPath(src, adj, topo, null);
    }

    public static int[] shortestPath(int src, List<List<int[]>> adj, List<Integer> topo, AlgorithmMetrics metrics) {
        int n = adj.size();
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        for (int u : topo) {
            if (dist[u] == Integer.MAX_VALUE) continue;
            for (int[] edge : adj.get(u)) {
                int v = edge[0], w = edge[1];
                if (metrics != null) metrics.count("relaxations");
                if (dist[u] + w < dist[v])
                    dist[v] = dist[u] + w;
            }
        }
        return dist;
    }

    public static List<Integer> reconstructPath(int src, int target, int[] dist, List<List<int[]>> adj, List<Integer> topo) {
        if (dist[target] == Integer.MAX_VALUE) return null;
        
        List<Integer> path = new ArrayList<>();
        int current = target;
        
        while (current != src) {
            path.add(0, current);
            boolean found = false;
            for (int u : topo) {
                if (u == current) continue;
                for (int[] edge : adj.get(u)) {
                    int v = edge[0], w = edge[1];
                    if (v == current && dist[u] + w == dist[current]) {
                        current = u;
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            if (!found) break;
        }
        
        path.add(0, src);
        return path;
    }
}
