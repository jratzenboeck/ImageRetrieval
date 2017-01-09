package com.imageretrieval.util;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileUtils {

    public static void convertCSVToArffFile(String csvFile, String arffFile) {
        CSVLoader csvLoader = new CSVLoader();
        try {
            csvLoader.setSource(new File(csvFile));
            csvLoader.setNominalAttributes("last");
            Instances data = csvLoader.getDataSet();

            writeToArffFile(data, arffFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Instances readArffFile(String inputFile) {
        try {
            final BufferedReader reader =
                new BufferedReader(new FileReader(inputFile.toString()));
            ArffLoader.ArffReader arffReader = null;
            arffReader = new ArffLoader.ArffReader(reader);
            Instances data = arffReader.getData();

            return data;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Instances removeFirstAttribute(Instances data) {
        Instances filteredData = null;
        try {
            Remove rm = new Remove();
            rm.setAttributeIndices("1");  // remove 1st attribute
            rm.setInputFormat(data);
            filteredData = Filter.useFilter(data, rm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filteredData;
    }

    public static void writeToArffFile(Instances data, String file) {
        ArffSaver arffSaver = new ArffSaver();
        arffSaver.setInstances(data);
        try {
            arffSaver.setFile(new File(file));
            arffSaver.writeBatch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
