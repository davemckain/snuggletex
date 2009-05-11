/* $Id:SnuggleTeXTestSuite.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Entry point for the SnuggleTeX Up-Conversion Test Suite
 *
 * @author  David McKain
 * @version $Revision:179 $
 */
@RunWith(Suite.class)
@SuiteClasses({
    MathUpConversionPMathMLTests.class,
    MathUpConversionCMathMLTests.class,
    MathUpConversionToMaximaTests.class
})
public final class UpConversionTestSuite {
    
    /* (Test classes are declared in the class annotation above) */

}
