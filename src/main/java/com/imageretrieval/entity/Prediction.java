package com.imageretrieval.entity;

import lombok.Data;
import lombok.ToString;
import weka.core.Instance;

@Data
@ToString

public class Prediction {

    private double similarityScore;
    private String photoId;
    private int ranking;
    private int cluster;
    private Instance instance;

    public Prediction (Instance instance, String photoId, double similarityScore, int ranking) {
        this.instance = instance;
        this.photoId = photoId;
        this.similarityScore = similarityScore;
        this.ranking = ranking;
    }
}
