package com.imageretrieval.service;

import com.imageretrieval.entity.Photo;
import com.imageretrieval.entity.XmlParsable;
import com.imageretrieval.util.XmlParser;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PhotoService extends AbstractService {

    private final String xmlFolderPath;
    private final String rGroundTruthFolderPath;
    private final String dGroundTruthFolderPath;
    private final String colorNamesPath;

    public PhotoService(String photoTextDescriptorsFile, String xmlFolderPath, String rGroundTruthFolderPath, String dGroundTruthFolderPath, String colorNamesPath) {
        super(photoTextDescriptorsFile);
        this.xmlFolderPath = xmlFolderPath;
        this.rGroundTruthFolderPath = rGroundTruthFolderPath;
        this.dGroundTruthFolderPath = dGroundTruthFolderPath;
        this.colorNamesPath = colorNamesPath;
    }

    public List<Photo> getPhotosByLocation(String locationId) {
        String fileFullPath = xmlFolderPath + "/" + locationId + ".xml";

        List<Photo> photoList = new ArrayList<>();

        try {
            XmlParser.parseXmlFile(fileFullPath, "photo", new XmlParser.ObjectCreator() {
                @Override
                public XmlParsable buildObject(Element xmlElement) {
                    Photo photo = new Photo(xmlElement);
//                    photo.setRelGroundTruth(0);
//                    photo.setDivGroundTruth(0);
                    return photo;
                }

                @Override
                public void useObject(XmlParsable object) {
                    photoList.add((Photo) object);
                }
            });
            return photoList;
        } catch (DocumentException ex) {
            throw new IllegalArgumentException("XML processing failed for location title " + locationId);
        }
    }

    @Override
    protected double calculateAdvancedTfIdf(double termFrequency, double documentFrequency, int numberOfDocuments) {
//        return super.calculateAdvancedTfIdf(termFrequency, documentFrequency, numberOfDocuments);
        // Do not use the default calculation in this case
        return Math.abs((1 + Math.log10(termFrequency)) * Math.log10(numberOfDocuments / documentFrequency));
    }

    private int getGroundTruthForPhoto (String locationId, String photoId, String folderPath, String ending) {
        String fullGroundTruthPath = folderPath + "/" + locationId + ending;

        try (Stream<String> lines = Files.lines(Paths.get(fullGroundTruthPath))) {
            return lines
                .filter(line -> line.split(",")[0].equals(photoId))
                .map(photoLine -> Integer.parseInt(photoLine.split(",")[1]))
                .findFirst()
                .orElse(0);
        } catch (IOException e) {
            throw new IllegalArgumentException("Getting ground truth value failed for photo " + photoId + " for location " + locationId);
        }
    }

    private int getRelGroundTruthValueForPhoto(String locationId, String photoId) {
        return getGroundTruthForPhoto(locationId, photoId, rGroundTruthFolderPath, " rGT.txt");
    }

    private int getDivGroundTruthValueForPhoto(String locationId, String photoId) {
        return getGroundTruthForPhoto(locationId, photoId, dGroundTruthFolderPath, " dGT.txt");
    }

    public Map<String, List<Double>> getColorNamesForPhotos(String locationId) {
        return getVisualDescriptorForPhotos(locationId, "CN");
    }

    public Map<String, List<Double>> getColorMomentsHSVForPhotos(String locationId) {
        return getVisualDescriptorForPhotos(locationId, "CM");
    }

    public Map<String, List<Double>> getColorStructureDescriptorsForPhotos(String locationId) {
        return getVisualDescriptorForPhotos(locationId, "CSD");
    }

    public Map<String, List<Double>> getLBPForPhotos(String locationId) {
        return getVisualDescriptorForPhotos(locationId, "LBP");
    }

    public Map<String, List<Double>> getHOGForPhotos(String locationId) {
        return getVisualDescriptorForPhotos(locationId, "HOG");
    }

    private Map<String, List<Double>> getVisualDescriptorForPhotos(String locationId, String visualDescriptorExtension) {
        String filename = colorNamesPath + "/" + locationId + " " + visualDescriptorExtension + ".csv";

        Map<String, List<Double>> photosWithVisualDescriptor = new HashMap<>();

        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
            lines
                .map(line -> line.split(","))
                .forEach(descriptors -> {
                    List<Double> visualDescriptorValues = new ArrayList<>();
                    for (int i = 1; i < descriptors.length; i++) {
                        visualDescriptorValues.add(Double.parseDouble(descriptors[i]));
                    }
                    photosWithVisualDescriptor.put(descriptors[0], visualDescriptorValues);
                });

        } catch (IOException e) {
            throw new IllegalArgumentException("Getting color names for location " + locationId + " failed");
        }
        return photosWithVisualDescriptor;
    }

}
