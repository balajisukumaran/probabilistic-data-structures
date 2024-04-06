package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;

public class CSVProcessor {
    public HashMap<Integer, String> lines = new HashMap<>();

    /**
     * Reads a CSV file, trims spaces from each line, and stores them in a list.
     * Skips the first line assuming it's a header.
     *
     * @param filePath Path to the CSV file.
     * @throws IOException If an I/O error occurs.
     */
    public void loadAndProcessCSV(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                lines.put(i, line.replace(" ", "")); // Remove spaces and add to the list
                i++;
            }
        }
    }

    /**
     * Returns n random strings from the loaded lines.
     *
     * @param n The number of strings to return.
     * @return A list of n random strings.
     * @throws IllegalArgumentException If n is greater than the number of available lines.
     */
    public List<Integer> getRandomStrings(int n) {
        Random random = new Random();
        List<Integer> randomIntegers = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            int next = random.nextInt(lines.size()); // Generates a number from 1 to lines.size()
            randomIntegers.add(next);
        }

        return randomIntegers;
    }
}
