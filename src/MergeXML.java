import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

public class MergeXML {
	public static void main(String args[]) {
		
		try {
			
			/* Create the document builder, and build/parse both the authors.xml and artowrks.xml documents */
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			InputStream inAuthors = MergeXML.class.getClassLoader().getResourceAsStream("authors.xml");
			InputStream inArtworks = MergeXML.class.getClassLoader().getResourceAsStream("artworks.xml");
			Document docAuthors = builder.parse(inAuthors);
			Document docArtworks = builder.parse(inArtworks);
			
			/* Create two lists of Nodes, one for authors and one for artworks */
			NodeList authors = docAuthors.getElementsByTagName("author");
			NodeList artworks = docArtworks.getElementsByTagName("artwork");
			/* Call function to create the new merged document */
			modifyAuthorsXML(authors, artworks, docAuthors, docArtworks);
			
			/* Sort the artworks elements in the new document through custom function */
			NodeList artworksNew = docAuthors.getElementsByTagName("artworks");
			sortArtworks(artworksNew, docAuthors);
			
			/* Set the xml delcaration for the xml document output */
			ProcessingInstruction xmlDeclaration = docAuthors.createProcessingInstruction("xml", "version=\"1.0\" encoding = \"utf-8\"");
			docAuthors.insertBefore(xmlDeclaration, docAuthors.getDocumentElement());
			
			/* Set the dtd declaration for the xml output */
			DOMImplementation domImpl = docAuthors.getImplementation();
			DocumentType docType = domImpl.createDocumentType("DOCTYPE", "", "output.dtd");
			
			/* Output the new document using the transformer class */
			Transformer tfr = TransformerFactory.newInstance().newTransformer();
			tfr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docType.getSystemId());
			tfr.setOutputProperty(OutputKeys.INDENT, "yes");
			tfr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			tfr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			tfr.transform(new DOMSource(docAuthors), new StreamResult(new FileOutputStream(new File("output/outputMichaelBaggott.xml"))));
			
		
		}
		
