package com.imageretrieval.entity;

import lombok.Data;

@Data
public class Coordinates {

    private final float latitude, longitude;

    public Coordinates(float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Coordinates(String latitudeStr, String longitudeStr) {
        this.latitude = Float.parseFloat(latitudeStr);
        this.longitude = Float.parseFloat(longitudeStr);
    }
}
