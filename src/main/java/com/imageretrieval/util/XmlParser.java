package com.imageretrieval.util;

import com.imageretrieval.entity.XmlParsable;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.Iterator;

public class XmlParser {

    public interface ObjectCreator {
        XmlParsable buildObject(Element xmlElement);
        void useObject(XmlParsable object);
    }

    public static Document parseXmlDocument(String fileFullPath) throws DocumentException{
        SAXReader reader = new SAXReader();
        return reader.read(fileFullPath);
    }

    public static void parseXmlFile (String fullPathToXmlFile, String tag, ObjectCreator objectCreator) throws DocumentException {
        Document xmlDocument = parseXmlDocument(fullPathToXmlFile);
        Element root = xmlDocument.getRootElement();
        for (Iterator iterator = root.elementIterator(tag); iterator.hasNext(); ) {
            Element xmlElement = (Element) iterator.next();
            XmlParsable object = objectCreator.buildObject(xmlElement);
            objectCreator.useObject(object);
        }
    }
}
