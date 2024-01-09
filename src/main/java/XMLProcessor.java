import java.io.*;
import java.util.*;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.Namespace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * This class searches tif for duplicates in the MPI import
 * @param filesWithDuplicates Number of files with duplicates.
 * @param totalDuplicates Total count of duplicates.
 */
public class XMLProcessor {
	private static final Logger logger = LogManager.getLogger(XMLProcessor.class);
	private static int filesWithDuplicates = 0;
	private static int totalDuplicates = 0;
	private static List<String> parentDirectory = new ArrayList<>();
	
    public static void main(String[] args) {
    	Configurator.setRootLevel(org.apache.logging.log4j.Level.DEBUG);
    	StringBuilder stringBuilder = new StringBuilder();
    	
        if (args.length != 1) {
        	// Comment: Checking if a single directory is specified
        	logger.error("Please specify only one directory."); 
        } else {
            File directory = new File(args[0]).getAbsoluteFile();
            if (directory.exists() && directory.isDirectory()) {
                processFiles(directory);
                // Comment: Output number of files with duplicates
                logger.info("Number of files with duplicates: " + filesWithDuplicates); 
                // Comment: Output total count of duplicates
                logger.info("Total count of duplicates: " + totalDuplicates); 
                
                for (int i = 0; i < parentDirectory.size(); i++) {
                    String element = parentDirectory.get(i);
                    stringBuilder.append(element);
                    if (i != parentDirectory.size() - 1) {
                        stringBuilder.append(" ");
                    }
                }
                // Comment: Output directory names as a concatenated string
                logger.info("\"id:" + stringBuilder + "\"");
            } else {
            	// Comment: Error message if the directory doesn't exist
            	logger.error("Das Verzeichnis existiert nicht."); 
            }
        }
    }

    /**
     * Recursively traverses all files and directories in the specified directory and processes XML files.
     * @param directory The directory to be searched.
     */
    private static void processFiles(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
                        processXmlFile(file, directory);
                    } else if (file.isDirectory()) {
                        processFiles(file);
                    }
                }
            }
        }
    }
    
    /**
     * Processes a single XML file and collects all XML elements.
     * @param file The XML file to be processed.
     * @param directory The directory where the XML file resides.
     */
    private static void processXmlFile(File file, File directory) {
        List<String> xmlElementsList = new ArrayList<>();

        try {
            SAXBuilder sax = new SAXBuilder();
            Document doc = sax.build(file);
            Element rootElement = doc.getRootElement();
            collectXmlElements(rootElement, file, xmlElementsList);
            findDuplicates(xmlElementsList, file);
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
    private static void findDuplicates(List<String> xmlElementsList, File xmlFile) {
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
                        if (!hrefDuplicatesList.contains(href)) {
                            hrefDuplicatesList.add(href);
                        }
                    }
                }
            }
        }

        if (duplicatesFound) {
        	filesWithDuplicates++;
            totalDuplicates += hrefDuplicatesList.size();
            logger.info(xmlFile.getAbsolutePath()); 
            File directoryAbove = xmlFile.getParentFile();
            parentDirectory.add(directoryAbove.getName());
            for (String element : hrefDuplicatesList) {
            	logger.debug("   " + element);
            }
            hrefDuplicatesList.clear();
        }
    }

}