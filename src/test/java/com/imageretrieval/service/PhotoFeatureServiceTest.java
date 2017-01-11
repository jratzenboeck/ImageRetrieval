package com.imageretrieval.service;

import org.junit.Test;

import java.util.Arrays;

public class PhotoFeatureServiceTest {

    private final PhotoService photoService = new PhotoService(
        "data/testset/desctxt/devset_textTermsPerImage.txt",
        "data/testset/xml",
        "data/devset/gt/rGT", "data/devset/gt/dGT", "data/testset/descvis/img");
    private final LocationService locationService = new LocationService(
        "data/testset/poiNameCorrespondences.txt",
        "data/devset/desctxt/devset_textTermsPerPOI.wFolderNames.txt",
        "data/testset/testset_topics.xml");

    private final PhotoFeatureService photoFeatureService =
        new PhotoFeatureService(photoService, locationService);

    private final String[] features = new String[] { "txt", "cnn", "cm", "csd", "lbp", "hog" };

    @Test
    public void testWriteSingleFeaturesForAllLocations() {
        Arrays.stream(features).forEach(featureName -> {
            photoFeatureService.writePhotoFeaturesForAllLocations("data/devset/features", new String[] {featureName});
        });
    }

    @Test
    public void testWriteFeatureCombinationForAllLocations() {
        photoFeatureService.writePhotoFeaturesForAllLocations("data/devset/features", features);
    }

    @Test
    public void testWriteTxtCnnForAllLocations() {
        String[] features = new String[] { "txt", "cnn" };
        photoFeatureService.writePhotoFeaturesForAllLocations("data/devset/features", features);
    }

    @Test
    public void testWriteTxtCnnCmForAllLocations() {
        String[] features = new String[] { "txt", "cnn", "cm" };
        photoFeatureService.writePhotoFeaturesForAllLocations("data/devset/features", features);
    }

    @Test
    public void testWriteTxtCmForAllLocations() {
        String[] features = new String[]{"txt", "cm"};
        photoFeatureService.writePhotoFeaturesForAllLocations("data/devset/features", features);
    }

    @Test
    public void testWriteVisualDescriptorsForAllLocations() {
        String[] features = new String[] { "cnn", "cm", "csd", "lbp", "hog" };
        photoFeatureService.writePhotoFeaturesForAllLocations("data/testset/features", features);
    }

    @Test
    public void testWriteCnnCmCsdLbpForAllLocations() {
        String[] features = new String[] { "cnn", "cm", "csd", "lbp" };
        photoFeatureService.writePhotoFeaturesForAllLocations("data/devset/features", features);
    }

    @Test
    public void testWriteCnnCmForAllLocations() {
        String[] features = new String[] { "cnn", "cm" };
        photoFeatureService.writePhotoFeaturesForAllLocations("data/devset/features", features);
    }

    @Test
    public void testWriteColorMomentsFeatureForOneLocation() {
        String[] features = new String[] { "cnn", "cm", "csd", "lbp", "hog" };
        photoFeatureService.writePhotoFeaturesForOneLocation("data/testset/featuresOneLocation", "bath_abbey", features);
    }

    @Test
    public void testWriteTextFeatureForOneLocation() {
        String[] features = new String[] { "txt" };
        photoFeatureService.writePhotoFeaturesForOneLocation("data/devset/featuresOneLocation", "acropolis_athens", features);
    }
}
