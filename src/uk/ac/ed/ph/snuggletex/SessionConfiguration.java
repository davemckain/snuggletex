/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

/**
 * Specifies options for how a {@link SnuggleSession} should parse and process
 * {@link SnuggleInput}s.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class SessionConfiguration implements Cloneable {
   
    /** Set to true to fail immediately on error. Default is to record error but keep going */
    private boolean failingFast;
    
    /** Set to true to perform limited semantic inferences on MathML elements. */
    private boolean inferringMathStructure;
    
    public SessionConfiguration() {
        this.failingFast = false;
        this.inferringMathStructure = false;
    }
    
    public boolean isFailingFast() {
        return failingFast;
    }
    
    public void setFailingFast(boolean failingFast) {
        this.failingFast = failingFast;
    }


    public boolean isInferringMathStructure() {
        return inferringMathStructure;
    }
    
    public void setInferringMathStructure(boolean inferringMathStructure) {
        this.inferringMathStructure = inferringMathStructure;
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
