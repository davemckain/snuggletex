/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * Implementation of {@link NumberMatcher} that uses a standard Java {@link DecimalFormat} to
 * match numbers.
 * <p>
 * By default, this uses the default {@link DecimalFormat} with the platform default {@link Locale}.
 * <p>
 * You will probably want your {@link DecimalFormat} to have grouping turned off, otherwise
 * something like <tt>1,2</tt> (in UK locale) would be parsed as a number 12.
 * 
 * @since 1.3.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public class DecimalFormatNumberMatcher implements NumberMatcher {
    
    private static final int READAHEAD_INCREMENT = 4;
    
    private DecimalFormat decimalFormat;
    
    public DecimalFormatNumberMatcher() {
        this(Locale.getDefault());
    }
    
    public DecimalFormatNumberMatcher(Locale locale) {
        this.decimalFormat = new DecimalFormat();
        decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(locale));
        decimalFormat.setGroupingUsed(false);
    }
    
    public DecimalFormatNumberMatcher(DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
    }
    
    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    public void setDecimalFormat(DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
    }
    
    //-------------------------------------------------------------------

    public int getNumberEnd(InputContext input, int startIndex) {
        decimalFormat.setParseBigDecimal(true);
        
        /* Since the number can potentially be quite long, we'll analyse a few characters
         * at a time, extending until we have parsed the entire number.
         */
        int maxReadAhead = input.length() - startIndex;
        int readAhead = Math.min(READAHEAD_INCREMENT, maxReadAhead);
        int endIndex = -1;
        if (readAhead > 0) {
            while (true) {
                String candidate = input.extract(startIndex, startIndex + readAhead).toString();
                ParsePosition parsePosition = new ParsePosition(0);
                BigDecimal parsed = (BigDecimal) decimalFormat.parse(candidate, parsePosition);
                if (parsed==null) {
                    /* Not a number */
                    break;
                }
                else if (parsed.compareTo(BigDecimal.ZERO) < 0) {
                    /* Negative numbers are treated separately */
                    break;
                }
                else if (parsePosition.getIndex()==readAhead && readAhead < maxReadAhead) {
                    /* Need to parse a bit more... */
                    readAhead = Math.min(readAhead + READAHEAD_INCREMENT, maxReadAhead);
                    continue;
                }
                else {
                    /* Got end of number */
                    endIndex = startIndex + parsePosition.getIndex();
                    break;
                }
            }
        }
        return endIndex;
    }
}
