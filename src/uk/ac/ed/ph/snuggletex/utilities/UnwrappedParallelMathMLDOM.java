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
public class UnwrappedParallelMathMLDOM {
    
    private Element firstBranch;
    private final Map<String, String> textAnnotations;
    private final Map<String, NodeList> xmlAnnotaions;
    
    public UnwrappedParallelMathMLDOM() {
        this.textAnnotations = new HashMap<String, String>();
        this.xmlAnnotaions = new HashMap<String, NodeList>();
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

    
    public Map<String, NodeList> getXmlAnnotaions() {
        return xmlAnnotaions;
    }
}
