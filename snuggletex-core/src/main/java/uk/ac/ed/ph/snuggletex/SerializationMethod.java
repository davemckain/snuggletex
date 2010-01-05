/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

/**
 * Enumerates the 3 XML serialization methods available, based on XSLT 1.0 & 2.0
 * <p>
 * <strong>NOTE:</strong> XHTML is only supported if you are using an XSLT 2.0
 * processor. If not supported, you will get XML output.
 * 
 * @author  David McKain
 * @version $Revision: 516 $
 */
public enum SerializationMethod {
    
    /** XML Serialization method */
    XML("xml"),
    
    /** XHTML Serialization method (if available) */
    XHTML("xhtml"),
    
    /** HTML serialization method */
    HTML("html"),
    ;
    
    private final String name;
    
    private SerializationMethod(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}