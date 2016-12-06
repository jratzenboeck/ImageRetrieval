package com.imageretrieval.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.dom4j.Element;

@Data
@AllArgsConstructor
public class Photo extends XmlParsable {

    private String description;
    private String id;
    private Coordinates coordinates;
    private int nbComments;
    private int rank;
    private String tags;
    private String title;
    private String url;
    private String userId;
    private int views;

    public Photo (Element xmlElement) {
        this(xmlElement.attributeValue("description"),
            xmlElement.attributeValue("id"),
            new Coordinates(
                xmlElement.attributeValue("latitude"),
                xmlElement.attributeValue("longitude")
            ),
            Integer.valueOf(xmlElement.attributeValue("nbComments")),
            Integer.valueOf(xmlElement.attributeValue("rank")),
            xmlElement.attributeValue("tags"),
            xmlElement.attributeValue("title"),
            xmlElement.attributeValue("url_b"),
            xmlElement.attributeValue("userid"),
            Integer.valueOf(xmlElement.attributeValue("views")));
    }
}
