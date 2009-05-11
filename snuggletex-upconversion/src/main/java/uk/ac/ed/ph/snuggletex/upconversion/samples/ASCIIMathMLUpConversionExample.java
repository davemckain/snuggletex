/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion.samples;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.DOMPostProcessor;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionParameters;
import uk.ac.ed.ph.snuggletex.upconversion.UpConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.UnwrappedParallelMathMLDOM;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Simple demonstration of how to use some of the utilities within SnuggleTeX to up-convert
 * some raw MathML extracted from ASCIIMathML.
 * <p>
 * (You can do the same with the raw MathML produced by SnuggleTeX as well; you just have to
 * call a {@link MathMLUpConverter#upConvertSnuggleTeXMathML(Document, Map)}) instead of the
 * method for SnuggleTeX. When using SnuggleTeX normally, all of this can be invoked during
 * the snuggling process by registering a {@link UpConvertingPostProcessor} with your
 * {@link DOMOutputOptions#setDOMPostProcessor(DOMPostProcessor)}.)
 * 
 * <h2>Running Notes</h2>
 * 
 * You will need the following in your ClassPath:
 * 
 * <ul>
 *   <li>snuggletex.jar</li> (Also needed at compile time)
 *   <li>saxon9.jar, saxon9-dom.jar</li> (These are required as the conversion process uses XSLT 2.0)
 * </ul>
 * 
 * If you're already using XSLT 1.0 in your application and <strong>don't</strong> want your
 * existing XSLT to run using SAXON (which is hard to justify, IMO, as SAXON is superior to other
 * XSLT processors in most respects) then you might find that SAXON becomes your JAXP default
 * processor, which you can override as you require as it is invoked here explicitly, rather than
 * using {@link TransformerFactory#newInstance()}.
 *
 * @author  David McKain
 * @version $Revision$
 */
public class ASCIIMathMLUpConversionExample {
    
    public static void main(String[] args) {
        /* This is the MathML String we pull out from ASCIIMathML when you enter (5x)/(1-x).
         * 
         * NOTE: I'll have to check that we will always get proper UTF-8 from ASCIIMath so that
         * we can store as a Java string without doing any further re-encoding work.
         */
        String asciiMathML = "<math title=\" (5x)/(1-x) \" xmlns=\"http://www.w3.org/1998/Math/MathML\">\n"
            + "  <mstyle mathcolor=\"blue\" fontfamily=\"serif\" displaystyle=\"true\">\n" 
            + "    <mfrac>\n" 
            + "      <mrow>\n"
            + "        <mn>5</mn>\n" 
            + "        <mi>x</mi>\n" 
            + "      </mrow>\n"
            + "      <mrow>\n" 
            + "        <mn>1</mn>\n" 
            + "        <mo>-</mo>\n"
            + "        <mi>x</mi>\n"
            + "      </mrow>\n"
            + "    </mfrac>\n" 
            + "  </mstyle>\n" 
            + "</math>";
        
        /* ============================================================================ */
        /* To use the conversion stuff, we first need to parse this to get a DOM.
         * This is all standard stuff.
         * 
         * Before doing this, we need to get a "DocumentBuilder" which is the JAXP
         * handle on a DOM parser.
         */
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            /* A failure here is going to be highly unlikely and fatal so is best thrown as some
             * kind of unchecked Exception. I'll just use RuntimeException here.
             */
            throw new RuntimeException("Blah...", e);
        }
        
        /* Do the actual parsing */
        Document asciiMathMLDocument;
        try {
            asciiMathMLDocument = documentBuilder.parse(new InputSource(new StringReader(asciiMathML)));
        }
        catch (SAXException e) {
            /* This would indicate bad MathML from ASCIIMath, which is a bug that would need fixed */
            throw new RuntimeException("Blah...", e);
        }
        catch (IOException e) {
            /* This shouldn't happen normally, unless we have an encoding issue */
            throw new RuntimeException("Blah...", e);
        }
        
        /* ============================================================================ */
        /* Now we use some of the utilities I've put in SnuggleTeX to "up-convert" this. */
        
        /* Note: The conversion process is done with XSLT so it makes sense to cache compiled
         * XSLT stylesheets for performance reasons. Hence, each MathMLUpConverter instance
         * contains a reference to a StylesheetCache that will be used to cache compiled
         * XSLT during its life.
         * 
         * There are 2 constructors to MathMLUpConverter. One takes an explicit cache, which
         * is useful if you use XSLT in your application and want to integrate your own
         * caching mechanism.
         * 
         * The default no-argument constructor constructs a simple cache and stores it within
         * your MathMLUpConverter. Use this if you don't use XSLT in your own application or
         * are happy to use the default caching behaviour.
         * In this case, you should seriously consider creating a single instance of this
         * class and ensuring it has a long life to maximise the performance gains of using
         * compiled stylesheets. E.g. In a servlet environment, you'd want your instance to be
         * shared across the application. Exactly how you do that depends on how much of a 
         * framework you've got in place. At the lowest level, you can do it via the ServletContext.
         */
        MathMLUpConverter upConverter = new MathMLUpConverter();
        
        /* You can control aspects of the conversion using a simple Map as follows.
         * (Note: all of the values set below are actually defaults but I've put them in for
         * demo purposes. Have a look at UpConversionParameters for all of the possibilities.)
         */
        Map<String, Object> upconversionParameters = new HashMap<String, Object>();
        upconversionParameters.put(UpConversionParameters.DO_CONTENT_MATHML, Boolean.TRUE);
        upconversionParameters.put(UpConversionParameters.DO_MAXIMA, Boolean.TRUE);
        
        /* Now we do the up-conversion magic, which produces a new DOM Document. */
        Document upconvertedDocument;
        try {
            upconvertedDocument = upConverter.upConvertASCIIMathML(asciiMathMLDocument, upconversionParameters);
        }
        catch (SnuggleRuntimeException e) {
            /* This indicates a bug in the process so is currently notified via an unchecked
             * Exception, so I'll just rethrow it here. You'll want this to be trickle up
             * noisily so that the failure is logged properly.
             */
            throw e;
        }
        
        /* ============================================================================ */
        /* Demo of some utility methods for extracting results */
        
        /* 1. This is a convenience for serializing the resulting DOM Document back to an XML String */
        String resultingMathMLString = MathMLUtilities.serializeDocument(upconvertedDocument);
        System.out.println("Resulting MathML is:\n" + resultingMathMLString);
        
        /* 2. This demonstrates extracting a single annotation */
        String maximaAnnotation = MathMLUtilities.extractAnnotationString(upconvertedDocument.getDocumentElement(),
                MathMLUpConverter.MAXIMA_ANNOTATION_NAME);
        System.out.println("Maxima Annotation was:\n" + maximaAnnotation);
        
        /* 3. Extracts all annotations into a "convenient" wrapper Object.
         * 
         * Use this if you need most/all of the annotations as it saves having to walk the DOM
         * tree over and over.
         */
        UnwrappedParallelMathMLDOM unwrappedDOM = MathMLUtilities.unwrapParallelMathMLDOM(upconvertedDocument.getDocumentElement());
        System.out.println("First branch of parallel MathML DOM was:\n"
                + MathMLUtilities.serializeElement(unwrappedDOM.getFirstBranch()));
        // Etc... 
    }
}
