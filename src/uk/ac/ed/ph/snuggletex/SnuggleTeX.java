/* $Id: SnuggleTeX.java,v 1.2 2008/04/17 16:45:39 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import java.io.IOException;
import java.util.List;

import org.w3c.dom.Element;

/**
 * TODO: This will contain a set of static methods for doing the most common types of jobs.
 *
 * @author  David McKain
 * @version $Revision: 1.2 $
 */
public final class SnuggleTeX {
    
    public static final String SNUGGLETEX_NAMESPACE = "http://www.ph.ed.ac.uk/snuggletex";
    
    public static List<InputError> snuggle(final Element targetRoot, final SnuggleInput... inputs)
            throws IOException {
        SnuggleTeXSession session = new SnuggleTeXEngine().createSession();
        for (SnuggleInput input : inputs) {
            session.parseInput(input);
        }
        session.buildDOM(targetRoot);
        return session.getErrors();
    }
}
