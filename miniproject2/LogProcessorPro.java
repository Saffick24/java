package com.miniproject2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class LogProcessorPro {

    /** Keywords we search for (can be expanded) */
    private static final List<String> KEYWORDS = List.of("ERROR", "WARN", "INFO");

    public static void main(String[] args) {

        final String inputFolder = "logs";   // Folder path
        final int threadPoolSize = 4;        // Adjustable for benchmarking

        try {
            System.out.println("\n==================================================");
            System.out.println(" SEQUENTIAL LOG PROCESSING");
            System.out.println("==================================================");
            Map<String, Long> seqResult = runSequential(inputFolder);

            System.out.println("\n==================================================");
            System.out.println(" CONCURRENT LOG PROCESSING");
            System.out.println("==================================================");
            Map<String, Long> concResult = runConcurrent(inputFolder, threadPoolSize);

            System.out.println("\n==================================================");
            System.out.println(" FINAL AGGREGATED RESULT (Concurrent)");
            System.out.println("==================================================");
            concResult.forEach((k, v) -> System.out.println(k + " = " + v));

            writeResultToFile("result.txt", concResult);

            System.out.println("\nOutput successfully written to result.txt");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Map<String, Long> runSequential(String folderPath) throws IOException {

        long start = System.currentTimeMillis();

        Map<String, Long> aggregated = new HashMap<>();

        for (Path file : listLogFiles(folderPath)) {

            List<String> lines = Files.readAllLines(file);

            Map<String, Long> counts =
                    lines.stream()
                            .flatMap(line -> Arrays.stream(line.split("\\s+")))
                            .filter(KEYWORDS::contains)
                            .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

            counts.forEach((k, v) -> aggregated.merge(k, v, Long::sum));
        }

        long end = System.currentTimeMillis();
        System.out.printf("Sequential Execution Time: %d ms%n", (end - start));

        return aggregated;
    }


    public static Map<String, Long> runConcurrent(String folderPath, int threads)
            throws InterruptedException, ExecutionException, IOException {

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        ConcurrentHashMap<String, Long> globalMap = new ConcurrentHashMap<>();

        List<Callable<Map<String, Long>>> tasks = new ArrayList<>();

        for (Path file : listLogFiles(folderPath)) {
            tasks.add(() -> processSingleFile(file));
        }

        long start = System.currentTimeMillis();

        List<Future<Map<String, Long>>> results = executor.invokeAll(tasks);

        for (Future<Map<String, Long>> f : results) {
            Map<String, Long> partialMap = f.get();
            partialMap.forEach((k, v) -> globalMap.merge(k, v, Long::sum));
        }

        executor.shutdown();

        long end = System.currentTimeMillis();
        System.out.printf("Concurrent Execution Time: %d ms%n", (end - start));

        return globalMap;
    }


    private static Map<String, Long> processSingleFile(Path file) throws IOException {

        System.out.printf("Thread %-18s processing: %s%n",
                Thread.currentThread().getName(), file.getFileName());

        List<String> lines = Files.readAllLines(file);

        return lines.stream()
                .flatMap(line -> Arrays.stream(line.split("\\s+")))
                .filter(KEYWORDS::contains)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
    }


    private static List<Path> listLogFiles(String folderPath) throws IOException {
        return Files.list(Paths.get(folderPath))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
    }


    private static void writeResultToFile(String filename, Map<String, Long> data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Map.Entry<String, Long> e : data.entrySet()) {
                writer.write(e.getKey() + " : " + e.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing result file: " + e.getMessage());
        }
    }
}

