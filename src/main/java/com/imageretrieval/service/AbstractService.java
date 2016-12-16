package com.imageretrieval.service;

import com.imageretrieval.entity.TermScore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractService {

    private final String textDescriptorsFile;

    protected AbstractService(String textDescriptorsFile) {
        this.textDescriptorsFile = textDescriptorsFile;
    }

    protected Map<String, TermScore> getTextDescriptorsForEntity(String entityId) {
        Map<String, TermScore> textDescriptors = new LinkedHashMap<>();
        try {
            String[] descriptors = Files
                .lines(Paths.get(textDescriptorsFile))
                .filter(line -> line.split(" ")[0].equals(entityId))
                .map(entityStr -> entityStr.split(" "))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException());

            int index = 0;
            for (; index < descriptors.length && !descriptors[index].startsWith("\""); index++);

            for (int i = index; i < descriptors.length; i += 4) {
                double termFrequency = Double.parseDouble(descriptors[i + 1]);
                double documentFrequency = Double.parseDouble(descriptors[i + 2]);
                double tfIdf = Double.parseDouble(descriptors[i + 3]);
                textDescriptors.put(descriptors[i], new TermScore(descriptors[i], termFrequency, documentFrequency, tfIdf));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textDescriptors;
    }

    protected double calculateAdvancedTfIdf(double termFrequency, double documentFrequency, int numberOfDocuments) {
        // Default implementation for tf idf
        return termFrequency * 1 / documentFrequency;
    }
}
