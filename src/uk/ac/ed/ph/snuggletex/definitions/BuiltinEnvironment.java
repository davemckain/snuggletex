/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import uk.ac.ed.ph.snuggletex.dombuilding.EnvironmentHandler;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;

import java.util.EnumSet;

/**
 * Defines a built-in LaTeX environment, as specified within a {@link DefinitionMap}.
 * 
 * @see BuiltinCommand
 * @see GlobalBuiltins
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class BuiltinEnvironment implements Environment {
    
    private final String texName;
    private final boolean allowingOptionalArgument;
    private final int argumentCount;
    private final EnumSet<LaTeXMode> allowedModes;
    private final Interpretation interpretation;
    private final EnvironmentHandler nodeBuilder;
    private final TextFlowContext textFlowContext;
    
    /** 
     * Mode to use when parsing content. If null, will preserve mode that environment
     * is called in.
     */
    private final LaTeXMode contentMode;
    
    public BuiltinEnvironment(final String name, final EnumSet<LaTeXMode> allowedModes,
            final LaTeXMode contentMode, final Interpretation interpretation,
            final EnvironmentHandler nodeBuilder, final TextFlowContext context) {
        this(name, false, 0, allowedModes, contentMode, interpretation, nodeBuilder, context);
    }
    
    public BuiltinEnvironment(final String name, final boolean allowOptionalArgument,
            final int argumentCount, final EnumSet<LaTeXMode> allowedModes,
            final LaTeXMode contentMode, final Interpretation interpretation,
            final EnvironmentHandler nodeBuilder, final TextFlowContext context) {
        this.texName = name;
        this.allowingOptionalArgument = allowOptionalArgument;
        this.argumentCount = argumentCount;
        this.allowedModes = allowedModes;
        this.contentMode = contentMode;
        this.interpretation = interpretation;
        this.nodeBuilder = nodeBuilder;
        this.textFlowContext = context;
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
     * TODO: Is this the right thing to do there? Or should we parametrise this?
     */
    public LaTeXMode getArgumentMode(int argumentIndex) {
        return LaTeXMode.PARAGRAPH;
    }
    
    public EnumSet<LaTeXMode> getAllowedModes() {
        return allowedModes;
    }
    
    public LaTeXMode getContentMode() {
        return contentMode;
    }
    
    public Interpretation getInterpretation() {
        return interpretation;
    }
    
    public EnvironmentHandler getNodeBuilder() {
        return nodeBuilder;
    }

    public TextFlowContext getTextFlowContext() {
        return textFlowContext;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + texName + ")";
    }
}
