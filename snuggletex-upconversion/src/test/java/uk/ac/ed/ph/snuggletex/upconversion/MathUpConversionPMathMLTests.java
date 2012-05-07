/* $Id:MathTests.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import uk.ac.ed.ph.snuggletex.MathTests;
import uk.ac.ed.ph.snuggletex.testutil.TestFileHelper;
import uk.ac.ed.ph.snuggletex.testutil.TestUtilities;
import uk.ac.ed.ph.snuggletex.upconversion.SnuggleTeXUpConversionTestDriver.DriverCallback;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;

/**
 * Same idea as {@link MathTests}, but tests the up-conversion to Content
 * MathML.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
@RunWith(Parameterized.class)
public class MathUpConversionPMathMLTests implements DriverCallback {
    
    public static final String TEST_RESOURCE_NAME = "math-upconversion-pmathml-tests.txt";
    
    @Parameters
    public static Collection<String[]> data() throws Exception {
        return TestFileHelper.readAndParseSingleLineInputTestResource(TEST_RESOURCE_NAME);
    }
    
    private final String inputLaTeX;
    private final String expectedResult;
    private final String expectedMathML;
    
    public MathUpConversionPMathMLTests(final String inputFragment, final String expectedMathMLContent) {
        this.inputLaTeX = inputFragment;
        this.expectedResult = expectedMathMLContent;
        this.expectedMathML = TestUtilities.wrapMathMLTestData(expectedMathMLContent);
    }
    
    @Test
    public void runTest() throws Throwable {
        /* Set up up-converter so that it only generates fixed up Presentation MathML */
        UpConversionOptions upConversionOptions = new UpConversionOptions();
        upConversionOptions.setSpecifiedOption(UpConversionOptionDefinitions.DO_CONTENT_MATHML_NAME, "false");
        upConversionOptions.setSpecifiedOption(UpConversionOptionDefinitions.DO_MAXIMA_NAME, "false");
        
        SnuggleTeXUpConversionTestDriver driver = new SnuggleTeXUpConversionTestDriver(upConversionOptions, this);
        driver.run(inputLaTeX, expectedResult);        
    }
    
    public void verifyErrorFreeDOM(Document document) throws Throwable {
        TestUtilities.verifyXML(expectedMathML, document);
    }
}
