/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

/**
 * Public SnuggleTeX-related constants.
 *
 * @author  David McKain
 * @version $Revision$
 */
public interface SnuggleConstants {
    
    /** Namespace for any SnuggleTeX-specific XML elements produced */
    public static final String SNUGGLETEX_NAMESPACE = "http://www.ph.ed.ac.uk/snuggletex";
    
    /** 
     * Value of the "encoding" attribute added to MathML element annotation elements, used
     * when requested.
     */
    public static final String SNUGGLETEX_MATHML_ANNOTATION_ENCODING = "SnuggleTeX";
}
