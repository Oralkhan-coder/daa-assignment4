package metrics;

import java.util.*;

public class AlgorithmMetrics implements Metrics {
    private long startTime;
    private long endTime;
    private Map<String, Integer> counters;

    public AlgorithmMetrics() {
        this.counters = new HashMap<>();
    }

    @Override
    public void start() {
        startTime = System.nanoTime();
    }

    @Override
    public void stop() {
        endTime = System.nanoTime();
    }

    @Override
    public void count(String key) {
        counters.put(key, counters.getOrDefault(key, 0) + 1);
    }

    @Override
    public void print() {
        System.out.println("Metrics:");
        System.out.println("  Time: " + (endTime - startTime) / 1_000_000.0 + " ms");
        for (Map.Entry<String, Integer> entry : counters.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
    }

    public long getTimeNanos() {
        return endTime - startTime;
    }

    public double getTimeMillis() {
        return (endTime - startTime) / 1_000_000.0;
    }

    public int getCount(String key) {
        return counters.getOrDefault(key, 0);
    }

    public Map<String, Integer> getCounters() {
        return new HashMap<>(counters);
    }
}

