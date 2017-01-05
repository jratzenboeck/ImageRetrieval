package com.imageretrieval.service;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ClassificationService {

    private final String pathToFeatureFolder;
    private final String pathToClassificationFolder;
    private final LocationService locationService;

    public ClassificationService(String pathToFeatureFolder, String pathToClassificationFolder, LocationService locationService) {
        this.pathToFeatureFolder = pathToFeatureFolder;
        this.pathToClassificationFolder = pathToClassificationFolder;
        this.locationService = locationService;
    }

    public void classifyAllLocations(String[] features, List<Classifier> classifiers) {
        locationService
            .getAllLocationTitles()
            .forEach(locationId -> {
                classifyLocation(locationId, features, classifiers);
            });
    }

    public void classifyLocation(String locationId, String[] features, List<Classifier> classifiers) {
        final Instances instances = getInstancesFromArffFile(locationId, features);
        final String filenameToResults = getFileNameForLocation(pathToClassificationFolder, locationId, features, ".txt");

        if (instances != null) {
            Evaluation bestResults = null;
            Classifier bestClassifier = null;
            for (Classifier classifier : classifiers) {
                Evaluation results = classifyInstances(classifier, instances);
                if (results != null) {
                    writeEvaluationResultsToFile(filenameToResults, classifier, results);
                    if (bestResults == null || results.pctCorrect() > bestResults.pctCorrect()) {
                        bestResults = results;
                        bestClassifier = classifier;
                    }
                }
            }
            writeBestEvaluationResultsForLocationToFile(
                getFileNameForBestResults(pathToClassificationFolder, features, "_best.txt"),
                locationId, bestClassifier, bestResults);
        }
    }

    private String getFileNameForBestResults(String pathToFolder, String[] features, String fileExtension) {
        return getFileNameForLocation(pathToFolder, "", features, fileExtension);
    }

    private String getFileNameForLocation(String pathToFolder, String locationId, String[] features, String fileExtension) {
        StringBuilder sb = new StringBuilder(pathToFolder + "/" + locationId);
        Arrays.stream(features).forEach(featureName -> {
            sb.append("_");
            sb.append(featureName);
        });
        sb.append(fileExtension);
        return sb.toString();
    }

    private Instances getInstancesFromArffFile(String locationId, String[] features) {
        final String filename = getFileNameForLocation(pathToFeatureFolder, locationId, features, ".arff");

        Instances data = null;

        try {
            final BufferedReader reader =
                new BufferedReader(new FileReader(filename.toString()));
            ArffLoader.ArffReader arffReader = new ArffLoader.ArffReader(reader);
            data = arffReader.getData();
            data.setClassIndex(data.numAttributes() - 1);
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename.toString() + " could not be found. " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Oops, something went wrong while reading ARFF file " + filename.toString() + ". " + e.getMessage());
        }
        return data;
    }

    private Evaluation classifyInstances(Classifier classifier, Instances instances) {
        try {
            classifier.buildClassifier(instances);
            final Evaluation evaluation = new Evaluation(instances);
            evaluation.crossValidateModel(classifier, instances, 10, new Random(1));
            return evaluation;
        } catch (Exception e) {
            System.out.println("Classifier " + classifier.toString()
                + " is not suitable for dataset and produced an error");
        }
        return null;
    }

    private void writeEvaluationResultsToFile(String filename, Classifier classifier, Evaluation evaluation) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(new File(filename), true))) {
            pw.append("Classifier: " + classifier.getClass().getSimpleName() + "\n");
            pw.append(evaluation.toSummaryString(false));
            pw.append("\n");
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " could not be found.");
        }
    }

    private void writeBestEvaluationResultsForLocationToFile(String filename, String locationId, Classifier classifier, Evaluation evaluation) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(new File(filename), true))) {
            pw.append(evaluation.toSummaryString("\nBest classifier "
                + "for location " + locationId + ": " + classifier.getClass().getSimpleName() +
                "\nResults\n" +
                "==================================================================", false));
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " could not be found.");
        }
    }
}
