package graph.scc;

import java.util.*;

public class CondensationGraph {
    private List<List<Integer>> originalAdj;
    private List<List<Integer>> components;
    private Map<Integer, Integer> nodeToComponent;
    private List<List<Integer>> condensationAdj;
    private List<List<int[]>> condensationWeightedAdj;

    public CondensationGraph(List<List<Integer>> originalAdj, List<List<Integer>> components) {
        this.originalAdj = originalAdj;
        this.components = components;
        this.nodeToComponent = new HashMap<>();
        
        for (int compId = 0; compId < components.size(); compId++) {
            for (int node : components.get(compId)) {
                nodeToComponent.put(node, compId);
            }
        }
        
        buildCondensationGraph();
    }

    private void buildCondensationGraph() {
        int numComponents = components.size();
        condensationAdj = new ArrayList<>();
        condensationWeightedAdj = new ArrayList<>();
        
        for (int i = 0; i < numComponents; i++) {
            condensationAdj.add(new ArrayList<>());
            condensationWeightedAdj.add(new ArrayList<>());
        }
        
        Set<String> edgeSet = new HashSet<>();
        
        for (int u = 0; u < originalAdj.size(); u++) {
            int compU = nodeToComponent.get(u);
            for (int v : originalAdj.get(u)) {
                int compV = nodeToComponent.get(v);
                if (compU != compV) {
                    String edge = compU + "," + compV;
                    if (!edgeSet.contains(edge)) {
                        edgeSet.add(edge);
                        condensationAdj.get(compU).add(compV);
                    }
                }
            }
        }
    }

    public List<List<Integer>> getCondensationAdj() {
        return condensationAdj;
    }

    public List<List<Integer>> getComponents() {
        return components;
    }

    public Map<Integer, Integer> getNodeToComponent() {
        return nodeToComponent;
    }

    public List<Integer> getComponentOrder(List<Integer> condensationTopo) {
        List<Integer> taskOrder = new ArrayList<>();
        for (int compId : condensationTopo) {
            taskOrder.addAll(components.get(compId));
        }
        return taskOrder;
    }

    public List<List<int[]>> buildWeightedCondensation(List<List<int[]>> originalWeightedAdj) {
        int numComponents = components.size();
        List<List<int[]>> weightedCondensation = new ArrayList<>();
        
        for (int i = 0; i < numComponents; i++) {
            weightedCondensation.add(new ArrayList<>());
        }
        
        Map<String, Integer> minWeight = new HashMap<>();
        
        for (int u = 0; u < originalWeightedAdj.size(); u++) {
            int compU = nodeToComponent.get(u);
            for (int[] edge : originalWeightedAdj.get(u)) {
                int v = edge[0];
                int w = edge[1];
                int compV = nodeToComponent.get(v);
                
                if (compU != compV) {
                    String edgeKey = compU + "," + compV;
                    if (!minWeight.containsKey(edgeKey) || w < minWeight.get(edgeKey)) {
                        minWeight.put(edgeKey, w);
                    }
                }
            }
        }
        
        for (Map.Entry<String, Integer> entry : minWeight.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int compU = Integer.parseInt(parts[0]);
            int compV = Integer.parseInt(parts[1]);
            int weight = entry.getValue();
            weightedCondensation.get(compU).add(new int[]{compV, weight});
        }
        
        return weightedCondensation;
    }
}

