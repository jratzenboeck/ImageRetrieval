package com.imageretrieval.util;

import com.imageretrieval.entity.Prediction;
import com.imageretrieval.service.LocationService;
import org.junit.Test;

import java.io.File;
import java.util.*;

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
            "data/testset/featuresWikiOneLocation/cnncmcsdlbphog/","doge_s_palace",
            "doge_s_palace-_cnn_cm_csd_lbp_hog.csv",
            locationService.getLocationByUniqueTitle("doge_s_palace").getQueryId());
    }

    @Test
    public void predictAll() throws Exception {
        Predictor predictor = new Predictor();
        predictor.createClassifier("data/devset/predictor_test/cnncmcsdlbp_combined.arff");

        String pathToTest = "data/testset/features/cnncmcsdlbp/";

        File dir = new File(pathToTest);
        Map<Integer, List<Prediction>> predictions = new HashMap<>();

        File[] directoryListing = dir.listFiles();
        List<File> files = Arrays.asList(directoryListing);
        files.sort(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return locationService.getLocationByUniqueTitle(o1.getName().split("-")[0]).getQueryId() -
                    locationService.getLocationByUniqueTitle(o2.getName().split("-")[0]).getQueryId();
            }
        });
        if (directoryListing != null) {
            for (File child : files) {
                String locationUniqueTitle = child.getName().split("-")[0];
                predictor.predict(pathToTest, "data/testset/featuresWiki/cnncmcsdlbp/",
                    locationUniqueTitle,
                    child.getName(),
                    locationService.getLocationByUniqueTitle(locationUniqueTitle).getQueryId());
            }
        }
    }

}