package com.imageretrieval.util;

import com.imageretrieval.service.LocationService;
import org.junit.Test;

import java.io.File;

/**
 * Created by mada on 11.01.17.
 */
public class PredictorTest {

    private final LocationService locationService = new LocationService(
        "data/testset/poiNameCorrespondences.txt",
        "data/devset/desctxt/devset_textTermsPerPOI.wFolderNames.txt",
        "data/testset/testset_topics.xml");

    @Test
    public void predict() throws Exception {
        Predictor predictor = new Predictor();
        predictor.createClassifier("data/devset/predictor_test/cnncmcsdlbphog_combined.arff");
        predictor.predict("data/testset/featuresOneLocation/cnncmcsdlbphog/",
            "bath_abbey_cnn_cm_csd_lbp_hog.csv",
            locationService.getLocationByUniqueTitle("bath_abbey").getQueryId());
    }

    @Test
    public void predictAll() throws Exception {
        Predictor predictor = new Predictor();
        predictor.createClassifier("data/devset/predictor_test/cnncmcsdlbphog_combined.arff");
        File dir = new File("data/testset/features/cnncmcsdlbphog/");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                String locationUniqueTitle = child.getName().split("-")[0];
                predictor.predict("data/testset/features/cnncmcsdlbphog/",
                    child.getName(),
                    locationService.getLocationByUniqueTitle(locationUniqueTitle).getQueryId());
            }
        }
    }

}