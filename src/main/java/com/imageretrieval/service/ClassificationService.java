package com.imageretrieval.service;

import com.imageretrieval.util.FileUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveType;

import java.io.*;
import java.util.ArrayList;
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

    public void makePredictionsForLocation(String pathToPredictionsFolder, String locationId, List<String[]> featureSets, Classifier classifier) {
        List<FastVector> predictions = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (String[] features : featureSets) {
            final Instances instances = getInstancesFromArffFile(locationId, features);

            if (instances != null) {
                Evaluation results = classifyInstances(classifier, instances);

                if (results != null) {
                    sb = appendHeaderForPredictionFile(sb, features);
                    sb.append(",");
                    predictions.add(results.predictions());
                }
            }
        }
        sb.append("actual\n");

        String filename = getFileNameForLocationMerged(pathToPredictionsFolder, locationId, featureSets, ".csv");
        writeToFile(filename, mergePredictionFiles(sb, predictions));
        String baseFilename = filename.substring(0, filename.length() - 3);
        FileUtils.convertCSVToArffFile(filename,  baseFilename + "arff", "");
    }

    private StringBuilder appendHeaderForPredictionFile(StringBuilder sb, String[] features) {
        sb.append("prediction");
        Arrays.stream(features).forEach(sb::append);
        return sb;
    }

    private String mergePredictionFiles(StringBuilder sb, List<FastVector> predictionsForFeatureSets) {
        for (int line = 0; line < predictionsForFeatureSets.get(0).size(); line++) {
            int i = 0;
            for (; i < predictionsForFeatureSets.size(); i++) {
                FastVector predictions = predictionsForFeatureSets.get(i);
                String predictionLine = String.valueOf(predictions.elementAt(line));
                String[] columns = predictionLine.split(" ");
                sb.append(columns[5]).append(",");
            }
            String actualValue = String.valueOf(predictionsForFeatureSets.get(0).elementAt(line)).split(" ")[1];
            sb.append(actualValue).append("\n");
        }
        return sb.toString();
    }

    private String getFileNameForBestResults(String pathToFolder, String[] features, String fileExtension) {
        return getFileNameForLocation(pathToFolder, "", features, fileExtension);
    }

    private String getFileNameForLocation(String pathToFolder, String locationId, String[] features, String fileExtension) {
        StringBuilder sb = new StringBuilder(pathToFolder + "/");
        Arrays.stream(features).forEach(sb::append);
        sb.append("/");
        sb.append(locationId);
        Arrays.stream(features).forEach(featureName -> sb.append("_").append(featureName));
        sb.append(fileExtension);
        return sb.toString();
    }

    private String getFileNameForLocationMerged(String pathToFolder, String locationId, List<String[]> features, String fileExtension) {
        StringBuilder sb = new StringBuilder(pathToFolder + "/");
        features.stream().flatMap(x -> Arrays.stream(x)).forEach(sb::append);
        sb.append("/");
        sb.append(locationId);
        features.stream().flatMap(x -> Arrays.stream(x)).forEach(featureName -> sb.append("_").append(featureName));
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

            RemoveType remove = new RemoveType();                         // new instance of filter
            remove.setInputFormat(data);                          // inform filter about dataset **AFTER** setting options

            data = Filter.useFilter(data, remove);
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename.toString() + " could not be found. " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Oops, something went wrong while reading ARFF file " + filename.toString() + ". " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Oops, something went wrong while applying filter to ARFF file " + filename.toString() + ". " + e.getMessage());
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

    private void writeToFile(String filename, String content) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(new File(filename), true))) {
            pw.append(content);
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " could not be found.");
        }
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
