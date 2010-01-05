/* $Id:SessionConfiguration.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

/**
 * Specifies options for how a {@link SnuggleSession} should parse and process
 * {@link SnuggleInput}s.
 *
 * @author  David McKain
 * @version $Revision:179 $
 */
public final class SessionConfiguration implements Cloneable {
   
    /** Set to true to fail immediately on error. Default is to record error but keep going */
    private boolean failingFast;
    
    public SessionConfiguration() {
        this.failingFast = false;
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

    
    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new SnuggleLogicException(e);
        }
    }
}
