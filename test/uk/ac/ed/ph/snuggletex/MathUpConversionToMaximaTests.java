/* $Id:MathTests.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.extensions.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.extensions.upconversion.UpConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

import java.util.Collection;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Same idea as {@link MathTests}, but tests the initial up-conversion to more
 * semantic Presentation MathML.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
@RunWith(Parameterized.class)
public class MathUpConversionToMaximaTests {
    
    public static final String TEST_RESOURCE_NAME = "math-upconversion-maxima-tests.txt";
    
    private static final Logger log = Logger.getLogger(MathUpConversionToMaximaTests.class.getName());
    
    private final String inputLaTeX;
    private final String expectedMaxima;
    
    public MathUpConversionToMaximaTests(final String inputLaTeX, final String expectedMaxima) {
        this.inputLaTeX = inputLaTeX;
        this.expectedMaxima = expectedMaxima;
    }
    
    @Parameters
    public static Collection<String[]> data() throws Exception {
        return TestFileHelper.readAndParseSingleLineInputTestResource(TEST_RESOURCE_NAME);
    }

    @Test
    public void runTest() throws Throwable {
        String maximaAnnotation = null;

        /* We'll fail fast as we're not anticipating any errors */
        SessionConfiguration configuration = new SessionConfiguration();
        configuration.setFailingFast(true);
        
        SnuggleSession session = new SnuggleEngine().createSession(configuration);
        try {
            /* Parse input */
            session.parseInput(new SnuggleInput("$" + inputLaTeX + "$"));
            
            /* Up-convert and build DOM */
            DOMOutputOptions domOptions = new DOMOutputOptions();
            domOptions.setDOMPostProcessor(new UpConvertingPostProcessor());
            NodeList nodeList = session.buildDOMSubtree(domOptions);
            
            /* First Node should be MathML element. */
            Assert.assertEquals(1, nodeList.getLength());
            Element mathmlElement = (Element) nodeList.item(0);
            
            /* Extract Maxima annotation */
            maximaAnnotation = MathMLUtilities.extractAnnotationString(mathmlElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME);
            
            /* Compare with expected */
            Assert.assertEquals(expectedMaxima, maximaAnnotation);
        }
        catch (Throwable e) {
            log.severe("Input was: " + inputLaTeX);
            log.severe("Resulting Maxima annotation was: " + maximaAnnotation);
            log.severe("Expected Maxima would have been: " + expectedMaxima);
            throw e;
        }
    }


}
