import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;

public class XmlParser {

    public static void main(String[] args) throws TransformerConfigurationException {
        try {
            // XML-Dokument erstellen und parsen
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse("employee.xml");
            document.getDocumentElement().normalize();

            // Iteration durch die Employee-Elemente
            NodeList employeeList = document.getElementsByTagName("employee");
            for (int i = 0; i < employeeList.getLength(); i++) {
                Node employee = employeeList.item(i);
                if (employee.getNodeType() == Node.ELEMENT_NODE) {
                    Element employeeElement = (Element) employee;
                    System.out.println("Employee ID: " + employeeElement.getAttribute("id"));

                    // Hinzufügen der Position "Software Developer" zu jedem Employee
                    Element positionElement = document.createElement("position");
                    positionElement.appendChild(document.createTextNode("Software Developer"));
                    employeeElement.appendChild(positionElement);

                    // Ausgabe der Details jedes Employees
                    NodeList employeeDetails = employee.getChildNodes();
                    for (int j = 0; j < employeeDetails.getLength(); j++) {
                        Node detail = employeeDetails.item(j);
                        if (detail.getNodeType() == Node.ELEMENT_NODE) {
                            Element detailElement = (Element) detail;
                            System.out.println(
                                    "        " + detailElement.getTagName() + ": " + detailElement.getTextContent());
                        }
                    }
                }
            }

            // Transformer, um das aktualisierte Dokument zu speichern
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File("employee.xml"));
            try {
                transformer.transform(source, result);
                System.out.println("Anreicherung der XML Datei erfolgreich");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }
}
