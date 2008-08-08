/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import uk.ac.ed.ph.commons.util.ConstraintUtilities;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;

import java.util.EnumSet;

/**
 * Partial base class for {@link BuiltinCommand} and {@link BuiltinEnvironment}.
 * 
 * @param <H> type of "handler" used to generate DOM subtrees.
 *
 * @author  David McKain
 * @version $Revision$
 */
abstract class BuiltinCommandOrEnvironment<H> implements CommandOrEnvironment {
    
    protected final String texName;
    protected final boolean allowingOptionalArgument;
    protected final int argumentCount;
    protected final EnumSet<LaTeXMode> allowedModes;
    protected final Interpretation interpretation;
    protected final TextFlowContext textFlowContext;
    protected final H domBuildingHandler;
    
    protected BuiltinCommandOrEnvironment(final String texName, final boolean allowingOptionalArgument,
            final int argumentCount, final EnumSet<LaTeXMode> allowedModes, final Interpretation interpretation,
            final TextFlowContext textFlowContext, final H domBuildingHandler) {
        ConstraintUtilities.ensureNotNull(texName, "texName");
        ConstraintUtilities.ensureNotNull(allowedModes, "allowedModes");
        this.texName = texName;
        this.allowingOptionalArgument = allowingOptionalArgument;
        this.argumentCount = argumentCount;
        this.allowedModes = allowedModes;
        this.interpretation = interpretation;
        this.textFlowContext = textFlowContext;
        this.domBuildingHandler = domBuildingHandler;
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
    
    public EnumSet<LaTeXMode> getAllowedModes() {
        return allowedModes;
    }
    
    public Interpretation getInterpretation() {
        return interpretation;
    }
    
    public TextFlowContext getTextFlowContext() {
        return textFlowContext;
    }
    
    public H getDOMBuildingHandler() {
        return domBuildingHandler;
    }
    
    @Override
    public final String toString() {
        return getClass().getSimpleName()
            + "("
            + (texName!=null ? texName : "@" + Integer.toHexString(hashCode()))
            + ")";
    }
}
