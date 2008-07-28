/* $Id: DOMDownConverter.java 121 2008-07-24 12:52:40Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.conversion.XMLUtilities;
import uk.ac.ed.ph.snuggletex.definitions.Globals;

import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This utility class "down-converts" a DOM {@link Document} so that simple MathML expressions
 * are replaced by XHTML + CSS alternatives.
 * <p>
 * More complex MathML expressions are left as-is.
 * <p>
 * This can be used independently of SnuggleTeX if required.
 *
 * @author  David McKain
 * @version $Revision: 121 $
 */
public final class MathMLDownConverter {
	
    private final DOMBuilderOptions options;
    private final Templates conversionStylesheet;
    
    public MathMLDownConverter(DOMBuilderOptions options, Templates conversionStylesheet) {
        this.options = options;
        this.conversionStylesheet = conversionStylesheet;
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

    	/* Run the conversion XSLT */
    	Templates templates = conversionStylesheet;
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

}
