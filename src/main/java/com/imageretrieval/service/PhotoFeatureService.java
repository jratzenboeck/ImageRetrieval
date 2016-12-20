package com.imageretrieval.service;

import com.imageretrieval.entity.Photo;
import com.imageretrieval.entity.TermScore;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhotoFeatureService {

    private final PhotoService photoService;
    private final LocationService locationService;
    private static final String[] COLOR_NAMES = new String[] {"cnn_black", "cnn_blue", "cnn_brown", "cnn_grey", "cnn_green", "cnn_orange", "cnn_pink", "cnn_purple", "cnn_red", "cnn_white", "cnn_yellow"};

    public PhotoFeatureService(PhotoService photoService, LocationService locationService) {
        this.photoService = photoService;
        this.locationService = locationService;
    }

    public void writePhotoFeaturesForAllLocations(String pathToFolder) {
        locationService.getAllLocationTitles().stream()
            .forEach(locationTitle -> {
                String filename = pathToFolder + "/" + locationTitle;
                writePhotoFeaturesToCSVFile(filename + ".csv", locationTitle);
                System.out.println("CSV file written for location " + locationTitle);
                convertCSVToArffFile(filename + ".csv", filename + ".arff");
                System.out.println("ARFF file written for location " + locationTitle);
            });
    }

    public void writePhotoFeaturesForOneLocation(String pathToFolder, String locationId) {
        String filename = pathToFolder + "/" + locationId;
        String csvFile = filename + ".csv";
        writePhotoFeaturesToCSVFile(csvFile, locationId);
        convertCSVToArffFile(csvFile, filename + ".arff");
    }

    private void writePhotoFeaturesToCSVFile(String filename, String locationId) {
        Map<String, TermScore> locationTermScores = locationService.getTextDescriptorsForEntity(locationId);
        List<Photo> photos = getPhotosExpandedWithFeatures(locationId);
        try {
            PrintWriter printWriter = new PrintWriter(new File(filename));

            StringBuilder sbHeader = new StringBuilder();
            sbHeader.append("id,");
            locationTermScores.keySet()
                .stream()
                .forEach(header -> sbHeader.append(header + ","));
            for (String colorName : COLOR_NAMES) {
                sbHeader.append(colorName + ",");
            }
            sbHeader.append("groundTruth\n");
            printWriter.write(sbHeader.toString());

            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                StringBuilder sb = writeFeaturesOfPhotoToCSVFile(photo);

                printWriter.write(sb.toString());
                if (i < photos.size() - 1) {
                    printWriter.write('\n');
                }
            }
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private StringBuilder writeFeaturesOfPhotoToCSVFile(Photo photo) {
        StringBuilder sb = new StringBuilder();
        sb.append(photo.getId());
        sb.append(',');
        photo.getTermScores().stream().map(x -> x.getTfIdf()).forEach(x -> sb.append(x + ","));
        photo.getColorNames().stream().forEach(x -> sb.append(x + ","));
        sb.append(photo.getGroundTruth());
        return sb;
    }

    private void convertCSVToArffFile(String csvFile, String arffFile) {
        CSVLoader csvLoader = new CSVLoader();
        try {
            csvLoader.setSource(new File(csvFile));
            csvLoader.setNominalAttributes("last");
            Instances data = csvLoader.getDataSet();

            ArffSaver arffSaver = new ArffSaver();
            arffSaver.setInstances(data);
            arffSaver.setFile(new File(arffFile));
            arffSaver.writeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Photo> getPhotosExpandedWithFeatures(String locationId) {
        List<Photo> photos = photoService.getPhotosByLocation(locationId);
        Map<String, List<Double>> colorNamesForPhotos = photoService.getColorNamesForPhotos(locationId);

        for (Photo photo : photos) {
            List<TermScore> tfIdfVector = getTermScoresForPhoto(locationId, photo.getId(), photos.size());
            photo.setTermScores(tfIdfVector);
            photo.setColorNames(colorNamesForPhotos.get(photo.getId()));
        }
        return photos;
    }

    private List<TermScore> getTermScoresForPhoto(String locationId, String photoId, int numberOfPhotos) {
        Map<String, TermScore> locationTermScores = locationService.getTextDescriptorsForEntity(locationId);
        Map<String, TermScore> photoTermScores = photoService.getTextDescriptorsForEntity(photoId);
        List<TermScore> tfIdfVector = new ArrayList<>();

        for (String term : locationTermScores.keySet()) {
            TermScore photoTermScore = photoTermScores.get(term);
            if (photoTermScore != null) {
                photoTermScore.setTfIdf(photoService.calculateAdvancedTfIdf(photoTermScore.getTermFrequency(),
                    photoTermScore.getDocumentFrequency(), numberOfPhotos));
                tfIdfVector.add(photoTermScore);
            } else {
                tfIdfVector.add(new TermScore(term));
            }
        }
        return tfIdfVector;
    }
}
