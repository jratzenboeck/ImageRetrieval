package com.imageretrieval.service;

import com.imageretrieval.entity.Location;
import com.imageretrieval.entity.TermScore;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class LocationServiceTest {

    private final LocationService service = new LocationService(
        "data/devset/poiNameCorrespondences.txt",
        "data/devset/desctxt/devset_textTermsPerPOI.wFolderNames.txt",
        "data/devset/devset_topics.xml");

    @Test
    public void testGetLocationFromQuery() {
        Location location = service.getLocationByQuery("angel of the north");

        Assert.assertEquals("angel_of_the_north", location.getQueryTitle());
        Assert.assertEquals(1, location.getQueryId());
        Assert.assertEquals(54.9141f, location.getCoordinates().getLatitude(), 0);
        Assert.assertEquals(-1.58949f, location.getCoordinates().getLongitude(), 0);
        Assert.assertEquals("http://en.wikipedia.org/wiki/Angel_of_the_North", location.getWikiUrl());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLocationFromQueryWrongTitle() {
        service.getLocationByQuery("blabla");
    }

    @Test
    public void testGetTextDescriptorsForLocation() {
        Map<String, TermScore> textDescriptors = service.getTextDescriptorsForEntity("angel_of_the_north");
        textDescriptors
            .keySet()
            .stream()
            .forEach(key ->
                System.out.println("Term: " + key + " Value: " + textDescriptors.get(key).getTfIdf()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTextDescriptorsForLocationWrongTitle() {
        service.getTextDescriptorsForEntity("blabla");
    }
}
