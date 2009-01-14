/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.upconversion;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.internal.XMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetCache;

import java.io.IOException;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public class MathMLUpConverter {
    
    private final StylesheetCache stylesheetCache;
    
    public static final String UPCONVERTER_BASE_LOCATION = "classpath:/uk/ac/ed/ph/snuggletex/extensions/upconversion";
    public static final String UPCONVERTER_XSL_LOCATION = UPCONVERTER_BASE_LOCATION + "/snuggletex-upconverter.xsl";
    
    public MathMLUpConverter(StylesheetCache stylesheetCache) {
        this.stylesheetCache = stylesheetCache;
    }
    
    public Document upConvertSnuggleTeXMathML(Document document) {
        Templates stylesheet = getStylesheet(UPCONVERTER_XSL_LOCATION);
        Document resultDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        try {
            stylesheet.newTransformer().transform(new DOMSource(document), new DOMResult(resultDocument));
        }
        catch (TransformerException e) {
            throw new SnuggleRuntimeException("Up-conversion failed", e);
        }
        return resultDocument;
    }
    
    //---------------------------------------------------------------------
    
    private TransformerFactory createSaxonTransformerFactory() {
        try {
            return (TransformerFactory) Class.forName("net.sf.saxon.TransformerFactoryImpl").newInstance();
        }
        catch (Exception e) {
            throw new SnuggleRuntimeException("Failed to explicitly instantiate SAXON net.sf.saxon.TransformerFactoryImpl - check your ClassPath!", e);
        }
    }
    
    private Templates getStylesheet(String location) {
        Templates result;
        if (stylesheetCache==null) {
            result = compileStylesheet(location);
        }
        else {
            synchronized(stylesheetCache) {
                result = stylesheetCache.getStylesheet(location);
                if (result==null) {
                    result = compileStylesheet(location);
                    stylesheetCache.putStylesheet(location, result);
                }
            }
        }
        return result;
    }
    
    private Templates compileStylesheet(String location) {
        TransformerFactory transformerFactory = createSaxonTransformerFactory();
        return XMLUtilities.compileInternalStylesheet(transformerFactory, location);
    }
    
    public static void main(String[] args) throws IOException {
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        session.parseInput(new SnuggleInput("Hello\\par There\\par $$1+\\frac{x}{y}$$"));
        
        DOMOutputOptions options = new DOMOutputOptions();
        options.setAddingMathAnnotations(true);
        options.setDomPostProcessor(new UpConvertingPostProcessor());
        
        String string = session.buildXMLString(options, true);
        System.out.println("Got " + string);
    }
}
