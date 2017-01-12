package com.imageretrieval.util;

import com.imageretrieval.entity.Prediction;
import weka.classifiers.trees.RandomForest;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by mada on 11.01.17.
 */
public class Predictor {

    private RandomForest tree;
    private SimpleKMeans kMeans;
    private Instances trainingData;
    private static final int NUMBER_OF_CLUSTERS = 10;
    private static final int MIN_INSTANCES_PER_CLUSTER = 5;

    BufferedReader reader;

    public void createClassifier(String trainingFile) {
        try {
            reader = new BufferedReader(
                new FileReader(trainingFile));
            trainingData = new Instances(reader);
            reader.close();
            if (trainingData.classIndex() == -1)
                trainingData.setClassIndex(trainingData.numAttributes() - 1);
            tree = new RandomForest();
            tree.buildClassifier(trainingData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void predict(String path, String testFile, int queryId) {
        List<Prediction> predictions = new ArrayList<>();
        String line;
        String cvsSplitBy = ",";
        try (BufferedReader br = new BufferedReader(new FileReader(path+testFile))) {
            boolean first = true;

            CSVLoader loader = new CSVLoader();
            loader.setFile(new File(path+testFile));
            Instances structure = loader.getStructure();
            for (int i=0;i<structure.numAttributes();i++) {
                Attribute attribute = structure.attribute(i);
                Attribute newAttr = new Attribute(attribute.name(),Attribute.NUMERIC);
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
                    for (int i = 0; i < features.length; i++) {
                        if (!features[i].isEmpty()) {
                            instance.setValue(i, Double.valueOf(features[i]));
                        }
                    }
                    structure.add(instance);
                    try {
                        double similarityScore = tree.distributionForInstance(instance)[1];
                        predictions.add(new Prediction(instance, photoId,similarityScore,0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    first = false;
                }
            }
            int[] assignments = clustering(structure);
            for (int i=0; i<assignments.length; i++) {
                predictions.get(i).setCluster(assignments[i]);
            }
            List<List<Prediction>> listOfPredictedClusters = new ArrayList<>();

            for (int i = 0; i < NUMBER_OF_CLUSTERS; i++) { // iterate over number of cluster
                List<Prediction> predictionsPerCluster = new ArrayList<>();
                for (Prediction p : predictions) {
                    if (p.getCluster() == i) {
                        predictionsPerCluster.add(p);
                    }
                }
                predictionsPerCluster.sort(new Comparator<Prediction>() {
                    @Override
                    public int compare(Prediction prediction, Prediction t1) {
                        return -((Double) prediction.getSimilarityScore()).compareTo(t1.getSimilarityScore());
                    }
                });
                listOfPredictedClusters.add(predictionsPerCluster.subList(0, MIN_INSTANCES_PER_CLUSTER));
            }
            List<Prediction> finalPredictions = new ArrayList<>();
            for (int i = 0; i < MIN_INSTANCES_PER_CLUSTER; i++) {
                for (int j = 0; j < NUMBER_OF_CLUSTERS; j++) {
                    finalPredictions.add(listOfPredictedClusters.get(j).get(i));
                }
            }
            finalPredictions.sort(new Comparator<Prediction>() {
                @Override
                public int compare(Prediction prediction, Prediction t1) {
                    return -((Double) prediction.getSimilarityScore()).compareTo(t1.getSimilarityScore());
                }
            });
            writeToFile(finalPredictions, queryId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[] clustering (Instances instances) {
        try {
            kMeans = new SimpleKMeans();
            String[] options = new String[1];
            options[0] = "-O";
            kMeans.setOptions(options);
            kMeans.setNumClusters(NUMBER_OF_CLUSTERS);
            kMeans.buildClusterer(instances);
            ClusterEvaluation eval = new ClusterEvaluation();
            eval.setClusterer(kMeans);
            eval.evaluateClusterer(instances);
            return kMeans.getAssignments();
        } catch (Exception e) {
            e.printStackTrace();
            return new int[0];
        }
    }

    private String getEntryForPredictionFile (Prediction prediction, int queryId) {
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
                new FileOutputStream(new File("data/testset/predictions.csv"),true));
            for (int i = 0; i <predictionList.size(); i++) {
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
