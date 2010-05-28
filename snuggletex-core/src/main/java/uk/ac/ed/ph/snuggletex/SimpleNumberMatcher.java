/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Very basic but fast implementation of {@link NumberMatcher} that handles western-style
 * numeric characters, supporting Locale-specific decimal points.
 * 
 * @since 1.3.0
 * 
 * @see DecimalFormatNumberMatcher
 *
 * @author  David McKain
 * @version $Revision$
 */
public class SimpleNumberMatcher implements NumberMatcher {
    
    private final char decimalChar;
    
    public SimpleNumberMatcher() {
        this(Locale.getDefault());
    }
    
    public SimpleNumberMatcher(Locale locale) {
        this.decimalChar = new DecimalFormatSymbols(locale).getDecimalSeparator();
    }
    
    public int getNumberEnd(InputContext input, int startIndex) {
        int index = startIndex; /* Current number search index */
        int c;
        boolean foundDigitsBeforeDecimalPoint = false;
        boolean foundDigitsAfterDecimalPoint  = false;
        boolean foundDecimalPoint = false;
        
        /* Read zero or more digits */
        while(true) {
            c = input.charAt(index);
            if (c>='0' && c<='9') {
                foundDigitsBeforeDecimalPoint = true;
                index++;
            }
            else {
                break;
            }
        }
        /* Maybe read decimal point */
        if (input.charAt(index)==decimalChar) {
            /* Found leading decimal point, so only allow digits afterwards */
            foundDecimalPoint = true;
            index++;
        }
        /* Bail out if we didn't find a number before and didn't find a decimal point */
        if (!foundDigitsBeforeDecimalPoint && !foundDecimalPoint) {
            return -1;
        }
        /* Read zero or more digits */
        while(true) {
            c = input.charAt(index);
            if (c>='0' && c<='9') {
                foundDigitsAfterDecimalPoint = true;
                index++;
            }
            else {
                break;
            }
        }
        /* Make sure we read in some number! */
        if (!foundDigitsBeforeDecimalPoint && !foundDigitsAfterDecimalPoint) {
            return -1;
        }
        return index;
    }

}
