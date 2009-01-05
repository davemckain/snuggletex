/* $Id:MathOperatorInterpretation.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

/**
 * Base interface for a Mathematical operator
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
public interface MathOperatorInterpretation extends MathInterpretation {
    
    MathMLOperator getOperator();
    
}
