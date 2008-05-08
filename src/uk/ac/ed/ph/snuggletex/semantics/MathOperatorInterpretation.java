/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

/**
 * Base interface for a Mathematical operator
 * 
 * @author  David McKain
 * @version $Revision$
 */
public interface MathOperatorInterpretation extends MathInterpretation {
    
    MathMLOperator getOperator();
    
}
