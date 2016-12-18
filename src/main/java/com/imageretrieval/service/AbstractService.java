package com.imageretrieval.service;

import com.imageretrieval.entity.TermScore;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractService {

    private final String textDescriptorsFile;
    private final Set<String> stopWords;
    private final String pathToStopWordsFile = "stopwords/stopwords.txt";

    protected AbstractService(String textDescriptorsFile) {
        this.textDescriptorsFile = textDescriptorsFile;
        this.stopWords = getStopWords();
    }

    protected Map<String, TermScore> getTextDescriptorsForEntity(String entityId) {
        Map<String, TermScore> textDescriptors = new LinkedHashMap<>();
        String[] descriptors = {};
        try (Stream<String> lines = Files.lines(Paths.get(textDescriptorsFile))) {
            descriptors =
                lines
                    .filter(line -> line.split(" ")[0].equals(entityId))
                    .map(entityStr -> entityStr.split(" "))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int index = 0;
        for (; index < descriptors.length && !descriptors[index].startsWith("\""); index++) ;

        for (int i = index; i < descriptors.length; i += 4) {
            String term = descriptors[i].toLowerCase();
            double termFrequency = Double.parseDouble(descriptors[i + 1]);
            double documentFrequency = Double.parseDouble(descriptors[i + 2]);
            double tfIdf = Double.parseDouble(descriptors[i + 3]);
            if (isAllowedTerm(term)) {
                textDescriptors.put(term, new TermScore(term, termFrequency, documentFrequency, tfIdf));
            }
        }
        return textDescriptors;
    }

    protected double calculateAdvancedTfIdf(double termFrequency, double documentFrequency, int numberOfDocuments) {
        // Default implementation for tf idf
        return termFrequency * 1 / documentFrequency;
    }

    private Set<String> getStopWords() {
        try (Stream<String> lines = Files.lines(Paths.get(pathToStopWordsFile))) {
            return lines.collect(Collectors.toSet());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return new HashSet<>();
    }

    private boolean isStopWord(String term) {
        return stopWords.contains(term);
    }

    private boolean isAllowedTerm(String term) {
        // Has to be truncated at the beginning and end because the term looks like "..."
        String actualTerm = term.substring(1, term.length() - 1);
        return !StringUtils.isNumeric(actualTerm) && !isStopWord(actualTerm);
    }
}
