/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.testutil.TestFileHelper;

import java.util.Collection;

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
public class SimpleErrorTests extends ErrorTestBase {
    
    public static final String TEST_RESOURCE_NAME = "simple-error-tests.txt";
    
    @Parameters
    public static Collection<String[]> data() throws Exception {
        return TestFileHelper.readAndParseSingleLineInputTestResource(TEST_RESOURCE_NAME);
    }
    
    public SimpleErrorTests(final String inputLaTeX, final String expectedErrorCodeStrings) {
        super(inputLaTeX, expectedErrorCodeStrings);
    }
}
