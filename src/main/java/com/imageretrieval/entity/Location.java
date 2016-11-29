package com.imageretrieval.entity;

import lombok.Data;

import java.util.List;

@Data
public class Location {

    private final String queryTitle;
    private final int queryId;
    private final Coordinates coordinates;
    private final String wikiUrl;
    private List<String> relevantImageIds;

    public Location(String queryTitle, int queryId, Coordinates coordinates, String wikiUrl) {
        this.queryTitle = queryTitle;
        this.queryId = queryId;
        this.coordinates = coordinates;
        this.wikiUrl = wikiUrl;
    }
}
