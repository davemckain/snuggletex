/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

/**
 * Interface for classes which identify numbers in Math mode input.
 * <p>
 * The default implementation is {@link SimpleNumberMatcher}, which will probably be fine
 * for Western use. Use {@link DecimalFormatNumberMatcher} if you need something more complex,
 * or create your own implementation if required.
 * 
 * @see SimpleNumberMatcher
 * @see DecimalFormatNumberMatcher
 * 
 * @since 1.3.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public interface NumberMatcher {
    
    /**
     * Implementations should analyse the given {@link InputContext} starting at the given
     * index to see if it looks like an (unsigned) number. If it is a number, return the
     * index of the next non-number character. Otherwise, return -1.
     */
    int getNumberEnd(InputContext input, int startIndex);

}
