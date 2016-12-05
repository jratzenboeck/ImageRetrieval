package com.imageretrieval.service;

import com.imageretrieval.entity.Photo;
import com.imageretrieval.entity.XmlParsable;
import com.imageretrieval.util.XmlParser;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class PhotoService {

    private String folderPath;

    public PhotoService(String xmlFolderPath) {
        this.folderPath = xmlFolderPath;
    }

    public List<Photo> getPhotosByLocation(String locationId) {
        String fileFullPath = folderPath + "/" + locationId + ".xml";
        List<Photo> photoList = new ArrayList<>();
        try {
            XmlParser.parseXmlFile(fileFullPath, "photo", new XmlParser.ObjectCreator() {
                @Override
                public XmlParsable buildObject(Element xmlElement) {
                    return new Photo(xmlElement);
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
}
