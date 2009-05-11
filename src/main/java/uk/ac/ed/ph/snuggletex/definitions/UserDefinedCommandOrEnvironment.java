/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

/**
 * Partial base class for {@link UserDefinedCommand}s and {@link UserDefinedEnvironment}s.
 * 
 * @see UserDefinedEnvironment
 * 
 * @author  David McKain
 * @version $Revision$
 */
abstract class UserDefinedCommandOrEnvironment implements CommandOrEnvironment {
 
    protected final String texName;
    protected final boolean allowingOptionalArgument;
    protected final int argumentCount;
    
    public UserDefinedCommandOrEnvironment(final String texName, final boolean allowingOptionalArgument,
            final int argumentCount) {
        this.texName = texName;
        this.allowingOptionalArgument = allowingOptionalArgument;
        this.argumentCount = argumentCount;
    }

    public String getTeXName() {
        return texName;
    }

    public boolean isAllowingOptionalArgument() {
        return allowingOptionalArgument;
    }

    public int getArgumentCount() {
        return argumentCount;
    }

    /**
     * User-defined commands and environments stay in the same mode when parsing arguments.
     */
    public LaTeXMode getArgumentMode(int argumentIndex) {
        return null;
    }
    
    @Override
    public final String toString() {
        return getClass().getSimpleName() + "(" + texName + ")";
    }
}
