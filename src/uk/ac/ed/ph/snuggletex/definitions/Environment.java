/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

/**
 * Base interface for LaTeX environment, i.e. constructs defined like
 * <tt>\\begin{environment} ... \\end{environment}</tt>.
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
