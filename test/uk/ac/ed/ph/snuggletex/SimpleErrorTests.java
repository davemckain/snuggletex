/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Set of tests defined in <tt>{@link #TEST_RESOURCE_NAME}</tt> that take single line
 * inputs in the hope of generating errors. These are then compared against the
 * specified list of errors. See the input file for examples.
 *
 * @author  David McKain
 * @version $Revision$
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
        /* Convert %n in input LaTeX to a newline (cheap hack for simple multi-line inputs!) */
        String inputLaTeXLines = inputLaTeX.replace("%n", "\n");
        
        /* Parse document and build XML */
        SnuggleSession session = new SnuggleEngine().createSession();
        try {
            session.parseInput(new SnuggleInput(inputLaTeXLines));
            session.buildXMLString();
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Parsing failed unexpectedly on input " + inputLaTeX, e);
            throw e;
        }
        /* Extract list of error codes */
        List<InputError> errors = session.getErrors();
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
