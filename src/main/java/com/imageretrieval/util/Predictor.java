package com.imageretrieval.util;

import com.imageretrieval.entity.Photo;
import com.imageretrieval.entity.Prediction;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by mada on 11.01.17.
 */
public class Predictor {

    private RandomForest tree;
    private Instances trainingData;

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
                    try {
                        double similarityScore = tree.distributionForInstance(instance)[1];
                        predictions.add(new Prediction(photoId,similarityScore,0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    first = false;
                }
            }
            predictions.sort(new Comparator<Prediction>() {
                @Override
                public int compare(Prediction prediction, Prediction t1) {
                    return -((Double)prediction.getSimilarityScore()).compareTo(t1.getSimilarityScore());
                }
            });
            writeToFile(predictions.subList(0,50), queryId);
        } catch (IOException e) {
            e.printStackTrace();
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
