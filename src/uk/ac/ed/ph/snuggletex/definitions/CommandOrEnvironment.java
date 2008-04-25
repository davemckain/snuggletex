/* $Id: CommandOrEnvironment.java,v 1.2 2008/04/03 09:46:37 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision: 1.2 $
 */
public interface CommandOrEnvironment {
    
    String getTeXName();
    boolean isAllowingOptionalArgument();
    int getArgumentCount();
    LaTeXMode getArgumentMode(int argumentIndex);

}
