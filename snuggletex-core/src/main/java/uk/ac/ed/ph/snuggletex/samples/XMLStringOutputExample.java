/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.samples;

import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.XMLStringOutputOptions;

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
public final class XMLStringOutputExample {
    
    public static void main(String[] args) throws IOException {
        /* Create vanilla SnuggleEngine and new SnuggleSession */
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        
        /* Parse some very basic Math Mode input */
        SnuggleInput input = new SnuggleInput("$$\\alpha \\cap \\beta$$");
        session.parseInput(input);
        
        XMLStringOutputOptions options = new XMLStringOutputOptions();
        options.setEncoding("UTF-8");
        if (engine.getStylesheetManager().supportsXSLT20()) {
            /* User has an XSLT 2.0 processor, so let's output named entities for readability */
            options.setUsingNamedEntities(true);
        }
        
        /* Convert the results to an XML String, which in this case will
         * be a single MathML <math>...</math> element. */
        String xmlString = session.buildXMLString(options);
        System.out.println("Input " + input.getString()
                + " was converted to:\n" + xmlString);
    }
}
