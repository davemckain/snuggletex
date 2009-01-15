/* $Id:MathTests.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.extensions.upconversion.UpConversionParameters;
import uk.ac.ed.ph.snuggletex.extensions.upconversion.UpConvertingPostProcessor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Same idea as {@link MathUpConversionTests}, but tests up-conversion of maths.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
@RunWith(Parameterized.class)
public class MathUpConversionTests extends AbstractGoodXMLTests {
    
    public static final String TEST_RESOURCE_NAME = "math-upconversion-tests.txt";
    
    private final UpConvertingPostProcessor upconverter;
    
    @Parameters
    public static Collection<String[]> data() throws Exception {
        return TestFileHelper.readAndParseSingleLineInputTestResource(TEST_RESOURCE_NAME);
    }
    
    public MathUpConversionTests(final String inputLaTeXMaths, final String expectedMathMLContent) {
        super("$" + inputLaTeXMaths + "$",
                "<math xmlns='" + Globals.MATHML_NAMESPACE + "'>"
                + expectedMathMLContent.replaceAll("(?m)^\\s+", "").replaceAll("(?m)\\s+$", "").replace("\n", "")
                + "</math>");
        
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
