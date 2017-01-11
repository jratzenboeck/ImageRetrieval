package com.imageretrieval.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString

public class Prediction {

    private double similarityScore;
    private String photoId;
    private int ranking;

    public Prediction (String photoId, double similarityScore, int ranking) {
        this.photoId = photoId;
        this.similarityScore = similarityScore;
        this.ranking = ranking;
    }
}
