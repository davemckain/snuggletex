/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion.samples;

import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.XMLStringOutputOptions;
import uk.ac.ed.ph.snuggletex.upconversion.UpConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.upconversion.internal.UpConversionPackageDefinitions;

import java.io.IOException;

/**
 * Basic example of up-converting some simple LaTeX input to Content MathML and Maxima forms.
 * 
 * <h2>Running Notes</h2>
 * 
 * You will need the following in your ClassPath:
 * 
 * <ul>
 *   <li>snuggletex-core.jar</li>
 *   <li>snuggletex-upconversion.jar</li>
 *   <li>saxon9.jar, saxon9-dom.jar</li> (These are required as the conversion process uses XSLT 2.0)
 * </ul>
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public final class BasicUpConversionExample {
    
    public static void main(String[] args) throws IOException {
        /* We will up-convert this LaTeX input */
        String input = "$$ \\frac{2x-y^2}{\\sin xy(x-2)} $$";
        
        /* Set up SnuggleEngine, remembering to register package providing up-conversion support */
        SnuggleEngine engine = new SnuggleEngine();
        engine.addPackage(UpConversionPackageDefinitions.getPackage());
        
        /* Create session in usual way */
        SnuggleSession session = engine.createSession();
        
        /* Parse input. I won't bother checking it here */
        session.parseInput(new SnuggleInput(input));
        
        /* We are going to up-convert, creating a simple XML String output.
         * For that, we use DOMOutputOptions and pass an UpConvertingPostProcessor
         * to it, which hooks in the up-conversion process.
         * 
         * Note that the constructor for UpConvertingPostProcesor allows us to
         * pass in options controlling the process; we'll use the (sensible)
         * defaults here.
         */
        UpConvertingPostProcessor upConverter = new UpConvertingPostProcessor();
        XMLStringOutputOptions xmlStringOutputOptions = new XMLStringOutputOptions();
        xmlStringOutputOptions.addDOMPostProcessors(upConverter);
        xmlStringOutputOptions.setIndenting(true);
        
        /* Do the up-conversion process */
        String result = session.buildXMLString(xmlStringOutputOptions);
        System.out.println("Up-Conversion process generated: " + result);
    }
}
