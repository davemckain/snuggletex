/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.definitions.Globals;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;

/**
 * FIXME: Document this type!
 * 
 * @author  David McKain
 * @version $Revision$
 */
@RunWith(Parameterized.class)
public class LineTests extends AbstractGoodXMLTests {
    
    public static final String TEST_RESOURCE_NAME = "line-tests.txt";
    
    @Parameters
    public static Collection<String[]> data() throws Exception {
        return TestFileHelper.readAndParseSingleLineInputTestResource(TEST_RESOURCE_NAME);
    }
    
    public LineTests(final String inputLaTeX, final String expectedXML) {
        super(inputLaTeX, "<body xmlns='" + Globals.XHTML_NAMESPACE + "'>"
                + expectedXML.replaceAll("(?m)^\\s+", "").replaceAll("(?m)\\s+$", "").replace("\n", "")
                + "</body>"
        );
    }
    
    @Override
    protected void fixupDocument(Document document) {
        /* Nothing to do */
    }
    
    @Override
    @Test
    public void runTest() throws Throwable {
        super.runTest();
    }
}
