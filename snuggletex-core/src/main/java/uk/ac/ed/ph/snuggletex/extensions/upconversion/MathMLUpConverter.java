/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.upconversion;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.DOMPostProcessor;
import uk.ac.ed.ph.snuggletex.SnuggleConstants;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.SimpleStylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

/**
 * Standalone utility class for "up-converting" MathML Documents created by either SnuggleTeX
 * or ASCIIMathML as follows:
 * <ul>
 *   <li>Presentation MathML is "enhanced" with extra semantics</li>
 *   <li>Conversion to Content MathML is optionally attempted (assuming PMathML follows certain conventions)</li>
 *   <li>Conversion to Maxima input is optionally attempted (assuming PMathML follows certain conventions)</li>
 * </ul>
 * This can be invoked within the normal SnuggleTeX parsing process using by passing an
 * {@link UpConvertingPostProcessor} via {@link DOMOutputOptions#setDOMPostProcessor(DOMPostProcessor)}.
 * 
 * <h2>Usage Notes</h2>
 * 
 * <ul>
 *   <li>
 *     If the up-conversion process fails because the input cannot be understood, you can find
 *     out what went wrong by passing the resulting {@link Document} to
 *     {@link UpConversionUtilities#extractUpConversionFailures(Document)}, to get a List
 *     of {@link UpConversionFailure} Objects that can be inspected further.
 *   </li>
 *   <li>An implementation of this class is thread-safe</li>
 *   <li>
 *     If you use XSLT in your own application, consider using the constructor that
 *     takes a {@link StylesheetCache} to integrate with your own caching mechanism.
 *   </li> 
 *     If you don't use XSLT in your own application, you can use the default constructor
 *     that creates a cache used by the instance of this class. In this case, you'll want
 *     to ensure your instance has "application-wide" context to maximise performance.
 *   </li>
 * </ul>
 * 
 * @author  David McKain
 * @version $Revision$
 */
public class MathMLUpConverter {

    /* (Names of the various annotations. These are also defined in the XSLT so, if changing, we
     * must ensure that things are manually kept in sync. I could pass all of these as parameters
     * but it seems like overkill here!)
     */
    
    public static final String SNUGGLETEX_ANNOTATION_NAME = "SnuggleTeX";
    public static final String LATEX_ANNOTATION_NAME = "LaTeX";
    public static final String CONTENT_MATHML_ANNOTATION_NAME = "MathML-Content";
    public static final String CONTENT_FAILURES_ANNOTATION_NAME = "MathML-Content-upconversion-failures";
    public static final String MAXIMA_ANNOTATION_NAME = "Maxima";
    public static final String MAXIMA_FAILURES_ANNOTATION_NAME = "Maxima-upconversion-failures";
    public static final String ASCIIMATH_INPUT_ANNOTATION_NAME = "ASCIIMathInput";

    /** "Base" location for the XSLT stylesheets used here */
    private static final String UPCONVERTER_BASE_LOCATION = "classpath:/uk/ac/ed/ph/snuggletex/extensions/upconversion";
    
    /** Location of the initial XSLT for fixing up ASCIIMathML */
    private static final String ASCIIMATH_FIXER_XSL_LOCATION = UPCONVERTER_BASE_LOCATION + "/asciimathml-fixer.xsl";
    
    /** Location of the main up-converting XSLT */
    private static final String UPCONVERTER_XSL_LOCATION = UPCONVERTER_BASE_LOCATION + "/snuggletex-upconverter.xsl";
    
    /** Helper to manage the {@link StylesheetCache} */
    private final StylesheetManager stylesheetManager;
    
    /**
     * Creates a new up-converter using a simple internal cache.
     * <p>
     * Use this constructor if you don't use XSLT yourself. In this case, you'll want your
     * instance of this class to be reused as much as possible to get the benefits of caching.
     */
    public MathMLUpConverter() {
        this(new SimpleStylesheetCache());
    }
    
    /**
     * Creates a new up-converter using the given {@link StylesheetCache} to cache internal XSLT
     * stylesheets.
     * <p>
     * Use this constructor if you do your own XSLT caching that you want to integrate in, or
     * if the default doesn't do what you want.
     */
    public MathMLUpConverter(final StylesheetCache stylesheetCache) {
        this.stylesheetManager = new StylesheetManager(stylesheetCache);
    }
    
    public Document upConvertSnuggleTeXMathML(final Document document, final Map<String, Object> upconversionParameters) {
        Document resultDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        try {
            /* Create required XSLT */
            Templates upconverterStylesheet = stylesheetManager.getStylesheet(UPCONVERTER_XSL_LOCATION, true);
            Transformer upconverter = upconverterStylesheet.newTransformer();
            
            /* Set any specified parameters */
            if (upconversionParameters!=null) {
                for (Entry<String, Object> entry : upconversionParameters.entrySet()) {
                    /* (Recall that the actual stylesheets assume the parameters are in the SnuggleTeX
                     * namespace, so we need to use {uri}localName format for the parameter name.) */
                    upconverter.setParameter("{" + SnuggleConstants.SNUGGLETEX_NAMESPACE + "}" + entry.getKey(),
                            entry.getValue());
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
            Templates fixerStylesheet = stylesheetManager.getStylesheet(ASCIIMATH_FIXER_XSL_LOCATION, true);
            fixerStylesheet.newTransformer().transform(new DOMSource(document), new DOMResult(fixedDocument));
        }
        catch (TransformerException e) {
            throw new SnuggleRuntimeException("ASCIIMathML fixing step failed", e);
        }
        /* Then do the normal SnuggleTeX up-conversion */
        return upConvertSnuggleTeXMathML(fixedDocument, upconversionParameters);
    }
}
