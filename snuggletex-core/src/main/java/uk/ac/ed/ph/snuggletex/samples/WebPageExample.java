/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.samples;

import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptionsBuilder;

import java.io.IOException;

/**
 * Example demonstrating using SnuggleTeX to create an HTML 5 + MathJax web page,
 * outputting the resulting web page content to the console.
 * 
 * @see MinimalExample
 * @see XMLStringOutputExample
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class WebPageExample {
    
    public static void main(String[] args) throws IOException {
        /* Create vanilla SnuggleEngine and new SnuggleSession */
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        
        /* Parse some very basic Math Mode input */
        SnuggleInput input = new SnuggleInput("$$ a^2 = b^2 + c^2 $$");
        session.parseInput(input);
        
        /* Create "options" Object to SnuggleTeX what kind of web page we want. We're going
         * to generate one that will work fine with MOZILLA and tweak a few options, just for
         * fun!
         */
        WebPageOutputOptions options = WebPageOutputOptionsBuilder.createHTML5MathJaxOptions();
        options.setTitle("My Web Page");
        options.setAddingTitleHeading(true);
        options.setIndenting(true);
        options.setAddingMathSourceAnnotations(true);
        options.setIncludingStyleElement(false);
        
        /* Now ask SnuggleTeX to write the resulting output to the console.
         * (You would normally send the output somewhere more interesting, though!) */
        session.writeWebPage(options, System.out);
    }
}
