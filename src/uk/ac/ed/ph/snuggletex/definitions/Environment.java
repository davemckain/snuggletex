/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public interface Environment extends CommandOrEnvironment {
    
    /**
     * Specifies which mode to parse content in. If null, then the "current" mode is used.
     */
    LaTeXMode getContentMode();
    
}
