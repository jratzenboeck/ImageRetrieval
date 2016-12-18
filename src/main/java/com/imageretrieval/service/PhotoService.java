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
import java.util.List;
import java.util.stream.Stream;

public class PhotoService extends AbstractService {

    private final String xmlFolderPath;
    private final String rGroundTruthFolderPath;

    public PhotoService(String photoTextDescriptorsFile, String xmlFolderPath, String rGroundTruthFolderPath) {
        super(photoTextDescriptorsFile);
        this.xmlFolderPath = xmlFolderPath;
        this.rGroundTruthFolderPath = rGroundTruthFolderPath;
    }

    public List<Photo> getPhotosByLocation(String locationId) {
        String fileFullPath = xmlFolderPath + "/" + locationId + ".xml";

        List<Photo> photoList = new ArrayList<>();

        try {
            XmlParser.parseXmlFile(fileFullPath, "photo", new XmlParser.ObjectCreator() {
                @Override
                public XmlParsable buildObject(Element xmlElement) {
                    Photo photo = new Photo(xmlElement);
                    photo.setGroundTruth(getGroundTruthValueForPhoto(locationId, photo.getId()));
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

    private int getGroundTruthValueForPhoto(String locationId, String photoId) {
        String fullGroundTruthPath = rGroundTruthFolderPath + "/" + locationId + " rGT.txt";

        try (Stream<String> lines = Files.lines(Paths.get(fullGroundTruthPath))) {
            return lines
                .filter(line -> line.split(",")[0].equals(photoId))
                .map(photoLine -> Integer.parseInt(photoLine.split(",")[1]))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no ground truth for photo " + photoId + " in location " + locationId));
        } catch (IOException e) {
            throw new IllegalArgumentException("Getting ground truth value failed for photo " + photoId + " for location " + locationId);
        }
    }

}
