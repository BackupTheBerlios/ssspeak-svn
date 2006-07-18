/**
 * Generic utilities
 */
package org.nargila.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Jaxp Xslt helper class 
 * @author tshalif
 *
 */
public class Xslt {

    private Transformer transformer;

    /**
     * ctor with stylesheet
     * @param xsl the stylesheet
     * @throws TransformerConfigurationException
     */
    public Xslt(InputStream xsl) throws TransformerConfigurationException {
        transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(xsl));
    }
    
    /**
     * ctor with stylesheet
     * @param xsl the stylesheet
     * @throws TransformerConfigurationException
     * @throws FileNotFoundException
     */
    public Xslt(String xsl) throws TransformerConfigurationException, FileNotFoundException {
        this(new FileInputStream(xsl));
    }

    /**
     * set stylesheet parameter
     * @param name param name
     * @param value param value
     */
    public void setParameter(String name, Object value) {
        transformer.setParameter(name, value);
    }

    /**
     * perform transformation.
     * @param node input XML node
     * @return result node
     * @throws TransformerException
     * @throws TransformerConfigurationException
     */
    public Node xslt(Node node) throws TransformerException, TransformerConfigurationException {
        Source xml = new DOMSource(node);
        DOMResult result = new DOMResult();
        transformer.transform(xml, result);

        return result.getNode();
    }

    /**
     * perform transformation writing result to output file
     * @param node input node
     * @param path output to write result into
     * @throws TransformerException
     * @throws TransformerConfigurationException
     * @throws IOException
     */
    public void xslt(Node node, String path) throws TransformerException, TransformerConfigurationException, IOException {
        Source xml = new DOMSource(node);

        System.err.println("path: " + path);
        
        Result result = new StreamResult(new FileOutputStream(path));
        transformer.transform(xml, result);
    }        
        
    /**
     * transform with input taken from file
     * @param xmlfile the input file path
     * @return transformation result as node
     * @throws TransformerException
     */
    public Node xslt(String xmlfile) throws TransformerException {
        Source xml = new StreamSource(new File(xmlfile));

        DOMResult result = new DOMResult();
        transformer.transform(xml, result);

        return result.getNode();
    }
        
    /**
     * perform transformation from file writing result to file
     * @param inpath input data path
     * @param outpath output path
     * @throws TransformerException
     * @throws IOException
     */
    public void xslt(String inpath, String outpath) throws TransformerException , IOException {
        Source src = new StreamSource(new File(inpath));
        Result result = new StreamResult(new FileOutputStream(outpath));
        transformer.transform(src, result);
    }
        
    /**
     * Apply xpath expresion on XML node
     * @param source input XML node
     * @param expr XPATH expression to apply
     * @return result nodes
     * @throws XPathException
     */
    public static Node[] xpath(Node source, String expr) throws XPathException {    	
    	
    	NodeList res;
    	
    	try {
    		/*
    		 * Some bug in jaxp bundled with xalan 2.7 - try to use 
    		 * Xalan APIs directly
    		 */    		
    		Class.forName("org.apache.xpath.XPathAPI");
    		res = org.apache.xpath.XPathAPI.eval(source, expr).nodelist();
		} catch (ClassNotFoundException e) {
			/*
			 * Xalan implementation not found - let's hope jaxp is somehow configured:
			 */
			XPath xpath = XPathFactory.newInstance().newXPath();
			res = (NodeList)xpath.evaluate(expr, source, XPathConstants.NODESET);
		} catch (TransformerException e) {
			throw new XPathException(e);
		}
		
    	Node[] retval;
    	
    	if (null != res) {
    		final int len = res.getLength();
    		
    		retval = new Node[len];

    		for (int i = 0; i < len; ++i) {
    			retval[i] = res.item(i);
    		}
    	} else {
        	retval = new Node[0];
    	}
    	
        return retval;
    }

    /**
     * Serialize dom node into output stream
     * @param source input XML 
     * @param out output stream
     * @throws TransformerException
     */
    public static void serialize(Node source, OutputStream out) throws TransformerException {
        Transformer serializer = TransformerFactory.newInstance().newTransformer();

        serializer.transform(new DOMSource(source), 
                             new StreamResult(out));
    }

    /**
     * serialize dom node into output path
     * @param source input node
     * @param path output path
     * @throws TransformerException
     * @throws FileNotFoundException
     */
    public static void serialize(Node source, String path) throws TransformerException, FileNotFoundException {
    	serialize(source, new FileOutputStream(path));
    }

    /**
     * get document builder instance
     * @return document builder instance
     * @throws SAXException
     */
    private static DocumentBuilder createDocumentBuilder() throws SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //dbf.setValidating(true);
        //dbf.setIgnoringElementContentWhitespace(true);
        //dbf.setExpandEntityReferences(true);
        DocumentBuilder db = null;
        try {
          db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
          throw new SAXException(ex.getMessage());
        }
        //db.setErrorHandler(new MyErrorHandler(new PrintWriter(System.err)));
        //db.setEntityResolver(new ParserEntityResolver(System.getProperties().getProperty("nargila.external.entity.path")));
        return db;
    }   
    
   /**
    * build DOM Document from Reader input
    */
   public static org.w3c.dom.Document loadXML(Reader  input) throws IOException, SAXException {
        return loadXML(new InputSource(input));
    }
    /**
     * build DOM Document from InputSource input
     */
    public static org.w3c.dom.Document loadXML(InputSource input)
        throws IOException, SAXException {
        DocumentBuilder db = createDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(input);
        doc.getDocumentElement().normalize();
        return doc;
    }
    
    /**
     * load XML from file
     */
    public static org.w3c.dom.Document loadXML(String file) throws IOException, SAXException {
        return loadXML(new InputSource(file));
    }
}
