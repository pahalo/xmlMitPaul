import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Scanner;
import java.util.Arrays;
import java.util.List;

public class XmlParser {

    public static void main(String[] args) {
    	Scanner scanner = new Scanner (System.in);
    	// This main method is just an example for demonstration purposes
        // You can use it to test the getSAXParsedDocument method
        String fileName = "src/main/resources/employees.xml";
        Document document = getSAXParsedDocument("src/main/resources/employees.xml");
        Element rootNode = document.getRootElement();
        
    	List<String> actions = Arrays.asList("Liste ausgeben", "Liste ergänzen", "Listeninhalt löschen?");
    	System.out.println("Was willst du machen?");
    	for (String action : actions) {
            System.out.println(action);
        }
    	String choosenAction = scanner.nextLine();
    	if(choosenAction.equals("1")) {
    		rootNode.getChildren("employee").forEach(XmlParser::readEmployeeNode );
    	} else if(choosenAction.equals("2")) {
    		addEmployee(rootNode, document);
    	} else if(choosenAction.equals("3")) {
    		deleteEmployee(rootNode,document);
    	}
    	System.out.println("");
        callMainMethod();
		scanner.close();
    }

    public static Document getSAXParsedDocument(final String fileName) {
        SAXBuilder builder = new SAXBuilder();
        Document document = null;
        try {
            document = builder.build(fileName);
            
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return document;
    }
    private static void readEmployeeNode(Element employeeNode) {

    	//Employee Id
    	System.out.println("Id : " + employeeNode.getAttributeValue("id"));

    	//First Name
    	System.out.println("FirstName : " + employeeNode.getChildText("firstName"));

    	//Last Name
    	System.out.println("LastName : " + employeeNode.getChildText("lastName"));

    	//Country
    	System.out.println("country : " + employeeNode.getChild("country").getText());

    	/**Read Department Content*/
    	employeeNode.getChildren("department").forEach( XmlParser::readDepartmentNode );
    }
    private static void readDepartmentNode(Element deptNode) {

    	//Department Id
    	System.out.println("Department Id : " + deptNode.getAttributeValue("id"));

    	//Department Name
    	System.out.println("Department Name : " + deptNode.getChildText("name"));
    }
    private static void addEmployee(Element rootNode, Document document) {
    	Scanner scanner = new Scanner(System.in);
    	Element newEmployee = new Element("employee"); 	
    	List<Element> children = rootNode.getChildren();
    	newEmployee.setAttribute("id", String.valueOf(children.size() +1));
    	
    	System.out.println("First name?");
    	String inputFirstName = scanner.nextLine();
    	Element firstName = new Element("firstName").setText(inputFirstName);
    	newEmployee.addContent(firstName);
    	
    	System.out.println("Last name?");
    	String inputLastName = scanner.nextLine();
    	Element lastName = new Element("lastName").setText(inputLastName);
    	newEmployee.addContent(lastName);
    	
    	System.out.println("Country?");
    	String inputcountry = scanner.nextLine();
    	Element country = new Element("country").setText(inputcountry);
        newEmployee.addContent(country);

        rootNode.addContent(newEmployee);
        document.setContent(rootNode);

        save(document);
    }
    private static void deleteEmployee(Element rootNode,Document document) {
    	System.out.print("Welche ID soll gefeuert werden?");
    	Scanner scanner = new Scanner(System.in);
    	String inputID = scanner.nextLine();
    	List<Element> employees = rootNode.getChildren("employee");
    	boolean employeeFound = false;
    	Element employeeToRemove = null;
    	
    	
    	for(Element employee : employees) {
    		if (employee.getAttributeValue("id").equals(inputID)) {
    			System.out.println("Employee ID: " + employee.getAttributeValue("id"));
                System.out.println("First Name: " + employee.getChildText("firstName"));
                System.out.println("Last Name: " + employee.getChildText("lastName"));
                System.out.println("Country: " + employee.getChildText("country"));
                employeeFound = true;
                employeeToRemove = employee;
                break;
    		}
    	}
    	if (!employeeFound) {
            System.out.println("Employee with ID " + inputID + " not found.");
        } else {
        	System.out.println("Delete this Employee?");
        	String confirmation = scanner.nextLine();
        	if(confirmation.equals("yes")) {
        		rootNode.removeContent(employeeToRemove);
        		save(document);
        	}
        }
    	rootNode.getChildren("employee").forEach(XmlParser::readEmployeeNode );
    }
    
    private static void save(Document document) {
    	try {
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(document, new FileWriter("src/main/resources/employees.xml"));
            System.out.println("Mitarbeiter erfolgreich hinzugefügt und XML-Datei aktualisiert!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void callMainMethod() {
            String[] arguments = {};
            main(arguments);
        }
}
