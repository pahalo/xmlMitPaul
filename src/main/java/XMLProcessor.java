import java.io.*;
import java.util.*;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.Namespace;

/**
 * This class searches tif for duplicates in the MPI import
 */
public class XMLProcessor {

    public static void main(String[] args) {
    	int filesWithDuplicates = 0;
        int totalDuplicates = 0;
        
        if (args.length != 1) {
            System.out.println("Bitte nur ein Verzeichnis angeben.");
        } else {
            File directory = new File(args[0]).getAbsoluteFile();
            if (directory.exists() && directory.isDirectory()) {
                processFiles(directory, filesWithDuplicates, totalDuplicates);
                System.out.println("Anzahl der Dateien mit Duplikaten: " + filesWithDuplicates);
                System.out.println("Gesamtanzahl der Duplikate: " + totalDuplicates);
            } else {
                System.out.println("Das Verzeichnis existiert nicht.");
            }
        }
    }

    /**
     * Recursively traverses all files and directories in the specified directory and processes XML files.
     * @param directory The directory to be searched.
     * @param filesWithDuplicates Number of files with duplicates.
     * @param totalDuplicates Total count of duplicates.
     */
    
    private static void processFiles(File directory, int filesWithDuplicates, int totalDuplicates) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
                        processXmlFile(file, directory, filesWithDuplicates, totalDuplicates);
                    } else if (file.isDirectory()) {
                        processFiles(file, filesWithDuplicates, totalDuplicates);
                    }
                }
            }
        }
    }
    /**
     * Processes a single XML file and collects all XML elements.
     * @param file The XML file to be processed.
     * @param directory The directory where the XML file resides.
     * @param filesWithDuplicates Number of files with duplicates.
     * @param totalDuplicates Total count of duplicates.
     */

    private static void processXmlFile(File file, File directory, int filesWithDuplicates, int totalDuplicates) {
        List<String> xmlElementsList = new ArrayList<>();

        try {
            SAXBuilder sax = new SAXBuilder();
            Document doc = sax.build(file);
            Element rootElement = doc.getRootElement();
            collectXmlElements(rootElement, file, xmlElementsList);
            findDuplicates(xmlElementsList, file,  filesWithDuplicates, totalDuplicates);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Collects XML elements recursively.
     * @param element The XML element being processed.
     * @param xmlFile The XML file currently being processed.
     * @param xmlElementsList List to collect XML elements.
     */
    private static void collectXmlElements(Element element, File xmlFile, List<String> xmlElementsList) {
        if (element != null) {
            StringBuilder elementString = new StringBuilder();

            Namespace ns = element.getNamespace(); 
            String prefix = ns.getPrefix(); 

            if (!prefix.isEmpty()) {
                elementString.append("<").append(prefix).append(":").append(element.getName());
            } else {
                elementString.append("<").append(element.getName());
            }

            List<Attribute> attributes = element.getAttributes();
            for (Attribute attribute : attributes) {
                elementString.append(" ").append(attribute.getQualifiedName()).append("=\"").append(attribute.getValue()).append("\"");
            }

            List<Element> children = element.getChildren();

            if (!children.isEmpty()) {
                elementString.append(">");

                xmlElementsList.add(elementString.toString());

                for (Element child : children) {
                    collectXmlElements(child, xmlFile, xmlElementsList);
                }

                if (!prefix.isEmpty()) {
                    xmlElementsList.add("</" + prefix + ":" + element.getName() + ">");
                } else {
                    xmlElementsList.add("</" + element.getName() + ">");
                }
            } else {
                if (!prefix.isEmpty()) {
                    xmlElementsList.add(elementString.append("/>").toString());
                } else {
                    xmlElementsList.add(elementString.append("/>").toString());
                }
            }
        }
    }
    
    /**
     * Finds duplicates of xlink:href attributes within XML elements.
     * @param xmlElementsList List of XML elements.
     * @param xmlFile The XML file currently being processed.
     * @param filesWithDuplicates Number of files with duplicates.
     * @param totalDuplicates Total count of duplicates.
     */
    
    private static void findDuplicates(List<String> xmlElementsList, File xmlFile, int filesWithDuplicates, int totalDuplicates) {
        boolean duplicatesFound = false;
        Set<String> hrefs = new HashSet<>();
        List<String> hrefDuplicatesList = new ArrayList<>();

        for (String element : xmlElementsList) {
            int hrefIndex = element.indexOf("xlink:href=\"");
            if (hrefIndex != -1) {
                int start = hrefIndex + "xlink:href=\"".length();
                int end = element.indexOf("\"", start);
                if (end != -1) {
                    String href = element.substring(start, end);
                    if (!hrefs.add(href)) {
                        duplicatesFound = true;
                        if (!hrefDuplicatesList.contains(element)) {
                            hrefDuplicatesList.add(href);
                        }
                    }
                }
            }
        }

        if (duplicatesFound) {
        	filesWithDuplicates++;
            totalDuplicates += hrefDuplicatesList.size();
            System.out.println(xmlFile.getAbsolutePath());
            for (String element : hrefDuplicatesList) {
                System.out.println("xlink:href=\"" + element + "\"");
            }
            hrefDuplicatesList.clear();
        }
    }

}