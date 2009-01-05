/* $Id:BuiltinEnvironment.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
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
 * @version $Revision:179 $
 */
public final class BuiltinEnvironment extends BuiltinCommandOrEnvironment<EnvironmentHandler>
        implements Environment {
    
    /** 
     * Mode to use when parsing content. If null, will preserve mode that environment
     * is called in.
     */
    private final LaTeXMode contentMode;
    
    public BuiltinEnvironment(final String texName, final boolean allowingOptionalArgument,
            final int argumentCount, final EnumSet<LaTeXMode> allowedModes,
            final LaTeXMode contentMode, final Interpretation interpretation,
            final EnvironmentHandler domBuildingHandler, final TextFlowContext textFlowContext) {
        super(texName, allowingOptionalArgument, argumentCount, allowedModes, interpretation,
                textFlowContext, domBuildingHandler);
        this.contentMode = contentMode;
    }
    
    /** (Currently all built-in environments parse their arguments in {@link LaTeXMode#PARAGRAPH}.) */
    public LaTeXMode getArgumentMode(int argumentIndex) {
        return LaTeXMode.PARAGRAPH;
    }
    
    public LaTeXMode getContentMode() {
        return contentMode;
    }
}
