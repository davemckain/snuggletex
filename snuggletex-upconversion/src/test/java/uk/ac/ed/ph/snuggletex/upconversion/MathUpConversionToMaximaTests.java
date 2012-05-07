/* $Id:MathTests.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import uk.ac.ed.ph.snuggletex.MathTests;
import uk.ac.ed.ph.snuggletex.testutil.TestFileHelper;
import uk.ac.ed.ph.snuggletex.upconversion.SnuggleTeXUpConversionTestDriver.DriverCallback;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

import java.util.Collection;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Same idea as {@link MathTests}, but tests the initial up-conversion to more
 * semantic Presentation MathML.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
@RunWith(Parameterized.class)
public class MathUpConversionToMaximaTests implements DriverCallback {
    
    private static final Logger log = Logger.getLogger(MathUpConversionToMaximaTests.class.getName());
    
    public static final String TEST_RESOURCE_NAME = "math-upconversion-maxima-tests.txt";
    
    @Parameters
    public static Collection<String[]> data() throws Exception {
        return TestFileHelper.readAndParseSingleLineInputTestResource(TEST_RESOURCE_NAME);
    }
    
    private final String inputLaTeX;
    private final String expectedResult;
    
    public MathUpConversionToMaximaTests(final String inputFragment, final String expectedResult) {
        this.inputLaTeX = inputFragment;
        this.expectedResult = expectedResult;
    }
    
    @Test
    public void runTest() throws Throwable {
        /* Set up up-converter so that it only generates fixed up Presentation MathML */
        UpConversionOptions upConversionOptions = new UpConversionOptions();
        upConversionOptions.setSpecifiedOption(UpConversionOptionDefinitions.DO_CONTENT_MATHML_NAME, "true");
        upConversionOptions.setSpecifiedOption(UpConversionOptionDefinitions.DO_MAXIMA_NAME, "true");
        
        SnuggleTeXUpConversionTestDriver driver = new SnuggleTeXUpConversionTestDriver(upConversionOptions, this);
        driver.run(inputLaTeX, expectedResult);        
    }
    
    public void verifyErrorFreeDOM(Document document) throws Throwable {
        /* Extract Maxima annotation */
        Element mathmlElement = document.getDocumentElement();
        String maximaAnnotation = MathMLUtilities.extractAnnotationString(mathmlElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME);
        
        /* Compare with expected */
        try {
            Assert.assertEquals(expectedResult, maximaAnnotation);
        }
        catch (Throwable e) {
            log.severe("Maxima output comparison failed");
            log.severe("Expected result: " + expectedResult);
            log.severe("Actual result:   " + maximaAnnotation);
            throw e;
        }
    }
}
