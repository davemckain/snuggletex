/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.snuggletex.CSSUtilities;
import uk.ac.ed.ph.snuggletex.DOMBuilderOptions;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.SnuggleTeX;
import uk.ac.ed.ph.snuggletex.definitions.Globals;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * FIXME: Document this type!
 * 
 * FIXME: Add caching of the stylesheet for this!
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public class DOMDownConverter {
	
	private final SessionContext sessionContext;
	private final DOMBuilderOptions options;
    
    public DOMDownConverter(final SessionContext sessionContext, final DOMBuilderOptions options) {
    	this.sessionContext = sessionContext;
        this.options = options;
    }
    
    public Document downConvertDOM(Document document) {
    	/* If inlining CSS, create a document to hold the name/value pairs as described in
    	 * buildCSSPropertiesDocument(). Otherwise, we'll create an empty one to indicate
    	 * that nothing should be inlined */
    	Document cssPropertiesDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
    	if (options.isInliningCSS()) {
    		buildCSSPropertiesDocument(cssPropertiesDocument);
    	}
    	
    	/* Create URI Resolver to let the XSLT get at this document */
    	CSSPropertiesURIResolver uriResolver = new CSSPropertiesURIResolver(cssPropertiesDocument);

    	/* Compile and cache the MathML -> XHTML stylesheet and run on the input Document, creating
    	 * a replacement Document as the result */
    	Map<String, Templates> stylesheetCache = sessionContext.getXSLTStylesheetCache();
    	Templates templates = stylesheetCache.get(Globals.MATHML_TO_XHTML_XSL_NAME);
    	if (templates==null) {
    		TransformerFactory transformerFactory = XMLUtilities.createTransformerFactory();
    		InputStream xslStream = getClass().getClassLoader().getResourceAsStream(Globals.MATHML_TO_XHTML_XSL_NAME);
    		try {
				templates = transformerFactory.newTemplates(new StreamSource(xslStream));
			}
    		catch (TransformerConfigurationException e) {
    			throw new SnuggleRuntimeException("Could not compile down-conversion XSLT stylesheet", e);
			}
    		stylesheetCache.put(Globals.MATHML_TO_XHTML_XSL_NAME, templates);
    	}

		Document result = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
		try {
			Transformer transformer = templates.newTransformer();
			transformer.setURIResolver(uriResolver);
			transformer.transform(new DOMSource(document), new DOMResult(result));
		}
		catch (Exception e) {
			throw new SnuggleRuntimeException("Unexpected Exception down-converting DOM", e);
		}
		return result;
    }
    
    /**
     * FIXME: Document this type!
     *
     * @author  David McKain
     * @version $Revision: 3 $
     */
    protected static class CSSPropertiesURIResolver implements URIResolver {
    	
    	private final Source cssPropertiesSource;
    	
    	public CSSPropertiesURIResolver(final Document cssPropertiesDocument) {
    		this.cssPropertiesSource = new DOMSource(cssPropertiesDocument, Globals.CSS_PROPERTIES_DOCUMENT_URN);
    	}
    	
    	public Source resolve(String href, String base) {
    		return href.equals(Globals.CSS_PROPERTIES_DOCUMENT_URN) ? cssPropertiesSource : null;
    	}
    }
    
    /**
     * Converts the CSS Properties specified within the {@link DOMBuilderOptions} into an XML
     * document of the form:
     * 
     * <pre><![CDATA[
     * <properties xmlns="http;//www.ph.ed.ac.uk/snuggletex">
     *   <property name="..." value="..."/>
     *   ...
     * </properties>
     * ]]></pre>
     */
    public void buildCSSPropertiesDocument(Document result) {
		/* Make Properties XML */
		Properties cssProperties = CSSUtilities.readInlineCSSProperties(options);
		Element root = result.createElementNS(SnuggleTeX.SNUGGLETEX_NAMESPACE, "properties");
		result.appendChild(root);
		
		Element element;
		for (Entry<Object,Object> entry : cssProperties.entrySet()) {
			element = result.createElementNS(SnuggleTeX.SNUGGLETEX_NAMESPACE, "property");
			element.setAttribute("name", (String) entry.getKey());
			element.setAttribute("value", (String) entry.getValue());
			root.appendChild(element);
		}
    }
    
    public static void main(String[] args) throws Exception {
		Document d = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
		Element math = d.createElementNS(Globals.MATHML_NAMESPACE, "math");
		d.appendChild(math);
		
		Element e = d.createElementNS(Globals.MATHML_NAMESPACE, "mn");
		e.appendChild(d.createTextNode("1"));
		math.appendChild(e);
		
		e = d.createElementNS(Globals.MATHML_NAMESPACE, "mo");
		e.appendChild(d.createTextNode("+"));
		math.appendChild(e);
		
		e = d.createElementNS(Globals.MATHML_NAMESPACE, "mn");
		e.appendChild(d.createTextNode("2"));
		math.appendChild(e);
		
		/* Make Properties XML */
		Properties properties = CSSUtilities.readBuiltinInlineCSSProperties();
		Document pd = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
		Element root = pd.createElementNS(SnuggleTeX.SNUGGLETEX_NAMESPACE, "properties");
		pd.appendChild(root);
		for (Entry<Object,Object> entry : properties.entrySet()) {
			e = pd.createElementNS(SnuggleTeX.SNUGGLETEX_NAMESPACE, "property");
			e.setAttribute("name", (String) entry.getKey());
			e.setAttribute("value", (String) entry.getValue());
			root.appendChild(e);
		}
		
		final Source propertiesSource = new DOMSource(pd);
		final URIResolver propertiesResolver = new URIResolver() {
			public Source resolve(String href, String base) {
				System.out.println("Resolve: href=" + href + ",base=" + base);
				return href.equals("urn:snuggletex-properties") ? propertiesSource : null;
			}
		};
		
		StringWriter resultWriter = new StringWriter();
		TransformerFactory tf = XMLUtilities.createTransformerFactory();
		tf.setURIResolver(propertiesResolver);
		
		InputStream xslStream = DOMDownConverter.class.getClassLoader().getResourceAsStream("uk/ac/ed/ph/snuggletex/mathml-to-xhtml.xsl");
		System.out.println("GOT " + xslStream);
		Transformer t = tf.newTransformer(new StreamSource(xslStream));
		t.transform(new DOMSource(d), new StreamResult(resultWriter));
		
		System.out.println("Got " + resultWriter);
		
	}


}
