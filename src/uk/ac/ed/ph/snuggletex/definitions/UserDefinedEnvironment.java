/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import uk.ac.ed.ph.snuggletex.internal.FrozenSlice;

/**
 * Represents a user-defined environment, created by clients using the
 * <tt>\\newenvironment</tt> command.
 * 
 * @see UserDefinedCommand
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class UserDefinedEnvironment extends UserDefinedCommandOrEnvironment
        implements Environment {
 
    private final FrozenSlice beginDefinitionSlice;
    private final FrozenSlice endDefinitionSlice;
    
    public UserDefinedEnvironment(final String texName, final boolean allowingOptionalArgument,
            final int argumentCount, final FrozenSlice beginDefinitionSlice,
            final FrozenSlice endDefinitionSlice) {
        super(texName, allowingOptionalArgument, argumentCount);
        this.beginDefinitionSlice = beginDefinitionSlice;
        this.endDefinitionSlice = endDefinitionSlice;
    }

    public FrozenSlice getBeginDefinitionSlice() {
        return beginDefinitionSlice;
    }
    
    public FrozenSlice getEndDefinitionSlice() {
        return endDefinitionSlice;
    }
    
    /** Use existing content mode */
    public LaTeXMode getContentMode() {
        return null;
    }
}
