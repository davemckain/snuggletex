/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.CommandType;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.internal.FrozenSlice;

/**
 * This token represents a LaTeX {@link BuiltinCommand}.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public class CommandToken extends FlowToken {
    
    private final BuiltinCommand command;
    
    /** 
     * For {@link CommandType#COMPLEX}, this returns the optional argument or null if nothing
     * was provided.
     */
    private final ArgumentContainerToken optionalArgument;
    
    /** 
     * For {@link CommandType#COMPLEX}, this returns a possibly empty array of arguments. Otherwise,
     * null is returned.
     */
    private final ArgumentContainerToken[] arguments;
    
    /** 
     * For {@link CommandType#COMBINER}, this returns the token that the command is being
     * combined with.
     */
    private final FlowToken combinerTarget;
    
    public CommandToken(FrozenSlice slice, LaTeXMode latexMode, BuiltinCommand command) {
        this(slice, latexMode, command, null, null, null);
    }
    
    public CommandToken(FrozenSlice slice, LaTeXMode latexMode, BuiltinCommand command, FlowToken combinerTarget) {
        this(slice, latexMode, command, combinerTarget, null, null);
    }
    
    public CommandToken(FrozenSlice slice, LaTeXMode latexMode, BuiltinCommand command,
            ArgumentContainerToken optionalArgument, ArgumentContainerToken[] arguments) {
        this(slice, latexMode, command, null, optionalArgument, arguments);
    }
    
    private CommandToken(FrozenSlice slice, LaTeXMode latexMode, BuiltinCommand command, FlowToken combinerTarget,
            ArgumentContainerToken optionalArgument, ArgumentContainerToken[] arguments) {
        super(slice, TokenType.COMMAND, latexMode, command.getInterpretation(), command.getTextFlowContext());
        this.command = command;
        this.combinerTarget = combinerTarget;
        this.optionalArgument = optionalArgument;
        this.arguments = arguments;
    }

    public BuiltinCommand getCommand() {
        return command;
    }
    
    public FlowToken getCombinerTarget() {
        return combinerTarget;
    }

    public ArgumentContainerToken getOptionalArgument() {
        return optionalArgument;
    }
    
    public ArgumentContainerToken[] getArguments() {
        return arguments;
    }
}
