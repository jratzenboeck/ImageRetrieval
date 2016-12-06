package com.imageretrieval.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Coordinates {

    private final float latitude, longitude;

    public Coordinates(String latitudeStr, String longitudeStr) {
        this(Float.parseFloat(latitudeStr), Float.parseFloat(longitudeStr));
    }
}
