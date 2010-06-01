/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.internal.SnuggleInputReader;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.internal.WorkingDocument;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some random utility methods for tests. (Also useful for standalone messing about with
 * certain classes.)
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class TestUtilities {
    
    public static final WorkingDocument createWorkingDocument(String input)
            throws IOException, SnuggleParseException {
        SnuggleInput snuggleInput = new SnuggleInput(input);
        SnuggleEngine engine = new SnuggleEngine();
        return new SnuggleInputReader(engine.createSession(), snuggleInput).createWorkingDocument();
    }

    public static String massageInputLaTeX(String inputLaTeX) {
        /* Allow %n for newlines */
        String result = inputLaTeX.replace("%n", "\n");
        
        /* Allow %unn up to %unnnnn for arbitrary Unicode characters */
        Pattern unicodePattern = Pattern.compile("%u([0-9a-fA-F]{2,5})");
        Matcher matcher = unicodePattern.matcher(result);
        StringBuffer resultBuilder = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            /* (Note that this may go past U+FFFF so require multiple UTF-16 chars) */
            matcher.appendReplacement(resultBuilder, new String(Character.toChars(Integer.parseInt(hex, 16))));
        }
        matcher.appendTail(resultBuilder);
        return resultBuilder.toString();
    }

}
