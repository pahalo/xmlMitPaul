import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.*;

import java.io.*;

public class XmlParser {

    private static final String EMPLOYEE_XML_FILE = "employee.xml";

    public static void main(String[] args) {
        try {
            Document document = parseXmlFile();
            NodeList employeeList = document.getElementsByTagName("employee");

            for (int i = 0; i < employeeList.getLength(); i++) {
                Node employee = employeeList.item(i);
                if (employee.getNodeType() == Node.ELEMENT_NODE) {
                    updateEmployeePosition(document, (Element) employee);
                    printEmployeeDetails((Element) employee);
                }
            }
            
            saveUpdatedXml(document);
        } catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private static Document parseXmlFile() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(XmlParser.class.getClassLoader().getResourceAsStream(EMPLOYEE_XML_FILE));
    }

    private static void updateEmployeePosition(Document document, Element employeeElement) {
        Element positionElement = document.createElement("position");
        positionElement.appendChild(document.createTextNode("Software Developer"));
        employeeElement.appendChild(positionElement);
    }

    private static void printEmployeeDetails(Element employeeElement) {
        System.out.println("Employee ID: " + employeeElement.getAttribute("id"));
        NodeList employeeDetails = employeeElement.getChildNodes();
        for (int j = 0; j < employeeDetails.getLength(); j++) {
            Node detail = employeeDetails.item(j);
            if (detail.getNodeType() == Node.ELEMENT_NODE) {
                Element detailElement = (Element) detail;
                System.out.println("        " + detailElement.getTagName() + ": " + detailElement.getTextContent());
            }
        }
    }

    private static void saveUpdatedXml(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(EMPLOYEE_XML_FILE));
        transformer.transform(source, result);
        System.out.println("Anreicherung der XML Datei erfolgreich");
    }
}
