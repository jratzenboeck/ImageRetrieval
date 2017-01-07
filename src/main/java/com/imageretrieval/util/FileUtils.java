package com.imageretrieval.util;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;

public class FileUtils {

    public static void convertCSVToArffFile(String csvFile, String arffFile, String stringAttributesIndex) {
        CSVLoader csvLoader = new CSVLoader();
        try {
            csvLoader.setSource(new File(csvFile));
            csvLoader.setStringAttributes(stringAttributesIndex);
            csvLoader.setNominalAttributes("last");
            Instances data = csvLoader.getDataSet();

            ArffSaver arffSaver = new ArffSaver();
            arffSaver.setInstances(data);
            arffSaver.setFile(new File(arffFile));
            arffSaver.writeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
