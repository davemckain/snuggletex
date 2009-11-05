/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion.internal;

import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

import java.util.Map;

import org.w3c.dom.Element;

/**
 * Trivial "wrapper" for a DOM {@link Element} that makes it suitable for storing
 * in a {@link Map}.
 *
 * @author  David McKain
 * @version $Revision$
 */
final class ElementMapKeyWrapper {
    
    private final Element symbolElement;
    private final int hashCode;
    
    public ElementMapKeyWrapper(final Element symbolElement) {
        this.symbolElement = symbolElement;
        this.hashCode = MathMLUtilities.serializeElement(symbolElement).hashCode();
    }
    
    public Element getSymbolElement() {
        return symbolElement;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==null || !(obj instanceof ElementMapKeyWrapper)) {
            return false;
        }
        ElementMapKeyWrapper other = (ElementMapKeyWrapper) obj;
        return symbolElement.isEqualNode(other.getSymbolElement());
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
}