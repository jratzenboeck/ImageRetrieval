package com.imageretrieval.util;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;

public class FileUtils {

    public static void convertCSVToArffFile(String csvFile, String arffFile) {
        CSVLoader csvLoader = new CSVLoader();
        try {
            csvLoader.setSource(new File(csvFile));

            Instances data = csvLoader.getDataSet();
            FastVector values = new FastVector();
            values.addElement("1.0");
            values.addElement("0.0");
            values.addElement("-1.0");
            data.insertAttributeAt(new Attribute("groundTruth",values),data.numAttributes());
            data.setClassIndex(data.numAttributes()-1);

            ArffSaver arffSaver = new ArffSaver();
            arffSaver.setInstances(data);
            arffSaver.setFile(new File(arffFile));
            arffSaver.writeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
