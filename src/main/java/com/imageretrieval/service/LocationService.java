package com.imageretrieval.service;

import com.imageretrieval.entity.Location;
import com.imageretrieval.util.XmlParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocationService extends AbstractService {

    private final String locationCorrespondenceFile;
    private final String topicsFile;

    public LocationService(String locationCorrespondenceFile, String locationTextDescriptorsFile, String topicsFile) {
        super(locationTextDescriptorsFile);
        this.locationCorrespondenceFile = locationCorrespondenceFile;
        this.topicsFile = topicsFile;
    }

    public List<String> getAllLocationTitles() {
        try (Stream<String> lines = Files.lines(Paths.get(locationCorrespondenceFile))) {
            return lines
                .map(line -> line.split("\t")[1])
                .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Location getLocationByQuery(String query) {
        String locationTitle = getLocationTitleByQuery(query);
        return getLocationByUniqueTitle(locationTitle);
    }

    private String getLocationTitleByQuery(String query) {
        String locationTitle = null;
        try (Stream<String> lines = Files.lines(Paths.get(locationCorrespondenceFile))) {
            locationTitle = lines
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
}
