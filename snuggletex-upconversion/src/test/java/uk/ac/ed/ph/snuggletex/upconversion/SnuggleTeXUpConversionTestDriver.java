/* $Id: SnuggleTeXTestDriver.java 702 2011-03-11 20:11:27Z davemckain $
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.testutil.TestUtilities;
import uk.ac.ed.ph.snuggletex.upconversion.internal.UpConversionPackageDefinitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Helper class to drive tests that expect successful parsing and DOM Building,
 * carefully logging things if they go wrong.
 *
 * @author  David McKain
 * @version $Revision: 702 $
 */
public final class SnuggleTeXUpConversionTestDriver {
    
    private static final Logger log = Logger.getLogger(SnuggleTeXUpConversionTestDriver.class.getName());
    
    public static interface DriverCallback {
        void verifyErrorFreeDOM(Document document) throws Throwable;
    }
    
    private final UpConversionOptions upConversionOptions;
    private final DriverCallback driverCallback;
    
    public SnuggleTeXUpConversionTestDriver(final UpConversionOptions upConversionOptions, final DriverCallback driverCallback) {
        this.upConversionOptions = upConversionOptions;
        this.driverCallback = driverCallback;
    }
    
    public void run(String inputMathFragment, String expectedResult) throws Throwable {
        try {
            doRun(inputMathFragment, expectedResult);
        }
        catch (Throwable e) {
            log.severe("^^^ Failure on input " + inputMathFragment);
            log.log(Level.SEVERE, "Error thrown was: ", e);
            log.severe("-------------------------------------------");
            throw e;
        }
    }
    
    private void doRun(String inputMathFragment, String expectedResult) throws Throwable {
        String inputLaTeX = inputMathFragment.endsWith("$") ? inputMathFragment : "$" + inputMathFragment + "$";
        Document document;
        
        /* We're going to assume that the input parses OK, so don't need to go into
         * as much detail as the driver for the core tests.
         */
        SnuggleEngine engine = new SnuggleEngine();
        engine.addPackage(UpConversionPackageDefinitions.getPackage());
        
        SnuggleSession session = engine.createSession();
        session.parseInput(new SnuggleInput(TestUtilities.massageInputLaTeX(inputLaTeX)));
        
        DOMOutputOptions domOutputOptions = new DOMOutputOptions();
        domOutputOptions.setMathVariantMapping(true);
        domOutputOptions.setPrefixingSnuggleXML(true);
        domOutputOptions.setDOMPostProcessors(new UpConvertingPostProcessor(upConversionOptions));
        
        /* Build DOM under a fake <temp/> element */
        document = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        Element rootElement = document.createElement("temp");
        document.appendChild(rootElement);
        session.buildDOMSubtree(rootElement, domOutputOptions);
        
        /* Make sure nothing went wrong during parsing or DOM Building */
        TestUtilities.assertNoErrors(session);
        
        /* Replace <temp/> with the single <math/> element we should have got */
        TestUtilities.promoteMathElement(document);
        
        /* Extract any up-conversion failures */
        List<UpConversionFailure> upConversionFailures = UpConversionUtilities.extractUpConversionFailures(document);
        List<String> upConversionFailureCodes = new ArrayList<String>();
        for (UpConversionFailure failure : upConversionFailures) {
            upConversionFailureCodes.add(failure.getErrorCode().toString());
        }

        /* See if caller expected errors by specifying output of the form !CODE, CODE, ... */
        String[] expectedFailureCodes = new String[0];
        if (expectedResult.length()>0 && expectedResult.charAt(0)=='!') {
            expectedFailureCodes = expectedResult.substring(1).split(",\\s*");
        }
        
        /* Now handle each case as appropriate */
        if (expectedFailureCodes.length==0) {
            /* Expected success */
            if (!upConversionFailures.isEmpty()) {
                log.severe("Expected no up-conversion failures but got " + upConversionFailures.size());
                log.severe("Actual failure codes were:   " + upConversionFailureCodes);
                Assert.assertTrue(upConversionFailures.isEmpty());            
            }
            
            /* Do RELAX-NG validation on the MathML */
            TestUtilities.assertMathMLValid(document);
            
            /* Now get caller to do verification */
            driverCallback.verifyErrorFreeDOM(document);
        }
        else {
            /* Expected failure */
            try {
                Assert.assertEquals(expectedFailureCodes.length, upConversionFailures.size());
                for (int i=0; i<expectedFailureCodes.length; i++) {
                    Assert.assertEquals(expectedFailureCodes[i], upConversionFailures.get(i).getErrorCode().toString());
                }
            }
            catch (Throwable e) {
                log.severe("Expected " + expectedFailureCodes.length + " up-conversion failures but got " + upConversionFailures.size());
                log.severe("Expected failure codes were: " + Arrays.toString(expectedFailureCodes));
                log.severe("Actual failure codes were:   " + upConversionFailureCodes);
                throw e;
            }
        }
    }
}
