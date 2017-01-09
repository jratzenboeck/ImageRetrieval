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

import java.util.*;

public class ClassificationServiceTest {

    private final LocationService locationService = new LocationService(
        "data/devset/poiNameCorrespondences.txt",
        "data/devset/desctxt/devset_textTermsPerPOI.wFolderNames.txt",
        "data/devset/devset_topics.xml");

    private final List<Classifier> classifiers = new ArrayList<>();

    private final String[] features = new String[] { "txt", "cnn", "cm", "csd", "lbp", "hog" };

    private final ClassificationService classificationServiceAllFeatures =
        new ClassificationService("data/devset/features",
            "data/devset/classificationResults",
            locationService);

    private final ClassificationService classificationServiceOneFeature =
        new ClassificationService("data/devset/featuresOneLocation",
            "data/devset/classificationResults",
            locationService);

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

        Arrays.stream(features).forEach(featureName -> {
            classificationServiceAllFeatures.classifyAllLocations(new String[] {featureName}, classifiers);
        });
    }

    @Test
    public void testClassifyAllLocationsForTxt() {
        String[] features = {"txt"};

        classificationServiceAllFeatures.classifyAllLocations(features, classifiers);
    }

    @Test
    public void testClassifyAllLocationsForCm() {
        String[] features = {"cm"};

        classificationServiceAllFeatures.classifyAllLocations(features, classifiers);
    }

    @Test
    public void testClassifyLocation() {
        String[] features = {"cm"};

        classificationServiceOneFeature.classifyLocation("acropolis_athens", features, classifiers);
    }

    @Test
    public void testMakePredictionsForCmForLocation() {
        String[] features = {"cm"};
        List<String[]> featureSets = new ArrayList<>();
        featureSets.add(features);

        Map<String[], Classifier> classifierForFeatures = new HashMap<>();
        classifierForFeatures.put(features, classifiers.get(9));

        classificationServiceOneFeature
            .makePredictionsForLocation("data/devset/predictions", "acropolis_athens",
                featureSets, classifierForFeatures);
    }

    @Test
    public void testMakePredictionsForTxtCmForLocation() {
        String[] txt = {"txt"};
        String[] cnn = {"cnn"};

        List<String[]> featureSets = new ArrayList<>();
        featureSets.add(txt);
        featureSets.add(cnn);

        Map<String[], Classifier> classifierForFeatures = new HashMap<>();
        classifierForFeatures.put(txt, classifiers.get(9));
        classifierForFeatures.put(cnn, classifiers.get(9));

        classificationServiceOneFeature
            .makePredictionsForLocation("data/devset/predictions", "acropolis_athens",
                featureSets, classifierForFeatures);
    }

    @Test
    public void testMakePredictionsForTxtCmForAllLocations() {
        String[] txt = {"txt"};
        String[] cm = {"cm"};

        List<String[]> featureSets = new ArrayList<>();
        featureSets.add(txt);
        featureSets.add(cm);

        Map<String[], Classifier> classifierForFeatures = new HashMap<>();
        classifierForFeatures.put(txt, classifiers.get(9));
        classifierForFeatures.put(cm, classifiers.get(9));

        classificationServiceAllFeatures
            .makePredictionsForAllLocations("data/devset/predictions",
                featureSets, classifierForFeatures);
    }

    @Test
    public void testMakePredictionsForVisualAndTextDescriprotsForAllLocations() {
        String[] txt = {"txt"};
        String[] visual = {"cnn", "cm", "csd", "lbp", "hog"};

        List<String[]> featureSets = new ArrayList<>();
        featureSets.add(txt);
        featureSets.add(visual);

        Map<String[], Classifier> classifierForFeatures = new HashMap<>();
        classifierForFeatures.put(txt, classifiers.get(9));
        classifierForFeatures.put(visual, classifiers.get(9));

        classificationServiceAllFeatures
            .makePredictionsForAllLocations("data/devset/predictions",
                featureSets, classifierForFeatures);
    }

    @Test
    public void testMakePredictionsForTxtCnnCmCsdLbpForAllLocations() {
        String[] txt = {"txt"};
        String[] visual = {"cnn", "cm", "csd", "lbp"};

        List<String[]> featureSets = new ArrayList<>();
        featureSets.add(txt);
        featureSets.add(visual);

        Map<String[], Classifier> classifierForFeatures = new HashMap<>();
        classifierForFeatures.put(txt, classifiers.get(9));
        classifierForFeatures.put(visual, classifiers.get(9));

        classificationServiceAllFeatures
            .makePredictionsForAllLocations("data/devset/predictions",
                featureSets, classifierForFeatures);
    }

    @Test
    public void testBuildModelsForAllLocations() {
        String[] features = {"cm"};

        classificationServiceAllFeatures.buildModelsForAllLocations(features, new RandomForest(), "data/devset/models");
    }

    @Test
    public void testVotingClassifier() {
        String[] features = {"cm"};

        classificationServiceAllFeatures.voting("data/devset/models", "data/devset/features", features);
    }
}
