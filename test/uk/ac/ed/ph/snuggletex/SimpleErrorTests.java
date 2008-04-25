/* $Id: SimpleErrorTests.java,v 1.2 2008/04/01 14:13:24 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.SnuggleTeXConfiguration.ErrorOptions;
import uk.ac.ed.ph.snuggletex.definitions.Globals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision: 1.2 $
 */
@RunWith(Parameterized.class)
public class SimpleErrorTests {
    
    private static final Logger logger = Logger.getLogger(SimpleErrorTests.class.getName());
    
    public static final String TEST_RESOURCE_NAME = "simple-error-tests.txt";
    
    @Parameters
    public static Collection<String[]> data() throws Exception {
        return TestFileHelper.readAndParseSingleLineInputTestResource(TEST_RESOURCE_NAME);
    }
    
    private final String inputLaTeX;
    private final String expectedErrorCodeString;
    
    public SimpleErrorTests(final String inputLaTeX, final String expectedErrorCodeString) {
        this.inputLaTeX = inputLaTeX;
        this.expectedErrorCodeString = expectedErrorCodeString;
    }
    
    @Test
    public void runTest() throws Throwable {
        SnuggleTeXConfiguration configuration =  new SnuggleTeXConfiguration();
        configuration.setErrorOptions(ErrorOptions.LIST_AND_XML_FULL);
        
        /* Convert %n in input LaTeX to a newline (cheap hack for simple multi-line inputs!) */
        String inputLaTeXLines = inputLaTeX.replace("%n", "\n");
        
        List<InputError> errors;
        try {
            /* Parse document and build XML */
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document resultDocument = docBuilder.newDocument();
            Element rootElement = resultDocument.createElementNS(Globals.XHTML_NAMESPACE, "body");
            resultDocument.appendChild(rootElement);
            
            errors = SnuggleTeX.snuggle(rootElement, new SnuggleInput(inputLaTeXLines));
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Parsing failed unexpectedly on input " + inputLaTeX, e);
            throw e;
        }

        /* Extract list of error codes */
        String[] actualErrorCodes = new String[errors.size()];
        for (int i=0; i<actualErrorCodes.length; i++) {
            actualErrorCodes[i] = errors.get(i).getErrorCode().name();
        }
        
        /* Work out which error codes we expected */
        String[] expectedErrorCodes = expectedErrorCodeString.trim().split(",\\s*");
        
        /* Now check things */
        if (!Arrays.equals(expectedErrorCodes, actualErrorCodes)) {
            StringBuilder messageBuilder = new StringBuilder("Test failed!\nInput was: ").append(inputLaTeX)
                .append("\nExpected error codes were: ").append(Arrays.toString(expectedErrorCodes))
                .append("\nActual error codes were:   ").append(Arrays.toString(actualErrorCodes));
            for (int i=0; i<errors.size(); i++) {
                messageBuilder.append("\nError ").append(i+1)
                    .append(" was: ")
                    .append(MessageFormatter.formatErrorAsString(errors.get(i)));
            }
            logger.warning(messageBuilder.toString());
            Assert.fail("Failed on " + inputLaTeX);
        }
    }
}
