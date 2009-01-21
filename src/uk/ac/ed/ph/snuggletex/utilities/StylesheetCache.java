/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.utilities;

import uk.ac.ed.ph.snuggletex.SnuggleEngine;

import javax.xml.transform.Templates;

/**
 * Encapsulates a simple cache for the internal XSLT stylesheets used by SnuggleTeX.
 * This can be used if you want SnuggleTeX to integrate with some kind of XSLT caching mechanism
 * (e.g. your own).
 * <p>
 * A {@link SnuggleEngine} creates a default implementation of this that caches stylesheets
 * over the lifetime of the {@link SnuggleEngine} Object, which is reasonable. If you want
 * to change this, create your own implementation and attach it to your {@link SnuggleEngine}.
 * 
 * <h2>Internal Note</h2>
 * 
 * (I'm not currently enforcing that implementations of this should be thread-safe. Therefore, make
 * sure that you synchronise correctly when accessing an instance of this cache.) 
 *
 * @author  David McKain
 * @version $Revision$
 */
public interface StylesheetCache {
   
    /**
     * Tries to retrieve an XSLT stylesheet from the cache having the given resourceName
     * (as used in {@link ClassLoader#getResourceAsStream(String)}).
     * <p>
     * Return a previously cached {@link Templates} or null if your cache doesn't want to cache
     * this or if it does not contain the required result.
     */
    Templates getStylesheet(String resourceName);
    
    /**
     * Instructs the cache that it might want to store the given XSLT stylesheet corresponding
     * to the given resourceName.
     * <p>
     * Implementations can safely choose to do absolutely nothing here if they want.
     */
    void putStylesheet(String resourceName, Templates stylesheet);

}
