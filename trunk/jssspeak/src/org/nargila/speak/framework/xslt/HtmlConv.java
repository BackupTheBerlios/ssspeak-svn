package org.nargila.speak.framework.xslt;

import java.net.URL;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;



public class HtmlConv {
    

    public static String guessHtmlProvider(URL url) {
        String res = null;
        
        if (-1 != url.getHost().indexOf("wikipedia")) {
            res = "wikipedia";
        }
        return res;
    }



    public static Node prepHtml(Node html, URL location) throws TransformerException {
        String provider = guessHtmlProvider(location);

        if (null == provider) {
            return html;
        }

        return prepHtml(html, provider);
    }


    public static Node prepHtml(Node html, String provider) throws TransformerException {
        Node res = XsltHelper.xslt(provider + "2html", html);
        
        return res;
    }
}