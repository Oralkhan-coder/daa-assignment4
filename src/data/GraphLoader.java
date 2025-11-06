package data;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class GraphLoader {
    public static GraphData loadGraph(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return parseGraph(content);
    }

    private static GraphData parseGraph(String json) {
        int n = extractInt(json, "\"n\":");
        int source = extractInt(json, "\"source\":");
        
        List<int[]> edges = new ArrayList<>();
        Pattern edgePattern = Pattern.compile("\\{\"u\":\\s*(\\d+)\\s*,\\s*\"v\":\\s*(\\d+)\\s*,\\s*\"w\":\\s*(\\d+)\\s*\\}");
        Matcher matcher = edgePattern.matcher(json);
        
        while (matcher.find()) {
            int u = Integer.parseInt(matcher.group(1));
            int v = Integer.parseInt(matcher.group(2));
            int w = Integer.parseInt(matcher.group(3));
            edges.add(new int[]{u, v, w});
        }
        
        return new GraphData(n, edges, source);
    }

    private static int extractInt(String json, String key) {
        int start = json.indexOf(key);
        if (start == -1) return 0;
        start += key.length();
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == ':')) {
            start++;
        }
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (end == start) return 0;
        return Integer.parseInt(json.substring(start, end));
    }

    public static class GraphData {
        public final int n;
        public final List<int[]> edges;
        public final int source;

        public GraphData(int n, List<int[]> edges, int source) {
            this.n = n;
            this.edges = edges;
            this.source = source;
        }
    }
}
