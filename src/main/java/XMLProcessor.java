import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class XMLProcessor {

	private static List<String> duplicatesList = new ArrayList<>();
	private static boolean duplicatesFound = false;
	
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bitte nur eine Datei angeben.");
        } else {
            File directory = new File(args[0]).getAbsoluteFile();
            if (directory.exists() && directory.isDirectory()) {
                Map<Integer, Set<String>> elementsAtDepth = new HashMap<>();
                searchTree(directory, elementsAtDepth, directory);
            } else {
                System.out.println("Das Verzeichnis existiert nicht.");
            }
        }
    }

    private static void searchTree(File file, Map<Integer, Set<String>> elementsAtDepth, File directory) {
        if (file.isDirectory()) {
            File[] sBaum = file.listFiles();
            if (sBaum != null) {
                for (File f : sBaum) {
                    if (f.getName().equals("meta.xml")) {
                        openXmlWithJdom(f, elementsAtDepth, directory);
                    }
                    searchTree(f, elementsAtDepth, directory);
                }
            }
        }
    }

    private static void openXmlWithJdom(File f, Map<Integer, Set<String>> elementsAtDepth, File directory) {
        try {
            SAXBuilder sax = new SAXBuilder();
            Document doc = sax.build(f);
            Element rootElement = doc.getRootElement();
            checkDuplicatesAtSameLevel(rootElement, directory, f);
            if(duplicatesFound) {
            	generateBackupFile(f, directory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkDuplicatesAtSameLevel(Element element, File directory, File xmlFile) {
        if (element != null) {
            Map<String, Set<String>> elementsAtSameLevel = new HashMap<>();

            for (Element child : element.getChildren()) {
                String elementName = child.getName();
                Set<String> elements = elementsAtSameLevel.computeIfAbsent(elementName, k -> new HashSet<>());

                StringBuilder elementTextBuilder = new StringBuilder();
                List<Attribute> attributes = child.getAttributes();
                
                elementTextBuilder.append("<").append(elementName);

                for (Attribute attribute : attributes) {
                    elementTextBuilder.append(" ").append(attribute.getName()).append("=\"").append(attribute.getValue()).append("\"");
                }

                elementTextBuilder.append(">").append(child.getTextNormalize()).append("</").append(elementName).append(">");

                String elementText = elementTextBuilder.toString();
                if (elements.contains(elementText)) {
                	duplicatesFound = true;
                    duplicatesList.addAll(elements);
                    	    
                } else {
                    elements.add(elementText);
                	
                }
            }

            for (Element child : element.getChildren()) {
                checkDuplicatesAtSameLevel(child, directory, xmlFile);
            }
        }
    }

    private static void generateBackupFile(File xmlFile,File directory) {
    	LocalDateTime currentTime = LocalDateTime.now();
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    	
        String backupFileName = currentTime.format(formatter) + "_meta.xml"; // Name der Backup-Datei

        File backupFile = new File(directory, backupFileName);
    	
        try (BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
        	    BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile))) {
        	    String line;

        	    while ((line = reader.readLine()) != null) {
        	    	boolean isDuplicate = false;
        	    	for (String duplicate : duplicatesList) {
                        if (normalizeString(line).equals(normalizeString(duplicate))) {
                        	isDuplicate = true;
                        	duplicatesList.remove(duplicate);
                        	break;
                        }
                    }
        	    	if(!isDuplicate) {
                    	writer.write(line + "\n");
                    }
        	    }
        	} catch (IOException e) {
        	    e.printStackTrace();
        	}

    }
    private static String normalizeString(String input) {
        return input.trim().replaceAll("\\s+", " "); // Entferne führende und nachfolgende Leerzeichen, ersetze Mehrfach-Leerzeichen durch eins
    }
}
