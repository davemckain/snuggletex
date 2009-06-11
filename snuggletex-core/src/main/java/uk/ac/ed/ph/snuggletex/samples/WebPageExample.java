/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.samples;

import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptionsTemplates;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.internal.util.IOUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Example demonstrating using SnuggleTeX to create a web page,
 * outputting the resulting XHTML to the console. (This is obviously
 * not very useful!)
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
        SnuggleInput input = new SnuggleInput("$$1+2=3$$");
        session.parseInput(input);
        
        /* Create "options" Object to SnuggleTeX what kind of web page we want. We're going
         * to generate one that will work fine with MOZILLA and tweak a few options, just for
         * fun!
         */
        WebPageOutputOptions options = WebPageOutputOptionsTemplates.createWebPageOptions(WebPageType.MOZILLA);
        options.setTitle("My Web Page");
        options.setAddingTitleHeading(true);
        options.setIndenting(true);
        options.setAddingMathAnnotations(true);
        options.setIncludingStyleElement(false);    
        
        /* Now we ask SnuggleTeX to generate the resulting web page. By default, it outputs to
         * an OutputStream using the UTF-8 encoding, so we'll capture this in a byte array and
         * then read that as a String. (This might seem convoluted, but remember that you'll
         * generally want to write out your web page as bytes to store/send. Printing it out
         * as a String is rather odd when you think about it!) 
         */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        session.writeWebPage(options, outputStream);
        String webPageAsString = IOUtilities.readUnicodeStream(new ByteArrayInputStream(outputStream.toByteArray()));
        
        /* Phew! Now dump it to the console */
        System.out.println("Input " + input.getString()
                + " generated page:\n"
                +  webPageAsString);
    }
}
