/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.utilities;

import javax.xml.transform.Templates;

/**
 * Trivial implementation of {@link StylesheetCache} that actually doesn't cache anything
 * at all. This might be useful during development when you don't want caching at all.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class DoNothingStylesheetCache implements StylesheetCache {

    public Templates getStylesheet(String key) {
        return null;
    }
    
    public void putStylesheet(String key, Templates stylesheet) {
        /* (Do nothing) */
    }
}
