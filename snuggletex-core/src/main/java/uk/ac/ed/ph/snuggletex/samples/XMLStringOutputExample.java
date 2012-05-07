/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.samples;

import uk.ac.ed.ph.snuggletex.SerializationMethod;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.XMLStringOutputOptions;

import java.io.IOException;

/**
 * This example generalises {@link MinimalExample} to create a slightly
 * more interesting output.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class XMLStringOutputExample {
    
    public static void main(String[] args) throws IOException {
        /* Create vanilla SnuggleEngine and new SnuggleSession */
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        
        /* Parse some LaTeX input */
        SnuggleInput input = new SnuggleInput("\\section*{The quadratic formula}"
                + "$$ \\frac{-b \\pm \\sqrt{b^2-4ac}}{2a} $$");
        session.parseInput(input);
        
        /* Specify how we want the resulting XML */
        XMLStringOutputOptions options = new XMLStringOutputOptions();
        options.setSerializationMethod(SerializationMethod.XHTML);
        options.setIndenting(true);
        options.setEncoding("UTF-8");
        options.setAddingMathSourceAnnotations(true);
        options.setUsingNamedEntities(true); /* (Only used if caller has an XSLT 2.0 processor) */
        
        /* Convert the results to an XML String, which in this case will
         * be a single MathML <math>...</math> element. */
        System.out.println(session.buildXMLString(options));
    }
}
