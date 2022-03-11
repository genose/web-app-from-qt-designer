package parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.swing.JOptionPane;

import document.MyHTMLDocumentImpl;
import model.NodeConverter;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import app.WebApplicationView;

public class QtUiFileParser {

	private static Logger LOGGER = Logger.getLogger(WebApplicationView.class.getName());
	public QtUiFileParser(){

	}

	public void parse(File fXmlFile){
		parse(fXmlFile, false, true);

	}

	public void parse(File fXmlFile, Boolean interactiveMode, Boolean useBootstrapCSS){
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			LOGGER.info("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("widget");
			Node mainNode = doc.getFirstChild();
			NodeConverter nodeConverter = null;
			
			Document htmlDoc = MyHTMLDocumentImpl.makeBasicHtmlDoc(fXmlFile.getName().replaceAll("\\.ui", ".html"), useBootstrapCSS);
			
			if(mainNode.getNodeName().equals("ui")){
				LOGGER.info("BEGIN - Conversion Step node ui : \n");
				NodeList uiChildren =  mainNode.getChildNodes();
				for(int i = 0 ; i < uiChildren.getLength(); i++){
					try{
						
						Node tempNode = uiChildren.item(i);
						if(tempNode.getNodeName().equals("widget")){
							LOGGER.info("BEGIN - Conversion Step node ui widget : \n");
							nodeConverter = new NodeConverter(htmlDoc, tempNode);
							LOGGER.info("END - Conversion Step node ui widget : \n");
							break;
						}
						else{
							System.out.println("Node name is: " + tempNode.getNodeName());
							LOGGER.info("WARNING Conversion Step ui node: '"+ tempNode.getNodeName()+"' :: '"+tempNode.getNodeValue()+"' :: '"+tempNode.getNodeType()+"' :: '"+tempNode.getTextContent()+"' :: '"+tempNode.getAttributes()+"' ");
						}
						
					} catch (Exception e){
						LOGGER.info("Conversion ERROR widget : \n"+ e.getMessage()+" \n\n -------- \n\n ");
					}
										
				}

				LOGGER.info("END - Conversion Step node ui : \n");
			}
			else{
				System.out.println("Main node is not ui");
				LOGGER.info("Conversion Step : \n"+"Main node is not ui");
			}
			
			if(nodeConverter != null){
				LOGGER.info("BEGIN - Conversion Step visit nodes: \n");
				nodeConverter.visitNodes();
				LOGGER.info("END - Conversion Step visit nodes: \n");
			}
			
			LOGGER.info("BEGIN ----------------------------");
			try{

			
				for (int temp = 0; temp < nList.getLength(); temp++) {

					Node nNode = nList.item(temp);

					LOGGER.info("\n- Current Element :" + nNode.getNodeName());

					if (nNode.getNodeType() == Node.ELEMENT_NODE) {

						Element eElement = (Element) nNode;


						if(eElement.getAttribute("class").length() >0){
							LOGGER.info("Widget Class : " + eElement.getAttribute("class"));
							addQObject(htmlDoc, eElement);

						}else{
							LOGGER.info("WARNING : EMPTY Widget Class : " +"'"+ eElement.getAttribute("class")+"'"+" element : "+eElement);
						}
						
						//					System.out.println("First Name : " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
						//					System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
						//					System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
						//					System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());

					}
				}
			}catch(Exception e){
				e.printStackTrace();
				LOGGER.info("Conversion NODE ERROR : \n"+ e.getMessage()+" \n\n -------- \n\n "+e.getStackTrace());
				JOptionPane.showMessageDialog( null, new JLabel("Conversion ERROR : \n"+ e.getMessage()+" \n\n -------- \n\n "+e.getStackTrace()));
			
			}
			LOGGER.info("END ----------------------------");
			LOGGER.info("BEGIN - Conversion prepare to write file \n");
			//to serialize
	        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
	        DOMImplementationLS domImplLS = (DOMImplementationLS) registry.getDOMImplementation("LS");

	        LSSerializer lsSerializer = domImplLS.createLSSerializer();
	        DOMConfiguration domConfig = lsSerializer.getDomConfig();
	        domConfig.setParameter("format-pretty-print", true);  //if you want it pretty and indented

	        LSOutput lsOutput = domImplLS.createLSOutput();
	        lsOutput.setEncoding("UTF-8");
			LOGGER.info("Conversion prepare to write file - 0 \n");
	        //to write to file
			int result = -1;
			String selectedFileName = fXmlFile.getName().replaceAll("\\.ui", ".html");
			File directoryName = null;
			File selectedSavePath = null;
			LOGGER.info("Conversion write file -- 1 : \n");
			if(interactiveMode){
	        	JFileChooser chooser = new JFileChooser();
	        	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);  
	        	result = chooser.showSaveDialog(null);
				directoryName = chooser.getSelectedFile().getAbsoluteFile();
				selectedSavePath = new File(directoryName.getAbsolutePath());
			} else {
				directoryName = new File(".");
				
				selectedSavePath = new File(directoryName.getAbsolutePath());
				
			}

	        
			
			
			LOGGER.info("Conversion write file -- 2 : \n"+ directoryName.getCanonicalPath());
	        
			if(!selectedSavePath.exists() && !selectedSavePath.isDirectory()){
				// JOptionPane.showMessageDialog( null, new JLabel("ERROR filename : "+directoryName+"/" +selectedFileName));
				String directoryNameStr =  directoryName.getCanonicalPath();
				directoryNameStr = directoryNameStr.substring(0, directoryNameStr.lastIndexOf("/"));
				if(interactiveMode)
					JOptionPane.showMessageDialog( null, new JLabel("Warning new filename : "+directoryNameStr+"/" +selectedFileName ));
				else 
					LOGGER.info("Warning new filename : "+directoryNameStr+"/" +selectedFileName );

				directoryName = new File(directoryNameStr);

			}

			if(directoryName.exists() && directoryName.isDirectory()){
			
				File outFile = new File(directoryName, selectedFileName);
				LOGGER.info("BEGIN - Conversion write file name: \n"+ selectedFileName+"\n "+directoryName);
				 try (OutputStream os = new FileOutputStream(outFile)) {
					lsOutput.setByteStream(os);
					lsSerializer.write(htmlDoc, lsOutput);
				}catch (Exception e) {
					LOGGER.info("Conversion ERROR write file : \n"+ e.getMessage()+" \n\n -------- \n\n "+e.getStackTrace());
				}
				changeBreakLines(outFile);
				LOGGER.info("END - Conversion write file name: \n"+ selectedFileName+"\n "+directoryName);
				
				if(interactiveMode)
					JOptionPane.showMessageDialog( null, new JLabel("Success Conversion stred at : \n"+directoryName+"/"+selectedFileName));
				else
					LOGGER.info("Success Conversion stred at : \n"+directoryName+"/"+selectedFileName);
				
			}else{
				LOGGER.info(" WARNING :: Write File error for : \n"+directoryName+"/"+selectedFileName);
			}
	      
		}
		catch(Exception e){
			e.printStackTrace();
			LOGGER.info("Conversion ERROR : \n"+ e.getMessage()+" \n\n -------- \n\n "+e.getStackTrace());
			if(interactiveMode)
				JOptionPane.showMessageDialog( null, new JLabel("Conversion ERROR : \n"+ e.getMessage()+" \n\n -------- \n\n "+e.getStackTrace()));
			
				
			
		}
	}
	
	public void changeBreakLines(File outFile){
		Scanner scanner;
		try {
			scanner = new Scanner(outFile, "UTF-8" );
			String text = scanner.useDelimiter("\\A").next();
			text = text.replaceAll("\\&lt;", "<");
			text = text.replaceAll("\\&gt;", ">");
			scanner.close();
			try( PrintWriter out = new PrintWriter(outFile)){
			    out.println(text);
			    out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			LOGGER.info("ERROR : \n"+ e.getStackTrace());
		}
	}
	
	public void addQObject(Document htmlDoc, Element eElement){
		String qClassName = eElement.getAttribute("class");
		if(qClassName.equals("QPushButton")){
			convertQPushButtonToHtmlElement(htmlDoc, eElement);
		}
		else if(qClassName.equals("QToolButton")){
			convertQToolButtonToHtmlElement(htmlDoc, eElement);
		}
		else if(qClassName.equals("QRadioButton")){
			convertQRadioButtonToHtmlElement(htmlDoc, eElement);
		}
		else if(qClassName.equals("QCheckBox")){
			convertQCheckBoxToHtmlElement(htmlDoc, eElement);
		}
		else if(qClassName.equals("QCommandLinkButton")){
			convertQCommandLinkButtonToHtmlElement(htmlDoc, eElement);
		}
		else if(qClassName.equals("QDialogButton")){
			convertQDialogButtonToHtmlElement(htmlDoc, eElement);
		}
		else if(qClassName.equals("QLineEdit")){
			convertQLineEditToHtmlElement(htmlDoc, eElement);
		}
		else if(qClassName.equals("QPlainTextEdit")){
			LOGGER.info(" >>> Create "+qClassName);
			convertQPlainTextEditToHtmlElement(htmlDoc, eElement);
		}
		else if(qClassName.equals("QTextEdit")){
			LOGGER.info(" >>> Create "+qClassName);
			convertQPlainTextEditToHtmlElement(htmlDoc, eElement);
		}
		else if(qClassName.equals("QLabel")){
			convertQLabelToHtmlElement(htmlDoc, eElement);
		}
		else if(qClassName.equals("QProgressBar")){
			convertQProgressBarToHtmlElement(htmlDoc, eElement);
		}
		else if(qClassName.equals("QSpinBox")){
			convertQSpinBoxToHtmlElement(htmlDoc, eElement);
		}
		else{
			LOGGER.info("Conversion Warning : \n unknow '"+qClassName+"'");
		}

	}


	public void convertQPushButtonToHtmlElement(Document htmlDoc, Element eElement){
		Element bodyElement = (Element) htmlDoc.getElementsByTagName("body").item(0);
		String id = eElement.getAttribute("name");
		String bName = getButtonName(eElement);
		String style = getStyle(eElement);
		Element pElement = htmlDoc.createElement("button");
		pElement.setAttribute("id", id);
		pElement.setAttribute("type", "button");
		pElement.setAttribute("class", "btn btn-default btn-sm");
		if(bName != null){
			pElement.setTextContent(bName);
		}
		else{
			pElement.setTextContent("Default");
		}
		if(style != null){
			pElement.setAttribute("style", style);
		}
		bodyElement.appendChild(pElement);
	}

	public void convertQCommandLinkButtonToHtmlElement(Document htmlDoc, Element eElement){
		Element bodyElement = (Element) htmlDoc.getElementsByTagName("body").item(0);
		String id = eElement.getAttribute("name");
		String bName = getButtonName(eElement);
		String style = getStyle(eElement);
		Element pElement = htmlDoc.createElement("button");
		
		pElement.setAttribute("id", id);
		pElement.setAttribute("type", "button");
		pElement.setAttribute("class", "btn btn-default btn-sm");
		if(bName != null){
			pElement.setTextContent(bName);
		}
		else{
			pElement.setTextContent("Default");
		}
		if(style != null){
			pElement.setAttribute("style", style);
		}
		bodyElement.appendChild(pElement);
	}

	public void convertQToolButtonToHtmlElement(Document htmlDoc, Element eElement){
		Element bodyElement = (Element) htmlDoc.getElementsByTagName("body").item(0);
		String id = eElement.getAttribute("name");
		String bName = getButtonName(eElement);
		String style = getStyle(eElement);
		Element pElement = htmlDoc.createElement("button");
		
		pElement.setAttribute("id", id);
		pElement.setAttribute("type", "button");
		pElement.setAttribute("class", "btn btn-default btn-sm");
		if(bName != null){
			pElement.setTextContent(bName);
		}
		else{
			pElement.setTextContent("Default");
		}
		if(style != null){
			pElement.setAttribute("style", style);
		}
		bodyElement.appendChild(pElement);

	}

	public void convertQRadioButtonToHtmlElement(Document htmlDoc, Element eElement){
		Element bodyElement = (Element) htmlDoc.getElementsByTagName("body").item(0);
		String id = eElement.getAttribute("name");
		String bName = getButtonName(eElement);
		String style = getStyle(eElement);
		Element pElement = htmlDoc.createElement("input");
		
		pElement.setAttribute("id", id);
		pElement.setAttribute("type", "radio");
		pElement.setAttribute("aria-label", "Default");
		if(bName != null){
			pElement.setTextContent(bName);
		}
		else{
			pElement.setTextContent("Default");
		}
		if(style != null){
			pElement.setAttribute("style", style);
		}
		bodyElement.appendChild(pElement);
	}

	public void convertQCheckBoxToHtmlElement(Document htmlDoc, Element eElement){
		Element bodyElement = (Element) htmlDoc.getElementsByTagName("body").item(0);
		String id = eElement.getAttribute("name");
		String bName = getButtonName(eElement);
		String style = getStyle(eElement);
		Element pElement = htmlDoc.createElement("input");
		
		pElement.setAttribute("id", id);
		pElement.setAttribute("type", "checkbox");
		pElement.setAttribute("aria-label", "DEFAULT");
		if(bName != null){
			pElement.setTextContent(bName);
		}
		else{
			pElement.setTextContent("Default");
		}
		if(style != null){
			pElement.setAttribute("style", style);
		}
		bodyElement.appendChild(pElement);
	}

	public void convertQDialogButtonToHtmlElement(Document htmlDoc, Element eElement){
		Element bodyElement = (Element) htmlDoc.getElementsByTagName("body").item(0);
		String id = eElement.getAttribute("name");
		String style = getStyle(eElement);
		Element pElement = htmlDoc.createElement("div");
		
		pElement.setAttribute("id", id);
		pElement.setAttribute("role", "group");
		pElement.setAttribute("class", "btn-group");
		pElement.setAttribute("aria-label", "DEFAULT");
		if(style != null){
			pElement.setAttribute("style", style);
		}

		Element firstElement = htmlDoc.createElement("button");
		firstElement.setAttribute("id", "default");
		firstElement.setAttribute("type", "button");
		firstElement.setAttribute("class", "btn btn-default  btn-sm");
		firstElement.setTextContent("Ok");
		pElement.appendChild(firstElement);

		Element secondElement = htmlDoc.createElement("button");
		secondElement.setAttribute("id", "default");
		secondElement.setAttribute("type", "button");
		secondElement.setAttribute("class", "btn btn-default btn-sm");
		secondElement.setTextContent("Cancel");
		pElement.appendChild(secondElement);

		bodyElement.appendChild(pElement);

	}

	public void convertQLineEditToHtmlElement(Document htmlDoc, Element eElement){
		Element bodyElement = (Element) htmlDoc.getElementsByTagName("body").item(0);
		String id = eElement.getAttribute("name");
		String style = getStyle(eElement);
		Element pElement = htmlDoc.createElement("input");
		
		pElement.setAttribute("id", id);
		pElement.setAttribute("type", "text");
		pElement.setAttribute("class", "form-control");
		//pElement.setAttribute("placeholder", "placeholder");
		pElement.setAttribute("aria-describedby", "basic-addon1");
		if(style != null){
			pElement.setAttribute("style", style);
		}
		bodyElement.appendChild(pElement);

	}

	public void convertQPlainTextEditToHtmlElement(Document htmlDoc, Element eElement){
		Element bodyElement = (Element) htmlDoc.getElementsByTagName("body").item(0);
		String id = eElement.getAttribute("name");
		String style = getStyle(eElement);
		Element pElement = htmlDoc.createElement("input");
		
		pElement.setAttribute("id", id);
		pElement.setAttribute("type", "text");
		pElement.setAttribute("class", "form-control");
		pElement.setAttribute("placeholder", "placeholder");
		pElement.setAttribute("aria-describedby", "basic-addon1");
		if(style != null){
			pElement.setAttribute("style", style);
		}
		bodyElement.appendChild(pElement);

	}

	public void convertQLabelToHtmlElement(Document htmlDoc, Element eElement){
		Element bodyElement = (Element) htmlDoc.getElementsByTagName("body").item(0);
		String id = eElement.getAttribute("name");
		String style = getStyle(eElement);
		String sName = getButtonName(eElement);

		Element pElement = htmlDoc.createElement("span");
		pElement.setAttribute("id", id);
		pElement.setAttribute("class", "label label-default");
		if(sName != null){
			pElement.setTextContent(sName);
		}
		else{
			pElement.setTextContent("Default");
		}
		if(style != null){
			pElement.setAttribute("style", style);
		}
		bodyElement.appendChild(pElement);

	}

	public void convertQProgressBarToHtmlElement(Document htmlDoc, Element eElement){
		Element bodyElement = (Element) htmlDoc.getElementsByTagName("body").item(0);
		String id = eElement.getAttribute("name");
		String style = getStyle(eElement);
		Element pElement = htmlDoc.createElement("div");
		pElement.setAttribute("id", id);
		pElement.setAttribute("class", "progress");
		if(style != null){
			pElement.setAttribute("style", style);
		}

		Element firstElement = htmlDoc.createElement("div");
		firstElement.setAttribute("class", "progress-bar");
		firstElement.setAttribute("role", "progressbar");
		firstElement.setAttribute("aria-valuenow", "60");
		firstElement.setAttribute("aria-valuemin", "100");
		firstElement.setAttribute("aria-valuemax", "progress-bar");
		firstElement.setAttribute("style", "width:60px");
		pElement.appendChild(firstElement);

		bodyElement.appendChild(pElement);

	}
	
	public void convertQSpinBoxToHtmlElement(Document htmlDoc, Element eElement){
		Element bodyElement = (Element) htmlDoc.getElementsByTagName("body").item(0);
		String id = eElement.getAttribute("name");
		String style = getStyle(eElement);
		Element pElement = htmlDoc.createElement("input");
		//Max ve min de�erleri �ekilerek buraya eklenecek
		pElement.setAttribute("id", id);
		pElement.setAttribute("type", "number");
		pElement.setAttribute("class", "form-control");
		if(style != null){
			pElement.setAttribute("style", style);
		}
		bodyElement.appendChild(pElement);
	}
	
	public String getButtonName(Element eElement){
		NodeList nList = eElement.getElementsByTagName("property");
		for(int i = 0; i < nList.getLength(); i++){
			Element el = (Element) nList.item(i);
			String prop = el.getAttribute("name");
			if(prop.equals("text")){
				return el.getElementsByTagName("string").item(0).getTextContent();
			}			
		}
		return null;
	}
	
	public String getStyle(Element eElement){
		StringBuilder sb = new StringBuilder();
		NodeList nList = eElement.getElementsByTagName("property");
		sb.append("position: absolute; ");
		try{
			for(int i = 0; i < nList.getLength(); i++){
				Element el = (Element) nList.item(i);
				String prop = el.getAttribute("name");
				if(prop.equals("geometry")){
					sb.append("left: ");
					sb.append(el.getElementsByTagName("x").item(0).getTextContent());
					sb.append("px; top: ");
					sb.append(el.getElementsByTagName("y").item(0).getTextContent());
					sb.append("px; width: ");
					sb.append(el.getElementsByTagName("width").item(0).getTextContent());
					sb.append("px; height: ");
					sb.append(el.getElementsByTagName("height").item(0).getTextContent());
					sb.append("px;");
					return sb.toString();
				}			
			}
		}
		catch(Exception e){
			return null;
		}
		return null;
	}
}
