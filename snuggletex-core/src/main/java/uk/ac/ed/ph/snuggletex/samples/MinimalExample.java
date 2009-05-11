/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.samples;

import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleSession;

import java.io.IOException;

/**
 * Example demonstrating a minimal example use of SnuggleTeX.
 * <p>
 * This simply converts a fixed input String of LaTeX to XML. 
 * (In this case, the result is a fragment of MathML.)
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MinimalExample {
    
    public static void main(String[] args) throws IOException {
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        
        SnuggleInput input = new SnuggleInput("$$1+2=3$$");
        session.parseInput(input);
        String xmlString = session.buildXMLString();
        
        System.out.println("Input " + input.getString()
                + " was converted to:\n" + xmlString);
    }
}
