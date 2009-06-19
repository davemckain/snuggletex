/* $Id: MathAnnotationTests.java 302 2009-05-11 13:46:21Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.utilities.SnuggleUtilities;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Tests the generation of MathML annotations.
 * 
 * TODO: Currently the annotations are exactly the same as the input so this could maybe be moved
 * into {@link MathTests}.
 *
 * @author  David McKain
 * @version $Revision: 302 $
 */
public final class MathAnnotationTests {

    /**
     * Tests basic annotations of bog-standard Math regions.
     * 
     * @throws Exception
     */
    @Test
    public void testMathEnvironmentAnnotation() throws Exception {
        doTest("$\\frac{1}{2}$");
    }
    
    @Test
    public void testEqnArrayAnnotation() throws Exception {
        doTest("\\begin{eqnarray*} x &=& 1 \\end{eqnarray*}");
    }
    
    protected void doTest(final String inputMathLaTeXAndExpectedAnnotation) throws Exception {
        doTest(inputMathLaTeXAndExpectedAnnotation, inputMathLaTeXAndExpectedAnnotation);
    }
    
    protected void doTest(final String inputMathLaTeX, final String expectedAnnotation) throws Exception {
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        session.parseInput(new SnuggleInput(inputMathLaTeX));
        
        DOMOutputOptions domOptions = new DOMOutputOptions();
        domOptions.setAddingMathAnnotations(true);
        NodeList result = session.buildDOMSubtree(domOptions);
        
        Assert.assertEquals(1, result.getLength());
        Assert.assertTrue(result.item(0) instanceof Element);
        
        Element mathElement = (Element) result.item(0);
        String annotation = SnuggleUtilities.extractSnuggleTeXAnnotation(mathElement);
        Assert.assertEquals(expectedAnnotation, annotation);
    }
}
