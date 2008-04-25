/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Entry point for the SnuggleTeX Test Suite
 *
 * @author  David McKain
 * @version $Revision$
 */
@RunWith(Suite.class)
@SuiteClasses({
    SimpleErrorTests.class,
    LineTests.class,
    MathTests.class,
    MultiLineTests.class,
    DefinitionMapTest.class
})
public final class SnuggleTeXTestSuite {
    
    /* (Test classes are declared in the class annotation above) */

}
