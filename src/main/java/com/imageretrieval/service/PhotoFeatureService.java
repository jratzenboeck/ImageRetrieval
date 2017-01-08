package com.imageretrieval.service;

import com.imageretrieval.entity.Photo;
import com.imageretrieval.entity.TermScore;
import com.imageretrieval.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class PhotoFeatureService {

    private final PhotoService photoService;
    private final LocationService locationService;

    public PhotoFeatureService(PhotoService photoService, LocationService locationService) {
        this.photoService = photoService;
        this.locationService = locationService;
    }

    public void writePhotoFeaturesForAllLocations(String pathToFolder, String[] features) {
        locationService.getAllLocationTitles()
            .forEach(locationTitle -> {
                writePhotoFeaturesForOneLocation(pathToFolder, locationTitle, features);
                System.out.println("ARFF file written for location " + locationTitle);
            });
    }

    public void writePhotoFeaturesForOneLocation(String pathToFolder, String locationId, String[] features) {
        StringBuilder filename = new StringBuilder();
        filename.append(pathToFolder + "/");
        Arrays.stream(features).forEach(filename::append);
        filename.append("/");
        filename.append(locationId);
        Arrays.stream(features).forEach(featureName -> filename.append("_" + featureName));

        String csvFile = filename + ".csv";
        writePhotoFeaturesToCSVFile(csvFile, locationId, features);
        FileUtils.convertCSVToArffFile(csvFile, filename + ".arff", "first");
    }

    private void writePhotoFeaturesToCSVFile(String filename, String locationId, String[] features) {
        Map<String, TermScore> locationTermScores = locationService.getTextDescriptorsForEntity(locationId);
        List<Photo> photos = getPhotosExpandedWithFeatures(locationId);

        try {
            PrintWriter printWriter = new PrintWriter(new File(filename));

            StringBuilder sbHeader = writeHeadersToStringBuilder(locationTermScores, features);
            printWriter.write(sbHeader.toString());

            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                StringBuilder sb = writeFeaturesOfPhotoToStringBuilder(photo, features);

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

    private StringBuilder writeHeadersToStringBuilder(Map<String, TermScore> locationTermScores, String[] features) {
        Map<String, Integer> visualFeatureMap = getVisualFeatureDetails();

        StringBuilder sbHeader = new StringBuilder();
        sbHeader.append("id,");

        for (String featureName : features) {
            if (featureName.equals("txt")) {
                locationTermScores.keySet()
                    .forEach(header -> sbHeader.append(header + ","));
            }
            Integer featureSize = visualFeatureMap.get(featureName);
            if (featureSize != null) {
                appendFeatureHeader(sbHeader, featureName, featureSize);
            }
        }
        sbHeader.append("divGroundTruth\n");
        return sbHeader;
    }

    private Map<String, Integer> getVisualFeatureDetails() {
        Map<String, Integer> featureMap = new HashMap<>();
        featureMap.put("cnn", 11);
        featureMap.put("cm", 9);
        featureMap.put("csd", 64);
        featureMap.put("lbp", 16);
        featureMap.put("hog", 81);
        return featureMap;
    }

    private StringBuilder appendFeatureHeader(StringBuilder sb, String featureName, int featureSize) {
        for (int i = 0; i < featureSize; i++) {
            sb.append(featureName + i + ",");
        }
        return sb;
    }

    private StringBuilder writeFeaturesOfPhotoToStringBuilder(Photo photo, String[] features) {
        StringBuilder sb = new StringBuilder();
        sb.append(photo.getId());
        sb.append(',');

        for (String featureName : features) {
            if (featureName.equals("txt"))
                photo.getTermScores().stream().map(TermScore::getTfIdf).forEach(x -> sb.append(x + ","));
            if (featureName.equals("cnn"))
                photo.getColorNames().forEach(x -> sb.append(x + ","));
            if (featureName.equals("cm"))
                photo.getColorMomentsHSV().forEach(x -> sb.append(x + ","));
            if (featureName.equals("csd"))
                photo.getColorStructureDescriptors().forEach(x -> sb.append(x + ","));
            if (featureName.equals("lbp"))
                photo.getLbp().forEach(x -> sb.append(x + ","));
            if (featureName.equals("hog"))
                photo.getHog().forEach(x -> sb.append(x + ","));
        }

        sb.append(photo.getDivGroundTruth());
        return sb;
    }

    private List<Photo> getPhotosExpandedWithFeatures(String locationId) {
        List<Photo> photos = photoService.getPhotosByLocation(locationId);
        Map<String, List<Double>> colorNamesForPhotos = photoService.getColorNamesForPhotos(locationId);
        Map<String, List<Double>> colorMomentsHSVForPhotos = photoService.getColorMomentsHSVForPhotos(locationId);
        Map<String, List<Double>> colorStructureDescriptorsForPhotos = photoService.getColorStructureDescriptorsForPhotos(locationId);
        Map<String, List<Double>> lbpForPhotos = photoService.getLBPForPhotos(locationId);
        Map<String, List<Double>> hogForPhotos = photoService.getHOGForPhotos(locationId);

        for (Photo photo : photos) {
            List<TermScore> tfIdfVector = getTermScoresForPhoto(locationId, photo.getId(), photos.size());
            photo.setTermScores(tfIdfVector);
            photo.setColorNames(colorNamesForPhotos.get(photo.getId()));
            photo.setColorMomentsHSV(colorMomentsHSVForPhotos.get(photo.getId()));
            photo.setColorStructureDescriptors(colorStructureDescriptorsForPhotos.get(photo.getId()));
            photo.setLbp(lbpForPhotos.get(photo.getId()));
            photo.setHog(hogForPhotos.get(photo.getId()));
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
