package com.imageretrieval.service;

import org.junit.Before;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.net.BayesNetGenerator;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.Stacking;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassificationServiceTest {

    private final LocationService locationService = new LocationService(
        "data/devset/poiNameCorrespondences.txt",
        "data/devset/desctxt/devset_textTermsPerPOI.wFolderNames.txt",
        "data/devset/devset_topics.xml");

    private final List<Classifier> classifiers = new ArrayList<>();

    private final String[] features = new String[] { "txt", "cnn", "cm", "csd", "lbp", "hog" };

    @Before
    public void initialize() {
        classifiers.add(new J48());
        classifiers.add(new NaiveBayes());
        classifiers.add(new AdaBoostM1());
        classifiers.add(new Bagging());
        classifiers.add(new BayesNet());
        classifiers.add(new BayesNetGenerator());
        classifiers.add(new IBk());
        classifiers.add(new Stacking());
        classifiers.add(new KStar());
        classifiers.add(new RandomForest());
        classifiers.add(new Vote());
    }

    @Test
    public void testClassifyAllLocationsForAllFeatures() {
        List<Classifier> classifiers = new ArrayList<>();
        classifiers.add(new J48());
        ClassificationService classificationService =
            new ClassificationService("data/devset/features",
                "data/devset/classificationResults",
                locationService);
        Arrays.stream(features).forEach(featureName -> {
            classificationService.classifyAllLocations(new String[] {featureName}, classifiers);
        });
    }

    @Test
    public void testClassifyAllLocations() {
        String[] features = {"txt"};
        ClassificationService classificationService =
            new ClassificationService("data/devset/features",
                "data/devset/classificationResults",
                locationService);
        classificationService.classifyAllLocations(features, classifiers);
    }

    @Test
    public void testClassifyLocation() {
        String[] features = {"txt"};
        ClassificationService classificationService =
            new ClassificationService("data/devset/featuresOneLocation",
                "data/devset/classificationResults",
                locationService);
        classificationService.classifyLocation("acropolis_athens", features, classifiers);
    }
}
