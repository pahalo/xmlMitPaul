import java.io.*;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;


public class XmlParser {

    public static void main(String[] args) throws TransformerConfigurationException {
        try {
            // XML-Dokument erstellen und parsen
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse("src/main/resources/employee.xml");
            document.getDocumentElement().normalize();

            // Erstellen einer temporären Datei zum Zwischenspeichern
            File tempFile = new File("temp_employee.xml");
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Speichern des originalen Dokuments in der temporären Datei
            DOMSource source = new DOMSource(document);
            StreamResult tempResult = new StreamResult(tempFile);
            transformer.transform(source, tempResult);

            // Durchführung von Änderungen im Dokument
            NodeList employeeList = document.getElementsByTagName("employee");
            for (int i = 0; i < employeeList.getLength(); i++) {
                Node employee = employeeList.item(i);
                if (employee.getNodeType() == Node.ELEMENT_NODE) {
                    Element employeeElement = (Element) employee;

                    // Hinzufügen der Position "Software Developer" zu jedem Employee
                    Element positionElement = document.createElement("position");
                    positionElement.appendChild(document.createTextNode("Software Developer"));
                    employeeElement.appendChild(positionElement);
                }
            }

            // Aktualisiertes Dokument in die Originaldatei schreiben
            DOMSource updatedSource = new DOMSource(document);
            StreamResult result = new StreamResult(new File("src/main/resources/employee.xml"));
            transformer.transform(updatedSource, result);
            System.out.println("Anreicherung der XML Datei erfolgreich");

            // Löschen der temporären Datei
            tempFile.delete();

        } catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
            e.printStackTrace();
        }
    }
}
