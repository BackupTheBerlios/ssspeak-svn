/**
 * Xslt helper and stylesheets
 */
package org.nargila.speak.framework.xslt;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.transform.TransformerException;

import org.nargila.util.Xslt;
import org.w3c.dom.Node;

/**
 * Xslt helper for HTML=>SSML etc. conversions.
 * @author tshalif
 *
 */
public class XsltHelper {

	/**
	 * Apply stylesheet denoted by 'name' on given XML node.
	 * @param name name of stylesheet to use - assumed &lt;name&gt;.xsl exists as resource for current class
	 * @param node the input node
	 * @return the xslt result as node
	 * @throws TransformerException
	 */	
	public static  Node xslt(String name, Node node) throws TransformerException {
	    return xslt(name, node, null);
	}

	/**
	 * Apply stylesheet denoted by 'name' on given XML node with params.
	 * @param name name of stylesheet to use - assumed &lt;name&gt;.xsl exists as resource for current class
	 * @param node the input node
	 * @param params stylesheet params to pass to processor
	 * @return to xslt result as node
	 * @throws TransformerException
	 */
	public static  Node xslt(String name, Node node, String[] params) throws TransformerException {
	    InputStream xsl = XsltHelper.class.getResourceAsStream(name + ".xsl");
	    Xslt xslt = new Xslt(xsl);
	
	    if (null != params) {
	    	Iterator i = Arrays.asList(params).iterator();
	    	
	    	while (i.hasNext()) {
	    		String p = (String) i.next();
	            String[] pair = p.split("=");
	
	            if (pair.length != 2) {
	                throw new IllegalArgumentException("params passed on to Xslt.xslt must be of the form name=vallue");
	            }
	            xslt.setParameter(pair[0], pair[1]);
	        }
	    }
	
	    Node res = xslt.xslt(node);
	
	    return res;
	}

}
