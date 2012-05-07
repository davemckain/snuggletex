/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

/**
 * Enumerates the XML serialization methods available.
 * <p>
 * This is based on XSLT 2, but provides two HTML serialization methods - one which
 * keeps non-XHTML in a namespace and one which moves everything into the null namespace.
 * <p>
 * <strong>NOTE:</strong> XHTML is only supported if you are using an XSLT 2.0
 * processor. If not supported, you will get XML output.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public enum SerializationMethod {
    
    /** XML Serialization method */
    XML("xml"),
    
    /** XHTML Serialization method (if available) */
    XHTML("xhtml"),
    
    /** 
     * Default ("Partial") HTML serialization method. 
     * <p>
     * HTML elements are serialized in NO namespace, XML elements in the MathML and other
     * namespaces are kept as-is
     * <p>
     * (This is how you might serialize HTML for IE/MathPlayer, for example)
     * <p>
     * NOTE: The old version of Xalan that comes bundled with Java 6 (and below) appears to be
     * unable to indent HTML output when using {@link XMLStringOutputOptions#setIndenting(boolean)}.
     * I have not checked this behaviour with Java 7 (or above). I however recommend using Saxon, 
     * or including a newer version of Xalan in the ClassPath.
     */
    HTML("html"),
    
    /** 
     * Pure HTML serialization method. 
     * <p>
     * All XML elements have their namespaces removed on serialization. (Hence, there is the
     * potential for name clashes if you use something like XLink & XHTML together.)
     * <p>
     * (This is how you would serialize HTML5, for example.)
     * 
     * @since 1.3.0
     */
    STRICTLY_HTML("html"),
    
    ;
    
    private final String name;
    
    private SerializationMethod(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}