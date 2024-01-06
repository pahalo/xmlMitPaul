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
    private static List<String> hrefDuplicatesList = new ArrayList<>();

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
                        processXmlFile(file, directory);
                    } else if (file.isDirectory()) {
                        processFiles(file);
                    }
                }
            }
        }
    }

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

    private static void collectXmlElements(Element element, File xmlFile, List<String> xmlElementsList) {
        if (element != null) {
            StringBuilder elementString = new StringBuilder();
            
            Namespace ns = element.getNamespace(); 
            String prefix = ns.getPrefix(); 

            // Präfix
            if (!prefix.isEmpty()) {
                elementString.append("<").append(prefix).append(":").append(element.getName());
            } else {
                elementString.append("<").append(element.getName());
            }

            // Attribute 
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


    private static void findDuplicates(List<String> xmlElementsList, File xmlFile) {
        boolean duplicatesFound = false;
        Set<String> orderLabels = new HashSet<>();
        Set<String> hrefs = new HashSet<>();
        Set<String> duplicateIDsList = new HashSet<>();

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
                            //hrefDuplicatesList.add(element);
                        	hrefDuplicatesList.add(href);
                        }
                    }
                }
            }

            if (element.startsWith("</") || element.startsWith("<metadataname") || stringsToIgnore.contains(element)) {
                continue;
            }
            /* 
            int orderLabelIndex = element.indexOf("ORDERLABEL=\"");
            if (orderLabelIndex != -1) {
                int start = orderLabelIndex + "ORDERLABEL=\"".length();
                int end = element.indexOf("\"", start);
                if (end != -1) {
                    String orderLabel = element.substring(start, end);
                    if (!orderLabels.add(orderLabel)) {
                        duplicatesFound = true;
                        if (!duplicatesList.contains(element)) {
                            //duplicatesList.add(element);
                        	duplicatesList.add(orderLabel);
                            String idValue = extractIDValue(element);
                            if (idValue != null) {
                                duplicateIDsList.add(idValue);
                            }
                        }
                    }
                }
            }
        */}

        // Hier kannst du die extrahierten ID-Werte für die Duplikate ausgeben
        if (duplicatesFound) {
        	System.out.println(xmlFile.getAbsolutePath());
        	for(String element : hrefDuplicatesList) {
        		System.out.println("xlink:href=\"" + element +"\"");
        	}
            //generateBackupFile(xmlFile, duplicatesList, hrefDuplicatesList, duplicateIDsList);
        }
    }


    private static void generateBackupFile(File xmlFile, List<String> duplicatesList, List<String> hrefDuplicatesList, Set<String> duplicateIDsList) {
    	LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

        String backupFileName = currentTime.format(formatter) + "_meta.xml"; 

        File backupFile = new File(xmlFile.getParentFile(), backupFileName); 
        
        try (BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
               BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile))) {
               String line;
               List<String> linesToWrite = new ArrayList<>();
               int deleteFollowingLines = 0;
               int removedDuplicates = 0;
               int removedHrefDuplicates = 0;
               int removedPHYSDuplicates = 0;
               boolean secondLine = false;
               
               while ((line = reader.readLine()) != null) {
                   boolean isDuplicate = false;
                   
                   for (String duplicate : hrefDuplicatesList) {
                       if (normalizeString(line).equals(normalizeString(duplicate))) {
                           isDuplicate = true;
                           deleteFollowingLines = 1;
                           linesToWrite.remove(linesToWrite.size() - 1);
                           hrefDuplicatesList.remove(duplicate);
                           removedHrefDuplicates -= 1;
                           break;
                       }
                   }
                   
                   for (String duplicate : duplicatesList) {
                       if (normalizeString(line).equals(normalizeString(duplicate))) {
                           isDuplicate = true;
                           deleteFollowingLines = 2;
                           duplicatesList.remove(duplicate);
                           removedDuplicates -= 1;
                           break;
                       }
                   }
                   for (String duplicate : duplicateIDsList) {
                       if(normalizeString(line).contains(duplicate) && line.contains("xlink:from=\"")) {
                    	   isDuplicate = true;
                    	   removedPHYSDuplicates -= 1;
                    	   break;
                       }
                   }
                   
                   if (!isDuplicate && deleteFollowingLines == 0) {
                	   if (removedHrefDuplicates != 0 && line.contains("MIMETYPE")) {
                		   line = rewritingHrefDuplicateLines(line, removedHrefDuplicates);
                	   }
                	   else if(removedDuplicates != 0 && line.contains("ORDERLABEL=\"")) {
                		   line = rewritingDuplicateLines(line, removedDuplicates, secondLine);
                	   } else if(removedDuplicates != 0 && line.contains("FILEID=\"") ) {
                		   secondLine = true;
                		   line = rewritingDuplicateLines(line, removedDuplicates, secondLine);
                	   } else if(line.contains("xlink:from=\"") && removedPHYSDuplicates != 0) {
                		   line = rewritingXlinkDuplicateLines(line, removedPHYSDuplicates);
                	   }
                       linesToWrite.add(line);
                       
                   } else if (!isDuplicate && deleteFollowingLines != 0){
                	   deleteFollowingLines -= 1;               	   
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
        return input.trim().replaceAll("\\s+", " "); 
    }
    
    private static String extractIDValue(String element) {
        int idIndex = element.indexOf("ID=\"");
        if (idIndex != -1) {
            int start = idIndex + "ID=\"".length();
            int end = element.indexOf("\"", start);
            if (end != -1) {
                return element.substring(start, end);
            }
        }
        return null;
    }
    
    private static String rewritingHrefDuplicateLines(String line, int removedDuplicates) {
    	String[] idParts = line.split("ID=\"");
    	String beforeID = idParts[0];
    	String afterID = idParts[1];
    	int idEndIndex = afterID.indexOf('"');
    	String idValue = afterID.substring(0, idEndIndex);

    	try {
    	    String cleanedIDValue = idValue.replaceAll("[^\\d]", "");
    	    String afterIDPart = afterID.substring(idEndIndex);
    	    int originalIDLength = cleanedIDValue.length();
    	    int idNumericValue = Integer.parseInt(cleanedIDValue);
    	    idNumericValue += removedDuplicates;
    	    String formattedNewIDValue = String.format("%0" + originalIDLength + "d", idNumericValue);
    	    line = beforeID + "ID=\"FILE_" + formattedNewIDValue + afterIDPart;
    	
    	} catch (NumberFormatException | StringIndexOutOfBoundsException e) {
    	    e.printStackTrace();
    	}
    	
        return line;
    }
    
    private static String rewritingDuplicateLines(String line, int removedDuplicates, boolean secondLine) {
    	if (secondLine) {
    		String[] fileIDParts = line.split("FILEID=\"");
            if (fileIDParts.length > 1) {
                String beforeFILEID = fileIDParts[0];
                String afterFILEID = fileIDParts[1];
                int fileIDEndIndex = afterFILEID.indexOf('"');
                String fileIDValue = afterFILEID.substring(0, fileIDEndIndex);
                try {
                    String cleanedFILEIDValue = fileIDValue.replaceAll("[^\\d]", "");
                    String afterFILEIDPart = afterFILEID.substring(fileIDEndIndex);
                    int originalFILEIDLength = cleanedFILEIDValue.length();
                    int fileIDNumericValue = Integer.parseInt(cleanedFILEIDValue);
                    fileIDNumericValue += removedDuplicates;
                    String formattedNewIDValue = String.format("%0" + originalFILEIDLength + "d", fileIDNumericValue);
                    line = beforeFILEID + "ID=\"FILE_" + formattedNewIDValue + afterFILEIDPart;
                    secondLine = false;
                    return line;
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } 
           }
        }
    	String[] idParts = line.split("ID=\"");
    	String beforeID = idParts[0];
    	String afterID = idParts[1];
    	int idEndIndex = afterID.indexOf('"');
    	String idValue = afterID.substring(0, idEndIndex);

    	try {
    	    String cleanedIDValue = idValue.replaceAll("[^\\d]", "");
    	    String afterIDPart = afterID.substring(idEndIndex);
    	    int originalIDLength = cleanedIDValue.length();
    	    int idNumericValue = Integer.parseInt(cleanedIDValue);
    	    idNumericValue -= removedDuplicates;
    	    String formattedNewIDValue = String.format("%0" + originalIDLength + "d", idNumericValue);
    	    line = beforeID + "ID=\"PHYS_" + formattedNewIDValue + afterIDPart;
    	
    	} catch (NumberFormatException | StringIndexOutOfBoundsException e) {
    	    e.printStackTrace();
    	}
        
        String[] orderParts = line.split("ORDER=\"");
        String beforeORDER = orderParts[0];
        String afterORDER = orderParts[1];
        int orderEndIndex = afterORDER.indexOf('"');
        String orderValue = afterORDER.substring(0, orderEndIndex);
        
        try {
            int value = Integer.parseInt(orderValue);
            value += removedDuplicates;
            return beforeORDER + "ORDER=\"" + value + afterORDER.substring(orderEndIndex);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            return line;
        }
    }
    private static String rewritingXlinkDuplicateLines(String line, int removedPHYSDuplicates) {
        String[] xlinkParts = line.split("xlink:to=\"");
        String beforeXlink = xlinkParts[0];
        String afterXlink = xlinkParts[1];
        int xlinkEndIndex = afterXlink.indexOf('"');
        String xlinkValue = afterXlink.substring(0, xlinkEndIndex);
        
        try {
            String cleanedXlinkValue = xlinkValue.replaceAll("[^\\d]", "");
            String afterXlinkPart = afterXlink.substring(xlinkEndIndex);
            int originalXlinkLength = cleanedXlinkValue.length();
            int xlinkNumericValue = Integer.parseInt(cleanedXlinkValue);         
            xlinkNumericValue += removedPHYSDuplicates;
            String formattedNewXlinkValue = String.format("%0" + originalXlinkLength + "d", xlinkNumericValue);
            line = beforeXlink + "xlink:to=\"PHYS_" + formattedNewXlinkValue + afterXlinkPart;
            return line;
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            return line;
        }
        
    }

}