/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
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

import uk.ac.ed.ph.snuggletex.dombuilding.AccentHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.AnchorHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.ArrayHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.AssumeHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.BoxHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.CharacterCommandHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.DoNothingHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EnsureMathHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EqnArrayHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.GetVarHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.HSpaceHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.HrefHandler;
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
import uk.ac.ed.ph.snuggletex.semantics.MathFunctionInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLSymbol;
import uk.ac.ed.ph.snuggletex.semantics.MathNegatableInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.StyleDeclarationInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.TabularInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketInterpretation.BracketType;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

/**
 * This defines the default {@link DefinitionMap} (containing {@link BuiltinCommand} and
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
public final class GlobalBuiltins {
    
    public static BuiltinCommand CMD_CHAR_BACKSLASH;
    public static BuiltinCommand CMD_FRAC;
    public static BuiltinCommand CMD_NOT;
    public static BuiltinCommand CMD_APPLY_FUNCTION;
    public static BuiltinCommand CMD_INVISIBLE_TIMES;
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
    
    private static final DefinitionMap map;
    
    public static final DefinitionMap getDefinitionMap() {
        return map;
    }
    
    static {
        map = new DefinitionMap();
        
        /* =================================== COMMANDS ================================= */
        
        //------------------------------------------------------------
        // Single (funny) character commands. These do not eat trailing whitespace
        //
        // NOTE: The \( and \[ are not included here as they're picked up explicitly during parsing.
        CMD_CHAR_BACKSLASH = map.addSimpleCommand("\\", ALL_MODES, new LineBreakHandler(), null);
        map.addSimpleCommand("$", ALL_MODES, new CharacterCommandHandler("$"), ALLOW_INLINE);
        map.addSimpleCommand("%", ALL_MODES, new CharacterCommandHandler("%"), ALLOW_INLINE);
        map.addSimpleCommand("#", ALL_MODES, new CharacterCommandHandler("#"), ALLOW_INLINE);
        map.addSimpleCommand("&", ALL_MODES, new CharacterCommandHandler("&"), ALLOW_INLINE);
        map.addSimpleCommand("_", ALL_MODES, new CharacterCommandHandler("_"), ALLOW_INLINE);
        map.addSimpleCommand("{", ALL_MODES, new Interpretation[] {
                new MathOperatorInterpretation(MathMLSymbol.OPEN_CURLY_BRACKET),
                new MathBracketInterpretation(MathMLSymbol.OPEN_CURLY_BRACKET, BracketType.OPENER, true),                
            }, new ModeDelegatingHandler(new CharacterCommandHandler("{"), new InterpretableSimpleMathHandler()), null);
        map.addSimpleCommand("}", ALL_MODES, new Interpretation[] {
                new MathOperatorInterpretation(MathMLSymbol.CLOSE_CURLY_BRACKET),
                new MathBracketInterpretation(MathMLSymbol.CLOSE_CURLY_BRACKET, BracketType.CLOSER, true),                
            }, new ModeDelegatingHandler(new CharacterCommandHandler("}"), new InterpretableSimpleMathHandler()), null);
        map.addSimpleCommand(",", ALL_MODES, new SpaceHandler("\u2009", "0.167em"), ALLOW_INLINE); /* Thin space, all modes */
        map.addSimpleCommand(":", MATH_MODE_ONLY, new SpaceHandler(null, "0.222em"), null); /* Medium space, math only */
        map.addSimpleCommand(";", MATH_MODE_ONLY, new SpaceHandler(null, "0.278em"), null); /* Thick space, math only */
        map.addSimpleCommand("!", MATH_MODE_ONLY, new SpaceHandler(null, "-0.167em"), null); /* Negative thin space */
        map.addSimpleCommand(" ", ALL_MODES, new CharacterCommandHandler("\u00a0"), ALLOW_INLINE);
        
        /* Accents (non-math, complex) See Table 3.1, p. 38 on LaTeX companion.
         * (See also accents that apply in either MATH mode only or all modes, declared elsewhere!)
         */
        map.addComplexCommandSameArgMode("'", false, 1, TEXT_MODE_ONLY, new AccentHandler(AccentMaps.ACCENT), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("`", false, 1, TEXT_MODE_ONLY, new AccentHandler(AccentMaps.GRAVE), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("^", false, 1, TEXT_MODE_ONLY, new AccentHandler(AccentMaps.CIRCUMFLEX), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("~", false, 1, TEXT_MODE_ONLY, new AccentHandler(AccentMaps.TILDE), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("\"", false, 1, TEXT_MODE_ONLY, new AccentHandler(AccentMaps.UMLAUT), ALLOW_INLINE);
        
        /* Traditional LaTeX commands */
        CMD_PAR = map.addSimpleCommand("par", TEXT_MODE_ONLY, null, null); /* (This is substituted during fixing) */
        map.addSimpleCommand("newline", ALL_MODES, new LineBreakHandler(), null);
        CMD_VERB = map.addSimpleCommand("verb", PARA_MODE_ONLY, new VerbatimHandler(false), null);
        CMD_VERBSTAR = map.addSimpleCommand("verb*", PARA_MODE_ONLY, new VerbatimHandler(true), null);
        CMD_ITEM = map.addSimpleCommand("item", PARA_MODE_ONLY, new ListEnvironmentHandler(), null);
        map.addComplexCommandOneArg("ensuremath", false, ALL_MODES, MATH, new EnsureMathHandler(), null);
        
        /* TODO: Is there an equivalent of the following in LaTeX for doing "literal" input, sort of like \verb
         * but output using a normal typeface???
         */
        map.addComplexCommandOneArg("literal", false, TEXT_MODE_ONLY, VERBATIM, new LiteralHandler(), null); 

        /* Tree version of a paragraph. The {@link TokenFixer} will create these, removing any
         * instances of {@link #PAR} and {@link TokenType#NEW_PARAGRAPH}.
         * <p>
         * I am not allowing this to be directly input, as this makes processing a bit easier
         * since it avoids the possibility of nested paragraphs.
         */
        CMD_PARAGRAPH = map.addComplexCommandSameArgMode("<paragraph>", false, 1, TEXT_MODE_ONLY, new ParagraphHandler(), START_NEW_XHTML_BLOCK);
        
        /* Tree version of standard \item. Any \items are converted to these during token fixing.
         * I'm not allowing this to be directly input, which makes list handling a bit easier.
         */
        CMD_LIST_ITEM = map.addComplexCommandSameArgMode("<list item>", false, 1, PARA_MODE_ONLY, new ListEnvironmentHandler(), START_NEW_XHTML_BLOCK);
        
        /* Tree-like placeholders for specifying columns and rows in environments such as 'tabular'.
         * We don't allow to be inputed as the containment requirements can make it awkward to ensure
         * that the input is valid. These tokens are produced during the fixing process and make it
         * easier to handle the table content further down the line.
         */
        CMD_TABLE_ROW = map.addComplexCommandSameArgMode("<tr>", false, 1, ALL_MODES, null, null);
        CMD_TABLE_COLUMN = map.addComplexCommandSameArgMode("<td>", false, 1, ALL_MODES, null, null);
        
        /* We'll support the usual LaTeX sectioning commands...
         * 
         * TODO: Decide whether to support traditional LaTeX numbering or not.
         */
        map.addComplexCommandOneArg("section", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuildingHandler("h2"), START_NEW_XHTML_BLOCK);
        map.addComplexCommandOneArg("section*", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuildingHandler("h2"), START_NEW_XHTML_BLOCK);
        map.addComplexCommandOneArg("subsection", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuildingHandler("h3"), START_NEW_XHTML_BLOCK);
        map.addComplexCommandOneArg("subsection*", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuildingHandler("h3"), START_NEW_XHTML_BLOCK);
        map.addComplexCommandOneArg("subsubsection", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuildingHandler("h4"), START_NEW_XHTML_BLOCK);
        map.addComplexCommandOneArg("subsubsection*", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuildingHandler("h4"), START_NEW_XHTML_BLOCK);
        
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
        map.addSimpleCommand("em", ALL_MODES, StyleDeclarationInterpretation.EM, null, null);
        map.addSimpleCommand("bf", ALL_MODES, StyleDeclarationInterpretation.BF, null, null);
        map.addSimpleCommand("rm", ALL_MODES, StyleDeclarationInterpretation.RM, null, null);
        map.addSimpleCommand("it", ALL_MODES, StyleDeclarationInterpretation.IT, null, null);
        map.addSimpleCommand("tt", ALL_MODES, StyleDeclarationInterpretation.TT, null, null);
        map.addSimpleCommand("sc", ALL_MODES, StyleDeclarationInterpretation.SC, null, null);
        map.addSimpleCommand("sl", ALL_MODES, StyleDeclarationInterpretation.SL, null, null);
        map.addSimpleCommand("sf", ALL_MODES, StyleDeclarationInterpretation.SF, null, null);
        
        /* New style P/LR mode style change macros. These take the text they are being applied to
         * as a single argument.
         * 
         * As of 1.0.0-beta4, we now support using these macros in MATH mode. We force the arguments
         * into PARAGRAPH mode here so that the resulting content will end up inside <mtext/>
         * element(s) wrapped inside a <mstyle/> setting the appropriate style.
         */
        StyleInterpretationHandler styleInterpretationNodeBuilder = new StyleInterpretationHandler(); /* (Stateless so can share) */
        map.addComplexCommandOneArg("textrm", false, ALL_MODES, PARAGRAPH, StyleDeclarationInterpretation.RM, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addComplexCommandOneArg("textsf", false, ALL_MODES, PARAGRAPH, StyleDeclarationInterpretation.SF, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addComplexCommandOneArg("textit", false, ALL_MODES, PARAGRAPH, StyleDeclarationInterpretation.IT, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addComplexCommandOneArg("textsl", false, ALL_MODES, PARAGRAPH, StyleDeclarationInterpretation.SL, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addComplexCommandOneArg("textsc", false, ALL_MODES, PARAGRAPH, StyleDeclarationInterpretation.SC, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addComplexCommandOneArg("textbf", false, ALL_MODES, PARAGRAPH, StyleDeclarationInterpretation.BF, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addComplexCommandOneArg("texttt", false, ALL_MODES, PARAGRAPH, StyleDeclarationInterpretation.TT, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addComplexCommandOneArg("emph",   false, ALL_MODES, PARAGRAPH, StyleDeclarationInterpretation.EM, styleInterpretationNodeBuilder, ALLOW_INLINE);
        
        /* Text size control macros. As above, these are converted to environments of the same name
         * during token fixing, which are easier to deal with.
         */
        map.addSimpleCommand("tiny", TEXT_MODE_ONLY, StyleDeclarationInterpretation.TINY, null, null);
        map.addSimpleCommand("scriptsize", TEXT_MODE_ONLY, StyleDeclarationInterpretation.SCRIPTSIZE, null, null);
        map.addSimpleCommand("footnotesize", TEXT_MODE_ONLY, StyleDeclarationInterpretation.FOOTNOTESIZE, null, null);
        map.addSimpleCommand("small", TEXT_MODE_ONLY, StyleDeclarationInterpretation.SMALL, null, null);
        map.addSimpleCommand("normalsize", TEXT_MODE_ONLY, StyleDeclarationInterpretation.NORMALSIZE, null, null);
        map.addSimpleCommand("large", TEXT_MODE_ONLY, StyleDeclarationInterpretation.LARGE, null, null);
        map.addSimpleCommand("Large", TEXT_MODE_ONLY, StyleDeclarationInterpretation.LARGE_2, null, null);
        map.addSimpleCommand("LARGE", TEXT_MODE_ONLY, StyleDeclarationInterpretation.LARGE_3, null, null);
        map.addSimpleCommand("huge", TEXT_MODE_ONLY, StyleDeclarationInterpretation.HUGE, null, null);
        map.addSimpleCommand("Huge", TEXT_MODE_ONLY, StyleDeclarationInterpretation.HUGE_2, null, null);
        
        /* Non-English symbols (non-math, simple) See Table 3.2, p.39 on LaTeX companion */
        map.addSimpleCommand("ae", TEXT_MODE_ONLY, new CharacterCommandHandler("\u00e6"), ALLOW_INLINE);
        map.addSimpleCommand("oe", TEXT_MODE_ONLY, new CharacterCommandHandler("\u0153"), ALLOW_INLINE);
        
        /* Special punctuation (non-math, simple) See p.39 on LC */
        map.addSimpleCommand("dag", TEXT_MODE_ONLY, new CharacterCommandHandler("\u2020"), ALLOW_INLINE);
        
        /* Ellipses (All modes) */
        map.addSimpleCommand("ldots", ALL_MODES, new MathIdentifierInterpretation("\u2026"), new TextSafeInterpretableMathIdentifierHandler(), ALLOW_INLINE);
        
        /* Other symbols valid in all modes */
        map.addSimpleCommand("pounds", ALL_MODES, new MathIdentifierInterpretation("\u00a3"), new TextSafeInterpretableMathIdentifierHandler(), ALLOW_INLINE);
        
        //---------------------------------------------------------------
        // Math Mode stuff (see LaTeX Companion pp39-52)
        
        /* Semantic versions of MathML "&ApplyFunction;" and "&InvisibleTimes;" entities */
        CMD_APPLY_FUNCTION = map.addSimpleMathCommand("af", new MathOperatorInterpretation(MathMLSymbol.APPLY_FUNCTION));
        CMD_INVISIBLE_TIMES = map.addSimpleMathCommand("itimes", new MathOperatorInterpretation(MathMLSymbol.INVISIBLE_TIMES));
        
        /* Placeholders for corresponding MathML constructs. These are substituted from traditional LaTeX constructs
         * by {@link TokenFixer}.
         * 
         * Note that subscript/superscripts will either be converted to <msub/> et al or <munder/> et al
         * according to the type of operator being handled.
         */
        MathLimitsHandler mathLimitsBuilder = new MathLimitsHandler();
        CMD_MROW = map.addComplexCommandSameArgMode("<mrow>", false, 1, MATH_MODE_ONLY, new MrowHandler(), null);
        CMD_MSUB_OR_MUNDER = map.addComplexCommandSameArgMode("<msubormunder>", false, 2, MATH_MODE_ONLY, mathLimitsBuilder, null);
        CMD_MSUP_OR_MOVER = map.addComplexCommandSameArgMode("<msupormover>", false, 2, MATH_MODE_ONLY, mathLimitsBuilder, null);
        CMD_MSUBSUP_OR_MUNDEROVER = map.addComplexCommandSameArgMode("<msubsupormunderover>", false, 3, MATH_MODE_ONLY, mathLimitsBuilder, null);
        
        /* A related idea to sub/super is \\stackrel */
        map.addComplexCommand("stackrel", false, 2, MATH_MODE_ONLY, null, new MathStackrelHandler(), null);
        
        /* Styling (c.f. equivalents in text mode, listed above) */
        map.addComplexCommandSameArgMode("mathrm", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.RM, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addComplexCommandSameArgMode("mathsf", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.SF, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addComplexCommandSameArgMode("mathit", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.IT, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addComplexCommandSameArgMode("mathbf", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.BF, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addComplexCommandSameArgMode("mathtt", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.TT, styleInterpretationNodeBuilder, ALLOW_INLINE);
        
        /* Styling done via character mappings (e.g. calligraphic) */
        map.addComplexCommandSameArgMode("mathcal", false, 1, MATH_MODE_ONLY, new MathVariantMapHandler(MathVariantMaps.SCRIPT), null);
        map.addComplexCommandSameArgMode("mathsc", false, 1, MATH_MODE_ONLY, new MathVariantMapHandler(MathVariantMaps.SCRIPT), null);
        map.addComplexCommandSameArgMode("mathbb", false, 1, MATH_MODE_ONLY, new MathVariantMapHandler(MathVariantMaps.DOUBLE_STRUCK), null);
        map.addComplexCommandSameArgMode("mathfrak", false, 1, MATH_MODE_ONLY, new MathVariantMapHandler(MathVariantMaps.FRAKTUR), null);
        
        /* Ellipses (Math-mode only) */
        map.addSimpleMathCommand("cdots", new MathIdentifierInterpretation(MathMLSymbol.CDOTS));
        map.addSimpleMathCommand("vdots", new MathIdentifierInterpretation(MathMLSymbol.VDOTS));
        map.addSimpleMathCommand("ddots", new MathIdentifierInterpretation(MathMLSymbol.DDOTS));
        
        /* Greek letters (need turned into Unicode characters) */
        map.addSimpleMathCommand("alpha", new MathIdentifierInterpretation(MathMLSymbol.ALPHA));
        map.addSimpleMathCommand("beta", new MathIdentifierInterpretation(MathMLSymbol.BETA));
        map.addSimpleMathCommand("gamma", new MathIdentifierInterpretation(MathMLSymbol.GAMMA));
        map.addSimpleMathCommand("delta", new MathIdentifierInterpretation(MathMLSymbol.DELTA));
        map.addSimpleMathCommand("epsilon", new MathIdentifierInterpretation(MathMLSymbol.EPSILON));
        map.addSimpleMathCommand("varepsilon", new MathIdentifierInterpretation(MathMLSymbol.VAREPSILON));
        map.addSimpleMathCommand("zeta", new MathIdentifierInterpretation(MathMLSymbol.ZETA));
        map.addSimpleMathCommand("eta", new MathIdentifierInterpretation(MathMLSymbol.ETA));
        map.addSimpleMathCommand("theta", new MathIdentifierInterpretation(MathMLSymbol.THETA));
        map.addSimpleMathCommand("vartheta", new MathIdentifierInterpretation(MathMLSymbol.VARTHETA));
        map.addSimpleMathCommand("iota", new MathIdentifierInterpretation(MathMLSymbol.IOTA));
        map.addSimpleMathCommand("kappa", new MathIdentifierInterpretation(MathMLSymbol.KAPPA));
        map.addSimpleMathCommand("lambda", new MathIdentifierInterpretation(MathMLSymbol.LAMBDA));
        map.addSimpleMathCommand("mu", new MathIdentifierInterpretation(MathMLSymbol.MU));
        map.addSimpleMathCommand("nu", new MathIdentifierInterpretation(MathMLSymbol.NU));
        map.addSimpleMathCommand("xi", new MathIdentifierInterpretation(MathMLSymbol.XI));
        map.addSimpleMathCommand("pi", new MathIdentifierInterpretation(MathMLSymbol.PI));
        map.addSimpleMathCommand("varpi", new MathIdentifierInterpretation(MathMLSymbol.VARPI));
        map.addSimpleMathCommand("rho", new MathIdentifierInterpretation(MathMLSymbol.RHO));
        map.addSimpleMathCommand("varrho", new MathIdentifierInterpretation(MathMLSymbol.VARRHO));
        map.addSimpleMathCommand("sigma", new MathIdentifierInterpretation(MathMLSymbol.SIGMA));
        map.addSimpleMathCommand("varsigma", new MathIdentifierInterpretation(MathMLSymbol.VARSIGMA));
        map.addSimpleMathCommand("tau", new MathIdentifierInterpretation(MathMLSymbol.TAU));
        map.addSimpleMathCommand("upsilon", new MathIdentifierInterpretation(MathMLSymbol.UPSILON));
        map.addSimpleMathCommand("phi", new MathIdentifierInterpretation(MathMLSymbol.PHI));
        map.addSimpleMathCommand("varphi", new MathIdentifierInterpretation(MathMLSymbol.VARPHI));
        map.addSimpleMathCommand("chi", new MathIdentifierInterpretation(MathMLSymbol.CHI));
        map.addSimpleMathCommand("psi", new MathIdentifierInterpretation(MathMLSymbol.PSI));
        map.addSimpleMathCommand("omega", new MathIdentifierInterpretation(MathMLSymbol.OMEGA));
        map.addSimpleMathCommand("Gamma", new MathIdentifierInterpretation(MathMLSymbol.UC_GAMMA));
        map.addSimpleMathCommand("Delta", new MathIdentifierInterpretation(MathMLSymbol.UC_DELTA));
        map.addSimpleMathCommand("Theta", new MathIdentifierInterpretation(MathMLSymbol.UC_THETA));
        map.addSimpleMathCommand("Lambda", new MathIdentifierInterpretation(MathMLSymbol.UC_LAMBDA));
        map.addSimpleMathCommand("Xi", new MathIdentifierInterpretation(MathMLSymbol.UC_XI));
        map.addSimpleMathCommand("Pi", new MathIdentifierInterpretation(MathMLSymbol.UC_PI));
        map.addSimpleMathCommand("Sigma", new MathIdentifierInterpretation(MathMLSymbol.UC_SIGMA));
        map.addSimpleMathCommand("Upsilon", new MathIdentifierInterpretation(MathMLSymbol.UC_UPSILON));
        map.addSimpleMathCommand("Phi", new MathIdentifierInterpretation(MathMLSymbol.UC_PHI));
        map.addSimpleMathCommand("Psi", new MathIdentifierInterpretation(MathMLSymbol.UC_PSI));
        map.addSimpleMathCommand("Omega", new MathIdentifierInterpretation(MathMLSymbol.UC_OMEGA));
        
        /* Math "functions" (treated as identifiers in MathML) */
        map.addSimpleMathCommand("arccos", new MathFunctionInterpretation("arccos"));
        map.addSimpleMathCommand("arcsin", new MathFunctionInterpretation("arcsin"));
        map.addSimpleMathCommand("arctan", new MathFunctionInterpretation("arctan"));
        map.addSimpleMathCommand("arg", new MathFunctionInterpretation("arg"));
        map.addSimpleMathCommand("cos", new MathFunctionInterpretation("cos"));
        map.addSimpleMathCommand("cosh", new MathFunctionInterpretation("cosh"));
        map.addSimpleMathCommand("cot", new MathFunctionInterpretation("cot"));
        map.addSimpleMathCommand("coth", new MathFunctionInterpretation("coth"));
        map.addSimpleMathCommand("csc", new MathFunctionInterpretation("csc"));
        map.addSimpleMathCommand("deg", new MathFunctionInterpretation("deg"));
        map.addSimpleMathCommand("det", new MathFunctionInterpretation("det"));
        map.addSimpleMathCommand("dim", new MathFunctionInterpretation("dim"));
        map.addSimpleMathCommand("exp", new MathFunctionInterpretation("exp"));
        map.addSimpleMathCommand("gcd", new MathFunctionInterpretation("gcd"));
        map.addSimpleMathCommand("hom", new MathFunctionInterpretation("hom"));
        map.addSimpleMathCommand("inf", new MathFunctionInterpretation("inf"));
        map.addSimpleMathCommand("ker", new MathFunctionInterpretation("ker"));
        map.addSimpleMathCommand("lg", new MathFunctionInterpretation("lg"));
        map.addSimpleMathCommand("lcm", new MathFunctionInterpretation("lcm"));
        map.addSimpleMathCommand("lim", new MathFunctionInterpretation("lim"));
        map.addSimpleMathCommand("liminf", new MathFunctionInterpretation("lim inf"));
        map.addSimpleMathCommand("limsup", new MathFunctionInterpretation("lim sup"));
        map.addSimpleMathCommand("ln", new MathFunctionInterpretation("ln"));
        map.addSimpleMathCommand("log", new MathFunctionInterpretation("log"));
        map.addSimpleMathCommand("max", new MathFunctionInterpretation("max"));
        map.addSimpleMathCommand("min", new MathFunctionInterpretation("min"));
        map.addSimpleMathCommand("Pr", new MathFunctionInterpretation("Pr"));
        map.addSimpleMathCommand("sec", new MathFunctionInterpretation("sec"));
        map.addSimpleMathCommand("sin", new MathFunctionInterpretation("sin"));
        map.addSimpleMathCommand("sinh", new MathFunctionInterpretation("sinh"));
        map.addSimpleMathCommand("sup", new MathFunctionInterpretation("sup"));
        map.addSimpleMathCommand("tan", new MathFunctionInterpretation("tan"));
        map.addSimpleMathCommand("tanh", new MathFunctionInterpretation("tanh"));
        
        /* Extra Math functions (added for consistency with standard Content MathML operators) */
        map.addSimpleMathCommand("sech", new MathFunctionInterpretation("sech"));
        map.addSimpleMathCommand("csch", new MathFunctionInterpretation("csch"));
        map.addSimpleMathCommand("coth", new MathFunctionInterpretation("coth"));
        map.addSimpleMathCommand("arcsec", new MathFunctionInterpretation("arcsec"));
        map.addSimpleMathCommand("arccsc", new MathFunctionInterpretation("arccsc"));
        map.addSimpleMathCommand("arccot", new MathFunctionInterpretation("arccot"));
        map.addSimpleMathCommand("arccosh", new MathFunctionInterpretation("arccosh"));
        map.addSimpleMathCommand("arcsinh", new MathFunctionInterpretation("arcsinh"));
        map.addSimpleMathCommand("arctanh", new MathFunctionInterpretation("arctanh"));
        map.addSimpleMathCommand("arcsech", new MathFunctionInterpretation("arcsech"));
        map.addSimpleMathCommand("arccsch", new MathFunctionInterpretation("arccsch"));
        map.addSimpleMathCommand("arccoth", new MathFunctionInterpretation("arccoth"));

        /* Variable-sized symbols */
        MathBigLimitOwnerInterpretation bigLimitOwner = new MathBigLimitOwnerInterpretation();
        map.addSimpleMathCommand("sum", new MathOperatorInterpretation(MathMLSymbol.SUM), bigLimitOwner);
        map.addSimpleMathCommand("prod", new MathOperatorInterpretation(MathMLSymbol.PROD), bigLimitOwner);
        map.addSimpleMathCommand("coprod", new MathOperatorInterpretation(MathMLSymbol.COPROD), bigLimitOwner);
        map.addSimpleMathCommand("int", new MathOperatorInterpretation(MathMLSymbol.INTEGRAL));
        map.addSimpleMathCommand("oint", new MathOperatorInterpretation(MathMLSymbol.OINT), bigLimitOwner);
        map.addSimpleMathCommand("bigcap", new MathOperatorInterpretation(MathMLSymbol.BIGCAP), bigLimitOwner);
        map.addSimpleMathCommand("bigcup", new MathOperatorInterpretation(MathMLSymbol.BIGCUP), bigLimitOwner);
        map.addSimpleMathCommand("bigsqcup", new MathOperatorInterpretation(MathMLSymbol.BIGSQCUP), bigLimitOwner);
        map.addSimpleMathCommand("bigvee", new MathOperatorInterpretation(MathMLSymbol.BIGVEE), bigLimitOwner);
        map.addSimpleMathCommand("bigwedge", new MathOperatorInterpretation(MathMLSymbol.BIGWEDGE), bigLimitOwner);
        map.addSimpleMathCommand("bigodot", new MathOperatorInterpretation(MathMLSymbol.BIGODOT), bigLimitOwner);
        map.addSimpleMathCommand("bigotimes", new MathOperatorInterpretation(MathMLSymbol.BIGOTIMES), bigLimitOwner);
        map.addSimpleMathCommand("bigoplus", new MathOperatorInterpretation(MathMLSymbol.BIGOPLUS), bigLimitOwner);
        map.addSimpleMathCommand("biguplus", new MathOperatorInterpretation(MathMLSymbol.BIGUPLUS), bigLimitOwner);
        
        /* Binary operators */
        map.addSimpleMathCommand("pm", new MathOperatorInterpretation(MathMLSymbol.PM));
        map.addSimpleMathCommand("mp", new MathOperatorInterpretation(MathMLSymbol.MP));
        map.addSimpleMathCommand("times", new MathOperatorInterpretation(MathMLSymbol.TIMES));
        map.addSimpleMathCommand("div", new MathOperatorInterpretation(MathMLSymbol.DIV));
        map.addSimpleMathCommand("ast", new MathOperatorInterpretation(MathMLSymbol.AST));
        map.addSimpleMathCommand("star", new MathOperatorInterpretation(MathMLSymbol.STAR));
        map.addSimpleMathCommand("circ", new MathOperatorInterpretation(MathMLSymbol.CIRC));
        map.addSimpleMathCommand("bullet", new MathOperatorInterpretation(MathMLSymbol.BULLET));
        map.addSimpleMathCommand("cdot", new MathOperatorInterpretation(MathMLSymbol.CDOT));
        map.addSimpleMathCommand("cap", new MathOperatorInterpretation(MathMLSymbol.CAP));
        map.addSimpleMathCommand("cup", new MathOperatorInterpretation(MathMLSymbol.CUP));
        map.addSimpleMathCommand("uplus", new MathOperatorInterpretation(MathMLSymbol.UPLUS));
        map.addSimpleMathCommand("sqcap", new MathOperatorInterpretation(MathMLSymbol.SQCAP));
        map.addSimpleMathCommand("sqcup", new MathOperatorInterpretation(MathMLSymbol.SQCUP));
        map.addSimpleMathCommand("vee", new MathOperatorInterpretation(MathMLSymbol.VEE));
        map.addSimpleMathCommand("lor", new MathOperatorInterpretation(MathMLSymbol.VEE));
        map.addSimpleMathCommand("wedge", new MathOperatorInterpretation(MathMLSymbol.WEDGE));
        map.addSimpleMathCommand("land", new MathOperatorInterpretation(MathMLSymbol.WEDGE));
        map.addSimpleMathCommand("setminus", new MathOperatorInterpretation(MathMLSymbol.SETMINUS));
        map.addSimpleMathCommand("wr", new MathOperatorInterpretation(MathMLSymbol.WR));
        map.addSimpleMathCommand("diamond", new MathOperatorInterpretation(MathMLSymbol.DIAMOND));
        map.addSimpleMathCommand("bigtriangleup", new MathOperatorInterpretation(MathMLSymbol.BIGTRIANGLEUP));
        map.addSimpleMathCommand("bigtriangledown", new MathOperatorInterpretation(MathMLSymbol.BIGTRIANGLEDOWN));
        map.addSimpleMathCommand("triangleleft", new MathOperatorInterpretation(MathMLSymbol.TRIANGLELEFT));
        map.addSimpleMathCommand("triangleright", new MathOperatorInterpretation(MathMLSymbol.TRIANGLERIGHT));
        map.addSimpleMathCommand("oplus", new MathOperatorInterpretation(MathMLSymbol.OPLUS));
        map.addSimpleMathCommand("ominus", new MathOperatorInterpretation(MathMLSymbol.OMINUS));
        map.addSimpleMathCommand("otimes", new MathOperatorInterpretation(MathMLSymbol.OTIMES));
        map.addSimpleMathCommand("oslash", new MathOperatorInterpretation(MathMLSymbol.OSLASH));
        map.addSimpleMathCommand("odot", new MathOperatorInterpretation(MathMLSymbol.ODOT));
        map.addSimpleMathCommand("bigcirc", new MathOperatorInterpretation(MathMLSymbol.BIGCIRC));
        map.addSimpleMathCommand("dagger", new MathOperatorInterpretation(MathMLSymbol.DAGGER));
        map.addSimpleMathCommand("ddagger", new MathOperatorInterpretation(MathMLSymbol.DDAGGER));
        map.addSimpleMathCommand("amalg", new MathOperatorInterpretation(MathMLSymbol.AMALG));
        map.addSimpleMathCommand("leq", new MathOperatorInterpretation(MathMLSymbol.LEQ), new MathNegatableInterpretation(MathMLSymbol.NOT_LEQ));
        map.addSimpleMathCommand("le", new MathOperatorInterpretation(MathMLSymbol.LEQ), new MathNegatableInterpretation(MathMLSymbol.NOT_LEQ));
        map.addSimpleMathCommand("prec", new MathOperatorInterpretation(MathMLSymbol.PREC), new MathNegatableInterpretation(MathMLSymbol.NOT_PREC));
        map.addSimpleMathCommand("preceq", new MathOperatorInterpretation(MathMLSymbol.PRECEQ));
        map.addSimpleMathCommand("ll", new MathOperatorInterpretation(MathMLSymbol.LL));
        map.addSimpleMathCommand("subset", new MathOperatorInterpretation(MathMLSymbol.SUBSET), new MathNegatableInterpretation(MathMLSymbol.NOT_SUBSET));
        map.addSimpleMathCommand("subseteq", new MathOperatorInterpretation(MathMLSymbol.SUBSETEQ), new MathNegatableInterpretation(MathMLSymbol.NOT_SUBSETEQ));
        map.addSimpleMathCommand("sqsubset", new MathOperatorInterpretation(MathMLSymbol.SQSUBSET));
        map.addSimpleMathCommand("sqsubseteq", new MathOperatorInterpretation(MathMLSymbol.SQSUBSETEQ), new MathNegatableInterpretation(MathMLSymbol.NOT_SQSUBSETEQ));
        map.addSimpleMathCommand("in", new MathOperatorInterpretation(MathMLSymbol.IN), new MathNegatableInterpretation(MathMLSymbol.NOT_IN));
        map.addSimpleMathCommand("vdash", new MathOperatorInterpretation(MathMLSymbol.VDASH), new MathNegatableInterpretation(MathMLSymbol.NOT_VDASH));
        map.addSimpleMathCommand("geq", new MathOperatorInterpretation(MathMLSymbol.GEQ), new MathNegatableInterpretation(MathMLSymbol.NOT_GEQ));
        map.addSimpleMathCommand("ge", new MathOperatorInterpretation(MathMLSymbol.GEQ), new MathNegatableInterpretation(MathMLSymbol.NOT_GEQ));
        map.addSimpleMathCommand("succ", new MathOperatorInterpretation(MathMLSymbol.SUCC), new MathNegatableInterpretation(MathMLSymbol.NOT_SUCC));
        map.addSimpleMathCommand("succeq", new MathOperatorInterpretation(MathMLSymbol.SUCCEQ));
        map.addSimpleMathCommand("gg", new MathOperatorInterpretation(MathMLSymbol.GG));
        map.addSimpleMathCommand("supset", new MathOperatorInterpretation(MathMLSymbol.SUPSET), new MathNegatableInterpretation(MathMLSymbol.NOT_SUPSET));
        map.addSimpleMathCommand("supseteq", new MathOperatorInterpretation(MathMLSymbol.SUPSETEQ), new MathNegatableInterpretation(MathMLSymbol.NOT_SUPSETEQ));
        map.addSimpleMathCommand("sqsupset", new MathOperatorInterpretation(MathMLSymbol.SQSUPSET));
        map.addSimpleMathCommand("sqsupseteq", new MathOperatorInterpretation(MathMLSymbol.SQSUPSETEQ), new MathNegatableInterpretation(MathMLSymbol.NOT_SQSUPSETEQ));
        map.addSimpleMathCommand("ni", new MathOperatorInterpretation(MathMLSymbol.NI), new MathNegatableInterpretation(MathMLSymbol.NOT_NI));
        map.addSimpleMathCommand("dashv", new MathOperatorInterpretation(MathMLSymbol.DASHV));
        map.addSimpleMathCommand("equiv", new MathOperatorInterpretation(MathMLSymbol.EQUIV), new MathNegatableInterpretation(MathMLSymbol.NOT_EQUIV));
        map.addSimpleMathCommand("sim", new MathOperatorInterpretation(MathMLSymbol.SIM), new MathNegatableInterpretation(MathMLSymbol.NOT_SIM));
        map.addSimpleMathCommand("simeq", new MathOperatorInterpretation(MathMLSymbol.SIMEQ), new MathNegatableInterpretation(MathMLSymbol.NOT_SIMEQ));
        map.addSimpleMathCommand("asymp", new MathOperatorInterpretation(MathMLSymbol.ASYMP));
        map.addSimpleMathCommand("approx", new MathOperatorInterpretation(MathMLSymbol.APPROX), new MathNegatableInterpretation(MathMLSymbol.NOT_APPROX));
        map.addSimpleMathCommand("cong", new MathOperatorInterpretation(MathMLSymbol.CONG), new MathNegatableInterpretation(MathMLSymbol.NOT_CONG));
        map.addSimpleMathCommand("neq", new MathOperatorInterpretation(MathMLSymbol.NOT_IN));
        map.addSimpleMathCommand("doteq", new MathOperatorInterpretation(MathMLSymbol.DOTEQ));
        map.addSimpleMathCommand("notin", new MathOperatorInterpretation(MathMLSymbol.NOT_IN));
        map.addSimpleMathCommand("models", new MathOperatorInterpretation(MathMLSymbol.MODELS));
        map.addSimpleMathCommand("perp", new MathOperatorInterpretation(MathMLSymbol.PERP));
        map.addSimpleMathCommand("mid", new MathOperatorInterpretation(MathMLSymbol.MID), new MathNegatableInterpretation(MathMLSymbol.NOT_MID));
        map.addSimpleMathCommand("parallel", new MathOperatorInterpretation(MathMLSymbol.PARALLEL));
        map.addSimpleMathCommand("bowtie", new MathOperatorInterpretation(MathMLSymbol.BOWTIE));
        map.addSimpleMathCommand("smile", new MathOperatorInterpretation(MathMLSymbol.SMILE));
        map.addSimpleMathCommand("frown", new MathOperatorInterpretation(MathMLSymbol.FROWN));
        map.addSimpleMathCommand("propto", new MathOperatorInterpretation(MathMLSymbol.PROPTO));
        
        /* Arrows */
        map.addSimpleMathCommand("leftarrow", new MathOperatorInterpretation(MathMLSymbol.LEFTARROW));
        map.addSimpleMathCommand("Leftarrow", new MathOperatorInterpretation(MathMLSymbol.UC_LEFTARROW));
        map.addSimpleMathCommand("rightarrow", new MathOperatorInterpretation(MathMLSymbol.RIGHTARROW));
        map.addSimpleMathCommand("Rightarrow", new MathOperatorInterpretation(MathMLSymbol.UC_RIGHTARROW));
        map.addSimpleMathCommand("leftrightarrow", new MathOperatorInterpretation(MathMLSymbol.LEFTRIGHTARROW));
        map.addSimpleMathCommand("Leftrightarrow", new MathOperatorInterpretation(MathMLSymbol.UC_LEFTRIGHTARROW));
        map.addSimpleMathCommand("mapsto", new MathOperatorInterpretation(MathMLSymbol.MAPSTO));
        map.addSimpleMathCommand("hookleftarrow", new MathOperatorInterpretation(MathMLSymbol.HOOKLEFTARROW));
        map.addSimpleMathCommand("leftharpoonup", new MathOperatorInterpretation(MathMLSymbol.LEFTHARPOONUP));
        map.addSimpleMathCommand("leftharpoondown", new MathOperatorInterpretation(MathMLSymbol.LEFTHARPOONDOWN));
        map.addSimpleMathCommand("rightleftharpoons", new MathOperatorInterpretation(MathMLSymbol.RIGHTLEFTHARPOONS));
        map.addSimpleMathCommand("longleftarrow", new MathOperatorInterpretation(MathMLSymbol.LEFTARROW)); /* NB: No appropriate Unicode symbols for long operators! */
        map.addSimpleMathCommand("Longleftarrow", new MathOperatorInterpretation(MathMLSymbol.UC_LEFTARROW));
        map.addSimpleMathCommand("longrightarrow", new MathOperatorInterpretation(MathMLSymbol.RIGHTARROW));
        map.addSimpleMathCommand("Longrightarrow", new MathOperatorInterpretation(MathMLSymbol.UC_RIGHTARROW));
        map.addSimpleMathCommand("longleftrightarrow", new MathOperatorInterpretation(MathMLSymbol.LEFTRIGHTARROW));
        map.addSimpleMathCommand("Longleftrightarrow", new MathOperatorInterpretation(MathMLSymbol.UC_LEFTRIGHTARROW));
        map.addSimpleMathCommand("longmapsto", new MathOperatorInterpretation(MathMLSymbol.MAPSTO));
        map.addSimpleMathCommand("hookrightarrow", new MathOperatorInterpretation(MathMLSymbol.HOOKRIGHTARROW));
        map.addSimpleMathCommand("rightharpoonup", new MathOperatorInterpretation(MathMLSymbol.RIGHTHARPOONOUP));
        map.addSimpleMathCommand("rightharpoondown", new MathOperatorInterpretation(MathMLSymbol.RIGHTHARPOONDOWN));
        map.addSimpleMathCommand("uparrow", new MathOperatorInterpretation(MathMLSymbol.UPARROW));
        map.addSimpleMathCommand("Uparrow", new MathOperatorInterpretation(MathMLSymbol.UC_UPARROW));
        map.addSimpleMathCommand("downarrow", new MathOperatorInterpretation(MathMLSymbol.DOWNARROW));
        map.addSimpleMathCommand("Downarrow", new MathOperatorInterpretation(MathMLSymbol.UC_DOWNARROW));
        map.addSimpleMathCommand("updownarrow", new MathOperatorInterpretation(MathMLSymbol.UPDOWNARROW));
        map.addSimpleMathCommand("Updownarrow", new MathOperatorInterpretation(MathMLSymbol.UC_UPDOWNARROW));
        map.addSimpleMathCommand("nearrow", new MathOperatorInterpretation(MathMLSymbol.NEARROW));
        map.addSimpleMathCommand("searrow", new MathOperatorInterpretation(MathMLSymbol.SEARROW));
        map.addSimpleMathCommand("swarrow", new MathOperatorInterpretation(MathMLSymbol.SWARROW));
        map.addSimpleMathCommand("nwarrow", new MathOperatorInterpretation(MathMLSymbol.NWARROW));
        
        /* Miscellaneous symbols */
        map.addSimpleMathCommand("aleph", new MathIdentifierInterpretation(MathMLSymbol.ALEPH));
        map.addSimpleMathCommand("imath", new MathIdentifierInterpretation(MathMLSymbol.IMATH));
        map.addSimpleMathCommand("jmath", new MathIdentifierInterpretation(MathMLSymbol.JMATH));
        map.addSimpleMathCommand("ell", new MathIdentifierInterpretation(MathMLSymbol.ELL));
        map.addSimpleMathCommand("wp", new MathIdentifierInterpretation(MathMLSymbol.WP));
        map.addSimpleMathCommand("Re", new MathIdentifierInterpretation(MathMLSymbol.RE));
        map.addSimpleMathCommand("Im", new MathIdentifierInterpretation(MathMLSymbol.IM));
        map.addSimpleMathCommand("mho", new MathIdentifierInterpretation(MathMLSymbol.MHO));
        map.addSimpleMathCommand("prime", new MathIdentifierInterpretation(MathMLSymbol.PRIME));
        map.addSimpleMathCommand("emptyset", new MathIdentifierInterpretation(MathMLSymbol.EMPTYSET));
        map.addSimpleMathCommand("nabla", new MathOperatorInterpretation(MathMLSymbol.NABLA));
        map.addSimpleMathCommand("surd", new MathOperatorInterpretation(MathMLSymbol.SURD));
        map.addSimpleMathCommand("top", new MathOperatorInterpretation(MathMLSymbol.TOP));
        map.addSimpleMathCommand("bot", new MathOperatorInterpretation(MathMLSymbol.BOT));
        map.addSimpleMathCommand("|", new MathOperatorInterpretation(MathMLSymbol.DOUBLE_VERT_BRACKET));
        map.addSimpleMathCommand("angle", new MathOperatorInterpretation(MathMLSymbol.ANGLE));
        map.addSimpleMathCommand("forall", new MathOperatorInterpretation(MathMLSymbol.FORALL));
        map.addSimpleMathCommand("exists", new MathOperatorInterpretation(MathMLSymbol.EXISTS));
        map.addSimpleMathCommand("neg", new MathOperatorInterpretation(MathMLSymbol.NEG));
        map.addSimpleMathCommand("lnot", new MathOperatorInterpretation(MathMLSymbol.NEG));
        map.addSimpleMathCommand("flat", new MathIdentifierInterpretation(MathMLSymbol.FLAT));
        map.addSimpleMathCommand("natural", new MathIdentifierInterpretation(MathMLSymbol.NATURAL));
        map.addSimpleMathCommand("sharp", new MathIdentifierInterpretation(MathMLSymbol.SHARP));
        map.addSimpleMathCommand("backslash", new MathOperatorInterpretation(MathMLSymbol.BACKSLASH));
        map.addSimpleMathCommand("partial", new MathOperatorInterpretation(MathMLSymbol.PARTIAL));
        map.addSimpleMathCommand("infty", new MathIdentifierInterpretation(MathMLSymbol.INFTY));
        map.addSimpleMathCommand("triangle", new MathIdentifierInterpretation(MathMLSymbol.TRIANGLE));
        map.addSimpleMathCommand("clubsuit", new MathIdentifierInterpretation(MathMLSymbol.CLUBSUIT));
        map.addSimpleMathCommand("diamondsuit", new MathIdentifierInterpretation(MathMLSymbol.DIAMONDSUIT));
        map.addSimpleMathCommand("heartsuit", new MathIdentifierInterpretation(MathMLSymbol.HEARTSUIT));
        map.addSimpleMathCommand("spadesuit", new MathIdentifierInterpretation(MathMLSymbol.SPADESUIT));
        
        /* Extra identifiers */
        map.addSimpleMathCommand("hbar", new MathIdentifierInterpretation(MathMLSymbol.HBAR));
        map.addSimpleMathCommand("aa", new MathIdentifierInterpretation(MathMLSymbol.AA));
        map.addSimpleMathCommand("AA", new MathIdentifierInterpretation(MathMLSymbol.UC_AA));

        /* Math combiner commands that absorb the (bracket) token immediately after. These are
         * converted to fences during token fixing.
         */
        CombinerTargetMatcher bracketTargetMatcher = new MathFenceHandler.BracketCombinerTargetMatcher();
        CMD_LEFT = map.addCombinerCommand("left", MATH_MODE_ONLY, bracketTargetMatcher, null, null);
        CMD_RIGHT = map.addCombinerCommand("right", MATH_MODE_ONLY, bracketTargetMatcher, null, null);
        
        /* Special bracket commands */
        map.addSimpleMathCommand("vert",
                new MathOperatorInterpretation(MathMLSymbol.VERT_BRACKET),
                new MathBracketInterpretation(MathMLSymbol.VERT_BRACKET, BracketType.OPENER_OR_CLOSER, true));
        map.addSimpleMathCommand("Vert",
                new MathOperatorInterpretation(MathMLSymbol.DOUBLE_VERT_BRACKET),
                new MathBracketInterpretation(MathMLSymbol.DOUBLE_VERT_BRACKET, BracketType.OPENER_OR_CLOSER, true));

        /* This is a LaTeX-specific combiner macro that always comes before a
         * {@link MathRelationInterpretation} command.
         */
        CombinerTargetMatcher notTargetMatcher = new CombinerTargetMatcher() {
            public boolean isAllowed(FlowToken target) {
                return target.hasInterpretationType(InterpretationType.MATH_NEGATABLE);
            }  
        };
        CMD_NOT = map.addCombinerCommand("not", MATH_MODE_ONLY, notTargetMatcher, new MathNotHandler(), null);

        /* Complex math macros */
        map.addComplexCommandSameArgMode("sqrt", true, 1, MATH_MODE_ONLY, new MathRootHandler(), null);
        CMD_FRAC = map.addComplexCommandSameArgMode("frac", false, 2, MATH_MODE_ONLY, new MathComplexCommandHandler("mfrac"), null);
        CMD_OVER = map.addSimpleCommand("over", MATH_MODE_ONLY, null, null); /* TeX style fractions {... \over ...}, replaced during fixing *;
        
        /* Spacing */
        map.addSimpleCommand("quad", ALL_MODES, new SpaceHandler("\u00a0", "1em"), null);
        map.addSimpleCommand("qquad", ALL_MODES, new SpaceHandler("\u00a0\u00a0", "2em"), null);
        map.addComplexCommandSameArgMode("hspace", false, 1, ALL_MODES, new HSpaceHandler(), null);
        map.addComplexCommandSameArgMode("hspace*", false, 1, ALL_MODES, new HSpaceHandler(), null);

        /* Math accents */
        map.addComplexCommandSameArgMode("hat", false, 1, MATH_MODE_ONLY, new AccentHandler(AccentMaps.CIRCUMFLEX, '\u0302', "mover"), null);
        map.addComplexCommandSameArgMode("bar", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u0304', "mover"), null);
        map.addComplexCommandSameArgMode("vec", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u2192', "mover"), null);
        map.addComplexCommandSameArgMode("dot", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u0307', "mover"), null);
        map.addComplexCommandSameArgMode("ddot", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u0308', "mover"), null);
        map.addComplexCommandSameArgMode("tilde", false, 1, MATH_MODE_ONLY, new AccentHandler(AccentMaps.TILDE, '~', "mover"), null);
        map.addComplexCommandSameArgMode("widehat", false, 1, MATH_MODE_ONLY, new AccentHandler(AccentMaps.CIRCUMFLEX, '\u0302', "mover"), null);
        map.addComplexCommandSameArgMode("widetilde", false, 1, MATH_MODE_ONLY, new AccentHandler(AccentMaps.TILDE, '\u02dc', "mover"), null);
        map.addComplexCommandSameArgMode("overline", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u00af', "mover"), null);
        map.addComplexCommandSameArgMode("overbrace", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\ufe37', "mover"), null);
        map.addComplexCommandSameArgMode("underbrace", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\ufe38', "munder"), null);
        map.addComplexCommandSameArgMode("overrightarrow", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u20d7', "mover"), null);
        map.addComplexCommandSameArgMode("overleftarrow", false, 1, MATH_MODE_ONLY, new AccentHandler(null, '\u20d6', "mover"), null);
        
        /* Dual-mode accents */
        map.addComplexCommandSameArgMode("underline", false, 1, ALL_MODES, StyleDeclarationInterpretation.UNDERLINE, new ModeDelegatingHandler(styleInterpretationNodeBuilder, new AccentHandler(null, '\u00af', "munder")), null);
        
        /* Complex multi-mode macros */
        map.addComplexCommandOneArg("mbox", false, ALL_MODES, LR, new BoxHandler("mbox"), null);
        map.addComplexCommandOneArg("fbox", false, ALL_MODES, LR, new BoxHandler("fbox"), null);
        
        /* Table stuff */
        CMD_HLINE = map.addSimpleCommand("hline", ALL_MODES, new TabularHandler(), IGNORE);
        
        /* Commands for creating user-defined commands and environments */
        DoNothingHandler doNothingHandler = new DoNothingHandler();
        CMD_NEWCOMMAND = map.addComplexCommandSameArgMode("newcommand", false, 1, ALL_MODES, doNothingHandler, IGNORE);
        CMD_RENEWCOMMAND = map.addComplexCommandSameArgMode("renewcommand", false, 1, ALL_MODES, doNothingHandler, IGNORE);
        CMD_NEWENVIRONMENT = map.addComplexCommandSameArgMode("newenvironment", false, 2, ALL_MODES, doNothingHandler, IGNORE);
        CMD_RENEWENVIRONMENT = map.addComplexCommandSameArgMode("renewenvironment", false, 2, ALL_MODES, doNothingHandler, IGNORE);
        
        /* Special XHTML helpers */
        map.addComplexCommand("href", true, 1, TEXT_MODE_ONLY, new LaTeXMode[] { LR, VERBATIM }, new HrefHandler(), ALLOW_INLINE);
        map.addComplexCommandOneArg("anchor", false, TEXT_MODE_ONLY, VERBATIM, new AnchorHandler(), ALLOW_INLINE);
        map.addComplexCommandOneArg("anchor*", false, TEXT_MODE_ONLY, LR, new AnchorHandler(), ALLOW_INLINE);
        
        /* Special MathML helpers */
        map.addComplexCommandOneArg("units", false, MATH_MODE_ONLY, MATH, new UnitsHandler(), null);
        
        /* Commands for creating custom XML (also see related environments) */
        CMD_XML_ATTR = map.addComplexCommand("xmlAttr", false, 3, ALL_MODES, new LaTeXMode[] { LR, LR, LR }, new XMLAttrHandler(), IGNORE);
        map.addComplexCommand("xmlBlockElement", true, 3, ALL_MODES, new LaTeXMode[] { LR, LR, LR, null }, new XMLBlockElementHandler(), START_NEW_XHTML_BLOCK);
        map.addComplexCommand("xmlInlineElement", true, 3, ALL_MODES, new LaTeXMode[] { LR, LR, LR, null }, new XMLInlineElementHandler(), ALLOW_INLINE);
        map.addComplexCommandOneArg("xmlName", false, ALL_MODES, VERBATIM, new XMLNameOrIdHandler(XMLNameOrIdHandler.NAME), IGNORE);
        map.addComplexCommandOneArg("xmlName*", false, ALL_MODES, LR, new XMLNameOrIdHandler(XMLNameOrIdHandler.NAME), IGNORE);
        map.addComplexCommandOneArg("xmlId", false, ALL_MODES, VERBATIM, new XMLNameOrIdHandler(XMLNameOrIdHandler.ID), IGNORE);
        map.addComplexCommandOneArg("xmlId*", false, ALL_MODES, LR, new XMLNameOrIdHandler(XMLNameOrIdHandler.ID), IGNORE);
        map.addComplexCommandSameArgMode("xmlUnparse", false, 1, TEXT_MODE_ONLY, new XMLUnparseHandler(), ALLOW_INLINE);
        
        /* Special commands for managing simple "variables" */
        map.addComplexCommandSameArgMode("getvar", true, 1, ALL_MODES, new GetVarHandler(), IGNORE);
        map.addComplexCommandSameArgMode("setvar", true, 2, ALL_MODES, new SetVarHandler(), IGNORE);
        
        /* Special commands for making up-conversion assumptions. (These should move into a separate "package") */
        map.addComplexCommand("assume", false, 2, TEXT_MODE_ONLY, new LaTeXMode[] { LR, MATH }, new AssumeHandler(), IGNORE);
        
        /* =================================== ENVIRONMENTS ================================= */
        
        ENV_MATH = map.addEnvironment("math", TEXT_MODE_ONLY, MATH, null, new MathEnvironmentHandler(), ALLOW_INLINE);
        ENV_DISPLAYMATH = map.addEnvironment("displaymath", TEXT_MODE_ONLY, MATH, null, new MathEnvironmentHandler(), ALLOW_INLINE);
        ENV_VERBATIM = map.addEnvironment("verbatim", PARA_MODE_ONLY, VERBATIM, null, new VerbatimHandler(false), START_NEW_XHTML_BLOCK);
        ENV_ITEMIZE = map.addEnvironment("itemize", PARA_MODE_ONLY, null, null, new ListEnvironmentHandler(), START_NEW_XHTML_BLOCK);
        ENV_ENUMERATE = map.addEnvironment("enumerate", PARA_MODE_ONLY, null, null, new ListEnvironmentHandler(), START_NEW_XHTML_BLOCK);
        
        TabularInterpretation tabularInterpretation = new TabularInterpretation();
        map.addEnvironment("tabular", false, 1, PARA_MODE_ONLY, PARAGRAPH, tabularInterpretation, new TabularHandler(), START_NEW_XHTML_BLOCK);
        map.addEnvironment("array", false, 1, MATH_MODE_ONLY, MATH, tabularInterpretation, new ArrayHandler(), null);
        map.addEnvironment("cases", MATH_MODE_ONLY, MATH, tabularInterpretation, new MatrixHandler(2, MathMLSymbol.OPEN_CURLY_BRACKET, ""), null);
        map.addEnvironment("eqnarray", PARA_MODE_ONLY, MATH, tabularInterpretation, new EqnArrayHandler(), START_NEW_XHTML_BLOCK);
        map.addEnvironment("eqnarray*", PARA_MODE_ONLY, MATH, tabularInterpretation, new EqnArrayHandler(), START_NEW_XHTML_BLOCK);
        
        /* AMS-LaTeX convenience environments */
        map.addEnvironment("matrix", MATH_MODE_ONLY, MATH, tabularInterpretation, new MatrixHandler(), null);
        map.addEnvironment("pmatrix", MATH_MODE_ONLY, MATH, tabularInterpretation, new MatrixHandler(MathMLSymbol.OPEN_BRACKET, MathMLSymbol.CLOSE_BRACKET), null);
        map.addEnvironment("bmatrix", MATH_MODE_ONLY, MATH, tabularInterpretation, new MatrixHandler(MathMLSymbol.OPEN_SQUARE_BRACKET, MathMLSymbol.CLOSE_SQUARE_BRACKET), null);
        map.addEnvironment("Bmatrix", MATH_MODE_ONLY, MATH, tabularInterpretation, new MatrixHandler(MathMLSymbol.OPEN_CURLY_BRACKET, MathMLSymbol.CLOSE_CURLY_BRACKET), null);
        map.addEnvironment("vmatrix", MATH_MODE_ONLY, MATH, tabularInterpretation, new MatrixHandler(MathMLSymbol.VERT_BRACKET, MathMLSymbol.VERT_BRACKET), null);
        map.addEnvironment("Vmatrix", MATH_MODE_ONLY, MATH, tabularInterpretation, new MatrixHandler(MathMLSymbol.DOUBLE_VERT_BRACKET, MathMLSymbol.DOUBLE_VERT_BRACKET), null);
        
        /* Simple text environments */
        map.addEnvironment("quote", PARA_MODE_ONLY, PARAGRAPH, null, new SimpleXHTMLContainerBuildingHandler("blockquote"), START_NEW_XHTML_BLOCK);
        
        /* Text justification environments. (Note that each line is supposed to be delimited by '\\' */
        map.addEnvironment("center", PARA_MODE_ONLY, PARAGRAPH, null, new SimpleXHTMLContainerBuildingHandler("div", "center"), START_NEW_XHTML_BLOCK);
        map.addEnvironment("flushleft", PARA_MODE_ONLY, PARAGRAPH, null, new SimpleXHTMLContainerBuildingHandler("div", "flushleft"), START_NEW_XHTML_BLOCK);
        map.addEnvironment("flushright", PARA_MODE_ONLY, PARAGRAPH, null, new SimpleXHTMLContainerBuildingHandler("div", "flushright"), START_NEW_XHTML_BLOCK);
        
        /* Alternative versions of \em and friends. These are converted internally to
         * environments as they're easier to deal with like that.
         */
        map.addEnvironment("em", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.EM, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("bf", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.BF, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("rm", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.RM, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("it", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.IT, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("tt", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.TT, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("sc", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SC, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("sl", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SL, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("sf", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SF, styleInterpretationNodeBuilder, ALLOW_INLINE);
        
        map.addEnvironment("tiny", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.TINY, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("scriptsize", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SCRIPTSIZE, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("footnotesize", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.FOOTNOTESIZE, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("small", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SMALL, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("normalsize", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.NORMALSIZE, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("large", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.LARGE, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("Large", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.LARGE_2, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("LARGE", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.LARGE_3, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("huge", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.HUGE, styleInterpretationNodeBuilder, ALLOW_INLINE);
        map.addEnvironment("Huge", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.HUGE_2, styleInterpretationNodeBuilder, ALLOW_INLINE);
        
        /* Special internal environment for enclosing content within two brackets. These are
         * inferred during token fixing, also handling the case where an opener or closer is missing.
         * When both an opener and closer is provided, this generates a MathML <mfenced/> element;
         * otherwise we degrade nicely.
         * 
         * NOTE: The arguments for this actually end up being in MATH mode.
         */
        ENV_BRACKETED = map.addEnvironment("<mfenced>", false, 2, MATH_MODE_ONLY, MATH, null, new MathFenceHandler(), null);

        /* Environments for generating custom XML islands (see corresponding command versions as well) */
        map.addEnvironment("xmlBlockElement", true, 2, ALL_MODES, null, null, new XMLBlockElementHandler(), START_NEW_XHTML_BLOCK);
        map.addEnvironment("xmlInlineElement", true, 2, ALL_MODES, null, null, new XMLInlineElementHandler(), ALLOW_INLINE);
        map.addEnvironment("xmlUnparse", false, 0, TEXT_MODE_ONLY, null, null, new XMLUnparseHandler(), ALLOW_INLINE);
    }
}
