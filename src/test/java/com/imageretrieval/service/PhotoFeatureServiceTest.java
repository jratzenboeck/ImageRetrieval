package com.imageretrieval.service;

import org.junit.Test;

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

    @Test
    public void testWritePhotoFeaturesForAllLocations() {
        photoFeatureService.writePhotoFeaturesForAllLocations("data/devset/features");
    }

    @Test
    public void testWritePhotoFeaturesForOneLocation() {
        photoFeatureService.writePhotoFeaturesForOneLocation("data/devset/featuresOneLocation", "angel_of_the_north");
    }
}
