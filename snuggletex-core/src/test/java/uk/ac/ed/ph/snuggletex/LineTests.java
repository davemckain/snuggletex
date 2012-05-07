/* $Id:LineTests.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.definitions.W3CConstants;
import uk.ac.ed.ph.snuggletex.testutil.SnuggleTeXTestDriver;
import uk.ac.ed.ph.snuggletex.testutil.SnuggleTeXTestDriver.DriverCallback;
import uk.ac.ed.ph.snuggletex.testutil.TestFileHelper;
import uk.ac.ed.ph.snuggletex.testutil.TestUtilities;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;

/**
 * Set of simple tests that read in data from <tt>{@link #TEST_RESOURCE_NAME}</tt>.
 * These all take one input line, parse and compare with XML, which
 * may be specified on multiple input lines for convenience.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
@RunWith(Parameterized.class)
public class LineTests implements DriverCallback {
    
    public static final String TEST_RESOURCE_NAME = "line-tests.txt";
    
    @Parameters
    public static Collection<String[]> data() throws Exception {
        return TestFileHelper.readAndParseSingleLineInputTestResource(TEST_RESOURCE_NAME);
    }
    
    private final String inputLaTeX;
    private final String expectedXML;
    
    public LineTests(final String inputLaTeX, final String expectedXMLContent) {
        this.inputLaTeX = inputLaTeX;
        this.expectedXML = "<body xmlns='" + W3CConstants.XHTML_NAMESPACE + "'>"
                + expectedXMLContent.replaceAll("(?m)^ +", "").replaceAll("(?m) +$", "").replace("\n", "")
                + "</body>";
    }
    
    @Test
    public void runTest() throws Throwable {
        SnuggleEngine engine = new SnuggleEngine();
        
        SnuggleTeXTestDriver caller = new SnuggleTeXTestDriver(engine, this);
        
        caller.run(inputLaTeX);
    }
    
    public void verifyDOM(Document document) throws Throwable {
        /* Check XML verifies against what we expect */
        TestUtilities.verifyXML(expectedXML, document);
    }
    
}
