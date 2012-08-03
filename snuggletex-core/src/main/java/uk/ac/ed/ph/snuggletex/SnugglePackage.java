/* $Id:DefinitionMap.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.CombinerTargetMatcher;
import uk.ac.ed.ph.snuggletex.definitions.CommandType;
import uk.ac.ed.ph.snuggletex.definitions.CorePackageDefinitions;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.definitions.MathCharacter;
import uk.ac.ed.ph.snuggletex.definitions.MathCharacter.MathCharacterType;
import uk.ac.ed.ph.snuggletex.definitions.TextFlowContext;
import uk.ac.ed.ph.snuggletex.dombuilding.CommandHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EnvironmentHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.InterpretableSimpleMathHandler;
import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.StringUtilities;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathBigLimitOwnerInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketInterpretation.BracketType;
import uk.ac.ed.ph.snuggletex.semantics.MathFunctionInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathNegatableInterpretation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link SnugglePackage} defines a collection of {@link BuiltinCommand}s, {@link BuiltinEnvironment}s,
 * and {@link ErrorCode}s/{@link ErrorGroup}s that can be registered with a {@link SnuggleEngine}
 * to add in extra functionality.
 * <p>
 * The core SnuggleTeX distribution comes with what is essentially a built-in package defined
 * in {@link CorePackageDefinitions} which provides its core functionality.
 * 
 * <h2>Notes</h2>
 * 
 * <ul>
 *   <li>
 *     As of SnuggleTeX 1.3.0, an instance of this class may now be used by multiple threads.
 *   <li>
 *     This replaces "DefinitionMap" from SnuggleTeX 1.0/1.1, which had fewer features.
 *   </li>
 * </ul>
 * 
 * @see BuiltinCommand
 * @see BuiltinEnvironment
 * 
 * @since 1.2.0
 *
 * @author  David McKain
 * @version $Revision:179 $
 */
public final class SnugglePackage {
    
    /** Share instance of {@link InterpretableSimpleMathHandler} since it is stateless */
    public static final InterpretableSimpleMathHandler interpretableSimpleMathBuilder = new InterpretableSimpleMathHandler();
    
    /** Short name, used when formatting {@link ErrorCode}s */
    private final String name;
    
    /** Thread-safe Map of built-in commands, keyed on name */
    private final ConcurrentHashMap<String, BuiltinCommand> builtinCommandMap;
    
    /** Thread-safe Map of built-in environments, keyed on name */
    private final ConcurrentHashMap<String, BuiltinEnvironment> builtinEnvironmentMap;
    
    /** Thread-safe Map of {@link MathCharacter}s defined in this package, keyed on Unicode code point */
    private final ConcurrentHashMap<Integer, MathCharacter> mathCharacterMap;
    
    /** Thread-safe List of all {@link ErrorGroup}s */
    private final List<ErrorGroup> errorGroupList;

    /** Thread-safe Map of all {@link ErrorGroup}s defined by this package */
    private final ConcurrentHashMap<ErrorGroup, List<ErrorCode>> errorGroupMap;
    
    /** {@link ResourceBundle} providing details for formatting {@link ErrorCode}s */
    private ResourceBundle errorMessageBundle;
    
    public SnugglePackage(final String name) {
        ConstraintUtilities.ensureNotNull(name, "name");
        this.name = name;
        this.builtinCommandMap = new ConcurrentHashMap<String, BuiltinCommand>();
        this.builtinEnvironmentMap = new ConcurrentHashMap<String, BuiltinEnvironment>();
        this.mathCharacterMap = new ConcurrentHashMap<Integer, MathCharacter>();
        this.errorGroupList = Collections.synchronizedList(new ArrayList<ErrorGroup>());
        this.errorGroupMap = new ConcurrentHashMap<ErrorGroup, List<ErrorCode>>();
    }
    
    /**
     * Returns the name for this package.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns an unmodifiable view of all of the {@link ErrorGroup}s declared for this package.
     */
    public List<ErrorGroup> getErrorGroups() {
        return Collections.unmodifiableList(errorGroupList);
    }
    
