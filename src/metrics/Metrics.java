package metrics;

public interface Metrics {
    void start();
    void stop();
    void count(String key);
    void print();
}
