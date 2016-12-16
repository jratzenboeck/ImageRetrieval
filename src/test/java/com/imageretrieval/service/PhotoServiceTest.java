package com.imageretrieval.service;

import com.imageretrieval.entity.TermScore;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;


public class PhotoServiceTest {

    private final PhotoService service = new PhotoService("data/devset/desctxt/devset_textTermsPerImage.txt",
        "data/devset/xml",
        "data/devset/gt/rGT");

    @Test
    public void testGetPhotosByLocation() {
        Assert.assertEquals(298, service.getPhotosByLocation("acropolis_athens").size());
        Assert.assertEquals(299, service.getPhotosByLocation("agra_fort").size());
        Assert.assertEquals(299, service.getPhotosByLocation("berlin_cathedral").size());
        Assert.assertEquals(292, service.getPhotosByLocation("cn_tower").size());
        Assert.assertEquals(300, service.getPhotosByLocation("pont_alexandre_iii").size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPhotosForInexistentLocation() {
        service.getPhotosByLocation("some invalid location");
    }

    @Test
    public void testGetTextDescriptorsForPhoto() {
        Map<String, TermScore> termScores = service.getTextDescriptorsForEntity("9067739127");
        termScores.keySet().stream().forEach(x -> System.out.println("Term: " + x + ", Value: " + termScores.get(x).getTfIdf()));
        Assert.assertEquals(195, termScores.size());
    }

}
