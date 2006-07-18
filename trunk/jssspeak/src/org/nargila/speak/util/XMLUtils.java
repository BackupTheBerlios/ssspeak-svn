package jssspeak.util;

import javax.xml.parsers.*;
import org.xml.sax.*;
import java.io.*;

/**
 * Helper class for parsing/building DOM from XML input.
 * When parsing XML documents, DTD files and other external
 * entities will be searched in ':' delimited paths defined 
 * by the <pre>nargila.external.entity.path</pre> property.
 * @see ParserEntityResolver
 */
public class XMLUtils {
    /**
     * build DOM Document from Reader input
     */
    public static org.w3c.dom.Document loadXML(Reader input) throws IOException, SAXException {
      return loadXML(new InputSource(input));
    }
    /**
     * build DOM Document from file input
     */
    public static org.w3c.dom.Document loadXML(String file) throws IOException, SAXException {
      return loadXML(new InputSource(file));
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
    /** create a DocumentBuilder */
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
        db.setErrorHandler(new MyErrorHandler(new PrintWriter(System.err)));
        //db.setEntityResolver(new ParserEntityResolver(System.getProperties().getProperty("nargila.external.entity.path")));
        return db;
    }
    /** Error handler to report errors and warnings */
    private static class MyErrorHandler implements ErrorHandler {
        /** Error handler output goes here */
        private PrintWriter out;

        MyErrorHandler(PrintWriter out) {
            this.out = out;
        }

        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                " Line=" + spe.getLineNumber() +
                ": " + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }

        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
}
