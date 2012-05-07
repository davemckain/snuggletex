/* $Id:MathTests.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import uk.ac.ed.ph.asciimath.parser.AsciiMathParser;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.definitions.W3CConstants;
import uk.ac.ed.ph.snuggletex.testutil.TestFileHelper;
import uk.ac.ed.ph.snuggletex.testutil.TestUtilities;

import java.util.Collection;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This does the same kind of thing as {@link MathUpConversionPMathMLTests}, but uses
 * {@link AsciiMathParser} to up-convert raw ASCIIMath input instead.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
@RunWith(Parameterized.class)
public class ASCIIMathUpConversionTests {
    
    private static final Logger log = Logger.getLogger(ASCIIMathUpConversionTests.class.getName());
    
    public static final String TEST_RESOURCE_NAME = "asciimath-upconversion-tests.txt";
    
    @Parameters
    public static Collection<String[]> data() throws Exception {
        return TestFileHelper.readAndParseSingleLineInputTestResource(TEST_RESOURCE_NAME);
    }
    
    private final String asciiMathInput;
    private final String expectedMathML;
    
    public ASCIIMathUpConversionTests(final String asciiMathInput, final String expectedMathMLContent) {
        this.asciiMathInput = asciiMathInput;
        this.expectedMathML = "<math xmlns='" + W3CConstants.MATHML_NAMESPACE + "'>"
            + expectedMathMLContent.replaceAll("(?m)^\\s+", "").replaceAll("(?m)\\s+$", "").replace("\n", "")
            + "</math>";
    }
    
    @Test
    public void runTest() throws Throwable {
        /* Do initial ASCIIMath parse */
        AsciiMathParser parser = new AsciiMathParser();
        Document mathDocument = parser.parseAsciiMath(asciiMathInput);
        
        /* Set up up-converter so that it only generates fixed up Presentation MathML */
        UpConversionOptions upConversionOptions = new UpConversionOptions();
        upConversionOptions.setSpecifiedOption(UpConversionOptionDefinitions.DO_CONTENT_MATHML_NAME, "false");
        upConversionOptions.setSpecifiedOption(UpConversionOptionDefinitions.DO_MAXIMA_NAME, "false");
        
        /* Set up-conversion options for tests */
        SnuggleEngine engine = new SnuggleEngine();
        upConversionOptions.assumeSymbol(createUpConversionSymbolElement(engine, "e"), "exponentialNumber");
        upConversionOptions.assumeSymbol(createUpConversionSymbolElement(engine, "f"), "function");
        upConversionOptions.assumeSymbol(createUpConversionSymbolElement(engine, "g"), "function");
        upConversionOptions.assumeSymbol(createUpConversionSymbolElement(engine, "i"), "imaginaryNumber");
        upConversionOptions.assumeSymbol(createUpConversionSymbolElement(engine, "\\pi"), "constantPi");
        upConversionOptions.assumeSymbol(createUpConversionSymbolElement(engine, "\\gamma"), "eulerGamma");
        
        /* Now up-convert the raw MathML generated by ASCIIMath */
        MathMLUpConverter upConverter = new MathMLUpConverter();
        Document upConvertedDocument = upConverter.upConvertASCIIMathML(mathDocument, upConversionOptions);
        
        /* Verify the document */
        try {
            TestUtilities.verifyXML(expectedMathML, upConvertedDocument);
        }
        catch (Throwable e) {
            log.severe("^^^ Input above was: " + asciiMathInput);
            log.severe("-------------------------------------------");
            throw e;
        }
    }
    
    private Element createUpConversionSymbolElement(SnuggleEngine engine, String mathInput) {
        try {
            SnuggleSession session = engine.createSession();
            session.parseInput(new SnuggleInput("$" + mathInput + "$"));
            return (Element) session.buildDOMSubtree().item(0).getFirstChild();
        }
        catch (Exception e) {
            throw new RuntimeException("Unexpected Exception in SnuggleTeX option generation");
        }
    }
}
