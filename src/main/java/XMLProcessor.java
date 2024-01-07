import java.io.*;
import java.util.*;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.Namespace;

public class XMLProcessor {
    // Liste von Duplikaten der 'xlink:href'-Attribute
    private static List<String> hrefDuplicatesList = new ArrayList<>();
    // Zähler für Dateien mit Duplikaten und Gesamtanzahl der Duplikate
    private static int filesWithDuplicates = 0;
    private static int totalDuplicates = 0;

    public static void main(String[] args) {
        // Überprüfung, ob ein Verzeichnis angegeben wurde
        if (args.length != 1) {
            System.out.println("Bitte nur ein Verzeichnis angeben.");
        } else {
            File directory = new File(args[0]).getAbsoluteFile();
            // Überprüfung, ob das Verzeichnis existiert und ein Verzeichnis ist
            if (directory.exists() && directory.isDirectory()) {
                processFiles(directory);
                // Ausgabe der Anzahl der Dateien mit Duplikaten und Gesamtanzahl der Duplikate
                System.out.println("Anzahl der Dateien mit Duplikaten: " + filesWithDuplicates);
                System.out.println("Gesamtanzahl der Duplikate: " + totalDuplicates);
            } else {
                System.out.println("Das Verzeichnis existiert nicht.");
            }
        }
    }

    // Verarbeitung von Dateien im Verzeichnis und seinen Unterverzeichnissen
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

    // Verarbeitung einer XML-Datei
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

    // Sammeln von XML-Elementen aus einer XML-Datei
    private static void collectXmlElements(Element element, File xmlFile, List<String> xmlElementsList) {
        if (element != null) {
            StringBuilder elementString = new StringBuilder();

            Namespace ns = element.getNamespace(); 
            String prefix = ns.getPrefix(); 

            // Erstellen der XML-Elementzeichenfolge mit Präfix und Attributen
            if (!prefix.isEmpty()) {
                elementString.append("<").append(prefix).append(":").append(element.getName());
            } else {
                elementString.append("<").append(element.getName());
            }

            // Hinzufügen der Attribute zum XML-Element
            List<Attribute> attributes = element.getAttributes();
            for (Attribute attribute : attributes) {
                elementString.append(" ").append(attribute.getQualifiedName()).append("=\"").append(attribute.getValue()).append("\"");
            }

            List<Element> children = element.getChildren();

            // Rekursive Verarbeitung von Kind-Elementen
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

    private static void findDuplicates(List<String> xmlElementsList, File xmlFile) {
        boolean duplicatesFound = false;
        Set<String> hrefs = new HashSet<>();

        for (String element : xmlElementsList) {
            int hrefIndex = element.indexOf("xlink:href=\"");
            if (hrefIndex != -1) {
                int start = hrefIndex + "xlink:href=\"".length();
                int end = element.indexOf("\"", start);
                if (end != -1) {
                    String href = element.substring(start, end);
                    if (!hrefs.add(href)) {
                        duplicatesFound = true;
                        // Überprüfen und Hinzufügen von Duplikaten der 'xlink:href'-Attribute
                        if (!hrefDuplicatesList.contains(element)) {
                            hrefDuplicatesList.add(element);
                        }
                    }
                }
            }
        }

        // Hier werden die extrahierten ID-Werte für die Duplikate ausgegeben
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