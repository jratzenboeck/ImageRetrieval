package com.imageretrieval.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class TermScores {

    private final float termFrequency;
    private final float documentFrequency;
    private final float tfIdf;

}
