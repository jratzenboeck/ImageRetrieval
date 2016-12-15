package com.imageretrieval.entity;

import lombok.Data;
import lombok.ToString;
import org.dom4j.Element;

@Data
@ToString
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
    private int groundTruth;

    public Photo(String description, String id, Coordinates coordinates, int nbComments, int rank, String tags,
                 String title, String url, String userId, int views) {
        this.description = description;
        this.id = id;
        this.coordinates = coordinates;
        this.nbComments = nbComments;
        this.rank = rank;
        this.tags = tags;
        this.title = title;
        this.url = url;
        this.userId = userId;
        this.views = views;
    }

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
