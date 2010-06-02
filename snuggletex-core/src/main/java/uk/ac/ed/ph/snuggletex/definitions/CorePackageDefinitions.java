/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import static uk.ac.ed.ph.snuggletex.definitions.Globals.ALL_MODES;
import static uk.ac.ed.ph.snuggletex.definitions.Globals.MATH_MODE_ONLY;
import static uk.ac.ed.ph.snuggletex.definitions.Globals.PARA_MODE_ONLY;
import static uk.ac.ed.ph.snuggletex.definitions.Globals.TEXT_MODE_ONLY;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.LR;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.MATH;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.PARAGRAPH;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.VERBATIM;
import static uk.ac.ed.ph.snuggletex.definitions.TextFlowContext.ALLOW_INLINE;
import static uk.ac.ed.ph.snuggletex.definitions.TextFlowContext.IGNORE;
import static uk.ac.ed.ph.snuggletex.definitions.TextFlowContext.START_NEW_XHTML_BLOCK;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.SnugglePackage;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.dombuilding.AccentHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.AnchorHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.ArrayHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.BoxHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.CharacterCommandHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.DoNothingHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EnsureMathHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EqnArrayHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.GetVarHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.HSpaceHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.HrefHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.InsertUnicodeHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.InterpretableSimpleMathHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.LineBreakHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.ListEnvironmentHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.LiteralHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.MathComplexCommandHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.MathEnvironmentHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.MathFenceHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.MathLimitsHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.MathNotHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.MathRootHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.MathStackrelHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.MathVariantMapHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.MatrixHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.ModeDelegatingHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.MrowHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.ParagraphHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.SetVarHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.SimpleXHTMLContainerBuildingHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.SpaceHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.StyleInterpretationHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.TabularHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.TextSafeInterpretableMathIdentifierHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.UnitsHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.VerbatimHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.XMLAttrHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.XMLBlockElementHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.XMLInlineElementHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.XMLNameOrIdHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.XMLUnparseHandler;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathBigLimitOwnerInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathCharacterInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathFunctionInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLSymbol;
import uk.ac.ed.ph.snuggletex.semantics.MathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.StyleDeclarationInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketInterpretation.BracketType;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This defines the default {@link SnugglePackage} (containing {@link BuiltinCommand} and
 * {@link BuiltinEnvironment}) supported by SnuggleTeX.
 * 
 * <h2>Notes</h2>
 * 
 * <ul>
 *   <li>This is defined in the static constructor(!)</li>
 *   <li>A subset of commands and environments are made available as constants.</li>
 *   <li>If you're looking at the JavaDoc for this, you won't see most of the definitions...</li>
 * </ul>
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class CorePackageDefinitions {
    
    /** Name of the core package */
    public static final String CORE_PACKAGE_NAME = "Core";
    
    /** Location of {@link ResourceBundle} providing error messages for this bundle */
    public static final String CORE_ERROR_MESSAGES_PROPERTIES_BASENAME = "uk/ac/ed/ph/snuggletex/core-error-messages";
    
    public static final String CORE_MATH_CHARACTER_DEFS_RESOURCE_NAME = "uk/ac/ed/ph/snuggletex/core-math-characters.txt";
    public static final String ALL_MATH_CHARACTER_DEFS_RESOURCE_NAME = "uk/ac/ed/ph/snuggletex/all-math-characters.txt";
    public static final String MATH_CHARACTER_ALIASES_RESOURCE_NAME = "uk/ac/ed/ph/snuggletex/math-character-aliases.txt";
    public static final String MATH_CHARACTER_NEGATIONS_RESOURCE_NAME = "uk/ac/ed/ph/snuggletex/math-character-negations.txt";
    
    public static BuiltinCommand CMD_CHAR_BACKSLASH;
    public static BuiltinCommand CMD_FRAC;
    public static BuiltinCommand CMD_ITEM;
    public static BuiltinCommand CMD_LIST_ITEM;
    public static BuiltinCommand CMD_LEFT;
    public static BuiltinCommand CMD_RIGHT;
    public static BuiltinCommand CMD_MROW;
    public static BuiltinCommand CMD_MSUB_OR_MUNDER;
    public static BuiltinCommand CMD_MSUP_OR_MOVER;
    public static BuiltinCommand CMD_MSUBSUP_OR_MUNDEROVER;
    public static BuiltinCommand CMD_NEWCOMMAND;
    public static BuiltinCommand CMD_RENEWCOMMAND;
    public static BuiltinCommand CMD_NEWENVIRONMENT;
    public static BuiltinCommand CMD_RENEWENVIRONMENT;
    public static BuiltinCommand CMD_OVER;
    public static BuiltinCommand CMD_PAR;
    public static BuiltinCommand CMD_PARAGRAPH;
    public static BuiltinCommand CMD_TABLE_ROW;
    public static BuiltinCommand CMD_TABLE_COLUMN;
    public static BuiltinCommand CMD_VERB;
    public static BuiltinCommand CMD_VERBSTAR;
    public static BuiltinCommand CMD_HLINE;
    public static BuiltinCommand CMD_XML_ATTR;

    public static BuiltinEnvironment ENV_VERBATIM;
    public static BuiltinEnvironment ENV_ITEMIZE;
    public static BuiltinEnvironment ENV_ENUMERATE;
    public static BuiltinEnvironment ENV_MATH;
    public static BuiltinEnvironment ENV_DISPLAYMATH;
    public static BuiltinEnvironment ENV_BRACKETED;
    
    private static final SnugglePackage corePackage;


    
    public static final SnugglePackage getPackage() {
        return corePackage;
    }
    
    static {
        corePackage = new SnugglePackage(CORE_PACKAGE_NAME);
        
        /* Set up error messages for this package */
        corePackage.addErrorCodes(CoreErrorCode.values());
        try {
            corePackage.setErrorMessageBundle(ResourceBundle.getBundle(CORE_ERROR_MESSAGES_PROPERTIES_BASENAME));
        }
        catch (MissingResourceException e) {
            throw new SnuggleRuntimeException(e);
        }
        
        /* ======================= MATH CHARACTERS & INPUT MACROS ============================== */
        
        /* Read in main definitions for each defined (generally non-alpha) math character.
         * We read in the 'CORE' defs first so that they can override anything dodgy in
         * the 'ALL' defs, which are auto-generated.
         */
        corePackage.loadMathCharacterDefinitions(CorePackageDefinitions.ALL_MATH_CHARACTER_DEFS_RESOURCE_NAME);
        corePackage.loadMathCharacterDefinitions(CorePackageDefinitions.CORE_MATH_CHARACTER_DEFS_RESOURCE_NAME);
        
        /* Read in details about math char/command negations and aliases */
        corePackage.loadMathCharacterNegations(MATH_CHARACTER_NEGATIONS_RESOURCE_NAME);
        corePackage.loadMathCharacterAliases(MATH_CHARACTER_ALIASES_RESOURCE_NAME);
        
        /* Additional interpretation data for polymorphic math characters */
        /* FIXME: These are all now brackets, so might be better done in a more structured way */
        final Object[] mathCharacterAdditionalInterpretationData = new Object[] {
           '(', new MathBracketInterpretation(MathMLSymbol.OPEN_BRACKET, BracketType.OPENER, true),
           ')', new MathBracketInterpretation(MathMLSymbol.CLOSE_BRACKET, BracketType.CLOSER, true),
           '[', new MathBracketInterpretation(MathMLSymbol.OPEN_SQUARE_BRACKET, BracketType.OPENER, true),
           ']', new MathBracketInterpretation(MathMLSymbol.CLOSE_SQUARE_BRACKET, BracketType.CLOSER, true),
           '<', new MathBracketInterpretation(MathMLSymbol.OPEN_ANGLE_BRACKET, BracketType.OPENER, false),
           '>', new MathBracketInterpretation(MathMLSymbol.CLOSE_ANGLE_BRACKET, BracketType.OPENER, false),
           '|', new MathBracketInterpretation(MathMLSymbol.VERT_BRACKET, BracketType.OPENER_OR_CLOSER, false)
           /* Etc... */
        };
        Object object;
        Integer currentCodePoint = null;
        for (int i=0; i<mathCharacterAdditionalInterpretationData.length; i++) {
            object = mathCharacterAdditionalInterpretationData[i];
            if (object instanceof Character) {
                currentCodePoint = Integer.valueOf((Character) object);
            }
            else if (object instanceof MathInterpretation) {
                if (currentCodePoint==null) {
                    throw new SnuggleLogicException("Bad raw data - currentCodePoint is null and we've now got an Interpretation");
                }
                MathCharacter mathCharacter = corePackage.getMathCharacter(currentCodePoint);
                if (mathCharacter==null) {
                    throw new SnuggleLogicException("No MathCharacter previously defined for codePoint " + currentCodePoint);
                }
                mathCharacter.addInterpretation((Interpretation) object);
            }
            else {
                throw new SnuggleLogicException("Unexpected logic branch: got " + object);
            }
        }
        
        /* =================================== COMMANDS ================================= */
        
        //------------------------------------------------------------
        // Single (funny) character commands. These do not eat trailing whitespace
        //
        // NOTE: The \( and \[ are not included here as they're picked up explicitly during parsing.
        CMD_CHAR_BACKSLASH = corePackage.addSimpleCommand("\\", ALL_MODES, new LineBreakHandler(), null);
        corePackage.addSimpleCommand("$", ALL_MODES, new CharacterCommandHandler("$"), ALLOW_INLINE);
        corePackage.addSimpleCommand("%", ALL_MODES, new CharacterCommandHandler("%"), ALLOW_INLINE);
        corePackage.addSimpleCommand("#", ALL_MODES, new CharacterCommandHandler("#"), ALLOW_INLINE);
        corePackage.addSimpleCommand("&", ALL_MODES, new CharacterCommandHandler("&"), ALLOW_INLINE);
        corePackage.addSimpleCommand("_", ALL_MODES, new CharacterCommandHandler("_"), ALLOW_INLINE);
        corePackage.addSimpleCommand("{", ALL_MODES, new Interpretation[] {
                new MathOperatorInterpretation(MathMLSymbol.OPEN_CURLY_BRACKET),
                new MathBracketInterpretation(MathMLSymbol.OPEN_CURLY_BRACKET, BracketType.OPENER, true),                
            }, new ModeDelegatingHandler(new CharacterCommandHandler("{"), new InterpretableSimpleMathHandler()), null);
        corePackage.addSimpleCommand("}", ALL_MODES, new Interpretation[] {
                new MathOperatorInterpretation(MathMLSymbol.CLOSE_CURLY_BRACKET),
                new MathBracketInterpretation(MathMLSymbol.CLOSE_CURLY_BRACKET, BracketType.CLOSER, true),                
            }, new ModeDelegatingHandler(new CharacterCommandHandler("}"), new InterpretableSimpleMathHandler()), null);
        corePackage.addSimpleCommand(",", ALL_MODES, new SpaceHandler("\u2009", "0.167em"), ALLOW_INLINE); /* Thin space, all modes */
        corePackage.addSimpleCommand(":", MATH_MODE_ONLY, new SpaceHandler(null, "0.222em"), null); /* Medium space, math only */
        corePackage.addSimpleCommand(";", MATH_MODE_ONLY, new SpaceHandler(null, "0.278em"), null); /* Thick space, math only */
        corePackage.addSimpleCommand("!", MATH_MODE_ONLY, new SpaceHandler(null, "-0.167em"), null); /* Negative thin space */
        corePackage.addSimpleCommand(" ", ALL_MODES, new CharacterCommandHandler("\u00a0"), ALLOW_INLINE);
        
        /* Accents (non-math, complex) See Table 3.1, p. 38 on LaTeX companion.
         * (See also accents that apply in either MATH mode only or all modes, declared elsewhere!)
         */
        corePackage.addComplexCommandSameArgMode("'", false, 1, TEXT_MODE_ONLY, new AccentHandler(AccentMaps.ACCENT), ALLOW_INLINE);
        corePackage.addComplexCommandSameArgMode("`", false, 1, TEXT_MODE_ONLY, new AccentHandler(AccentMaps.GRAVE), ALLOW_INLINE);
        corePackage.addComplexCommandSameArgMode("^", false, 1, TEXT_MODE_ONLY, new AccentHandler(AccentMaps.CIRCUMFLEX), ALLOW_INLINE);
        corePackage.addComplexCommandSameArgMode("~", false, 1, TEXT_MODE_ONLY, new AccentHandler(AccentMaps.TILDE), ALLOW_INLINE);
        corePackage.addComplexCommandSameArgMode("\"", false, 1, TEXT_MODE_ONLY, new AccentHandler(AccentMaps.UMLAUT), ALLOW_INLINE);
        
        /* Traditional LaTeX commands */
        CMD_PAR = corePackage.addSimpleCommand("par", TEXT_MODE_ONLY, null, null); /* (This is substituted during fixing) */
        corePackage.addSimpleCommand("newline", ALL_MODES, new LineBreakHandler(), null);
        CMD_VERB = corePackage.addSimpleCommand("verb", PARA_MODE_ONLY, new VerbatimHandler(false), null);
        CMD_VERBSTAR = corePackage.addSimpleCommand("verb*", PARA_MODE_ONLY, new VerbatimHandler(true), null);
        CMD_ITEM = corePackage.addSimpleCommand("item", PARA_MODE_ONLY, new ListEnvironmentHandler(), null);
        corePackage.addComplexCommandOneArg("ensuremath", false, ALL_MODES, MATH, new EnsureMathHandler(), null);
        
        /* TODO: Is there an equivalent of the following in LaTeX for doing "literal" input, sort of like \verb
         * but output using a normal typeface???
         */
        corePackage.addComplexCommandOneArg("literal", false, TEXT_MODE_ONLY, VERBATIM, new LiteralHandler(), null); 

        /* Tree version of a paragraph. The {@link TokenFixer} will create these, removing any
         * instances of {@link #PAR} and {@link TokenType#NEW_PARAGRAPH}.
         * <p>
         * I am not allowing this to be directly input, as this makes processing a bit easier
         * since it avoids the possibility of nested paragraphs.
         */
        CMD_PARAGRAPH = corePackage.addComplexCommandSameArgMode("<paragraph>", false, 1, TEXT_MODE_ONLY, new ParagraphHandler(), START_NEW_XHTML_BLOCK);
        
        /* Tree version of standard \item. Any \items are converted to these during token fixing.
         * I'm not allowing this to be directly input, which makes list handling a bit easier.
         */
        CMD_LIST_ITEM = corePackage.addComplexCommandSameArgMode("<list item>", false, 1, PARA_MODE_ONLY, new ListEnvironmentHandler(), START_NEW_XHTML_BLOCK);
        
        /* Tree-like placeholders for specifying columns and rows in environments such as 'tabular'.
         * We don't allow to be inputed as the containment requirements can make it awkward to ensure
         * that the input is valid. These tokens are produced during the fixing process and make it
         * easier to handle the table content further down the line.
         */
        CMD_TABLE_ROW = corePackage.addComplexCommandSameArgMode("<tr>", false, 1, ALL_MODES, null, null);
        CMD_TABLE_COLUMN = corePackage.addComplexCommandSameArgMode("<td>", false, 1, ALL_MODES, null, null);
        
        /* We'll support the usual LaTeX sectioning commands...
         * 
         * TODO: Decide whether to support traditional LaTeX numbering or not.
         */
        corePackage.addComplexCommandOneArg("section", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuildingHandler("h2"), START_NEW_XHTML_BLOCK);
        corePackage.addComplexCommandOneArg("section*", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuildingHandler("h2"), START_NEW_XHTML_BLOCK);
        corePackage.addComplexCommandOneArg("subsection", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuildingHandler("h3"), START_NEW_XHTML_BLOCK);
        corePackage.addComplexCommandOneArg("subsection*", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuildingHandler("h3"), START_NEW_XHTML_BLOCK);
        corePackage.addComplexCommandOneArg("subsubsection", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuildingHandler("h4"), START_NEW_XHTML_BLOCK);
        corePackage.addComplexCommandOneArg("subsubsection*", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuildingHandler("h4"), START_NEW_XHTML_BLOCK);
        
        /* Old-style P/LR mode style change macros, slightly complicated due to the way they
         * apply until the end of the current group, resulting in a lack of tree structure.
         * These are replaced by environments of the same name during token fixing, which agrees
         * with LaTeX spec. (see p28 of Lamport)
         * 
         * Note: each of these MUST be accompanied by a corresponding Environment definition.
         * (These are declared later in this file.)
         * 
         * As of 1.0.0-beta4, these are supported in MATH mode and behave in the same way as
         * the corresponding member of \mathxx{..} command family (defined below).
         */
        corePackage.addSimpleCommand("em", ALL_MODES, StyleDeclarationInterpretation.EM, null, null);
        corePackage.addSimpleCommand("bf", ALL_MODES, StyleDeclarationInterpretation.BF, null, null);
        corePackage.addSimpleCommand("rm", ALL_MODES, StyleDeclarationInterpretation.RM, null, null);
        corePackage.addSimpleCommand("it", ALL_MODES, StyleDeclarationInterpretation.IT, null, null);
        corePackage.addSimpleCommand("tt", ALL_MODES, StyleDeclarationInterpretation.TT, null, null);
        corePackage.addSimpleCommand("sc", ALL_MODES, StyleDeclarationInterpretation.SC, null, null);
        corePackage.addSimpleCommand("sl", ALL_MODES, StyleDeclarationInterpretation.SL, null, null);
        corePackage.addSimpleCommand("sf", ALL_MODES, StyleDeclarationInterpretation.SF, null, null);
        
        /* New style P/LR mode style change macros. These take the text they are being applied to
         * as a single argument.
         * 
         * As of 1.0.0-beta4, we now support using these macros in MATH mode. We force the arguments
         * into LR mode here so that the resulting content will end up inside <mtext/>
         * element(s) wrapped inside a <mstyle/> setting the appropriate style.
         */
        StyleInterpretationHandler styleInterpretationNodeBuilder = new StyleInterpretationHandler(); /* (Stateless so can share) */
        corePackage.addComplexCommandOneArg("textrm", false, ALL_MODES, LR, StyleDeclarationInterpretation.RM, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addComplexCommandOneArg("textsf", false, ALL_MODES, LR, StyleDeclarationInterpretation.SF, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addComplexCommandOneArg("textit", false, ALL_MODES, LR, StyleDeclarationInterpretation.IT, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addComplexCommandOneArg("textsl", false, ALL_MODES, LR, StyleDeclarationInterpretation.SL, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addComplexCommandOneArg("textsc", false, ALL_MODES, LR, StyleDeclarationInterpretation.SC, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addComplexCommandOneArg("textbf", false, ALL_MODES, LR, StyleDeclarationInterpretation.BF, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addComplexCommandOneArg("texttt", false, ALL_MODES, LR, StyleDeclarationInterpretation.TT, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addComplexCommandOneArg("emph",   false, ALL_MODES, LR, StyleDeclarationInterpretation.EM, styleInterpretationNodeBuilder, ALLOW_INLINE);
        
        /* Text size control macros. As above, these are converted to environments of the same name
         * during token fixing, which are easier to deal with.
         */
        corePackage.addSimpleCommand("tiny", TEXT_MODE_ONLY, StyleDeclarationInterpretation.TINY, null, null);
        corePackage.addSimpleCommand("scriptsize", TEXT_MODE_ONLY, StyleDeclarationInterpretation.SCRIPTSIZE, null, null);
        corePackage.addSimpleCommand("footnotesize", TEXT_MODE_ONLY, StyleDeclarationInterpretation.FOOTNOTESIZE, null, null);
        corePackage.addSimpleCommand("small", TEXT_MODE_ONLY, StyleDeclarationInterpretation.SMALL, null, null);
        corePackage.addSimpleCommand("normalsize", TEXT_MODE_ONLY, StyleDeclarationInterpretation.NORMALSIZE, null, null);
        corePackage.addSimpleCommand("large", TEXT_MODE_ONLY, StyleDeclarationInterpretation.LARGE, null, null);
        corePackage.addSimpleCommand("Large", TEXT_MODE_ONLY, StyleDeclarationInterpretation.LARGE_2, null, null);
        corePackage.addSimpleCommand("LARGE", TEXT_MODE_ONLY, StyleDeclarationInterpretation.LARGE_3, null, null);
        corePackage.addSimpleCommand("huge", TEXT_MODE_ONLY, StyleDeclarationInterpretation.HUGE, null, null);
        corePackage.addSimpleCommand("Huge", TEXT_MODE_ONLY, StyleDeclarationInterpretation.HUGE_2, null, null);
        
        /* Non-English symbols (non-math, simple) See Table 3.2, p.39 on LaTeX companion */
        corePackage.addSimpleCommand("ae", TEXT_MODE_ONLY, new CharacterCommandHandler("\u00e6"), ALLOW_INLINE);
        corePackage.addSimpleCommand("oe", TEXT_MODE_ONLY, new CharacterCommandHandler("\u0153"), ALLOW_INLINE);
        
        /* Special punctuation (non-math, simple) See p.39 on LC */
        corePackage.addSimpleCommand("dag", TEXT_MODE_ONLY, new CharacterCommandHandler("\u2020"), ALLOW_INLINE);
        
        /* Ellipses (All modes) */
        corePackage.addSimpleCommand("ldots", ALL_MODES, new MathIdentifierInterpretation("\u2026"), new TextSafeInterpretableMathIdentifierHandler(), ALLOW_INLINE);
        
        /* Other symbols valid in all modes */
        corePackage.addSimpleCommand("pounds", ALL_MODES, new MathIdentifierInterpretation("\u00a3"), new TextSafeInterpretableMathIdentifierHandler(), ALLOW_INLINE);
        
        //---------------------------------------------------------------
        // Math Mode stuff (see LaTeX Companion pp39-52)
        
        /* Semantic versions of MathML "&ApplyFunction;" and "&InvisibleTimes;" entities */
        corePackage.addSimpleMathCommand("af", new MathOperatorInterpretation(MathMLSymbol.APPLY_FUNCTION));
        corePackage.addSimpleMathCommand("itimes", new MathOperatorInterpretation(MathMLSymbol.INVISIBLE_TIMES));
        
        /* Placeholders for corresponding MathML constructs. These are substituted from traditional LaTeX constructs
         * by {@link TokenFixer}.
         * 
         * Note that subscript/superscripts will either be converted to <msub/> et al or <munder/> et al
         * according to the type of operator being handled.
         */
        MathLimitsHandler mathLimitsBuilder = new MathLimitsHandler();
        CMD_MROW = corePackage.addComplexCommandSameArgMode("<mrow>", false, 1, MATH_MODE_ONLY, new MrowHandler(), null);
        CMD_MSUB_OR_MUNDER = corePackage.addComplexCommandSameArgMode("<msubormunder>", false, 2, MATH_MODE_ONLY, mathLimitsBuilder, null);
        CMD_MSUP_OR_MOVER = corePackage.addComplexCommandSameArgMode("<msupormover>", false, 2, MATH_MODE_ONLY, mathLimitsBuilder, null);
        CMD_MSUBSUP_OR_MUNDEROVER = corePackage.addComplexCommandSameArgMode("<msubsupormunderover>", false, 3, MATH_MODE_ONLY, mathLimitsBuilder, null);
        
        /* A related idea to sub/super is \\stackrel */
        corePackage.addComplexCommand("stackrel", false, 2, MATH_MODE_ONLY, null, new MathStackrelHandler(), null);
        
        /* Styling (c.f. equivalents in text mode, listed above) */
        corePackage.addComplexCommandSameArgMode("mathrm", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.RM, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addComplexCommandSameArgMode("mathsf", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.SF, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addComplexCommandSameArgMode("mathit", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.IT, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addComplexCommandSameArgMode("mathbf", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.BF, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addComplexCommandSameArgMode("mathtt", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.TT, styleInterpretationNodeBuilder, ALLOW_INLINE);
        
        /* Styling done via character mappings (e.g. calligraphic) */
        corePackage.addComplexCommandSameArgMode("mathcal", false, 1, MATH_MODE_ONLY, new MathVariantMapHandler(MathVariantMaps.SCRIPT), null);
        corePackage.addComplexCommandSameArgMode("mathsc", false, 1, MATH_MODE_ONLY, new MathVariantMapHandler(MathVariantMaps.SCRIPT), null);
        corePackage.addComplexCommandSameArgMode("mathbb", false, 1, MATH_MODE_ONLY, new MathVariantMapHandler(MathVariantMaps.DOUBLE_STRUCK), null);
        corePackage.addComplexCommandSameArgMode("mathfrak", false, 1, MATH_MODE_ONLY, new MathVariantMapHandler(MathVariantMaps.FRAKTUR), null);
        
        /* Math "functions" (treated as identifiers in MathML) */
        corePackage.addSimpleMathCommand("arccos", new MathFunctionInterpretation("arccos"));
        corePackage.addSimpleMathCommand("arcsin", new MathFunctionInterpretation("arcsin"));
        corePackage.addSimpleMathCommand("arctan", new MathFunctionInterpretation("arctan"));
        corePackage.addSimpleMathCommand("arg", new MathFunctionInterpretation("arg"));
        corePackage.addSimpleMathCommand("cos", new MathFunctionInterpretation("cos"));
        corePackage.addSimpleMathCommand("cosh", new MathFunctionInterpretation("cosh"));
        corePackage.addSimpleMathCommand("cot", new MathFunctionInterpretation("cot"));
        corePackage.addSimpleMathCommand("coth", new MathFunctionInterpretation("coth"));
        corePackage.addSimpleMathCommand("csc", new MathFunctionInterpretation("csc"));
        corePackage.addSimpleMathCommand("deg", new MathFunctionInterpretation("deg"));
        corePackage.addSimpleMathCommand("det", new MathFunctionInterpretation("det"));
        corePackage.addSimpleMathCommand("dim", new MathFunctionInterpretation("dim"));
        corePackage.addSimpleMathCommand("exp", new MathFunctionInterpretation("exp"));
        corePackage.addSimpleMathCommand("gcd", new MathFunctionInterpretation("gcd"));
        corePackage.addSimpleMathCommand("hom", new MathFunctionInterpretation("hom"));
        corePackage.addSimpleMathCommand("inf", new MathFunctionInterpretation("inf"));
        corePackage.addSimpleMathCommand("ker", new MathFunctionInterpretation("ker"));
        corePackage.addSimpleMathCommand("lg", new MathFunctionInterpretation("lg"));
        corePackage.addSimpleMathCommand("lcm", new MathFunctionInterpretation("lcm"));
        corePackage.addSimpleMathCommand("lim", new MathFunctionInterpretation("lim"));
        corePackage.addSimpleMathCommand("liminf", new MathFunctionInterpretation("lim inf"));
        corePackage.addSimpleMathCommand("limsup", new MathFunctionInterpretation("lim sup"));
        corePackage.addSimpleMathCommand("ln", new MathFunctionInterpretation("ln"));
        corePackage.addSimpleMathCommand("log", new MathFunctionInterpretation("log"));
        corePackage.addSimpleMathCommand("max", new MathFunctionInterpretation("max"));
        corePackage.addSimpleMathCommand("min", new MathFunctionInterpretation("min"));
        corePackage.addSimpleMathCommand("Pr", new MathFunctionInterpretation("Pr"));
        corePackage.addSimpleMathCommand("sec", new MathFunctionInterpretation("sec"));
        corePackage.addSimpleMathCommand("sin", new MathFunctionInterpretation("sin"));
        corePackage.addSimpleMathCommand("sinh", new MathFunctionInterpretation("sinh"));
        corePackage.addSimpleMathCommand("sup", new MathFunctionInterpretation("sup"));
        corePackage.addSimpleMathCommand("tan", new MathFunctionInterpretation("tan"));
        corePackage.addSimpleMathCommand("tanh", new MathFunctionInterpretation("tanh"));
        
        /* Extra Math functions (added for consistency with standard Content MathML operators) */
        corePackage.addSimpleMathCommand("sech", new MathFunctionInterpretation("sech"));
        corePackage.addSimpleMathCommand("csch", new MathFunctionInterpretation("csch"));
        corePackage.addSimpleMathCommand("coth", new MathFunctionInterpretation("coth"));
        corePackage.addSimpleMathCommand("arcsec", new MathFunctionInterpretation("arcsec"));
        corePackage.addSimpleMathCommand("arccsc", new MathFunctionInterpretation("arccsc"));
        corePackage.addSimpleMathCommand("arccot", new MathFunctionInterpretation("arccot"));
        corePackage.addSimpleMathCommand("arccosh", new MathFunctionInterpretation("arccosh"));
        corePackage.addSimpleMathCommand("arcsinh", new MathFunctionInterpretation("arcsinh"));
        corePackage.addSimpleMathCommand("arctanh", new MathFunctionInterpretation("arctanh"));
        corePackage.addSimpleMathCommand("arcsech", new MathFunctionInterpretation("arcsech"));
        corePackage.addSimpleMathCommand("arccsch", new MathFunctionInterpretation("arccsch"));
        corePackage.addSimpleMathCommand("arccoth", new MathFunctionInterpretation("arccoth"));

        /* Add big limits to certain symbols */
        MathBigLimitOwnerInterpretation bigLimitOwner = new MathBigLimitOwnerInterpretation();
        String[] bigLimitCommandNames = {
                /* NB: no "int" here */
                "sum", "prod", "coprod", "oint",
                "bigcap", "bigcup", "bigsqcup", "bigvee", "bigwedge",
                "bigodot", "bigoplus", "biguplus"
        };
        for (String commandName : bigLimitCommandNames) {
            /* (These commands will already have been defined) */
            BuiltinCommand command = corePackage.getBuiltinCommandByTeXName(commandName);
            MathCharacter mathCharacter = ((MathCharacterInterpretation) command.getInterpretation(InterpretationType.MATH_CHARACTER)).getMathCharacter();
            
            mathCharacter.addInterpretation(bigLimitOwner);
        }

        /* This is a LaTeX-specific combiner macro that always comes before certain characters
         * or commands...
         */
        CombinerTargetMatcher notTargetMatcher = new CombinerTargetMatcher() {
            public boolean isAllowed(FlowToken target) {
                return target.hasInterpretationType(InterpretationType.MATH_NEGATABLE);
            }  
        };
        corePackage.addCombinerCommand("not", MATH_MODE_ONLY, notTargetMatcher, new MathNotHandler(), null);


        
        /* Math combiner commands that absorb the (bracket) token immediately after. These are
         * converted to fences during token fixing.
         */
        CombinerTargetMatcher bracketTargetMatcher = new MathFenceHandler.BracketCombinerTargetMatcher();
        CMD_LEFT = corePackage.addCombinerCommand("left", MATH_MODE_ONLY, bracketTargetMatcher, null, null);
        CMD_RIGHT = corePackage.addCombinerCommand("right", MATH_MODE_ONLY, bracketTargetMatcher, null, null);
        
        /* Special bracket commands */
        corePackage.addSimpleMathCommand("vert",
                new MathOperatorInterpretation(MathMLSymbol.VERT_BRACKET),
                new MathBracketInterpretation(MathMLSymbol.VERT_BRACKET, BracketType.OPENER_OR_CLOSER, true));
        corePackage.addSimpleMathCommand("Vert",
                new MathOperatorInterpretation(MathMLSymbol.DOUBLE_VERT_BRACKET),
                new MathBracketInterpretation(MathMLSymbol.DOUBLE_VERT_BRACKET, BracketType.OPENER_OR_CLOSER, true));


        /* Complex math macros */
        corePackage.addComplexCommandSameArgMode("sqrt", true, 1, MATH_MODE_ONLY, new MathRootHandler(), null);
        CMD_FRAC = corePackage.addComplexCommandSameArgMode("frac", false, 2, MATH_MODE_ONLY, new MathComplexCommandHandler("mfrac"), null);
        CMD_OVER = corePackage.addSimpleCommand("over", MATH_MODE_ONLY, null, null); /* TeX style fractions {... \over ...}, replaced during fixing *;
        
        /* Spacing */
        corePackage.addSimpleCommand("quad", ALL_MODES, new SpaceHandler("\u00a0", "1em"), null);
        corePackage.addSimpleCommand("qquad", ALL_MODES, new SpaceHandler("\u00a0\u00a0", "2em"), null);
        corePackage.addComplexCommandSameArgMode("hspace", false, 1, ALL_MODES, new HSpaceHandler(), null);
        corePackage.addComplexCommandSameArgMode("hspace*", false, 1, ALL_MODES, new HSpaceHandler(), null);

        /* Math accents */
        corePackage.addComplexCommandSameArgMode("hat", false, 1, MATH_MODE_ONLY, new AccentHandler(AccentMaps.CIRCUMFLEX, '\u0302', "mover"), null);
        corePackage.addComplexCommandSameArgMode("bar", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u0304', "mover"), null);
        corePackage.addComplexCommandSameArgMode("vec", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u2192', "mover"), null);
        corePackage.addComplexCommandSameArgMode("dot", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u0307', "mover"), null);
        corePackage.addComplexCommandSameArgMode("ddot", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u0308', "mover"), null);
        corePackage.addComplexCommandSameArgMode("tilde", false, 1, MATH_MODE_ONLY, new AccentHandler(AccentMaps.TILDE, '~', "mover"), null);
        corePackage.addComplexCommandSameArgMode("widehat", false, 1, MATH_MODE_ONLY, new AccentHandler(AccentMaps.CIRCUMFLEX, '\u0302', "mover"), null);
        corePackage.addComplexCommandSameArgMode("widetilde", false, 1, MATH_MODE_ONLY, new AccentHandler(AccentMaps.TILDE, '\u02dc', "mover"), null);
        corePackage.addComplexCommandSameArgMode("overline", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u00af', "mover"), null); /* (NB: U+AF gives better visual result than U+305) */
        corePackage.addComplexCommandSameArgMode("overbrace", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\ufe37', "mover"), null);
        corePackage.addComplexCommandSameArgMode("underbrace", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\ufe38', "munder"), null);
        corePackage.addComplexCommandSameArgMode("overrightarrow", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u20d7', "mover"), null);
        corePackage.addComplexCommandSameArgMode("overleftarrow", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u20d6', "mover"), null);
        
        /* Dual-mode accents */
        corePackage.addComplexCommandSameArgMode("underline", false, 1, ALL_MODES, StyleDeclarationInterpretation.UNDERLINE, new ModeDelegatingHandler(styleInterpretationNodeBuilder, new AccentHandler(null, '\u00af', "munder")), null);
        
        /* Complex multi-mode macros */
        corePackage.addComplexCommandOneArg("mbox", false, ALL_MODES, LR, new BoxHandler("mbox"), null);
        corePackage.addComplexCommandOneArg("fbox", false, ALL_MODES, LR, new BoxHandler("fbox"), null);
        
        /* Table stuff */
        CMD_HLINE = corePackage.addSimpleCommand("hline", ALL_MODES, new TabularHandler(), IGNORE);
        
        /* Commands for creating user-defined commands and environments */
        DoNothingHandler doNothingHandler = new DoNothingHandler();
        CMD_NEWCOMMAND = corePackage.addComplexCommandSameArgMode("newcommand", false, 1, ALL_MODES, doNothingHandler, IGNORE);
        CMD_RENEWCOMMAND = corePackage.addComplexCommandSameArgMode("renewcommand", false, 1, ALL_MODES, doNothingHandler, IGNORE);
        CMD_NEWENVIRONMENT = corePackage.addComplexCommandSameArgMode("newenvironment", false, 2, ALL_MODES, doNothingHandler, IGNORE);
        CMD_RENEWENVIRONMENT = corePackage.addComplexCommandSameArgMode("renewenvironment", false, 2, ALL_MODES, doNothingHandler, IGNORE);
        
        /* Special XHTML helpers */
        corePackage.addComplexCommand("href", true, 1, TEXT_MODE_ONLY, new LaTeXMode[] { LR, VERBATIM }, new HrefHandler(), ALLOW_INLINE);
        corePackage.addComplexCommandOneArg("anchor", false, TEXT_MODE_ONLY, VERBATIM, new AnchorHandler(), ALLOW_INLINE);
        corePackage.addComplexCommandOneArg("anchor*", false, TEXT_MODE_ONLY, LR, new AnchorHandler(), ALLOW_INLINE);
        
        /* Special MathML helpers */
        corePackage.addComplexCommandOneArg("units", false, MATH_MODE_ONLY, MATH, new UnitsHandler(), null);
        
        /* Commands for creating custom XML (also see related environments) */
        CMD_XML_ATTR = corePackage.addComplexCommand("xmlAttr", false, 3, ALL_MODES, new LaTeXMode[] { LR, LR, LR }, new XMLAttrHandler(), IGNORE);
        corePackage.addComplexCommand("xmlBlockElement", true, 3, ALL_MODES, new LaTeXMode[] { LR, LR, LR, null }, new XMLBlockElementHandler(), START_NEW_XHTML_BLOCK);
        corePackage.addComplexCommand("xmlInlineElement", true, 3, ALL_MODES, new LaTeXMode[] { LR, LR, LR, null }, new XMLInlineElementHandler(), ALLOW_INLINE);
        corePackage.addComplexCommandOneArg("xmlName", false, ALL_MODES, VERBATIM, new XMLNameOrIdHandler(XMLNameOrIdHandler.NAME), IGNORE);
        corePackage.addComplexCommandOneArg("xmlName*", false, ALL_MODES, LR, new XMLNameOrIdHandler(XMLNameOrIdHandler.NAME), IGNORE);
        corePackage.addComplexCommandOneArg("xmlId", false, ALL_MODES, VERBATIM, new XMLNameOrIdHandler(XMLNameOrIdHandler.ID), IGNORE);
        corePackage.addComplexCommandOneArg("xmlId*", false, ALL_MODES, LR, new XMLNameOrIdHandler(XMLNameOrIdHandler.ID), IGNORE);
        corePackage.addComplexCommandSameArgMode("xmlUnparse", false, 1, TEXT_MODE_ONLY, new XMLUnparseHandler(), ALLOW_INLINE);
        corePackage.addComplexCommandOneArg("ux", false, ALL_MODES, LR, new InsertUnicodeHandler(), IGNORE);
        
        /* Special commands for managing simple "variables" */
        corePackage.addComplexCommandSameArgMode("getvar", true, 1, ALL_MODES, new GetVarHandler(), IGNORE);
        corePackage.addComplexCommandSameArgMode("setvar", true, 2, ALL_MODES, new SetVarHandler(), IGNORE);
        
        /* =================================== ENVIRONMENTS ================================= */
        
        ENV_MATH = corePackage.addEnvironment("math", TEXT_MODE_ONLY, MATH, null, new MathEnvironmentHandler(), ALLOW_INLINE);
        ENV_DISPLAYMATH = corePackage.addEnvironment("displaymath", TEXT_MODE_ONLY, MATH, null, new MathEnvironmentHandler(), ALLOW_INLINE);
        ENV_VERBATIM = corePackage.addEnvironment("verbatim", PARA_MODE_ONLY, VERBATIM, null, new VerbatimHandler(false), START_NEW_XHTML_BLOCK);
        ENV_ITEMIZE = corePackage.addEnvironment("itemize", PARA_MODE_ONLY, null, Interpretation.LIST, new ListEnvironmentHandler(), START_NEW_XHTML_BLOCK);
        ENV_ENUMERATE = corePackage.addEnvironment("enumerate", PARA_MODE_ONLY, null, Interpretation.LIST, new ListEnvironmentHandler(), START_NEW_XHTML_BLOCK);
        
        corePackage.addEnvironment("tabular", false, 1, PARA_MODE_ONLY, PARAGRAPH, Interpretation.TABULAR, new TabularHandler(), START_NEW_XHTML_BLOCK);
        corePackage.addEnvironment("array", false, 1, MATH_MODE_ONLY, MATH, Interpretation.TABULAR, new ArrayHandler(), null);
        corePackage.addEnvironment("cases", MATH_MODE_ONLY, MATH, Interpretation.TABULAR, new MatrixHandler(2, MathMLSymbol.OPEN_CURLY_BRACKET, ""), null);
        corePackage.addEnvironment("eqnarray", PARA_MODE_ONLY, MATH, Interpretation.TABULAR, new EqnArrayHandler(), START_NEW_XHTML_BLOCK);
        corePackage.addEnvironment("eqnarray*", PARA_MODE_ONLY, MATH, Interpretation.TABULAR, new EqnArrayHandler(), START_NEW_XHTML_BLOCK);
        
        /* AMS-LaTeX convenience environments */
        corePackage.addEnvironment("matrix", MATH_MODE_ONLY, MATH, Interpretation.TABULAR, new MatrixHandler(), null);
        corePackage.addEnvironment("pmatrix", MATH_MODE_ONLY, MATH, Interpretation.TABULAR, new MatrixHandler(MathMLSymbol.OPEN_BRACKET, MathMLSymbol.CLOSE_BRACKET), null);
        corePackage.addEnvironment("bmatrix", MATH_MODE_ONLY, MATH, Interpretation.TABULAR, new MatrixHandler(MathMLSymbol.OPEN_SQUARE_BRACKET, MathMLSymbol.CLOSE_SQUARE_BRACKET), null);
        corePackage.addEnvironment("Bmatrix", MATH_MODE_ONLY, MATH, Interpretation.TABULAR, new MatrixHandler(MathMLSymbol.OPEN_CURLY_BRACKET, MathMLSymbol.CLOSE_CURLY_BRACKET), null);
        corePackage.addEnvironment("vmatrix", MATH_MODE_ONLY, MATH, Interpretation.TABULAR, new MatrixHandler(MathMLSymbol.VERT_BRACKET, MathMLSymbol.VERT_BRACKET), null);
        corePackage.addEnvironment("Vmatrix", MATH_MODE_ONLY, MATH, Interpretation.TABULAR, new MatrixHandler(MathMLSymbol.DOUBLE_VERT_BRACKET, MathMLSymbol.DOUBLE_VERT_BRACKET), null);
        
        /* Simple text environments */
        corePackage.addEnvironment("quote", PARA_MODE_ONLY, PARAGRAPH, null, new SimpleXHTMLContainerBuildingHandler("blockquote"), START_NEW_XHTML_BLOCK);
        
        /* Text justification environments. (Note that each line is supposed to be delimited by '\\' */
        corePackage.addEnvironment("center", PARA_MODE_ONLY, PARAGRAPH, null, new SimpleXHTMLContainerBuildingHandler("div", "center"), START_NEW_XHTML_BLOCK);
        corePackage.addEnvironment("flushleft", PARA_MODE_ONLY, PARAGRAPH, null, new SimpleXHTMLContainerBuildingHandler("div", "flushleft"), START_NEW_XHTML_BLOCK);
        corePackage.addEnvironment("flushright", PARA_MODE_ONLY, PARAGRAPH, null, new SimpleXHTMLContainerBuildingHandler("div", "flushright"), START_NEW_XHTML_BLOCK);
        
        /* Alternative versions of \em and friends. These are converted internally to
         * environments as they're easier to deal with like that.
         */
        corePackage.addEnvironment("em", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.EM, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("bf", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.BF, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("rm", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.RM, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("it", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.IT, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("tt", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.TT, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("sc", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SC, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("sl", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SL, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("sf", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SF, styleInterpretationNodeBuilder, ALLOW_INLINE);
        
        corePackage.addEnvironment("tiny", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.TINY, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("scriptsize", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SCRIPTSIZE, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("footnotesize", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.FOOTNOTESIZE, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("small", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SMALL, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("normalsize", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.NORMALSIZE, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("large", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.LARGE, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("Large", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.LARGE_2, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("LARGE", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.LARGE_3, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("huge", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.HUGE, styleInterpretationNodeBuilder, ALLOW_INLINE);
        corePackage.addEnvironment("Huge", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.HUGE_2, styleInterpretationNodeBuilder, ALLOW_INLINE);
        
        /* Special internal environment for enclosing content within two brackets. These are
         * inferred during token fixing, also handling the case where an opener or closer is missing.
         * When both an opener and closer is provided, this generates a MathML <mfenced/> element;
         * otherwise we degrade nicely.
         * 
         * NOTE: The arguments for this actually end up being in MATH mode.
         */
        ENV_BRACKETED = corePackage.addEnvironment("<mfenced>", false, 2, MATH_MODE_ONLY, MATH, null, new MathFenceHandler(), null);

        /* Environments for generating custom XML islands (see corresponding command versions as well) */
        corePackage.addEnvironment("xmlBlockElement", true, 2, ALL_MODES, null, null, new XMLBlockElementHandler(), START_NEW_XHTML_BLOCK);
        corePackage.addEnvironment("xmlInlineElement", true, 2, ALL_MODES, null, null, new XMLInlineElementHandler(), ALLOW_INLINE);
        corePackage.addEnvironment("xmlUnparse", false, 0, TEXT_MODE_ONLY, null, null, new XMLUnparseHandler(), ALLOW_INLINE);
    }

}
