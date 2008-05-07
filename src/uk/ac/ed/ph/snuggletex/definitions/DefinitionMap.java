/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import uk.ac.ed.ph.snuggletex.dombuilding.CommandHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EnvironmentHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.InterpretableSimpleMathBuilder;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathInterpretation;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a set of built-in commands and environments.
 * 
 * @see BuiltinCommand
 * @see BuiltinEnvironment
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class DefinitionMap {
    
    /** Map of built-in commands, keyed on name */
    public final Map<String, BuiltinCommand> builtinCommandMap;
    
    /** Map of built-in environments, keyed on name */
    public final Map<String, BuiltinEnvironment> builtinEnvironmentMap;
    
    public DefinitionMap() {
        this.builtinCommandMap = new HashMap<String, BuiltinCommand>();
        this.builtinEnvironmentMap = new HashMap<String, BuiltinEnvironment>();
    }
    
    public BuiltinCommand getCommandByTeXName(String texName) {
        return builtinCommandMap.get(texName);
    }
    
    public BuiltinEnvironment getEnvironmentByTeXName(String texName) {
        return builtinEnvironmentMap.get(texName);
    }
    
    //-------------------------------------------------------
    
    /**
     * Tests whether the name of a command or environment is "inputable". Ones which cannot be
     * directly input are enclosed in angle brackets. These commands are created during token
     * fixing.
     * 
     * @param texName
     */
    public static boolean isInputableTeXName(final String texName) {
        return texName!=null && !(texName.charAt(0)=='<' && texName.length()>3 && texName.endsWith(">"));
    }
    
    //-------------------------------------------------------
    
    public BuiltinCommand addSimpleCommand(final String name, final EnumSet<LaTeXMode> allowedModes,
            final CommandHandler nodeBuilder, final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.SIMPLE, false, 0,
                allowedModes, null, null,
                nodeBuilder, context, null));
    }
    
    public BuiltinCommand addSimpleCommand(final String name, final EnumSet<LaTeXMode> allowedModes,
            final Interpretation interpretation, final CommandHandler nodeBuilder,
            final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.SIMPLE, false, 0,
                allowedModes, null, interpretation,
                nodeBuilder, context, null));
    }

    /** Convenience method for creating simple MATH-only commands. */
    public BuiltinCommand addSimpleMathCommand(final String name,
            final MathInterpretation interpretation, final CommandHandler nodeBuilder) {
        return addCommand(new BuiltinCommand(name, CommandType.SIMPLE, false, 0,
                Globals.MATH_MODE_ONLY, null, interpretation,
                nodeBuilder, null, null));
    }
    
    public BuiltinCommand addSimpleMathCommand(final String name, final MathInterpretation interpretation) {
        return addSimpleMathCommand(name, interpretation, new InterpretableSimpleMathBuilder());
    }
    
    public BuiltinCommand addCombinerCommand(final String name, final EnumSet<LaTeXMode> allowedModes,
            final EnumSet<InterpretationType> allowedCombinerInterpretationTypes,
            final CommandHandler nodeBuilder, final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.COMBINER, false, 0,
                allowedModes, null,
                null, nodeBuilder,
                context, allowedCombinerInterpretationTypes));
    }
    
    public BuiltinCommand addComplexCommand(final String name, final boolean allowOptionalArgument,
            final int arguments, final EnumSet<LaTeXMode> allowedModes,
            final LaTeXMode[] argumentModes,
            final CommandHandler nodeBuilder, final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.COMPLEX, allowOptionalArgument, arguments,
                allowedModes, argumentModes,
                null, nodeBuilder,
                context, null));
    }
    
    public BuiltinCommand addComplexCommandSameArgMode(final String name, final boolean allowOptionalArgument,
            final int arguments, final EnumSet<LaTeXMode> allowedModes, final CommandHandler nodeBuilder,
            final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.COMPLEX, allowOptionalArgument, arguments,
                allowedModes, null,
                null, nodeBuilder,
                context, null));
    }
    
    public BuiltinCommand addComplexCommandSameArgMode(final String name, final boolean allowOptionalArgument,
            final int arguments, final EnumSet<LaTeXMode> allowedModes,
            final Interpretation interpretation, final CommandHandler nodeBuilder,
            final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.COMPLEX, allowOptionalArgument, arguments,
                allowedModes, null,
                interpretation, nodeBuilder,
                context, null));
    }
    
    public BuiltinCommand addComplexCommandOneArg(final String name, final boolean allowOptionalArgument,
            final EnumSet<LaTeXMode> allowedModes,
            final LaTeXMode argumentMode,
            final CommandHandler nodeBuilder, final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.COMPLEX, allowOptionalArgument, 1,
                allowedModes, new LaTeXMode[] { argumentMode },
                null, nodeBuilder,
                context, null));
    }
    
    private BuiltinCommand addCommand(final BuiltinCommand command) {
        if (isInputableTeXName(command.getTeXName())) {
            builtinCommandMap.put(command.getTeXName(), command);
        }
        return command;
    }
    

    
    public BuiltinEnvironment addEnvironment(final String name, final EnumSet<LaTeXMode> allowedModes,
            final LaTeXMode contentMode, final Interpretation interpretation,
            final EnvironmentHandler nodeBuilder, final TextFlowContext context) {
        return addEnvironment(new BuiltinEnvironment(name, false, 0, allowedModes,
                contentMode, interpretation, nodeBuilder, context));
    }
    
    public BuiltinEnvironment addEnvironment(final String name, final boolean allowOptionalArgument,
            final int argumentCount, final EnumSet<LaTeXMode> allowedModes,
            final LaTeXMode contentMode, final Interpretation interpretation,
            final EnvironmentHandler nodeBuilder, final TextFlowContext context) {
        return addEnvironment(new BuiltinEnvironment(name, allowOptionalArgument, argumentCount,
                allowedModes, contentMode, interpretation, nodeBuilder, context));
    }
    
    private BuiltinEnvironment addEnvironment(final BuiltinEnvironment environment) {
        if (isInputableTeXName(environment.getTeXName())) {
            builtinEnvironmentMap.put(environment.getTeXName(), environment);
        }
        return environment;
    }
}
