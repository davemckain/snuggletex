/* $Id:SessionConfiguration.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;

/**
 * Specifies options for how a {@link SnuggleSession} should parse and process
 * {@link SnuggleInput}s.
 *
 * @author  David McKain
 * @version $Revision:179 $
 */
public final class SessionConfiguration implements Cloneable {
    
    public static final int DEFAULT_EXPANSION_LIMIT = 100;
   
    /** Set to true to fail immediately on error. Default is to record error but keep going */
    private boolean failingFast;
    
    /** 
     * Maximum depth when expanding out user-defined commands and environments.
     * This prevents possible infinite recursion issues, which are hard to detect due to the 
     * highly dynamic nature of LaTeX input.
     * <p>
     * The default value is {@link #DEFAULT_EXPANSION_LIMIT}. 
     * Set this to 0 or less to disable this safeguard (and risk possible consequences,
     * such as the stack or heap being eaten up).
     * <p>
     * Evaluating a user-defined command body, or the begin/end clause of an environment increases
     * the current depth by 1.
     */
    private int expansionLimit;
    
    /**
     * Matcher used to identify numbers in math mode input.
     */
    private NumberMatcher numberMatcher;
    
    public SessionConfiguration() {
        this.failingFast = false;
        this.expansionLimit = DEFAULT_EXPANSION_LIMIT;
        this.numberMatcher = null;
    }
    
    /**
     * Returns whether or not the {@link SnuggleSession} will fail immediately on error, or
     * keep going.
     * 
     * @return true to fail immediately, false to keep going
     */
    public boolean isFailingFast() {
        return failingFast;
    }
    
    /**
     * Sets whether or not the {@link SnuggleSession} will fail immediately on error, or
     * keep going.
     * 
     * @param failingFast true to fail immediately, false to keep going
     */
    public void setFailingFast(boolean failingFast) {
        this.failingFast = failingFast;
    }
    

    /** 
     * Gets the maximum depth when expanding out user-defined commands and environments.
     * This prevents possible infinite recursion issues, which are hard to detect due to the 
     * highly dynamic nature of LaTeX input.
     * <p>
     * The default value is {@link #DEFAULT_EXPANSION_LIMIT}. 
     * Set this to 0 or less to disable this safeguard (and risk possible consequences,
     * such as the stack or heap being eaten up).
     * <p>
     * Evaluating a user-defined command body, or the begin/end clause of an environment increases
     * the current depth by 1.
     */
    public int getExpansionLimit() {
        return expansionLimit;
    }
    
    /** 
     * Sets the maximum depth when expanding out user-defined commands and environments.
     * This prevents possible infinite recursion issues, which are hard to detect due to the 
     * highly dynamic nature of LaTeX input.
     * <p>
     * The default value is {@link #DEFAULT_EXPANSION_LIMIT}. 
     * Set this to 0 or less to disable this safeguard (and risk possible consequences,
     * such as the stack or heap being eaten up).
     * <p>
     * Evaluating a user-defined command body, or the begin/end clause of an environment increases
     * the current depth by 1.
     */
    public void setExpansionLimit(int expansionLimit) {
        this.expansionLimit = expansionLimit;
    }
    
    
    /**
     * Gets the {@link NumberMatcher} used to identify numbers in Math mode input.
     * Returns null if a default {@link SimpleNumberMatcher} is being used.
     * 
     * @since 1.3.0
     */
    public NumberMatcher getNumberMatcher() {
        return numberMatcher;
    }
    
    /**
     * Sets the {@link NumberMatcher} used to identify numbers in Math mode input.
     * <p>
     * This may be null, which results in a default {@link SimpleNumberMatcher} being used.
     * 
     * @since 1.3.0
     */
    public void setNumberMatcher(NumberMatcher numberMatcher) {
        this.numberMatcher = numberMatcher;
    }


    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new SnuggleLogicException(e);
        }
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
