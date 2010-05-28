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

}
