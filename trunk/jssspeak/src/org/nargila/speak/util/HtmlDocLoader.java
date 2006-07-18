package jssspeak.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathException;

import jssspeak.xslt.Xslt;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

public class HtmlDocLoader {
	private static boolean isWindows() {
		return File.separatorChar == '\\';
	}
	
    public static Document load(String url) throws  IOException, java.net.MalformedURLException, TransformerException {
    	return load(new URL(url));
    }
    
    public static Document load(URL url) throws  IOException, TransformerException {
        Tidy tidy = new Tidy();

        tidy.setXmlOut(true);
        //tidy.setCharEncoding(Configuration.UTF8);
        tidy.setNumEntities(true);
       
        InputStream input = url.openStream();

        Document doc = tidy.parseDOM(input, null);

        return doc;
    }

    //public static Map<String,String> addLinkIds(Document doc) throws XPathException {
    public static Map addLinkIds(Document doc) throws XPathException {
    	//Map<String,String> res = new HashMap<String,String>();
    	Map res = new HashMap();
        Node[] anchors = Xslt.xpath(doc, "//*[local-name() = 'a' or local-name() = 'A'][@href or @name]");

        int counter = 0;

        Iterator i = Arrays.asList(anchors).iterator();
        
        while (i.hasNext()) {
        	Node n = (Node) i.next();
        	Element a = ((Element)n);
        	
        	String href = a.getAttribute("href");
        	String id = a.getAttribute("id");
        	
        	if (id.equals("")) {
        		id = "saya" + counter++;
        		a.setAttribute("id", id);
        	}
        	
            res.put(id, href);
        }
        
        return res;
    }

	/**
	 * Generate mark element for each referenced target.
	 * If there is an href refering back to a target in same document,
	 * a mark elemen (an 'a' element with 'ssspeakxx' id) will be prepended to target element,
	 * so later we can scroll play position to it.
	 * @param uri document uri, to check for file equivalence even for absolute target href
	 * @param html the HTML document node.
	 * @throws TransformerException 
	 */
	//public static Map<String,String> generateTargetMarks(URI uri, Document html) throws Exception {
    public static Map generateTargetMarks(URI uri, Document html) throws Exception {
		
		//Map<String,String> res = new HashMap<String,String>();
    	Map res = new HashMap();
		
		Node[] links = Xslt.xpath(html, "//*[local-name() = 'a' or local-name() = 'A']/@href[contains(., '#')]");
		
		int counter = 0;

		String baseSpec = uri.toString();
		
		Iterator i = Arrays.asList(links).iterator();
		
		while (i.hasNext()) {
			Node n = (Node) i.next();			
			String spec = n.getNodeValue();
						
			if (spec.split("#")[0].equals(baseSpec)) {
				spec = spec.substring(baseSpec.length());
			}

			if (spec.charAt(0) != '#') {
				continue;
			}

			String name = spec.substring(1);
				
			String id = addTargetMark(html, name, "ssspeak-target" + counter++);
		
			if (null != id) {
			    res.put(name, id);
			}
		}		
		
		return res;
	}


	private static String addTargetMark(Node html, String name, String id) throws Exception {
		Node[] targets = Xslt.xpath(html, "/descendant::*[@id = '" + name + "' or @name = '" + name + "'][1]");		
		
		if (targets.length != 0) {
			Element target = (Element)targets[0];
			
			Element a = target.getOwnerDocument().createElement("a");			
			target.getParentNode().insertBefore(a, target);
			a.setAttribute("id", id);
			
			return id;
		}
		
		return null;
	}
}

