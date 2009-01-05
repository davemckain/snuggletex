/* $Id:BuiltinCommand.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import uk.ac.ed.ph.snuggletex.dombuilding.CommandHandler;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;

import java.util.EnumSet;

/**
 * Represents a {@link Command} that has been defined internally via a {@link DefinitionMap}.
 * <p>
 * All of the core LaTeX macros have been defined in this way - see {@link GlobalBuiltins}.
 * 
 * @see BuiltinEnvironment
 * @see GlobalBuiltins
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
public final class BuiltinCommand extends BuiltinCommandOrEnvironment<CommandHandler>
        implements Command {
    
    private final CommandType type;
    private final EnumSet<InterpretationType> allowedCombinerIntepretationTypes;
    
    /** 
     * Mode to use when parsing arguments. If not supplied, will preserve mode that Macro
     * is being used in.
     * 
     * (Main example: \mbox{...} will go into LR mode for its arguments)
     */
    private final LaTeXMode[] argumentModes;
    
    //--------------------------------------------------
    
    public BuiltinCommand(String texName, CommandType commandType, boolean allowingOptionalArgument,
            int argumentCount, EnumSet<LaTeXMode> allowedModes, LaTeXMode[] argumentModes,
            Interpretation interpretation, CommandHandler domBuilderHandler, TextFlowContext textFlowContext,
            EnumSet<InterpretationType> allowedCombinerInterpretationTypes) {
        super(texName, allowingOptionalArgument, argumentCount, allowedModes,
                interpretation, textFlowContext, domBuilderHandler);
        this.type = commandType;
        this.argumentModes = argumentModes;
        this.allowedCombinerIntepretationTypes = allowedCombinerInterpretationTypes;
    }
    
    public CommandType getType() {
        return type;
    }
    
    public LaTeXMode getArgumentMode(int argumentIndex) {
        return argumentModes!=null ? argumentModes[argumentIndex] : null;
    }
    
    public EnumSet<InterpretationType> getAllowedCombinerIntepretationTypes() {
        return allowedCombinerIntepretationTypes;
    }
}
