import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.Namespace;

public class XMLProcessor {

    private static final List<String> stringsToIgnore = Arrays.asList("<mdWrapMDTYPE=\"MODS\">", "<xmlData>", "<mods>", "<extension>", "<goobi>");
    private static List<String> duplicatesList = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bitte nur ein Verzeichnis angeben.");
        } else {
            File directory = new File(args[0]).getAbsoluteFile();
            if (directory.exists() && directory.isDirectory()) {
                processFiles(directory);
            } else {
                System.out.println("Das Verzeichnis existiert nicht.");
            }
        }
    }

    private static void processFiles(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
                        processXmlFile(file);
                    } else if (file.isDirectory()) {
                        processFiles(file);
                    }
                }
            }
        }
    }

    private static void processXmlFile(File file) {
        List<String> xmlElementsList = new ArrayList<>();

        try {
            SAXBuilder sax = new SAXBuilder();
            Document doc = sax.build(file);
            Element rootElement = doc.getRootElement();
            collectXmlElements(rootElement, file, xmlElementsList);
            System.out.println("XML-Elemente der Datei " + file.getName() + ":");
            findDuplicates(xmlElementsList, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void collectXmlElements(Element element, File xmlFile, List<String> xmlElementsList) {
        if (element != null) {
            StringBuilder elementString = new StringBuilder();
            
            Namespace ns = element.getNamespace(); // Namespace des aktuellen Elements
            String prefix = ns.getPrefix(); // Präfix des Namespaces

            // Prüfe, ob ein Namespace-Präfix vorhanden ist und füge es dem Element hinzu
            if (!prefix.isEmpty()) {
                elementString.append("<").append(prefix).append(":").append(element.getName());
            } else {
                elementString.append("<").append(element.getName());
            }

            // Attribute hinzufügen (falls vorhanden)
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

                // Schließe das Element mit dem entsprechenden Namespace-Präfix
                if (!prefix.isEmpty()) {
                    xmlElementsList.add("</" + prefix + ":" + element.getName() + ">");
                } else {
                    xmlElementsList.add("</" + element.getName() + ">");
                }
            } else {
                // Wenn das Element keine Kinder hat, schließe es als Selbstschließendes Element
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
        for (int i = 0; i < xmlElementsList.size(); i++) {
            String element1 = xmlElementsList.get(i);
            if (element1.startsWith("</") || element1.startsWith("<metadataname") || stringsToIgnore.contains(element1)) {
                continue;
            }

            for (int j = i + 1; j < xmlElementsList.size(); j++) {
                String element2 = xmlElementsList.get(j);
                if (element1.equals(element2)) {
                    duplicatesFound = true;
                    if (!duplicatesList.contains(element1)) {
                    	System.out.println(element1);
                        duplicatesList.add(element1);
                    }
                }
            }
        }

        if (duplicatesFound) {
        	System.out.println(duplicatesList);
        	generateBackupFile(xmlFile, duplicatesList);
        }
    }

    private static void generateBackupFile(File xmlFile, List<String> duplicatesList) {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

        String backupFileName = currentTime.format(formatter) + "_meta.xml"; // Name der Backup-Datei

        File backupFile = new File(xmlFile.getParentFile(), backupFileName); // Backup-Datei im selben Verzeichnis erstellen

        try (BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
               BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile))) {
               String line;
               List<String> linesToWrite = new ArrayList<>();
               boolean postDuplicate = false;
               

               while ((line = reader.readLine()) != null) {
                   boolean isDuplicate = false;
                   
                   for (String duplicate : duplicatesList) {
                       if (normalizeString(line).equals(normalizeString(duplicate))) {
                           isDuplicate = true;
                           postDuplicate = true;
                           if (!linesToWrite.isEmpty()) {
                               linesToWrite.remove(linesToWrite.size() - 1);
                           }
                           duplicatesList.remove(duplicate);
                           break;
                       }
                   }
                   
                   
                   if (!isDuplicate && !postDuplicate) {
                       linesToWrite.add(line);
                       
                   } else if (!isDuplicate && postDuplicate){
                	   postDuplicate = false;
                	   
                   }
                   
               }
               

               for (String lineToWrite : linesToWrite) {
                   writer.write(lineToWrite + "\n");
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
    }

    private static String normalizeString(String input) {
        return input.trim().replaceAll("\\s+", " "); // Entferne führende und nachfolgende Leerzeichen, ersetze Mehrfach-Leerzeichen durch eins
    }

}
