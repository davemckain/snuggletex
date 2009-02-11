/* $Id:MathTests.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.definitions.Globals;

import junit.framework.Assert;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base class for tests which read in LaTeX and parse it in Math mode, expecting to get a single
 * <math/> element in the resulting DOM.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
abstract class AbstractGoodMathTests extends AbstractGoodXMLTests {
    
    public AbstractGoodMathTests(final String inputLaTeXMaths, final String expectedMathMLContent) {
        super("$" + inputLaTeXMaths + "$",
                "<math xmlns='" + Globals.MATHML_NAMESPACE + "'>"
                + expectedMathMLContent.replaceAll("(?m)^\\s+", "").replaceAll("(?m)\\s+$", "").replace("\n", "")
                + "</math>");
    }
    
    @Override
    protected void fixupDocument(Document document) {
        /* Should only have 1 child of doc root (<body/>) element here, which should be <math/>.
         * We'll make that the new root Node */
        Node rootElement = document.getChildNodes().item(0);
        NodeList childNodes = rootElement.getChildNodes();
        Assert.assertEquals(1, childNodes.getLength());
        Node newRoot = childNodes.item(0);
        
        Assert.assertEquals(Node.ELEMENT_NODE, newRoot.getNodeType());
        Assert.assertEquals("math", newRoot.getNodeName());
        Assert.assertEquals(Globals.MATHML_NAMESPACE, newRoot.getNamespaceURI());
        
        document.removeChild(rootElement);
        document.appendChild(newRoot);
    }
}
