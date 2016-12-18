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

public class TextDescriptorsService {

    private final PhotoService photoService;
    private final LocationService locationService;

    public TextDescriptorsService(PhotoService photoService, LocationService locationService) {
        this.photoService = photoService;
        this.locationService = locationService;
    }

    public void writePhotoTextDescriptorsForAllLocations(String pathToFolder) {
        locationService.getAllLocationTitles().stream()
            .forEach(locationTitle -> {
                String filename = pathToFolder + "/" + locationTitle;
                writePhotoTextDescriptorsToCSVFile(filename + ".csv", locationTitle);
                System.out.println("CSV file written for location " + locationTitle);
                convertCSVToArffFile(filename + ".csv", filename + ".arff");
                System.out.println("ARFF file written for location " + locationTitle);
            });
    }

    public void writePhotoTextDescriptorsForOneLocation(String pathToFolder, String locationId) {
        String filename = pathToFolder + "/" + locationId;
        String csvFile = filename + ".csv";
        writePhotoTextDescriptorsToCSVFile(csvFile, locationId);
        convertCSVToArffFile(csvFile, filename + ".arff");
    }

    private void writePhotoTextDescriptorsToCSVFile(String filename, String locationId) {
        Map<String, TermScore> locationTermScores = locationService.getTextDescriptorsForEntity(locationId);
        List<Photo> photos = getPhotosWithExpandedTfIdfVector(locationId);
        try {
            PrintWriter printWriter = new PrintWriter(new File(filename));

            StringBuilder sbHeader = new StringBuilder();
            sbHeader.append("id,");
            locationTermScores.keySet()
                .stream()
                .forEach(header -> sbHeader.append(header + ","));
            sbHeader.append("groundTruth\n");
            printWriter.write(sbHeader.toString());

            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                StringBuilder sb = new StringBuilder();
                sb.append(photo.getId());
                sb.append(',');
                photo.getTermScores().stream().map(x -> x.getTfIdf()).forEach(x -> sb.append(x + ","));
                sb.append(photo.getGroundTruth());

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

    private List<Photo> getPhotosWithExpandedTfIdfVector(String locationId) {
        List<Photo> photos = photoService.getPhotosByLocation(locationId);

        for (Photo photo : photos) {
            List<TermScore> tfIdfVector = getTermScoresForPhoto(locationId, photo.getId(), photos.size());
            photo.setTermScores(tfIdfVector);
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
