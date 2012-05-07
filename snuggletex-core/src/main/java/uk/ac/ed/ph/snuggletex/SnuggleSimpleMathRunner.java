/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

import java.io.IOException;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class SnuggleSimpleMathRunner {
    
    private final SnuggleSession session;
     
    SnuggleSimpleMathRunner(final SnuggleSession session) {
        this.session = session;
    }
    
    public Element doMathInput(String mathModeInput) {
        return doMathInput(mathModeInput, session.defaultDOMOutputOptions);
    }
    
    public Element doMathInput(String mathModeInput, DOMOutputOptions domOutputOptions) {
        ConstraintUtilities.ensureNotNull(mathModeInput, "Math Mode Input");
        String latexInput  = "\\[" + mathModeInput + "\\]";
        session.reset();
        try {
            session.parseInput(new SnuggleInput(latexInput, "Math Mode Input"));
        }
        catch (IOException e) {
            throw new SnuggleLogicException("Unexpected IOException");
        }
        NodeList nodeList = session.buildDOMSubtree(domOutputOptions);
        if (!session.getErrors().isEmpty()) {
            return null;
        }

        /* Extract and check <math> element */
        Element result = null;
        if (nodeList.getLength()==1) {
            result = (Element) nodeList.item(0);
            if (!MathMLUtilities.isMathMLElement(result, "math")) {
                throw new IllegalArgumentException("Input did not yield exactly 1 <math> element");
            }
        }
        else {
            throw new IllegalArgumentException("Input did not yield exactly 1 result element");
        }
        return result;
    }
    
    public String doMathInput(String mathModeInput, XMLStringOutputOptions xmlStringOutputOptions) {
        Element mathmlElement = doMathInput(mathModeInput, (DOMOutputOptions) xmlStringOutputOptions);
        return mathmlElement!=null ? MathMLUtilities.serializeElement(mathmlElement, xmlStringOutputOptions) : null;
    }
    
    public List<InputError> getLastErrors() {
        return session.getErrors();
    }
}