		catch (Exception e) {
			System.out.println("Error opening XML document: " + e);
			e.printStackTrace();
		}
	}
	
	/* Function to combine the authors.xml and artowrks.xml files into one document, ready for outputting */
	static void modifyAuthorsXML(NodeList authors, NodeList artworks, Document docAuthors, Document docArtworks) {
	
		String authorName = "";
		int matchedAuthor = 0;
		
		/* Loop through the authors nodelist by author element */
		for (int x = 0; x < authors.getLength(); x++) {
	
			/* Get the author element */
			Element author = (Element)authors.item(x);
			/* Create a new nodelist of just the child nodes of an individual author */
			NodeList authorNodes = author.getChildNodes();
			/* Loop through the child nodes of author */
			for (int i = 0; i < authorNodes.getLength(); i++) {
				/* If the child node is an element node */
				if (authorNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					/* Get the child node */
					Element authorNode = (Element)authorNodes.item(i);
					/* If the child node is an author name node */
					if (authorNode.getTagName().equals("name")) {
						/* Put the author name in a variable */
						authorName = authorNode.getTextContent();
						
						/* Create the various artworks elements ready for use */
						Element paintingEl = docAuthors.createElement("artworks");
						paintingEl.setAttribute("form", "painting");
						Element sculptureEl = docAuthors.createElement("artworks");
						sculptureEl.setAttribute("form", "sculpture");
						Element architectureEl = docAuthors.createElement("artworks");
						architectureEl.setAttribute("form", "architecture");
						Element metalworkEl = docAuthors.createElement("artworks");
						metalworkEl.setAttribute("form", "metalwork");
						
						
						/* Loop through all of the artworks nodes */
						for (int y = 0; y < artworks.getLength(); y++) {
							
							/* Create the artowrk child elements ready for use */
							Element artworkEl = docAuthors.createElement("artwork");
							Element titleEl = docAuthors.createElement("title");
							Element techniqueEl = docAuthors.createElement("technique");
							Element locationEl = docAuthors.createElement("location");
							
							/* Get the current artwork */
							Element artwork = (Element)artworks.item(y);
							/* Create a new nodelist of child elements of the artwork */
							NodeList artworkNodes = artwork.getChildNodes();
							matchedAuthor = 0;
							String formType = "";
							/* Loop through the artwork nodes */
							for (int j = 0; j < artworkNodes.getLength(); j++) {
								/* If the node is an element node */
								if (artworkNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
									
									/* Get the node */
									Element artworkNode = (Element)artworkNodes.item(j);
									
									/* If the artwork author matches the author name of the current author in the authors loop */
									if (artworkNode.getTagName().equals("author")) {
										if (artworkNode.getTextContent().equals(authorName)) {
											//This author is a match
											matchedAuthor = 1;
										}
										else {
											// This author is not a match */
											matchedAuthor = 0;
										}
									}
									
									/* Set the values of the created artwork elements to the value of the current artwork in the artowrks loop */
									if (artworkNode.getTagName().equals("title")) {
										titleEl.setTextContent(artworkNode.getTextContent());
									}
									if (artworkNode.getTagName().equals("technique")) {
										techniqueEl.setTextContent(artworkNode.getTextContent());
									}
									if (artworkNode.getTagName().equals("location")) {
										locationEl.setTextContent(artworkNode.getTextContent());
									}
									if (artworkNode.getTagName().equals("date")) {
										artworkEl.setAttribute("date", artworkNode.getTextContent());
									}
									
									/* If this artwork author, and author are a match AND the artowrk node is the form node */
									if (artworkNode.getTagName().equals("form") && matchedAuthor == 1) {
										/* Record the value of the appropriate form type */
										if (artworkNode.getTextContent().equals("painting")) {
											formType = "painting";
										}
										else if (artworkNode.getTextContent().equals("sculpture")) {
											formType = "sculpture";
										}
										else if (artworkNode.getTextContent().equals("architecture")) {
											formType = "architecture";
										}
										else if (artworkNode.getTextContent().equals("metalwork")) {
											formType = "metalwork";
										}
										else {
											formType = "none";
										}
									}
		
								}
								/* If the artwork author, and current author are a match */
								if (matchedAuthor == 1) {
				
									/* Append the newly created elements, to the newly created artwork element */
									artworkEl.appendChild(titleEl);
									artworkEl.appendChild(techniqueEl);
									artworkEl.appendChild(locationEl);
									/* Append the appropriate form type */
									if (formType.equals("painting")) {
										paintingEl.appendChild(artworkEl);
									}
									if (formType.equals("sculpture")) {
										sculptureEl.appendChild(artworkEl);
									}
									if (formType.equals("architecture")) {
										architectureEl.appendChild(artworkEl);
									}
									if (formType.equals("metalwork")) {
										metalworkEl.appendChild(artworkEl);
									}
									
								}
							}
						}
						
						/* Only append the element of a particular form, if children of it have been matched and created */
						if (paintingEl.hasChildNodes()) {
							author.appendChild(paintingEl);
						}
						if (sculptureEl.hasChildNodes()) {
							author.appendChild(sculptureEl);
						}
						if (architectureEl.hasChildNodes()) {
							author.appendChild(architectureEl);
						}
						if (metalworkEl.hasChildNodes()) {
							author.appendChild(metalworkEl);
						}
								
					}
				}
			}
		}
		return;
	}
	
	/* Method to sort the artworks in the newly created document */
	static void sortArtworks(NodeList artworksNew, Document docAuthors) {
		
		/* Declare a sortable data structure */
		Map<Integer, Node> artworkMap;
		/* Loop through all the artworks in the new document */
		for (int i = 0; i < artworksNew.getLength(); i++) {
			/* If the element in the nodelist is of type element */
			if (artworksNew.item(i).getNodeType() == Node.ELEMENT_NODE) {
				
				/* Create a new nodelist of the children of the artwork node */
				NodeList artwork = artworksNew.item(i).getChildNodes();
				/* Instantiate the data structure */
				artworkMap = new TreeMap<Integer, Node>();
				/* loop through the artwork */
				for (int j = 0; j < artwork.getLength(); j++) {
					/* If the node is an element node */
					if (artwork.item(j).getNodeType() == Node.ELEMENT_NODE) {
						/* Deep copy the artwork and it's children to a new node */
						Node artworkNode = artwork.item(j).cloneNode(true);
						/* Get and store the date of the current artwork */
						Element e = (Element) artwork.item(j);
						String dateString = e.getAttribute("date");
						int date = Integer.parseInt(dateString);
						/* Store the newly depp copied artwork node in the data structure with its date as a key value */
						artworkMap.put(date, artworkNode);
					}
				}
				/* Storing the new nodelist in the data structure has sorted it. After this artwork loop has finished 
				   if there were any artworks copied, delete the original artworks and replace them with the newly sorted artworks */
				 
				while (artworksNew.item(i).hasChildNodes()) {
					artworksNew.item(i).removeChild(artworksNew.item(i).getFirstChild());
				}
				for (Map.Entry<Integer, Node> entry : artworkMap.entrySet()) {
					Node node = entry.getValue();
					artworksNew.item(i).appendChild(node);
				}
			}
		}
	}
	
}
