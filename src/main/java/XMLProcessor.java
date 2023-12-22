import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.Attribute;

public class XMLProcessor {
    private static Set<String> leafElements = new HashSet<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Only one file at a time");
        } else {
            File directory = new File(args[0]).getAbsoluteFile();
            if (directory.exists() && directory.isDirectory()) {
                Map<Integer, Set<String>> elementsAtDepth = new HashMap<>(); // Initialisierung der Map
                searchTree(directory, elementsAtDepth); // Aufruf der Methode searchTree mit der Map
            } else {
                System.out.println("The directory does not exist.");
            }
        }
    }

    private static void searchTree(File file, Map<Integer, Set<String>> elementsAtDepth) {
        if (file.isDirectory()) {
            File[] sBaum = file.listFiles();
            if (sBaum != null) {
                for (File f : sBaum) {
                    if (f.getName().equals("meta.xml")) {
                        openXmlWithJdom(f, elementsAtDepth); // Übergabe der Map an openXmlWithJdom
                    }
                    searchTree(f, elementsAtDepth); // Übergabe der Map an searchTree
                }
            }
        }
    }

    private static void openXmlWithJdom(File f, Map<Integer, Set<String>> elementsAtDepth) {
        try {
            SAXBuilder sax = new SAXBuilder();
            Document doc = sax.build(f);
            Element rootElement = doc.getRootElement();
            checkDuplicatesAtSameLevel(rootElement); // Aufruf der neuen Methode
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkDuplicatesAtSameLevel(Element element) {
        if (element != null) {
            Map<String, Set<String>> elementsAtSameLevel = new HashMap<>();

            for (Element child : element.getChildren()) {
                String elementName = child.getName();
                Set<String> elements = elementsAtSameLevel.computeIfAbsent(elementName, k -> new HashSet<>());

                StringBuilder elementTextBuilder = new StringBuilder();

                List<Attribute> attributes = child.getAttributes();
                for (Attribute attribute : attributes) {
                    elementTextBuilder.append(attribute.getName()).append("=").append(attribute.getValue()).append("|");
                }

                // Append text content
                elementTextBuilder.append(child.getTextNormalize());

                String elementText = elementTextBuilder.toString();

                if (elements.contains(elementText)) {
                    System.out.println("Duplicate found at the same level for element '" + elementName + "': " + elementText);
                } else {
                    elements.add(elementText);
                }
            }

            for (Element child : element.getChildren()) {
                checkDuplicatesAtSameLevel(child);
            }
        }
    }
}
