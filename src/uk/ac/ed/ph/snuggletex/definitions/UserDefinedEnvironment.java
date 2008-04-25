/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import uk.ac.ed.ph.aardvark.commons.util.ObjectUtilities;
import uk.ac.ed.ph.snuggletex.conversion.FrozenSlice;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class UserDefinedEnvironment implements Environment {
 
    private final String texName;
    private final boolean allowingOptionalArgument;
    private final int argumentCount;
    private final FrozenSlice beginDefinitionSlice;
    private final FrozenSlice endDefinitionSlice;
    
    public UserDefinedEnvironment(final String texName, final boolean allowingOptionalArgument,
            final int argumentCount, final FrozenSlice beginDefinitionSlice,
            final FrozenSlice endDefinitionSlice) {
        this.texName = texName;
        this.allowingOptionalArgument = allowingOptionalArgument;
        this.argumentCount = argumentCount;
        this.beginDefinitionSlice = beginDefinitionSlice;
        this.endDefinitionSlice = endDefinitionSlice;
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

    public FrozenSlice getBeginDefinitionSlice() {
        return beginDefinitionSlice;
    }
    
    public FrozenSlice getEndDefinitionSlice() {
        return endDefinitionSlice;
    }
    
    /** Use existing argument Mode */
    public LaTeXMode getArgumentMode(int argumentIndex) {
        return null;
    }

    /** Use existing content mode */
    public LaTeXMode getContentMode() {
        return null;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
