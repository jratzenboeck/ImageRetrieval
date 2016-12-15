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

public class PhotoService {

    private final String xmlFolderPath;
    private final String rGroundTruthFolderPath;

    public PhotoService(String xmlFolderPath, String rGroundTruthFolderPath) {
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
            throw new IllegalArgumentException();
        }
    }

    private int getGroundTruthValueForPhoto(String locationId, String photoId) {
        String fullGroundTruthPath = rGroundTruthFolderPath + "/" + locationId + " rGT.txt";

        try {
            return Files
                .lines(Paths.get(fullGroundTruthPath))
                .filter(line -> line.split(",")[0].equals(photoId))
                .map(photoLine -> Integer.parseInt(photoLine.split(",")[1]))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no ground truth for photo " + photoId + " in location " + locationId));
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }
}
