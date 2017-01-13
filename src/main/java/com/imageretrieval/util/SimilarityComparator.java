package com.imageretrieval.util;

import com.imageretrieval.entity.Prediction;

import java.util.Comparator;

public class SimilarityComparator implements Comparator<Prediction> {

    @Override
    public int compare(Prediction o1, Prediction o2) {
        return -((Double) o1.getSimilarityScore()).compareTo(o2.getSimilarityScore());
    }
}
