package com.imageretrieval.entity;

import lombok.Data;

import java.util.List;
import org.dom4j.Element;

@Data
public class Location extends XmlParsable {

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

    public Location(Element topic) {
        this(topic.element("title").getStringValue(),
            Integer.parseInt(topic.element("number").getStringValue()),
            new Coordinates(topic.element("latitude").getStringValue(),
                topic.element("longitude").getStringValue()),
            topic.element("wiki").getStringValue());
    }
}
