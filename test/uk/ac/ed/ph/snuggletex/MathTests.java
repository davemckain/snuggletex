/* $Id:MathTests.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.definitions.Globals;

import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Set of simple maths-based tests that read their data in from <tt>{@link #TEST_RESOURCE_NAME}</tt>.
 * The input is a single line of LaTeX which will be put into <tt>$...$</tt> and parsed
 * then compared with the given multi-line XML. The enclosing <tt>math</tt> element is
 * automatically added to the XML for convenience. See the sample file for examples.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
@RunWith(Parameterized.class)
public class MathTests extends AbstractGoodXMLTests {
    
    public static final String TEST_RESOURCE_NAME = "math-tests.txt";
    
    @Parameters
    public static Collection<String[]> data() throws Exception {
        return TestFileHelper.readAndParseSingleLineInputTestResource(TEST_RESOURCE_NAME);
    }
    
    public MathTests(final String inputLaTeXMaths, final String expectedMathMLContent) {
        super("$" + inputLaTeXMaths + "$",
                "<math xmlns='" + Globals.MATHML_NAMESPACE + "'>"
                + expectedMathMLContent.replaceAll("(?m)^\\s+", "").replaceAll("(?m)\\s+$", "").replace("\n", "")
                + "</math>");
    }

    /**
     * We'll remove <body> element and make the (single) child become the document Node.
     */
    @Override
    protected void fixupDocument(Document document) {
        /* Should only have 1 child of <body/> here. We'll make that the new root Node */
        Node bodyElement = document.getChildNodes().item(0);
        NodeList childNodes = bodyElement.getChildNodes();
        Assert.assertEquals(1, childNodes.getLength());
        Node newRoot = childNodes.item(0);
        document.removeChild(bodyElement);
        document.appendChild(newRoot);
    }
    
    @Override
    @Test
    public void runTest() throws Throwable {
        super.runTest();
    }
}
