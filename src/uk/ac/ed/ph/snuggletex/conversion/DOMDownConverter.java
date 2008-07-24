/* $Id$
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
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class handles the optional "down-conversion" of simple MathML expressions into
 * XHTML + CSS alternatives, where possible.
 * 
 * @see DOMBuilderFacade
 *
 * @author  David McKain
 * @version $Revision$
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
     * Trivial {@link URIResolver} that returns an XML Document corresponding to
     * the current session's CSS Properties when the URI
     * {@link Globals#CSS_PROPERTIES_DOCUMENT_URN} is used.
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
}
