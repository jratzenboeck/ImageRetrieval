package com.imageretrieval.service;

import org.junit.Assert;
import org.junit.Test;


public class PhotoServiceTest {

    private final PhotoService service = new PhotoService("data/devset/xml", "data/devset/gt/rGT");

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

}
