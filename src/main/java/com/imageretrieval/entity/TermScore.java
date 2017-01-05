package com.imageretrieval.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class TermScore {

    private final String term;
    private final double termFrequency;
    private final double documentFrequency;
    private double tfIdf;

    public TermScore(String term) {
        this.term = term;
        this.termFrequency = 0.0;
        this.documentFrequency = 0.0;
        this.tfIdf = 0.0;
    }
}
