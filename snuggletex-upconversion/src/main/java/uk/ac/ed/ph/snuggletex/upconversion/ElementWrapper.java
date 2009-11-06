/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

import java.util.Map;

import org.w3c.dom.Element;

/**
 * Trivial "wrapper" for a DOM {@link Element} that makes it suitable for storing
 * in a {@link Map}.
 * 
 * @since 1.2.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class ElementWrapper {
    
    private final Element symbolElement;
    private final int hashCode;
    
    public ElementWrapper(final Element symbolElement) {
        this.symbolElement = symbolElement;
        this.hashCode = MathMLUtilities.serializeElement(symbolElement).hashCode();
    }
    
    public Element getSymbolElement() {
        return symbolElement;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==null || !(obj instanceof ElementWrapper)) {
            return false;
        }
        ElementWrapper other = (ElementWrapper) obj;
        return symbolElement.isEqualNode(other.getSymbolElement());
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
}