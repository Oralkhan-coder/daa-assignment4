package graph.dagsp;

import metrics.AlgorithmMetrics;
import java.util.*;

public class DAGLongestPath {
    public static int[] longestPath(int src, List<List<int[]>> adj, List<Integer> topo) {
        return longestPath(src, adj, topo, null);
    }

    public static int[] longestPath(int src, List<List<int[]>> adj, List<Integer> topo, AlgorithmMetrics metrics) {
        int n = adj.size();
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MIN_VALUE);
        dist[src] = 0;

        for (int u : topo) {
            if (dist[u] == Integer.MIN_VALUE) continue;
            for (int[] edge : adj.get(u)) {
                int v = edge[0], w = edge[1];
                if (metrics != null) metrics.count("relaxations");
                if (dist[u] + w > dist[v])
                    dist[v] = dist[u] + w;
            }
        }
        return dist;
    }

    public static List<Integer> reconstructPath(int src, int target, int[] dist, List<List<int[]>> adj, List<Integer> topo) {
        if (dist[target] == Integer.MIN_VALUE) return null;
        
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

    public static CriticalPathResult findCriticalPath(int src, List<List<int[]>> adj, List<Integer> topo, AlgorithmMetrics metrics) {
        int[] dist = longestPath(src, adj, topo, metrics);
        int maxDist = Integer.MIN_VALUE;
        int target = -1;
        
        for (int i = 0; i < dist.length; i++) {
            if (dist[i] > maxDist && dist[i] != Integer.MIN_VALUE) {
                maxDist = dist[i];
                target = i;
            }
        }
        
        if (target == -1) return null;
        
        List<Integer> path = reconstructPath(src, target, dist, adj, topo);
        return new CriticalPathResult(path, maxDist);
    }

    public static class CriticalPathResult {
        public final List<Integer> path;
        public final int length;

        public CriticalPathResult(List<Integer> path, int length) {
            this.path = path;
            this.length = length;
        }
    }
}
