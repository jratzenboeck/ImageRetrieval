package com.imageretrieval.service;

import com.imageretrieval.entity.Location;
import com.imageretrieval.entity.TermScores;
import com.imageretrieval.util.XmlParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LocationService {

    private final String locationCorrespondenceFile;
    private final String locationTextDescriptorsFile;
    private final String topicsFile;

    public LocationService(String locationCorrespondenceFile, String locationTextDescriptorsFile, String topicsFile) {
        this.locationCorrespondenceFile = locationCorrespondenceFile;
        this.locationTextDescriptorsFile = locationTextDescriptorsFile;
        this.topicsFile = topicsFile;
    }

    public Location getLocationByQuery(String query) {
        String locationTitle = getLocationTitleByQuery(query);
        return getLocationByUniqueTitle(locationTitle);
    }

    private String getLocationTitleByQuery(String query) {
        String locationTitle = null;
        try {
            locationTitle = Files
                .lines(Paths.get(locationCorrespondenceFile))
                .map(line -> line.split("\t"))
                .filter(locationTuple -> locationTuple[0].equals(query))
                .map(locationTuple -> locationTuple[1])
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No location for " + query + " could be found."));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locationTitle;
    }

    private Location getLocationByUniqueTitle(String uniqueTitle) {
        Document xmlDocument = parseXmlDocument();
        Element root = xmlDocument.getRootElement();

        for (Iterator iterator = root.elementIterator("topic"); iterator.hasNext(); ) {
            Element topic = (Element) iterator.next();
            String title = topic.element("title").getStringValue();
            if (title.equals(uniqueTitle)) {
                return new Location(topic);
            }
        }
        throw new IllegalArgumentException("No location for title " + uniqueTitle + " found.");
    }

    private Document parseXmlDocument() {
        Document document = null;
        try {
            document = XmlParser.parseXmlDocument(topicsFile);
        } catch (DocumentException e) {
            System.out.println("XML document " + topicsFile + " could not be parsed. " + e.getMessage());
        }
        return document;
    }

    public Map<String, TermScores> getTextDescriptorsForLocation(String locationId) {
        Map<String, TermScores> textDescriptors = new HashMap<>();
        try {
            String[] descriptors = Files
                .lines(Paths.get(locationTextDescriptorsFile))
                .filter(line -> line.split(" ")[0].equals(locationId))
                .map(locationStr -> locationStr.split(" "))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException());

            int index = 0;
            for (; index < descriptors.length && !descriptors[index].startsWith("\""); index++);

            for (int i = index; i < descriptors.length; i += 4) {
                textDescriptors.put(descriptors[i], new TermScores(Float.parseFloat(descriptors[i + 1]),
                    Float.parseFloat(descriptors[i + 2]),
                    Float.parseFloat(descriptors[i + 3])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textDescriptors;
    }

}
