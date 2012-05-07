/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import uk.ac.ed.ph.snuggletex.internal.FrozenSlice;

/**
 * Represents a user-defined {@link Command}, i.e. one defined in the client data using
 * <tt>\\newcommand</tt>.
 * 
 * @see UserDefinedEnvironment
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class UserDefinedCommand extends UserDefinedCommandOrEnvironment
        implements Command {
 
    private final FrozenSlice definitionSlice;
    
    public UserDefinedCommand(final String texName, final String optionalArgument,
            final int argumentCount, final FrozenSlice definitionSlice) {
        super(texName, optionalArgument, argumentCount);
        this.definitionSlice = definitionSlice;
    }

    public FrozenSlice getDefinitionSlice() {
        return definitionSlice;
    }
}
