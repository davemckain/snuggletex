/* $Id:MathTests.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import uk.ac.ed.ph.snuggletex.AbstractGoodMathTest;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.MathTests;
import uk.ac.ed.ph.snuggletex.testutil.TestFileHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Same idea as {@link MathTests}, but tests the up-conversion to Content
 * MathML.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
@RunWith(Parameterized.class)
public class MathUpConversionPMathMLTests extends AbstractGoodMathTest {
    
    public static final String TEST_RESOURCE_NAME = "math-upconversion-pmathml-tests.txt";
    
    @Parameters
    public static Collection<String[]> data() throws Exception {
        return TestFileHelper.readAndParseSingleLineInputTestResource(TEST_RESOURCE_NAME);
    }
    
    private final UpConvertingPostProcessor upconverter;
    
    public MathUpConversionPMathMLTests(final String inputLaTeXMaths, final String expectedMathMLContent) {
        super(inputLaTeXMaths, expectedMathMLContent);
        
        /* Set up up-converter so that it only generates fixed up Presentation MathML */
        Map<String, Object> upconversionParameterMap = new HashMap<String, Object>();
        upconversionParameterMap.put(UpConversionParameters.DO_CONTENT_MATHML, Boolean.FALSE);
        upconversionParameterMap.put(UpConversionParameters.DO_MAXIMA, Boolean.FALSE);
        upconverter = new UpConvertingPostProcessor(upconversionParameterMap);
    }

    /**
     * We add in the up-converter, only going as far as Presentation MathML this time.
     */
    @Override
    protected DOMOutputOptions createDOMOutputOptions() {
        DOMOutputOptions result = super.createDOMOutputOptions();
        result.setDOMPostProcessor(upconverter);
        return result;
    }
    
    @Override
    protected boolean showTokensOnFailure() {
        return false;
    }
    
    @Override
    @Test
    public void runTest() throws Throwable {
        super.runTest();
    }
}
