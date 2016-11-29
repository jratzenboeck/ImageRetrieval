package com.imageretrieval.service;

import com.imageretrieval.entity.Coordinates;
import com.imageretrieval.entity.Location;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LocationService {

    private final String locationCorrespondenceFile;
    private final String topicsFile;

    public LocationService(String locationCorrespondenceFile, String topicsFile) {
        this.locationCorrespondenceFile = locationCorrespondenceFile;
        this.topicsFile = topicsFile;
    }

    public Location getLocationByQuery(String query) {
        String locationTitle = getLocationTitleByQuery(query);
        if (locationTitle == null) {
            throw new IllegalArgumentException("No location for " + query + " could be found.");
        }
        return getLocationByUniqueTitle(locationTitle);
    }

    private String getLocationTitleByQuery(String query) {
        Map<String, String> locations = readLocationsFromFile();
        return locations.get(query);
    }

    private Map<String, String> readLocationsFromFile() {
        Map<String, String> locations = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(locationCorrespondenceFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split("\t");
                locations.put(lineSplit[0], lineSplit[1]);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File " + locationCorrespondenceFile + " could not be found. " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locations;
    }

    private Location getLocationByUniqueTitle(String uniqueTitle) {
        Document xmlDocument = parseXmlDocument();
        Element root = xmlDocument.getRootElement();

        for (Iterator iterator = root.elementIterator("topic"); iterator.hasNext(); ) {
            Element topic = (Element) iterator.next();
            String title = topic.element("title").getStringValue();
            if (title.equals(uniqueTitle)) {
                Coordinates coordinates = new Coordinates(topic.element("latitude").getStringValue(),
                                                     topic.element("longitude").getStringValue());
                Location location = new Location(title,
                    Integer.parseInt(topic.element("number").getStringValue()),
                    coordinates,
                    topic.element("wiki").getStringValue());
                return location;
            }
        }
        throw new IllegalArgumentException("No location for title " + uniqueTitle + " found.");
    }

    private Document parseXmlDocument() {
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(topicsFile);
        } catch (DocumentException e) {
            System.out.println("XML document " + topicsFile + " could not be parsed. " + e.getMessage());
        }
        return document;
    }

}
