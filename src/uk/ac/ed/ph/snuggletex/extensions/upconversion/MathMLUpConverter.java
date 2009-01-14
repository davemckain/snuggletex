/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.upconversion;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.SnuggleConstants;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.internal.XMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
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
    public static final String ASCIIMATH_FIXER_XSL_LOCATION = UPCONVERTER_BASE_LOCATION + "/asciimathml-fixer.xsl";
    public static final String UPCONVERTER_XSL_LOCATION = UPCONVERTER_BASE_LOCATION + "/snuggletex-upconverter.xsl";
    
    public MathMLUpConverter(final StylesheetCache stylesheetCache) {
        this.stylesheetCache = stylesheetCache;
    }
    
    public Document upConvertSnuggleTeXMathML(final Document document, final Map<String, Object> upconversionParameters) {
        Document resultDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        try {
            /* Create required XSLT */
            Templates upconverterStylesheet = getStylesheet(UPCONVERTER_XSL_LOCATION);
            Transformer upconverter = upconverterStylesheet.newTransformer();
            
            /* Set any specified parameters */
            if (upconversionParameters!=null) {
                for (Entry<String, Object> entry : upconversionParameters.entrySet()) {
                    /* (Recall that the actual stylesheets assume the parameters are in the SnuggleTeX
                     * namespace, so we need to use {uri}localName format for the parameter name.) */
                    upconverter.setParameter("{" + SnuggleConstants.SNUGGLETEX_NAMESPACE + "}" + entry.getKey(),
                            entry.getValue());
                    System.out.println("SET " + entry.getKey());
                }
            }
            
            /* Do the transform */
            upconverter.transform(new DOMSource(document), new DOMResult(resultDocument));
        }
        catch (TransformerException e) {
            throw new SnuggleRuntimeException("Up-conversion failed", e);
        }
        return resultDocument;
    }
    
    public Document upConvertASCIIMathML(final Document document, final Map<String, Object> upconversionParameters) {
        /* First of all we convert the ASCIIMathML into something equivalent to SnuggleTeX output */
        Document fixedDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        try {
            Templates fixerStylesheet = getStylesheet(ASCIIMATH_FIXER_XSL_LOCATION);
            fixerStylesheet.newTransformer().transform(new DOMSource(document), new DOMResult(fixedDocument));
        }
        catch (TransformerException e) {
            throw new SnuggleRuntimeException("ASCIIMathML fixing step failed", e);
        }
        /* Then do the normal SnuggleTeX up-conversion */
        return upConvertSnuggleTeXMathML(fixedDocument, upconversionParameters);
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
        session.parseInput(new SnuggleInput("$$1+\\frac{e}{y}$$"));
        
        Map<String, Object> upParameters = new HashMap<String, Object>();
        upParameters.put(UpConversionParameters.ASSUME_EXPONENTIAL_E, Boolean.TRUE);
        
        DOMOutputOptions options = new DOMOutputOptions();
        options.setAddingMathAnnotations(true);
        options.setDomPostProcessor(new UpConvertingPostProcessor(upParameters));
        
        String string = session.buildXMLString(options, true);
        System.out.println("Got " + string);
    }
}
