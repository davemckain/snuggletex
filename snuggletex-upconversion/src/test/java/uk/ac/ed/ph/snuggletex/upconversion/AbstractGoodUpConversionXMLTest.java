/* $Id:MathTests.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import uk.ac.ed.ph.snuggletex.AbstractGoodMathTest;
import uk.ac.ed.ph.snuggletex.AbstractGoodXMLTest;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.definitions.W3CConstants;
import uk.ac.ed.ph.snuggletex.upconversion.internal.UpConversionPackageDefinitions;

import org.w3c.dom.Document;

/**
 * Base class for up-conversion tests. This is pretty much the same as {@link AbstractGoodMathTest}
 * but only wraps the input in "$...$" delimiters if they don't already end with one of these.
 * This allows assumptions to be set up in advance.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
public abstract class AbstractGoodUpConversionXMLTest extends AbstractGoodXMLTest {
    
    public AbstractGoodUpConversionXMLTest(final String inputFragment, final String expectedMathMLContent) {
        super(inputFragment.endsWith("$") ? inputFragment : "$" + inputFragment + "$",
            "<math xmlns='" + W3CConstants.MATHML_NAMESPACE + "'>"
            + expectedMathMLContent.replaceAll("(?m)^\\s+", "").replaceAll("(?m)\\s+$", "").replace("\n", "")
            + "</math>");
    }
    
    @Override
    protected void fixupDocument(Document document) {
        AbstractGoodMathTest.extractMathElement(document);
    }
    
    @Override
    protected SnuggleSession createSnuggleSession() {
        SnuggleEngine engine = new SnuggleEngine();
        engine.addPackage(UpConversionPackageDefinitions.getPackage());
        
        return engine.createSession();
    }
}
