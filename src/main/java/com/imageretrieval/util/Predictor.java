package com.imageretrieval.util;

import com.imageretrieval.entity.Prediction;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.RandomForest;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ManhattanDistance;
import weka.core.converters.CSVLoader;
import weka.filters.unsupervised.attribute.Remove;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Predictor {

    private Vote classifier;
    private SimpleKMeans kMeans;
    private Instances trainingData;
    private static final int NUMBER_OF_CLUSTERS = 10;
    private static final int MIN_INSTANCES_PER_CLUSTER = 5;

    private BufferedReader reader;

    public Classifier createClassifier(String trainingFile) {
        try {
            reader = new BufferedReader(
                new FileReader(trainingFile));
            trainingData = new Instances(reader);

            for (int i = 0; i < trainingData.numInstances(); i++) {
                trainingData.instance(i).setWeight(1);
            }
            reader.close();

            if (trainingData.classIndex() == -1)
                trainingData.setClassIndex(trainingData.numAttributes() - 1);

            Classifier[] baseClassifiers = new Classifier[] {new RandomForest(), new IBk(), new AdaBoostM1(), new Bagging()};
            classifier = new Vote();
            classifier.setClassifiers(baseClassifiers);

            return classifier;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Instances getWikiInstances(String path, String locationTitle) throws Exception {
        CSVLoader loader = new CSVLoader();
//        String[] options = new String[1];
//        options[0] = "-H";
//        loader.setOptions(options);
        loader.setSource(new File(path + locationTitle + "-_cnn_cm_csd_lbp.csv"));
        return loader.getDataSet();
    }

    private void extendTrainingData(String path, String locationTitle) throws Exception {
        Instances wikiInstances = getWikiInstances(path, locationTitle);
        for (int i = 0; i < wikiInstances.numInstances(); i++) {
            wikiInstances.instance(i).setWeight(2);
            trainingData.add(wikiInstances.instance(i));
        }
    }

    private void buildFinalClassifier(String pathToWiki, String locationTitle) throws Exception {
        extendTrainingData(pathToWiki, locationTitle);

        Remove rm = new Remove();
        rm.setAttributeIndices("1");  // remove 1st attribute

        FilteredClassifier fc = new FilteredClassifier();
        fc.setFilter(rm);
        fc.setClassifier(classifier);
        fc.buildClassifier(trainingData);
    }

    public void predict(String pathToTest, String pathWiki, String locationTitle, String testFile, int queryId) {
        List<Prediction> predictions = new ArrayList<>();
        String line;
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(pathToTest + testFile))) {
            buildFinalClassifier(pathWiki, locationTitle);

            boolean first = true;

            CSVLoader loader = new CSVLoader();
            loader.setFile(new File(pathToTest + testFile));
            Instances structure = loader.getStructure();
            for (int i = 0; i < structure.numAttributes(); i++) {
                Attribute attribute = structure.attribute(i);
                Attribute newAttr = new Attribute(attribute.name(), Attribute.NUMERIC);
                structure.deleteAttributeAt(i);
                structure.insertAttributeAt(newAttr, i);
            }
            structure.delete();

            while ((line = br.readLine()) != null) {
                String[] features = line.split(cvsSplitBy);
                if (!first) {
                    Instance instance = new Instance(features.length);
                    instance.setDataset(trainingData);
                    String photoId = features[0];
                    for (int i = 1; i < features.length; i++) {
                        if (!features[i].isEmpty()) {
                            instance.setValue(i, Double.valueOf(features[i]));
                        }
                    }
                    structure.add(instance);

                    try {
                        double similarityScore = classifier.distributionForInstance(instance)[1];
                        predictions.add(new Prediction(instance, photoId, similarityScore, 0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    first = false;
                }
            }

            writeToFile(getFinalPredictions(predictions, structure), queryId);
            System.out.println("Predictions written for location " + locationTitle);

        } catch (Exception e) {
//            System.out.println("Exception happened for location " + locationTitle + ". " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Prediction> getFinalPredictions(List<Prediction> predictionsAfterClassify, Instances instances) {
        int[] assignments = clustering(instances, 1);

        for (int i = 0; i < assignments.length; i++) {
            predictionsAfterClassify.get(i).setCluster(assignments[i]);
        }
        List<List<Prediction>> listOfPredictedClusters = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_CLUSTERS; i++) { // iterate over number of cluster
            List<Prediction> predictionsPerCluster = new ArrayList<>();
            for (Prediction p : predictionsAfterClassify) {
                if (p.getCluster() == i) {
                    predictionsPerCluster.add(p);
                }
            }
            predictionsPerCluster.sort(new SimilarityComparator());
            listOfPredictedClusters.add(predictionsPerCluster.subList(0, MIN_INSTANCES_PER_CLUSTER));
        }
        List<Prediction> finalPredictions = new ArrayList<>();
        for (int i = 0; i < MIN_INSTANCES_PER_CLUSTER; i++) {
            for (int j = 0; j < NUMBER_OF_CLUSTERS; j++) {
                finalPredictions.add(listOfPredictedClusters.get(j).get(i));
            }
        }
        finalPredictions.sort(new SimilarityComparator());

        return finalPredictions;
    }

    private int[] clustering(Instances instances, int seed) {
        try {
            kMeans = new SimpleKMeans();
            String[] options = new String[1];
            options[0] = "-O";
            kMeans.setOptions(options);
            kMeans.setDistanceFunction(new ManhattanDistance());
            kMeans.setNumClusters(NUMBER_OF_CLUSTERS);
            kMeans.setSeed(seed);
            kMeans.buildClusterer(instances);
            ClusterEvaluation eval = new ClusterEvaluation();
            eval.setClusterer(kMeans);
            eval.evaluateClusterer(instances);

            for (double clusterSize : kMeans.getClusterSizes()) {
                if (clusterSize < MIN_INSTANCES_PER_CLUSTER) {
                    clustering(instances, seed + 1);
                }
            }
            return kMeans.getAssignments();
        } catch (Exception e) {
            e.printStackTrace();
            return new int[0];
        }
    }

    private String getEntryForPredictionFile(Prediction prediction, int queryId) {
        StringBuilder sb = new StringBuilder();
        sb.append(queryId + " ");
        sb.append(0 + " ");
        sb.append(prediction.getPhotoId() + " ");
        sb.append(prediction.getRanking() + " ");
        sb.append(prediction.getSimilarityScore() + " ");
        sb.append("run1_group4");
        return sb.toString();
    }

    private void writeToFile(List<Prediction> predictionList, int queryId) {
        try {
            PrintWriter printWriter = new PrintWriter(
                new FileOutputStream(new File("data/testset/predictions.csv"), true));
            for (int i = 0; i < predictionList.size(); i++) {
                Prediction prediction = predictionList.get(i);
                prediction.setRanking(i);
                String p = getEntryForPredictionFile(prediction, queryId);
                printWriter.write(p);
                printWriter.write('\n');
            }
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
