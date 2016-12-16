package com.imageretrieval.service;

import org.junit.Test;

public class TextDescriptorsServiceTest {

    private final PhotoService photoService = new PhotoService("data/devset/desctxt/devset_textTermsPerImage.txt",
        "data/devset/xml",
        "data/devset/gt/rGT");
    private final LocationService locationService = new LocationService(
        "data/devset/poiNameCorrespondences.txt",
        "data/devset/desctxt/devset_textTermsPerPOI.wFolderNames.txt",
        "data/devset/devset_topics.xml");
    private final TextDescriptorsService textDescriptorsService = new TextDescriptorsService(photoService, locationService);

    @Test
    public void testWritePhotoTextDescriptorsToCSVFile() {
        final String ANY_LOCATION_ID = "acropolis_athens";

        textDescriptorsService.writePhotoTextDescriptorsToCSVFile("data/devset/desctxt/devset_tfidfPerImage.csv", ANY_LOCATION_ID);
    }

    @Test
    public void testConvertCSVToArffFile() {
        final String filename = "data/devset/desctxt/devset_tfidfPerImage";
        textDescriptorsService.convertCSVToArffFile(filename + ".csv", filename + ".arff");
    }
}
