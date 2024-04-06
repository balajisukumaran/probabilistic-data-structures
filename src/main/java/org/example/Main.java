package org.example;


import BloomFilter.BloomFilter;
import ConcurrentSkipList.SkipList;
import CuckooFilter.CuckooFilter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

        ConfigLoader config = new ConfigLoader();
        String dsType = config.getProperty("datastructures.type");
        String inputLoc = config.getProperty("input.location");
        String operation = config.getProperty("operation");
        int querySize = Integer.parseInt(config.getProperty("querySize"));

        CSVProcessor csvProcessor = new CSVProcessor();
        csvProcessor.loadAndProcessCSV(inputLoc);
        List<Integer> testData = csvProcessor.getRandomStrings(querySize);

        long startTime = System.nanoTime();

        switch (dsType) {
            case "ConcurrentSkipList":
                SkipList skipList = new SkipList(csvProcessor.lines.size());

                for (int i = 0; i < csvProcessor.lines.size(); i++)
                    skipList.add(csvProcessor.lines.get(i));

                if (operation.equals("search")) {
                    for (Integer data : testData)
                        skipList.search(csvProcessor.lines.get(data));
                } else if (operation.equals("delete")) {
                    for (Integer data : testData)
                        skipList.remove(csvProcessor.lines.get(data));
                }
                break;

            case "BloomFiler":
                BloomFilter<String> bloomFilter = new BloomFilter<String>(csvProcessor.lines.size(), 0.01d);
                for (int i = 0; i < csvProcessor.lines.size(); i++)
                    bloomFilter.add(csvProcessor.lines.get(i));

                if (operation.equals("search")) {
                    for (Integer data : testData)
                        bloomFilter.contains(csvProcessor.lines.get(data));
                }
                break;


            case "CuckooFiler":
                CuckooFilter cuckooFilter = new CuckooFilter(csvProcessor.lines.size(), 32);
                for (int i = 0; i < csvProcessor.lines.size(); i++)
                    cuckooFilter.insert(csvProcessor.lines.get(i));

                if (operation.equals("search")) {
                    for (Integer data : testData)
                        cuckooFilter.contains(csvProcessor.lines.get(data));
                } else if (operation.equals("delete")) {
                    for (Integer data : testData)
                        cuckooFilter.delete(csvProcessor.lines.get(data));
                }
                break;
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  // In nanoseconds
        long memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); // In bytes

        System.out.println("Execution time: " + duration / 1_000_000 + " ms");
        System.out.println("Memory used: " + memoryUsage / 1024 / 1024 + " MB");
    }
}