package com.imageretrieval.service;

import com.imageretrieval.entity.Photo;
import com.imageretrieval.entity.TermScore;
import com.imageretrieval.util.FileUtils;
import weka.core.Instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

public class PhotoFeatureService {

    private final PhotoService photoService;
    private final LocationService locationService;

    public PhotoFeatureService(PhotoService photoService, LocationService locationService) {
        this.photoService = photoService;
        this.locationService = locationService;
    }

//    public void writePhotoFeaturesForAllLocations(String pathToFolder, String[] features) {
//        locationService.getAllLocationTitles()
//            .forEach(locationTitle -> {
//                writePhotoFeaturesForOneLocation(pathToFolder, locationTitle, features);
//                System.out.println("ARFF file written for location " + locationTitle);
//            });
//    }

    public void writePhotoFeaturesForAllLocationsToOneFile(String pathToFolder, String[] features) {
        StringBuilder filename = new StringBuilder();
        filename.append(pathToFolder + "/");
        Arrays.stream(features).forEach(filename::append);

        String csvFile = filename.toString() + "_combined.csv";
        writeVSHeadersToCSVFile(csvFile, features);

        locationService.getAllLocationTitles()
            .forEach(locationTitle -> {
                writePhotoFeaturesForOneLocation(locationTitle, features, csvFile);
                System.out.println("ARFF file written for location " + locationTitle);
            });

        FileUtils.convertCSVToArffFile(csvFile, filename.toString() + "_combined.arff");
    }

    public void removeIdAttributeOfAllLocations(String pathToFolder, String[] features) {
        locationService.getAllLocationTitles()
            .forEach(locationTitle -> {
                removeIdAttributeForOneLocation(pathToFolder, locationTitle, features);
                System.out.println("ARFF file written for location " + locationTitle);
            });
    }

    private void removeIdAttributeForOneLocation(String pathToFolder, String locationId, String[] features) {
        String filename = getBaseFilenameForLocationAndFeatures(pathToFolder, locationId, features);
        Instances data = FileUtils.readArffFile(filename + ".arff");
        FileUtils.writeToArffFile(FileUtils.removeFirstAttribute(data), filename + "_new.arff");
    }

    public void writePhotoFeaturesForOneLocation(String locationId, String[] features, String csvFile) {
        writePhotoFeaturesToCSVFile(csvFile, locationId, features, true);
        //FileUtils.convertCSVToArffFile(csvFile, filename.toString() + "_combined.arff");
    }

    private String getBaseFilenameForLocationAndFeatures(String pathToFolder, String locationId, String[] features) {
        StringBuilder filename = new StringBuilder();
        filename.append(pathToFolder + "/");
        Arrays.stream(features).forEach(filename::append);
        filename.append("/");
        filename.append(locationId);
        Arrays.stream(features).forEach(featureName -> filename.append("_" + featureName));
        return filename.toString();
    }

    private void writeHeadersToCSVFile(String filename, String locationId, String[] features) {
        Map<String, TermScore> locationTermScores = locationService.getTextDescriptorsForEntity(locationId);
        try(PrintWriter printWriter = new PrintWriter(new File(filename))) {
            StringBuilder sbHeader = writeHeadersToStringBuilder(locationTermScores, features);
            printWriter.write(sbHeader.toString());
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeVSHeadersToCSVFile(String filename, String[] features) {
        try(PrintWriter printWriter = new PrintWriter(new File(filename))) {
            StringBuilder sbHeader = writeVSDescriptorHeadersToStringBuilder(features);
            printWriter.write(sbHeader.toString());
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writePhotoFeaturesToCSVFile(String filename, String locationId, String[] features, boolean appendToFile) {
        List<Photo> photos = getPhotosExpandedWithFeatures(locationId);

        try(PrintWriter printWriter = new PrintWriter(new FileOutputStream(new File(filename), appendToFile))) {

            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                StringBuilder sb = writeFeaturesOfPhotoToStringBuilder(photo, features);

                printWriter.write('\n');
                printWriter.write(sb.toString());
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
        sbHeader.append("groundTruth\n");
        return sbHeader;
    }

    private StringBuilder writeVSDescriptorHeadersToStringBuilder(String[] features) {
        Map<String, Integer> visualFeatureMap = getVisualFeatureDetails();

        StringBuilder sbHeader = new StringBuilder();
        sbHeader.append("id,");

        for (String featureName : features) {
            Integer featureSize = visualFeatureMap.get(featureName);
            if (featureSize != null) {
                appendFeatureHeader(sbHeader, featureName, featureSize);
            }
        }
        sbHeader.append("groundTruth");
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

        sb.append(photo.getRelGroundTruth());
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
