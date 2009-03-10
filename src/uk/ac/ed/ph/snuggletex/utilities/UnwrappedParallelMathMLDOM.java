/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.utilities;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Trivial wrapper Object that encapsulates the results of
 * {@link MathMLUtilities#unwrapParallelMathMLDOM(Element)}.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class UnwrappedParallelMathMLDOM {
    
    /** Containing <math/> element */
    private Element mathElement;
    
    /** First branch of the <semantics/> element */
    private Element firstBranch;
    
    /** Map of all <annotation/> contents, keyed on encoding attribute */
    private final Map<String, String> textAnnotations;
    
    /** Map of all <annotation-xml/> contents, keyed on encoding attribute */
    private final Map<String, NodeList> xmlAnnotations;
    
    public UnwrappedParallelMathMLDOM() {
        this.textAnnotations = new HashMap<String, String>();
        this.xmlAnnotations = new HashMap<String, NodeList>();
    }
    
    public Element getMathElement() {
        return mathElement;
    }
    
    public void setMathElement(Element mathElement) {
        this.mathElement = mathElement;
    }


    public Element getFirstBranch() {
        return firstBranch;
    }
    
    public void setFirstBranch(Element firstBranch) {
        this.firstBranch = firstBranch;
    }

    
    public Map<String, String> getTextAnnotations() {
        return textAnnotations;
    }

    
    public Map<String, NodeList> getXmlAnnotations() {
        return xmlAnnotations;
    }
}
