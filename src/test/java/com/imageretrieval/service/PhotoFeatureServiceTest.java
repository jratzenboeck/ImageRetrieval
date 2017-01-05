package com.imageretrieval.service;

import org.junit.Test;

import java.util.Arrays;

public class PhotoFeatureServiceTest {

    private final PhotoService photoService = new PhotoService(
        "data/devset/desctxt/devset_textTermsPerImage.txt",
        "data/devset/xml",
        "data/devset/gt/rGT", "data/devset/descvis/img");
    private final LocationService locationService = new LocationService(
        "data/devset/poiNameCorrespondences.txt",
        "data/devset/desctxt/devset_textTermsPerPOI.wFolderNames.txt",
        "data/devset/devset_topics.xml");

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
    public void testWriteTxtCnnCmmForAllLocations() {
        String[] features = new String[] { "txt", "cnn", "cmm" };
        photoFeatureService.writePhotoFeaturesForAllLocations("data/devset/features", features);
    }

    @Test
    public void testWriteTxtCmForAllLocations() {
        String[] features = new String[] { "txt", "cm" };
        photoFeatureService.writePhotoFeaturesForAllLocations("data/devset/features", features);
    }

    @Test
    public void testWriteColorMomentsFeatureForOneLocation() {
        String[] features = new String[] { "cm" };
        photoFeatureService.writePhotoFeaturesForOneLocation("data/devset/featuresOneLocation", "acropolis_athens", features);
    }
}
