/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import uk.ac.ed.ph.snuggletex.dombuilding.CommandHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.InterpretableSimpleMathBuilder;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathInterpretation;

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
 * @version $Revision$
 */
public final class BuiltinCommand implements Command {
    
    private final String texName;
    private final CommandType type;
    private final boolean allowingOptionalArgument;
    private final int argumentCount;
    private final EnumSet<LaTeXMode> allowedModes;
    private final Interpretation interpretation;
    private final EnumSet<InterpretationType> allowedCombinerIntepretationTypes;
    private final CommandHandler nodeBuilder;
    private final TextFlowContext textFlowContext;
    
    /** 
     * Mode to use when parsing arguments. If not supplied, will preserve mode that Macro
     * is being used in.
     * 
     * (Main example: \mbox{...} will go into LR mode for its arguments)
     */
    private final LaTeXMode[] argumentModes;
    
    //--------------------------------------------------
    
    public BuiltinCommand(String name, EnumSet<LaTeXMode> allowedModes, CommandHandler nodeBuilder, TextFlowContext context) {
        this(name, CommandType.SIMPLE, false, 0, allowedModes, null, null, nodeBuilder, context, null);
    }
    
    public BuiltinCommand(String name, EnumSet<LaTeXMode> allowedModes,
            Interpretation interpretation, CommandHandler nodeBuilder, TextFlowContext context) {
        this(name, CommandType.SIMPLE, false, 0, allowedModes, null, interpretation, nodeBuilder, context, null);
    }
    
    /* (General MathInterpretation-based constructor) */
    public BuiltinCommand(String name, MathInterpretation interpretation, CommandHandler nodeBuilder) {
        this(name, CommandType.SIMPLE, false, 0, Globals.MATH_MODE_ONLY, null, interpretation,
                nodeBuilder, null, null);
    }
    
    /* (Convenience version of above that uses default Node Builder) */
    public BuiltinCommand(String name, MathInterpretation interpretation) {
        this(name, interpretation, new InterpretableSimpleMathBuilder());
    }
    
    public BuiltinCommand(String name, boolean allowOptionalArgument, int arguments,
            EnumSet<LaTeXMode> allowedModes, CommandHandler nodeBuilder, TextFlowContext context) {
        this(name, CommandType.COMPLEX, allowOptionalArgument, arguments, allowedModes, null, null, nodeBuilder, context, null);
    }
    
    /* Single argument, different mode */
    public BuiltinCommand(String name, boolean allowOptionalArgument,
            EnumSet<LaTeXMode> allowedModes, LaTeXMode argumentMode,
            CommandHandler nodeBuilder, TextFlowContext context) {
        this(name, CommandType.COMPLEX, allowOptionalArgument, 1, allowedModes,
                new LaTeXMode[] { argumentMode }, null, nodeBuilder, context, null);
    }
    
    public BuiltinCommand(String name, boolean allowOptionalArgument, int arguments,
            EnumSet<LaTeXMode> allowedModes, LaTeXMode[] argumentModes,
            CommandHandler nodeBuilder, TextFlowContext context) {
        this(name, CommandType.COMPLEX, allowOptionalArgument, arguments, allowedModes,
                argumentModes, null, nodeBuilder, context, null);
    }
    
    /* For combiners */
    public BuiltinCommand(String name, EnumSet<LaTeXMode> allowedModes,
            EnumSet<InterpretationType> allowedCombinerInterpretationTypes,
            CommandHandler nodeBuilder, TextFlowContext context) {
        this(name, CommandType.COMBINER, false, 0, allowedModes, null, null, nodeBuilder, context, allowedCombinerInterpretationTypes);
    }
    
    public BuiltinCommand(String name, CommandType commandType, boolean allowOptionalArgument,
            int arguments, EnumSet<LaTeXMode> allowedModes, LaTeXMode[] argumentModes,
            Interpretation interpretation, CommandHandler nodeBuilder, TextFlowContext context,
            EnumSet<InterpretationType> allowedCombinerInterpretationTypes) {
        this.texName = name;
        this.type = commandType;
        this.allowingOptionalArgument = allowOptionalArgument;
        this.argumentCount = arguments;
        this.allowedModes = allowedModes;
        this.argumentModes = argumentModes;
        this.interpretation = interpretation;
        this.nodeBuilder = nodeBuilder;
        this.textFlowContext = context;
        this.allowedCombinerIntepretationTypes = allowedCombinerInterpretationTypes;
    }
    
    public String getTeXName() {
        return texName;
    }
    
    public CommandType getType() {
        return type;
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
    
    public LaTeXMode getArgumentMode(int argumentIndex) {
        return argumentModes!=null ? argumentModes[argumentIndex] : null;
    }
    
    public Interpretation getInterpretation() {
        return interpretation;
    }
    
    public CommandHandler getNodeBuilder() {
        return nodeBuilder;
    }
    
    public TextFlowContext getTextFlowContext() {
        return textFlowContext;
    }

    public EnumSet<InterpretationType> getAllowedCombinerIntepretationTypes() {
        return allowedCombinerIntepretationTypes;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName()
            + "("
            + (texName!=null ? texName : "@" + Integer.toHexString(hashCode()))
            + ")";
    }
}
