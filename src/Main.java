import graph.scc.TarjanSCC;
import graph.scc.CondensationGraph;
import graph.topo.KahnTopo;
import graph.dagsp.DAGShortestPath;
import graph.dagsp.DAGLongestPath;
import data.GraphLoader;
import metrics.AlgorithmMetrics;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) {
        List<DatasetResult> allResults = new ArrayList<>();
        
        try {
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir = new File("src/data");
            }
            
            File[] jsonFiles = dataDir.listFiles((dir, name) -> name.endsWith(".json") && !name.equals("tasks_small.json"));
            
            if (jsonFiles == null || jsonFiles.length == 0) {
                System.out.println("No JSON datasets found in data directory");
                return;
            }
            
            System.out.println("Processing " + jsonFiles.length + " datasets...");
            
            for (File file : jsonFiles) {
                try {
                    String filePath = file.getPath();
                    GraphLoader.GraphData graphData = GraphLoader.loadGraph(filePath);
                    DatasetResult result = processGraph(graphData, file.getName());
                    allResults.add(result);
                    System.out.println("Processed: " + file.getName());
                } catch (Exception e) {
                    System.err.println("Error processing " + file.getName() + ": " + e.getMessage());
                }
            }
            
            writeResultsToFile(allResults, "output.json");
            System.out.println("\nAll results written to output.json");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static DatasetResult processGraph(GraphLoader.GraphData graphData, String datasetName) {
        List<List<Integer>> adj = buildAdjacencyList(graphData.n, graphData.edges);
        List<List<int[]>> weightedAdj = buildWeightedAdjacencyList(graphData.n, graphData.edges);

        AlgorithmMetrics sccMetrics = new AlgorithmMetrics();
        sccMetrics.start();
        TarjanSCC scc = new TarjanSCC(adj, sccMetrics);
        sccMetrics.stop();

        List<List<Integer>> components = scc.getComponents();
        
        CondensationGraph condensation = new CondensationGraph(adj, components);
        List<List<Integer>> condensationAdj = condensation.getCondensationAdj();

        AlgorithmMetrics topoMetrics = new AlgorithmMetrics();
        topoMetrics.start();
        List<Integer> condensationTopo = KahnTopo.topologicalSort(condensationAdj, topoMetrics);
        topoMetrics.stop();

        List<Integer> taskOrder = condensation.getComponentOrder(condensationTopo);
        List<List<int[]>> weightedCondensation = condensation.buildWeightedCondensation(weightedAdj);
        int sourceComponent = condensation.getNodeToComponent().get(graphData.source);
        
        AlgorithmMetrics shortestMetrics = new AlgorithmMetrics();
        shortestMetrics.start();
        int[] shortestDist = DAGShortestPath.shortestPath(sourceComponent, weightedCondensation, condensationTopo, shortestMetrics);
        shortestMetrics.stop();

        AlgorithmMetrics longestMetrics = new AlgorithmMetrics();
        longestMetrics.start();
        int[] longestDist = DAGLongestPath.longestPath(sourceComponent, weightedCondensation, condensationTopo, longestMetrics);
        DAGLongestPath.CriticalPathResult criticalPath = DAGLongestPath.findCriticalPath(sourceComponent, weightedCondensation, condensationTopo, longestMetrics);
        longestMetrics.stop();

        Map<String, Integer> sccCounters = sccMetrics.getCounters();
        Map<String, Integer> topoCounters = topoMetrics.getCounters();
        Map<String, Integer> shortestCounters = shortestMetrics.getCounters();
        Map<String, Integer> longestCounters = longestMetrics.getCounters();

        List<ComponentInfo> componentInfos = new ArrayList<>();
        for (int i = 0; i < components.size(); i++) {
            componentInfos.add(new ComponentInfo(i, components.get(i)));
        }

        List<CondensationEdge> condensationEdges = new ArrayList<>();
        for (int i = 0; i < condensationAdj.size(); i++) {
            for (int j : condensationAdj.get(i)) {
                condensationEdges.add(new CondensationEdge(i, j));
            }
        }

        Map<Integer, Integer> shortestDistances = new HashMap<>();
        for (int i = 0; i < shortestDist.length; i++) {
            if (shortestDist[i] != Integer.MAX_VALUE) {
                shortestDistances.put(i, shortestDist[i]);
            }
        }

        Map<Integer, Integer> longestDistances = new HashMap<>();
        for (int i = 0; i < longestDist.length; i++) {
            if (longestDist[i] != Integer.MIN_VALUE) {
                longestDistances.put(i, longestDist[i]);
            }
        }

        return new DatasetResult(
            datasetName,
            graphData.n,
            graphData.edges.size(),
            graphData.source,
            components.size(),
            componentInfos,
            condensationEdges,
            condensationTopo,
            taskOrder,
            sourceComponent,
            shortestDistances,
            longestDistances,
            criticalPath != null ? criticalPath.path : null,
            criticalPath != null ? criticalPath.length : null,
            sccMetrics.getTimeMillis(),
            sccCounters,
            topoMetrics.getTimeMillis(),
            topoCounters,
            shortestMetrics.getTimeMillis(),
            shortestCounters,
            longestMetrics.getTimeMillis(),
            longestCounters
        );
    }

    private static void writeResultsToFile(List<DatasetResult> results, String filename) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"datasets\": [\n");
        
        for (int i = 0; i < results.size(); i++) {
            DatasetResult r = results.get(i);
            json.append("    {\n");
            json.append("      \"dataset\": \"").append(r.dataset).append("\",\n");
            json.append("      \"graph_info\": {\n");
            json.append("        \"nodes\": ").append(r.nodes).append(",\n");
            json.append("        \"edges\": ").append(r.edges).append(",\n");
            json.append("        \"source\": ").append(r.source).append("\n");
            json.append("      },\n");
            
            json.append("      \"scc\": {\n");
            json.append("        \"number_of_components\": ").append(r.numComponents).append(",\n");
            json.append("        \"components\": [\n");
            for (int j = 0; j < r.components.size(); j++) {
                ComponentInfo comp = r.components.get(j);
                json.append("          {\n");
                json.append("            \"id\": ").append(comp.id).append(",\n");
                json.append("            \"nodes\": ").append(listToJson(comp.nodes)).append(",\n");
                json.append("            \"size\": ").append(comp.nodes.size()).append("\n");
                json.append("          }");
                if (j < r.components.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("        ],\n");
            json.append("        \"metrics\": {\n");
            json.append("          \"time_ms\": ").append(String.format("%.4f", r.sccTime)).append(",\n");
            json.append("          \"counters\": ").append(mapToJson(r.sccCounters)).append("\n");
            json.append("        }\n");
            json.append("      },\n");
            
            json.append("      \"condensation_graph\": {\n");
            json.append("        \"edges\": [\n");
            for (int j = 0; j < r.condensationEdges.size(); j++) {
                CondensationEdge e = r.condensationEdges.get(j);
                json.append("          {\"from\": ").append(e.from).append(", \"to\": ").append(e.to).append("}");
                if (j < r.condensationEdges.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("        ]\n");
            json.append("      },\n");
            
            json.append("      \"topological_sort\": {\n");
            json.append("        \"component_order\": ").append(listToJson(r.componentOrder)).append(",\n");
            json.append("        \"task_order\": ").append(listToJson(r.taskOrder)).append(",\n");
            json.append("        \"metrics\": {\n");
            json.append("          \"time_ms\": ").append(String.format("%.4f", r.topoTime)).append(",\n");
            json.append("          \"counters\": ").append(mapToJson(r.topoCounters)).append("\n");
            json.append("        }\n");
            json.append("      },\n");
            
            json.append("      \"shortest_paths\": {\n");
            json.append("        \"source_component\": ").append(r.sourceComponent).append(",\n");
            json.append("        \"distances\": ").append(intMapToJson(r.shortestDistances)).append(",\n");
            json.append("        \"metrics\": {\n");
            json.append("          \"time_ms\": ").append(String.format("%.4f", r.shortestTime)).append(",\n");
            json.append("          \"counters\": ").append(mapToJson(r.shortestCounters)).append("\n");
            json.append("        }\n");
            json.append("      },\n");
            
            json.append("      \"longest_paths\": {\n");
            json.append("        \"source_component\": ").append(r.sourceComponent).append(",\n");
            json.append("        \"distances\": ").append(intMapToJson(r.longestDistances)).append(",\n");
            if (r.criticalPath != null) {
                json.append("        \"critical_path\": {\n");
                json.append("          \"path\": ").append(listToJson(r.criticalPath)).append(",\n");
                json.append("          \"length\": ").append(r.criticalPathLength).append("\n");
                json.append("        },\n");
            }
            json.append("        \"metrics\": {\n");
            json.append("          \"time_ms\": ").append(String.format("%.4f", r.longestTime)).append(",\n");
            json.append("          \"counters\": ").append(mapToJson(r.longestCounters)).append("\n");
            json.append("        }\n");
            json.append("      }\n");
            
            json.append("    }");
            if (i < results.size() - 1) json.append(",");
            json.append("\n");
        }
        
        json.append("  ]\n");
        json.append("}\n");
        
        Files.write(Paths.get(filename), json.toString().getBytes());
    }

    private static String listToJson(List<Integer> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String mapToJson(Map<String, Integer> map) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\": ").append(entry.getValue());
            if (i < map.size() - 1) sb.append(", ");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String intMapToJson(Map<Integer, Integer> map) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\": ").append(entry.getValue());
            if (i < map.size() - 1) sb.append(", ");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    private static List<List<Integer>> buildAdjacencyList(int n, List<int[]> edges) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for (int[] edge : edges) {
            adj.get(edge[0]).add(edge[1]);
        }
        return adj;
    }

    private static List<List<int[]>> buildWeightedAdjacencyList(int n, List<int[]> edges) {
        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for (int[] edge : edges) {
            adj.get(edge[0]).add(new int[]{edge[1], edge[2]});
        }
        return adj;
    }

    static class DatasetResult {
        String dataset;
        int nodes, edges, source;
        int numComponents;
        List<ComponentInfo> components;
        List<CondensationEdge> condensationEdges;
        List<Integer> componentOrder, taskOrder;
        int sourceComponent;
        Map<Integer, Integer> shortestDistances, longestDistances;
        List<Integer> criticalPath;
        Integer criticalPathLength;
        double sccTime, topoTime, shortestTime, longestTime;
        Map<String, Integer> sccCounters, topoCounters, shortestCounters, longestCounters;

        DatasetResult(String dataset, int nodes, int edges, int source, int numComponents,
                     List<ComponentInfo> components, List<CondensationEdge> condensationEdges,
                     List<Integer> componentOrder, List<Integer> taskOrder, int sourceComponent,
                     Map<Integer, Integer> shortestDistances, Map<Integer, Integer> longestDistances,
                     List<Integer> criticalPath, Integer criticalPathLength,
                     double sccTime, Map<String, Integer> sccCounters,
                     double topoTime, Map<String, Integer> topoCounters,
                     double shortestTime, Map<String, Integer> shortestCounters,
                     double longestTime, Map<String, Integer> longestCounters) {
            this.dataset = dataset;
            this.nodes = nodes;
            this.edges = edges;
            this.source = source;
            this.numComponents = numComponents;
            this.components = components;
            this.condensationEdges = condensationEdges;
            this.componentOrder = componentOrder;
            this.taskOrder = taskOrder;
            this.sourceComponent = sourceComponent;
            this.shortestDistances = shortestDistances;
            this.longestDistances = longestDistances;
            this.criticalPath = criticalPath;
            this.criticalPathLength = criticalPathLength;
            this.sccTime = sccTime;
            this.sccCounters = sccCounters;
            this.topoTime = topoTime;
            this.topoCounters = topoCounters;
            this.shortestTime = shortestTime;
            this.shortestCounters = shortestCounters;
            this.longestTime = longestTime;
            this.longestCounters = longestCounters;
        }
    }

    static class ComponentInfo {
        int id;
        List<Integer> nodes;

        ComponentInfo(int id, List<Integer> nodes) {
            this.id = id;
            this.nodes = nodes;
        }
    }

    static class CondensationEdge {
        int from, to;

        CondensationEdge(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }
}