    /**
     * Returns an unmodifiable view of the {@link ErrorCode}s corresponding to the given
     * {@link ErrorGroup}.
     */
    public List<ErrorCode> getErrorCodes(ErrorGroup errorGroup) {
        List<ErrorCode> errorCodes = errorGroupMap.get(errorGroup);
        if (errorCodes==null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(errorCodes);
    }

    
    /**
     * Returns the {@link ResourceBundle} used to format error messages.
     */
    public ResourceBundle getErrorMessageBundle() {
        return errorMessageBundle;
    }

    /**
     * Sets the {@link ResourceBundle} used to format error messages.
     */
    public void setErrorMessageBundle(ResourceBundle errorMessageBundle) {
        this.errorMessageBundle = errorMessageBundle;
    }
    
    /**
     * Returns a read-only {@link Map} of all {@link MathCharacter}s defined in this package,
     * keyed on input Unicode code point.
     */
    public Map<Integer, MathCharacter> getMathCharacterMap() {
        return Collections.unmodifiableMap(mathCharacterMap);
    }
    
    /**
     * Returns the {@link MathCharacter} corresponding to the Unicode character at the given
     * input codePoint, or null if no {@link MathCharacter} is defined for this codepoint.
     */
    public MathCharacter getMathCharacter(int codePoint) {
        return mathCharacterMap.get(Integer.valueOf(codePoint));
    }
    
    /**
     * Returns a read-only {@link Map} of all {@link BuiltinCommand}s defined in this package,
     * keyed on the name <tt>texName</tt> of the command (<tt>\texName</tt>).
     */
    public Map<String, BuiltinCommand> getBuiltinCommandMap() {
        return Collections.unmodifiableMap(builtinCommandMap);
    }
    
    /**
     * Returns the {@link BuiltinCommand} corresponding to LaTeX command called
     * <tt>\texName</tt> defined by this package, or null if this command is not defined.
     */
    public BuiltinCommand getBuiltinCommandByTeXName(String texName) {
        return builtinCommandMap.get(texName);
    }
    
    /**
     * Returns a read-only {@link Map} of all {@link BuiltinEnvironment}s defined in this package,
     * keyed on the name of the environment.
     */
    public Map<String, BuiltinEnvironment> getBuiltinEnvironmentMap() {
        return Collections.unmodifiableMap(builtinEnvironmentMap);
    }
    
    /**
     * Returns the {@link BuiltinEnvironment} corresponding to LaTeX environment
     * called <tt>texName</tt> defined by this package, or null if this environment is not defined.
     */
    public BuiltinEnvironment getBuiltinEnvironmentByTeXName(String texName) {
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
    
    public void loadMathFunctionDefinitions(final String resourceLocation) {
        readResourceData(resourceLocation, new LineHandler() {
            public void handleLine(String line) {
                /* (Line is either functionName or laTexFunctionName->MathMLFunctionName) */
                String latexName, outputName;
                int mapsToIndex = line.indexOf("->");
                if (mapsToIndex!=-1) {
                    latexName = line.substring(0, mapsToIndex);
                    outputName = line.substring(mapsToIndex + 2);
                }
                else {
                    latexName = line;
                    outputName = line;
                }
                addSimpleMathCommand(latexName, new MathFunctionInterpretation(outputName));
            }
        });
    }

    public void loadMathCharacterDefinitions(final String resourceLocation) {
        readResourceData(resourceLocation, new LineHandler() {
            @SuppressWarnings("synthetic-access")
            public void handleLine(String line) {
                String[] fields = line.split(":"); /* codePointHex:commandName:type */
                int codePoint = Integer.parseInt(fields[0], 16);
                String commandName = StringUtilities.nullIfEmpty(fields[1]);
                MathCharacterType mathCharacterType = MathCharacterType.valueOf(fields[2]);
                MathCharacter mathCharacter = new MathCharacter(codePoint, commandName, mathCharacterType);
                
                /* Define character input and (maybe) input command */
                mathCharacterMap.put(Integer.valueOf(codePoint), mathCharacter);
                if (commandName!=null) {
                    addMathCharacterCommand(mathCharacter);
                }
            }
        });
    }
    
    public void loadMathCharacterNegations(final String resourceLocation) {
        readResourceData(resourceLocation, new LineHandler() {
            @SuppressWarnings("synthetic-access")
            public void handleLine(String line) {
                /* Line is either of the form commandName or commandName->negatedCommandName.
                 * 
                 * In the first case, the negatedCommandName is formed by prefixing the commandName
                 * with 'n'. E.g. \leq -> \nleq.
                 */
                String sourceName, targetName;
                int mapsToIndex = line.indexOf("->");
                if (mapsToIndex!=-1) {
                    sourceName = line.substring(0, mapsToIndex);
                    targetName = line.substring(mapsToIndex + 2);
                }
                else {
                    sourceName = line;
                    targetName = "n" + sourceName;
                }
                MathCharacter sourceCharacter = ensureBuiltinMathCharacterCommand(sourceName, "Source command for negation");
                MathCharacter targetCharacter = ensureBuiltinMathCharacterCommand(targetName, "Target command for negation");
                sourceCharacter.addInterpretation(new MathNegatableInterpretation(targetCharacter));
            }
        });
    }
    
    public void loadMathCharacterBrackets(final String resourceLocation) {
        readResourceData(resourceLocation, new LineHandler() {
            @SuppressWarnings("synthetic-access")
            public void handleLine(String line) {
                /* Line is of the form inputCommandName:outputBracketCommandName:bracketType:(INFER|NOINFER) */
                String[] fields = line.split(":");
                String inputCommandName = fields[0];
                String outputBracketCommandName = fields[1];
                BracketType bracketType = BracketType.valueOf(fields[2]);
                boolean inferFences = "INFER".equals(fields[3]);
                
                MathCharacter inputMathCharacter = ensureBuiltinMathCharacterCommand(inputCommandName, "Bracket input command");
                MathCharacter outputBracketMathCharacter = ensureBuiltinMathCharacterCommand(outputBracketCommandName, "Fence target");
                inputMathCharacter.addInterpretation(new MathBracketInterpretation(outputBracketMathCharacter, bracketType, inferFences));
            }
        });
    }
    
    public void loadMathCharacterAliases(final String resourceLocation) {
        readResourceData(resourceLocation, new LineHandler() {
            @SuppressWarnings("synthetic-access")
            public void handleLine(String line) {
                line = line.replaceFirst("\\s+#.+$", "");
                String[] fields = line.split("->"); /* aliasCommandName:targetCommandName */
                
                String aliasCommandName = fields[0];
                MathCharacter targetCharacter = ensureBuiltinMathCharacterCommand(fields[1], "Target command for alias");
                addMathCharacterCommandAlias(aliasCommandName, targetCharacter);
            }
        });
    }
    
    public void loadMathCharacterBigLimitTargets(final String resourceLocation) {
        final MathBigLimitOwnerInterpretation bigLimitOwner = new MathBigLimitOwnerInterpretation();
        readResourceData(resourceLocation, new LineHandler() {
            @SuppressWarnings("synthetic-access")
            public void handleLine(String line) {
                /* (Line is name of an existing command) */
                MathCharacter target = ensureBuiltinMathCharacterCommand(line, "Target command for big limit owner");
                target.addInterpretation(bigLimitOwner);
            }
        });
    }
    
    private void readResourceData(final String resourceLocation, final LineHandler handler) {
        InputStream inputStream = Globals.class.getClassLoader().getResourceAsStream(resourceLocation);
        if (inputStream==null) {
            throw new SnuggleRuntimeException("Could not load ClassPath resource at  " + resourceLocation);
        }
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream, "US-ASCII"));
            String line;
            while ((line = inputReader.readLine())!=null) {
                if (line.startsWith("#")) {
                    continue;
                }
                line = line.replaceFirst("\\s+#.+$", "");
                handler.handleLine(line);
            }
            inputReader.close();
        }
        catch (Exception e) {
            throw new SnuggleRuntimeException("Got Exception while reading and parsing math character definitions from resource " + resourceLocation, e);
        }
    }
    
    private MathCharacter ensureBuiltinMathCharacterCommand(String texName, String errorMessageContext) {
        BuiltinCommand command = getBuiltinCommandByTeXName(texName);
        MathCharacter mathCharacter = command!=null ? command.getMathCharacter() : null;
        if (command==null || mathCharacter==null) {
            throw new SnuggleRuntimeException(errorMessageContext + " must be a previously-defined math character input command");
        }
        return mathCharacter;
    }
    
    private static interface LineHandler {
        void handleLine(String line);
    }
    
    //-------------------------------------------------------
    
    public BuiltinCommand addSimpleCommand(final String name, final EnumSet<LaTeXMode> allowedModes,
            final CommandHandler handler, final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.SIMPLE, false, 0,
                allowedModes, null, null,
                handler, context, null));
    }
    
    public BuiltinCommand addSimpleCommand(final String name, final EnumSet<LaTeXMode> allowedModes,
            final Interpretation[] interpretations, final CommandHandler handler,
            final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.SIMPLE, false, 0,
                allowedModes, null, SnugglePackage.makeInterpretationMap(interpretations),
                handler, context, null));
    }
    
    public BuiltinCommand addSimpleCommand(final String name, final EnumSet<LaTeXMode> allowedModes,
            final Interpretation interpretation, final CommandHandler handler,
            final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.SIMPLE, false, 0,
                allowedModes, null, SnugglePackage.makeInterpretationMap(interpretation),
                handler, context, null));
    }
    
    public BuiltinCommand addMathCharacterCommand(final MathCharacter mathCharacter) {
        return addMathCharacterCommandAlias(mathCharacter.getInputCommandName(), mathCharacter);
    }
    
    public BuiltinCommand addMathCharacterCommandAlias(final String name, final MathCharacter mathCharacter) {
        return addCommand(new BuiltinCommand(name, CommandType.SIMPLE, false, 0,
                Globals.MATH_MODE_ONLY, null, mathCharacter.getInterpretationMap(),
                interpretableSimpleMathBuilder, null, null));
    }

    public BuiltinCommand addSimpleMathCommand(final String name, final MathInterpretation... interpretations) {
        return addSimpleMathCommand(name, interpretations, interpretableSimpleMathBuilder);
    }
    
    public BuiltinCommand addSimpleMathCommand(final String name,
            final MathInterpretation interpretation, final CommandHandler handler) {
        return addCommand(new BuiltinCommand(name, CommandType.SIMPLE, false, 0,
                Globals.MATH_MODE_ONLY, null, SnugglePackage.makeInterpretationMap(interpretation),
                handler, null, null));
    }
    
    public BuiltinCommand addSimpleMathCommand(final String name,
            final MathInterpretation[] interpretations, final CommandHandler handler) {
        return addCommand(new BuiltinCommand(name, CommandType.SIMPLE, false, 0,
                Globals.MATH_MODE_ONLY, null, SnugglePackage.makeInterpretationMap(interpretations),
                handler, null, null));
    }

    
    public BuiltinCommand addCombinerCommand(final String name, final EnumSet<LaTeXMode> allowedModes,
            final CombinerTargetMatcher combinerTargetMatcher,
            final CommandHandler handler, final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.COMBINER, false, 0,
                allowedModes, null,
                null, handler,
                context, combinerTargetMatcher));
    }
    
    public BuiltinCommand addComplexCommand(final String name, final boolean allowOptionalArgument,
            final int arguments, final EnumSet<LaTeXMode> allowedModes,
            final LaTeXMode[] argumentModes,
            final CommandHandler handler, final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.COMPLEX, allowOptionalArgument, arguments,
                allowedModes, argumentModes,
                null, handler,
                context, null));
    }
    
    public BuiltinCommand addComplexCommandSameArgMode(final String name, final boolean allowOptionalArgument,
            final int arguments, final EnumSet<LaTeXMode> allowedModes, final CommandHandler handler,
            final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.COMPLEX, allowOptionalArgument, arguments,
                allowedModes, null,
                null, handler,
                context, null));
    }
    
    public BuiltinCommand addComplexCommandSameArgMode(final String name, final boolean allowOptionalArgument,
            final int arguments, final EnumSet<LaTeXMode> allowedModes,
            final Interpretation interpretation, final CommandHandler handler,
            final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.COMPLEX, allowOptionalArgument, arguments,
                allowedModes, null,
                SnugglePackage.makeInterpretationMap(interpretation), handler,
                context, null));
    }
    
    public BuiltinCommand addComplexCommandOneArg(final String name, final boolean allowOptionalArgument,
            final EnumSet<LaTeXMode> allowedModes, final LaTeXMode argumentMode,
            final CommandHandler handler, final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.COMPLEX, allowOptionalArgument, 1,
                allowedModes, new LaTeXMode[] { argumentMode },
                null, handler,
                context, null));
    }
    
    public BuiltinCommand addComplexCommandOneArg(final String name, final boolean allowOptionalArgument,
            final EnumSet<LaTeXMode> allowedModes, final LaTeXMode argumentMode,
            final Interpretation interpretation, final CommandHandler handler,
            final TextFlowContext context) {
        return addCommand(new BuiltinCommand(name, CommandType.COMPLEX, allowOptionalArgument, 1,
                allowedModes, new LaTeXMode[] { argumentMode },
                SnugglePackage.makeInterpretationMap(interpretation), handler,
                context, null));
    }
    
    public BuiltinCommand addCommand(final BuiltinCommand command) {
        if (isInputableTeXName(command.getTeXName())) {
            builtinCommandMap.put(command.getTeXName(), command);
        }
        return command;
    }
    
    
    public void addCommandInterpretation(final String name, final Interpretation interpretation) {
        BuiltinCommand command = getBuiltinCommandByTeXName(name);
        if (command==null) {
            throw new IllegalArgumentException("No command defined with name " + name);
        }
        command.getInterpretationMap().put(interpretation.getType(), interpretation);
    }
    
    //-------------------------------------------------------
    
    public BuiltinEnvironment addEnvironment(final String name, final EnumSet<LaTeXMode> allowedModes,
            final LaTeXMode contentMode, final Interpretation interpretation,
            final EnvironmentHandler handler, final TextFlowContext context) {
        return addEnvironment(new BuiltinEnvironment(name, false, 0, allowedModes,
                contentMode, SnugglePackage.makeInterpretationMap(interpretation), handler, context));
    }
    
    public BuiltinEnvironment addEnvironment(final String name, final EnumSet<LaTeXMode> allowedModes,
            final LaTeXMode contentMode, final Interpretation[] interpretations,
            final EnvironmentHandler handler, final TextFlowContext context) {
        return addEnvironment(new BuiltinEnvironment(name, false, 0, allowedModes,
                contentMode, SnugglePackage.makeInterpretationMap(interpretations), handler, context));
    }
    
    public BuiltinEnvironment addEnvironment(final String name, final boolean allowOptionalArgument,
            final int argumentCount, final EnumSet<LaTeXMode> allowedModes,
            final LaTeXMode contentMode, final Interpretation interpretation,
            final EnvironmentHandler handler, final TextFlowContext context) {
        return addEnvironment(new BuiltinEnvironment(name, allowOptionalArgument, argumentCount,
                allowedModes, contentMode, SnugglePackage.makeInterpretationMap(interpretation), handler, context));
    }
    
    public BuiltinEnvironment addEnvironment(final String name, final boolean allowOptionalArgument,
            final int argumentCount, final EnumSet<LaTeXMode> allowedModes,
            final LaTeXMode contentMode, final Interpretation[] interpretations,
            final EnvironmentHandler handler, final TextFlowContext context) {
        return addEnvironment(new BuiltinEnvironment(name, allowOptionalArgument, argumentCount,
                allowedModes, contentMode, SnugglePackage.makeInterpretationMap(interpretations), handler, context));
    }
    
    public BuiltinEnvironment addEnvironment(final BuiltinEnvironment environment) {
        if (isInputableTeXName(environment.getTeXName())) {
            builtinEnvironmentMap.put(environment.getTeXName(), environment);
        }
        return environment;
    }
    
    //-------------------------------------------------------
    
    public void addErrorCode(ErrorCode errorCode) {
        ConstraintUtilities.ensureNotNull(errorCode, "errorCode");
        ErrorGroup errorGroup = errorCode.getErrorGroup();
        ConstraintUtilities.ensureNotNull(errorGroup, "errorCode.errorGroup");
        List<ErrorCode> errorCodesForGroup = errorGroupMap.get(errorGroup);
        if (errorCodesForGroup==null) {
            errorCodesForGroup = Collections.synchronizedList(new ArrayList<ErrorCode>());
            errorGroupList.add(errorGroup);
            errorGroupMap.put(errorGroup, errorCodesForGroup);
        }
        errorCodesForGroup.add(errorCode);
    }
    
    public void addErrorCodes(ErrorCode... errorCodes) {
        for (ErrorCode errorCode : errorCodes) {
            addErrorCode(errorCode);
        }
    }
    
    //-------------------------------------------------------

    public static EnumMap<InterpretationType, Interpretation> makeInterpretationMap(final Interpretation interpretation) {
        if (interpretation==null) {
            return null;
        }
        EnumMap<InterpretationType, Interpretation> result = new EnumMap<InterpretationType, Interpretation>(InterpretationType.class);
        result.put(interpretation.getType(), interpretation);
        return result;
    }

    public static EnumMap<InterpretationType, Interpretation> makeInterpretationMap(final Interpretation... interpretations) {
        if (interpretations.length==0) {
            return null;
        }
        EnumMap<InterpretationType, Interpretation> result = new EnumMap<InterpretationType, Interpretation>(InterpretationType.class);
        for (Interpretation interpretation : interpretations) {
            result.put(interpretation.getType(), interpretation);
        }
        return result;
    }

}
