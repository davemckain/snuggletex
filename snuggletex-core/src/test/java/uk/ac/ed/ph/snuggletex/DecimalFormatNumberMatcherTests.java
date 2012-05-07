/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.internal.WorkingDocument;
import uk.ac.ed.ph.snuggletex.testutil.TestUtilities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the {@link DecimalFormatNumberMatcher} class
 *
 * @author  David McKain
 * @version $Revision$
 */
@RunWith(Parameterized.class)
public class DecimalFormatNumberMatcherTests {
    
    private static Object[][] rawData = new Object[][] {
            /* (These all use UK locale) */
            { "x", 0, -1 },
            { "1", 0, 1 },
            { "10", 0, 2 },
            { "1x", 0, 1 },
            { "-1", 0, -1 }, /* Not allowing negatives */
            { "1.", 0, 2 },
            { "1.1", 0, 3 },
            { ".23", 0, 3 },
            { "123", 0, 3 },
            { "1,2", 0, 1 }, /* Grouping is turned off */
            { "12345678901234567890123456789012345678901234567890", 0, 50 }, /* Big number */
    };
    
    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> result = new ArrayList<Object[]>();
        for (int i=0; i<rawData.length; i++) {
            result.add(rawData[i]);
        }
        return result;
    }
    
    private final String input;
    private final int startIndex;
    private final int expectedEndIndex;
    
    public DecimalFormatNumberMatcherTests(String input, int startIndex, int expectedEndIndex) {
        this.input = input;
        this.startIndex = startIndex;
        this.expectedEndIndex = expectedEndIndex;
    }
    
    @Test
    public void runTest() throws Throwable {
        WorkingDocument document = TestUtilities.createWorkingDocument(input);
        DecimalFormatNumberMatcher matcher = new DecimalFormatNumberMatcher();
        
        /* Use UK locale here */
        DecimalFormat format = new DecimalFormat();
        format.setGroupingUsed(false);
        format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.UK));
        matcher.setDecimalFormat(format);
        
        Assert.assertEquals(expectedEndIndex, matcher.getNumberEnd(document, startIndex));
    }

}
