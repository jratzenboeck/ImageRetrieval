package com.imageretrieval.service;

import org.junit.Test;

public class TextDescriptorsServiceTest {

    private final PhotoService photoService = new PhotoService(
        "data/devset/desctxt/devset_textTermsPerImage.txt",
        "data/devset/xml",
        "data/devset/gt/rGT");
    private final LocationService locationService = new LocationService(
        "data/devset/poiNameCorrespondences.txt",
        "data/devset/desctxt/devset_textTermsPerPOI.wFolderNames.txt",
        "data/devset/devset_topics.xml");

    private final TextDescriptorsService textDescriptorsService =
        new TextDescriptorsService(photoService, locationService);

    @Test
    public void testWritePhotoTextDescriptorsForAllLocations() {
        textDescriptorsService.writePhotoTextDescriptorsForAllLocations("data/devset/features");
    }

    @Test
    public void testWritePhotoTextDescriptorsForOneLocation() {
        textDescriptorsService.writePhotoTextDescriptorsForOneLocation("data/devset/featuresOneLocation", "angel_of_the_north");
    }
}
