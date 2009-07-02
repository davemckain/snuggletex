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
import uk.ac.ed.ph.snuggletex.dombuilding.BoxHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.CharacterCommandHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.DoNothingHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EnsureMathHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EqnArrayHandler;
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
import uk.ac.ed.ph.snuggletex.dombuilding.ModeDelegatingHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.MrowHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.ParagraphHandler;
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
import uk.ac.ed.ph.snuggletex.semantics.MathBracketInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathFunctionInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.MathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathRelationInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.StyleDeclarationInterpretation;
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
    public static BuiltinEnvironment ENV_TABULAR;
    public static BuiltinEnvironment ENV_MATH;
    public static BuiltinEnvironment ENV_DISPLAYMATH;
    public static BuiltinEnvironment ENV_BRACKETED;
    public static BuiltinEnvironment ENV_ARRAY;
    public static BuiltinEnvironment ENV_EQNARRAY;
    public static BuiltinEnvironment ENV_EQNARRAYSTAR;
    
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
                new MathOperatorInterpretation(MathMLOperator.OPEN_CURLY_BRACKET),
                new MathBracketInterpretation(MathMLOperator.OPEN_CURLY_BRACKET, MathMLOperator.CLOSE_CURLY_BRACKET, BracketType.OPENER, true),                
            }, new ModeDelegatingHandler(new CharacterCommandHandler("{"), new InterpretableSimpleMathHandler()), null);
        map.addSimpleCommand("}", ALL_MODES, new Interpretation[] {
                new MathOperatorInterpretation(MathMLOperator.CLOSE_CURLY_BRACKET),
                new MathBracketInterpretation(MathMLOperator.CLOSE_CURLY_BRACKET, MathMLOperator.OPEN_CURLY_BRACKET, BracketType.CLOSER, true),                
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
         * TODO: Decide whether to support traditional LaTeX labelling or not.
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
        CMD_APPLY_FUNCTION = map.addSimpleMathCommand("af", new MathOperatorInterpretation(MathMLOperator.APPLY_FUNCTION));
        CMD_INVISIBLE_TIMES = map.addSimpleMathCommand("itimes", new MathOperatorInterpretation(MathMLOperator.INVISIBLE_TIMES));
        
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
        map.addSimpleMathCommand("cdots", new MathIdentifierInterpretation("\u00b7\u00b7\u00b7"));
        map.addSimpleMathCommand("vdots", new MathIdentifierInterpretation("\u22ee"));
        map.addSimpleMathCommand("ddots", new MathIdentifierInterpretation("\u22f1"));
        
        /* Greek letters (need turned into Unicode characters) */
        map.addSimpleMathCommand("alpha", new MathIdentifierInterpretation("\u03b1"));
        map.addSimpleMathCommand("beta", new MathIdentifierInterpretation("\u03b2"));
        map.addSimpleMathCommand("gamma", new MathIdentifierInterpretation("\u03b3"));
        map.addSimpleMathCommand("delta", new MathIdentifierInterpretation("\u03b4"));
        map.addSimpleMathCommand("epsilon", new MathIdentifierInterpretation("\u03f5"));
        map.addSimpleMathCommand("varepsilon", new MathIdentifierInterpretation("\u03b5"));
        map.addSimpleMathCommand("zeta", new MathIdentifierInterpretation("\u03b6"));
        map.addSimpleMathCommand("eta", new MathIdentifierInterpretation("\u03b7"));
        map.addSimpleMathCommand("theta", new MathIdentifierInterpretation("\u03b8"));
        map.addSimpleMathCommand("vartheta", new MathIdentifierInterpretation("\u03d1"));
        map.addSimpleMathCommand("iota", new MathIdentifierInterpretation("\u03b9"));
        map.addSimpleMathCommand("kappa", new MathIdentifierInterpretation("\u03ba"));
        map.addSimpleMathCommand("lambda", new MathIdentifierInterpretation("\u03bb"));
        map.addSimpleMathCommand("mu", new MathIdentifierInterpretation("\u03bc"));
        map.addSimpleMathCommand("nu", new MathIdentifierInterpretation("\u03bd"));
        map.addSimpleMathCommand("xi", new MathIdentifierInterpretation("\u03be"));
        map.addSimpleMathCommand("pi", new MathIdentifierInterpretation("\u03c0"));
        map.addSimpleMathCommand("varpi", new MathIdentifierInterpretation("\u03d6"));
        map.addSimpleMathCommand("rho", new MathIdentifierInterpretation("\u03c1"));
        map.addSimpleMathCommand("varrho", new MathIdentifierInterpretation("\u03f1"));
        map.addSimpleMathCommand("sigma", new MathIdentifierInterpretation("\u03c3"));
        map.addSimpleMathCommand("varsigma", new MathIdentifierInterpretation("\u03c2"));
        map.addSimpleMathCommand("tau", new MathIdentifierInterpretation("\u03c4"));
        map.addSimpleMathCommand("upsilon", new MathIdentifierInterpretation("\u03c5"));
        map.addSimpleMathCommand("phi", new MathIdentifierInterpretation("\u03c6"));
        map.addSimpleMathCommand("varphi", new MathIdentifierInterpretation("\u03d5"));
        map.addSimpleMathCommand("chi", new MathIdentifierInterpretation("\u03c7"));
        map.addSimpleMathCommand("psi", new MathIdentifierInterpretation("\u03c8"));
        map.addSimpleMathCommand("omega", new MathIdentifierInterpretation("\u03c9"));
        map.addSimpleMathCommand("Gamma", new MathIdentifierInterpretation("\u0393"));
        map.addSimpleMathCommand("Delta", new MathIdentifierInterpretation("\u0394"));
        map.addSimpleMathCommand("Theta", new MathIdentifierInterpretation("\u0398"));
        map.addSimpleMathCommand("Lambda", new MathIdentifierInterpretation("\u039b"));
        map.addSimpleMathCommand("Xi", new MathIdentifierInterpretation("\u039e"));
        map.addSimpleMathCommand("Pi", new MathIdentifierInterpretation("\u03a0"));
        map.addSimpleMathCommand("Sigma", new MathIdentifierInterpretation("\u03a3"));
        map.addSimpleMathCommand("Upsilon", new MathIdentifierInterpretation("\u03a5"));
        map.addSimpleMathCommand("Phi", new MathIdentifierInterpretation("\u03a6"));
        map.addSimpleMathCommand("Psi", new MathIdentifierInterpretation("\u03a8"));
        map.addSimpleMathCommand("Omega", new MathIdentifierInterpretation("\u03a9"));
        
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
        map.addSimpleMathCommand("sum", new MathOperatorInterpretation(MathMLOperator.SUM));
        map.addSimpleMathCommand("prod", new MathOperatorInterpretation(MathMLOperator.PROD));
        map.addSimpleMathCommand("coprod", new MathOperatorInterpretation(MathMLOperator.COPROD));
        map.addSimpleMathCommand("int", new MathOperatorInterpretation(MathMLOperator.INTEGRAL));
        map.addSimpleMathCommand("oint", new MathOperatorInterpretation(MathMLOperator.OINT));
        map.addSimpleMathCommand("bigcap", new MathOperatorInterpretation(MathMLOperator.BIGCAP));
        map.addSimpleMathCommand("bigcup", new MathOperatorInterpretation(MathMLOperator.BIGCUP));
        map.addSimpleMathCommand("bigsqcup", new MathOperatorInterpretation(MathMLOperator.BIGSQCUP));
        map.addSimpleMathCommand("bigvee", new MathOperatorInterpretation(MathMLOperator.BIGVEE));
        map.addSimpleMathCommand("bigwedge", new MathOperatorInterpretation(MathMLOperator.BIGWEDGE));
        map.addSimpleMathCommand("bigodot", new MathOperatorInterpretation(MathMLOperator.BIGODOT));
        map.addSimpleMathCommand("bigotimes", new MathOperatorInterpretation(MathMLOperator.BIGOTIMES));
        map.addSimpleMathCommand("bigoplus", new MathOperatorInterpretation(MathMLOperator.BIGOPLUS));
        map.addSimpleMathCommand("biguplus", new MathOperatorInterpretation(MathMLOperator.BIGUPLUS));
        
        /* Binary operators */
        map.addSimpleMathCommand("pm", new MathOperatorInterpretation(MathMLOperator.PM));
        map.addSimpleMathCommand("mp", new MathOperatorInterpretation(MathMLOperator.MP));
        map.addSimpleMathCommand("times", new MathOperatorInterpretation(MathMLOperator.TIMES));
        map.addSimpleMathCommand("div", new MathOperatorInterpretation(MathMLOperator.DIV));
        map.addSimpleMathCommand("ast", new MathOperatorInterpretation(MathMLOperator.AST));
        map.addSimpleMathCommand("star", new MathOperatorInterpretation(MathMLOperator.STAR));
        map.addSimpleMathCommand("circ", new MathOperatorInterpretation(MathMLOperator.CIRC));
        map.addSimpleMathCommand("bullet", new MathOperatorInterpretation(MathMLOperator.BULLET));
        map.addSimpleMathCommand("cdot", new MathOperatorInterpretation(MathMLOperator.CDOT));
        map.addSimpleMathCommand("cap", new MathOperatorInterpretation(MathMLOperator.CAP));
        map.addSimpleMathCommand("cup", new MathOperatorInterpretation(MathMLOperator.CUP));
        map.addSimpleMathCommand("uplus", new MathOperatorInterpretation(MathMLOperator.UPLUS));
        map.addSimpleMathCommand("sqcap", new MathOperatorInterpretation(MathMLOperator.SQCAP));
        map.addSimpleMathCommand("sqcup", new MathOperatorInterpretation(MathMLOperator.SQCUP));
        map.addSimpleMathCommand("vee", new MathOperatorInterpretation(MathMLOperator.VEE));
        map.addSimpleMathCommand("lor", new MathOperatorInterpretation(MathMLOperator.VEE));
        map.addSimpleMathCommand("wedge", new MathOperatorInterpretation(MathMLOperator.WEDGE));
        map.addSimpleMathCommand("land", new MathOperatorInterpretation(MathMLOperator.WEDGE));
        map.addSimpleMathCommand("setminus", new MathOperatorInterpretation(MathMLOperator.SETMINUS));
        map.addSimpleMathCommand("wr", new MathOperatorInterpretation(MathMLOperator.WR));
        map.addSimpleMathCommand("diamond", new MathOperatorInterpretation(MathMLOperator.DIAMOND));
        map.addSimpleMathCommand("bigtriangleup", new MathOperatorInterpretation(MathMLOperator.BIGTRIANGLEUP));
        map.addSimpleMathCommand("bigtriangledown", new MathOperatorInterpretation(MathMLOperator.BIGTRIANGLEDOWN));
        map.addSimpleMathCommand("triangleleft", new MathOperatorInterpretation(MathMLOperator.TRIANGLELEFT));
        map.addSimpleMathCommand("triangleright", new MathOperatorInterpretation(MathMLOperator.TRIANGLERIGHT));
        map.addSimpleMathCommand("oplus", new MathOperatorInterpretation(MathMLOperator.OPLUS));
        map.addSimpleMathCommand("ominus", new MathOperatorInterpretation(MathMLOperator.OMINUS));
        map.addSimpleMathCommand("otimes", new MathOperatorInterpretation(MathMLOperator.OTIMES));
        map.addSimpleMathCommand("oslash", new MathOperatorInterpretation(MathMLOperator.OSLASH));
        map.addSimpleMathCommand("odot", new MathOperatorInterpretation(MathMLOperator.ODOT));
        map.addSimpleMathCommand("bigcirc", new MathOperatorInterpretation(MathMLOperator.BIGCIRC));
        map.addSimpleMathCommand("dagger", new MathOperatorInterpretation(MathMLOperator.DAGGER));
        map.addSimpleMathCommand("ddagger", new MathOperatorInterpretation(MathMLOperator.DDAGGER));
        map.addSimpleMathCommand("amalg", new MathOperatorInterpretation(MathMLOperator.AMALG));
        map.addSimpleMathCommand("leq", new MathOperatorInterpretation(MathMLOperator.LEQ), new MathRelationInterpretation(MathMLOperator.LEQ, MathMLOperator.NOT_LEQ));
        map.addSimpleMathCommand("le", new MathOperatorInterpretation(MathMLOperator.LEQ), new MathRelationInterpretation(MathMLOperator.LEQ, MathMLOperator.NOT_LEQ));
        map.addSimpleMathCommand("prec", new MathOperatorInterpretation(MathMLOperator.PREC), new MathRelationInterpretation(MathMLOperator.PREC, MathMLOperator.NOT_PREC));
        map.addSimpleMathCommand("preceq", new MathOperatorInterpretation(MathMLOperator.PRECEQ));
        map.addSimpleMathCommand("ll", new MathOperatorInterpretation(MathMLOperator.LL));
        map.addSimpleMathCommand("subset", new MathOperatorInterpretation(MathMLOperator.SUBSET), new MathRelationInterpretation(MathMLOperator.SUBSET, MathMLOperator.NOT_SUBSET));
        map.addSimpleMathCommand("subseteq", new MathOperatorInterpretation(MathMLOperator.SUBSETEQ), new MathRelationInterpretation(MathMLOperator.SUBSETEQ, MathMLOperator.NOT_SUBSETEQ));
        map.addSimpleMathCommand("sqsubset", new MathOperatorInterpretation(MathMLOperator.SQSUBSET));
        map.addSimpleMathCommand("sqsubseteq", new MathOperatorInterpretation(MathMLOperator.SQSUBSETEQ), new MathRelationInterpretation(MathMLOperator.SQSUBSETEQ, MathMLOperator.NOT_SQSUBSETEQ));
        map.addSimpleMathCommand("in", new MathOperatorInterpretation(MathMLOperator.IN), new MathRelationInterpretation(MathMLOperator.IN, MathMLOperator.NOT_IN));
        map.addSimpleMathCommand("vdash", new MathOperatorInterpretation(MathMLOperator.VDASH), new MathRelationInterpretation(MathMLOperator.VDASH, MathMLOperator.NOT_VDASH));
        map.addSimpleMathCommand("geq", new MathOperatorInterpretation(MathMLOperator.GEQ), new MathRelationInterpretation(MathMLOperator.GEQ, MathMLOperator.NOT_GEQ));
        map.addSimpleMathCommand("ge", new MathOperatorInterpretation(MathMLOperator.GEQ), new MathRelationInterpretation(MathMLOperator.GEQ, MathMLOperator.NOT_GEQ));
        map.addSimpleMathCommand("succ", new MathOperatorInterpretation(MathMLOperator.SUCC), new MathRelationInterpretation(MathMLOperator.SUCC, MathMLOperator.NOT_SUCC));
        map.addSimpleMathCommand("succeq", new MathOperatorInterpretation(MathMLOperator.SUCCEQ));
        map.addSimpleMathCommand("gg", new MathOperatorInterpretation(MathMLOperator.GG));
        map.addSimpleMathCommand("supset", new MathOperatorInterpretation(MathMLOperator.SUPSET), new MathRelationInterpretation(MathMLOperator.SUPSET, MathMLOperator.NOT_SUPSET));
        map.addSimpleMathCommand("supseteq", new MathOperatorInterpretation(MathMLOperator.SUPSETEQ), new MathRelationInterpretation(MathMLOperator.SUPSETEQ, MathMLOperator.NOT_SUPSETEQ));
        map.addSimpleMathCommand("sqsupset", new MathOperatorInterpretation(MathMLOperator.SQSUPSET));
        map.addSimpleMathCommand("sqsupseteq", new MathOperatorInterpretation(MathMLOperator.SQSUPSETEQ), new MathRelationInterpretation(MathMLOperator.SQSUPSETEQ, MathMLOperator.NOT_SQSUPSETEQ));
        map.addSimpleMathCommand("ni", new MathOperatorInterpretation(MathMLOperator.NI), new MathRelationInterpretation(MathMLOperator.NI, MathMLOperator.NOT_NI));
        map.addSimpleMathCommand("dashv", new MathOperatorInterpretation(MathMLOperator.DASHV));
        map.addSimpleMathCommand("equiv", new MathOperatorInterpretation(MathMLOperator.EQUIV), new MathRelationInterpretation(MathMLOperator.EQUIV, MathMLOperator.NOT_EQUIV));
        map.addSimpleMathCommand("sim", new MathOperatorInterpretation(MathMLOperator.SIM), new MathRelationInterpretation(MathMLOperator.SIM, MathMLOperator.NOT_SIM));
        map.addSimpleMathCommand("simeq", new MathOperatorInterpretation(MathMLOperator.SIMEQ), new MathRelationInterpretation(MathMLOperator.SIMEQ, MathMLOperator.NOT_SIMEQ));
        map.addSimpleMathCommand("asymp", new MathOperatorInterpretation(MathMLOperator.ASYMP));
        map.addSimpleMathCommand("approx", new MathOperatorInterpretation(MathMLOperator.APPROX), new MathRelationInterpretation(MathMLOperator.APPROX, MathMLOperator.NOT_APPROX));
        map.addSimpleMathCommand("cong", new MathOperatorInterpretation(MathMLOperator.CONG), new MathRelationInterpretation(MathMLOperator.CONG, MathMLOperator.NOT_CONG));
        map.addSimpleMathCommand("neq", new MathOperatorInterpretation(MathMLOperator.NOT_IN));
        map.addSimpleMathCommand("doteq", new MathOperatorInterpretation(MathMLOperator.DOTEQ));
        map.addSimpleMathCommand("notin", new MathOperatorInterpretation(MathMLOperator.NOT_IN));
        map.addSimpleMathCommand("models", new MathOperatorInterpretation(MathMLOperator.MODELS));
        map.addSimpleMathCommand("perp", new MathOperatorInterpretation(MathMLOperator.PERP));
        map.addSimpleMathCommand("mid", new MathOperatorInterpretation(MathMLOperator.MID), new MathRelationInterpretation(MathMLOperator.MID, MathMLOperator.NOT_MID));
        map.addSimpleMathCommand("parallel", new MathOperatorInterpretation(MathMLOperator.PARALLEL));
        map.addSimpleMathCommand("bowtie", new MathOperatorInterpretation(MathMLOperator.BOWTIE));
        map.addSimpleMathCommand("smile", new MathOperatorInterpretation(MathMLOperator.SMILE));
        map.addSimpleMathCommand("frown", new MathOperatorInterpretation(MathMLOperator.FROWN));
        map.addSimpleMathCommand("propto", new MathOperatorInterpretation(MathMLOperator.PROPTO));
        
        /* Arrows */
        map.addSimpleMathCommand("leftarrow", new MathOperatorInterpretation(MathMLOperator.LEFTARROW));
        map.addSimpleMathCommand("Leftarrow", new MathOperatorInterpretation(MathMLOperator.UC_LEFTARROW));
        map.addSimpleMathCommand("rightarrow", new MathOperatorInterpretation(MathMLOperator.RIGHTARROW));
        map.addSimpleMathCommand("Rightarrow", new MathOperatorInterpretation(MathMLOperator.UC_RIGHTARROW));
        map.addSimpleMathCommand("leftrightarrow", new MathOperatorInterpretation(MathMLOperator.LEFTRIGHTARROW));
        map.addSimpleMathCommand("Leftrightarrow", new MathOperatorInterpretation(MathMLOperator.UC_LEFTRIGHTARROW));
        map.addSimpleMathCommand("mapsto", new MathOperatorInterpretation(MathMLOperator.MAPSTO));
        map.addSimpleMathCommand("hookleftarrow", new MathOperatorInterpretation(MathMLOperator.HOOKLEFTARROW));
        map.addSimpleMathCommand("leftharpoonup", new MathOperatorInterpretation(MathMLOperator.LEFTHARPOONUP));
        map.addSimpleMathCommand("leftharpoondown", new MathOperatorInterpretation(MathMLOperator.LEFTHARPOONDOWN));
        map.addSimpleMathCommand("rightleftharpoons", new MathOperatorInterpretation(MathMLOperator.RIGHTLEFTHARPOONS));
        map.addSimpleMathCommand("longleftarrow", new MathOperatorInterpretation(MathMLOperator.LEFTARROW)); /* NB: No appropriate Unicode symbols for long operators! */
        map.addSimpleMathCommand("Longleftarrow", new MathOperatorInterpretation(MathMLOperator.UC_LEFTARROW));
        map.addSimpleMathCommand("longrightarrow", new MathOperatorInterpretation(MathMLOperator.RIGHTARROW));
        map.addSimpleMathCommand("Longrightarrow", new MathOperatorInterpretation(MathMLOperator.UC_RIGHTARROW));
        map.addSimpleMathCommand("longleftrightarrow", new MathOperatorInterpretation(MathMLOperator.LEFTRIGHTARROW));
        map.addSimpleMathCommand("Longleftrightarrow", new MathOperatorInterpretation(MathMLOperator.UC_LEFTRIGHTARROW));
        map.addSimpleMathCommand("longmapsto", new MathOperatorInterpretation(MathMLOperator.MAPSTO));
        map.addSimpleMathCommand("hookrightarrow", new MathOperatorInterpretation(MathMLOperator.HOOKRIGHTARROW));
        map.addSimpleMathCommand("rightharpoonup", new MathOperatorInterpretation(MathMLOperator.RIGHTHARPOONOUP));
        map.addSimpleMathCommand("rightharpoondown", new MathOperatorInterpretation(MathMLOperator.RIGHTHARPOONDOWN));
        map.addSimpleMathCommand("uparrow", new MathOperatorInterpretation(MathMLOperator.UPARROW));
        map.addSimpleMathCommand("Uparrow", new MathOperatorInterpretation(MathMLOperator.UC_UPARROW));
        map.addSimpleMathCommand("downarrow", new MathOperatorInterpretation(MathMLOperator.DOWNARROW));
        map.addSimpleMathCommand("Downarrow", new MathOperatorInterpretation(MathMLOperator.UC_DOWNARROW));
        map.addSimpleMathCommand("updownarrow", new MathOperatorInterpretation(MathMLOperator.UPDOWNARROW));
        map.addSimpleMathCommand("Updownarrow", new MathOperatorInterpretation(MathMLOperator.UC_UPDOWNARROW));
        map.addSimpleMathCommand("nearrow", new MathOperatorInterpretation(MathMLOperator.NEARROW));
        map.addSimpleMathCommand("searrow", new MathOperatorInterpretation(MathMLOperator.SEARROW));
        map.addSimpleMathCommand("swarrow", new MathOperatorInterpretation(MathMLOperator.SWARROW));
        map.addSimpleMathCommand("nwarrow", new MathOperatorInterpretation(MathMLOperator.NWARROW));
        
        /* Miscellaneous symbols */
        map.addSimpleMathCommand("aleph", new MathIdentifierInterpretation("\u2135"));
        map.addSimpleMathCommand("imath", new MathIdentifierInterpretation("\u0131"));
        map.addSimpleMathCommand("jmath", new MathIdentifierInterpretation("\u006a"));
        map.addSimpleMathCommand("ell", new MathIdentifierInterpretation("\u2113"));
        map.addSimpleMathCommand("wp", new MathIdentifierInterpretation("\u2118"));
        map.addSimpleMathCommand("Re", new MathIdentifierInterpretation("\u211c"));
        map.addSimpleMathCommand("Im", new MathIdentifierInterpretation("\u2111"));
        map.addSimpleMathCommand("mho", new MathIdentifierInterpretation("\u2127"));
        map.addSimpleMathCommand("prime", new MathIdentifierInterpretation("\u2032"));
        map.addSimpleMathCommand("emptyset", new MathIdentifierInterpretation("\u2205"));
        map.addSimpleMathCommand("nabla", new MathOperatorInterpretation(MathMLOperator.NABLA));
        map.addSimpleMathCommand("surd", new MathOperatorInterpretation(MathMLOperator.SURD));
        map.addSimpleMathCommand("top", new MathOperatorInterpretation(MathMLOperator.TOP));
        map.addSimpleMathCommand("bot", new MathOperatorInterpretation(MathMLOperator.BOT));
        map.addSimpleMathCommand("|", new MathOperatorInterpretation(MathMLOperator.DOUBLE_VERT_BRACKET));
        map.addSimpleMathCommand("angle", new MathOperatorInterpretation(MathMLOperator.ANGLE));
        map.addSimpleMathCommand("forall", new MathOperatorInterpretation(MathMLOperator.FORALL));
        map.addSimpleMathCommand("exists", new MathOperatorInterpretation(MathMLOperator.EXISTS));
        map.addSimpleMathCommand("neg", new MathOperatorInterpretation(MathMLOperator.NEG));
        map.addSimpleMathCommand("lnot", new MathOperatorInterpretation(MathMLOperator.NEG));
        map.addSimpleMathCommand("flat", new MathIdentifierInterpretation("\u266d"));
        map.addSimpleMathCommand("natural", new MathIdentifierInterpretation("\u266e"));
        map.addSimpleMathCommand("sharp", new MathIdentifierInterpretation("\u266f"));
        map.addSimpleMathCommand("backslash", new MathOperatorInterpretation(MathMLOperator.BACKSLASH));
        map.addSimpleMathCommand("partial", new MathOperatorInterpretation(MathMLOperator.PARTIAL));
        map.addSimpleMathCommand("infty", new MathIdentifierInterpretation("\u221e"));
        map.addSimpleMathCommand("triangle", new MathIdentifierInterpretation("\u25b5"));
        map.addSimpleMathCommand("clubsuit", new MathIdentifierInterpretation("\u2663"));
        map.addSimpleMathCommand("diamondsuit", new MathIdentifierInterpretation("\u2662"));
        map.addSimpleMathCommand("heartsuit", new MathIdentifierInterpretation("\u2661"));
        map.addSimpleMathCommand("spadesuit", new MathIdentifierInterpretation("\u2660"));
        
        /* Extra identifiers */
        map.addSimpleMathCommand("hbar", new MathIdentifierInterpretation("\u210f"));
        map.addSimpleMathCommand("aa", new MathIdentifierInterpretation("\u00e5"));
        map.addSimpleMathCommand("AA", new MathIdentifierInterpretation("\u00c5"));

        /* Math combiner commands that absorb the (bracket) token immediately after. These are
         * converted to fences during token fixing.
         */
        CombinerTargetMatcher bracketTargetMatcher = new CombinerTargetMatcher() {
            public boolean isAllowed(FlowToken target) {
                boolean isAllowed = false;
                if (target.hasInterpretationType(InterpretationType.MATH_BRACKET)) {
                    isAllowed = true;
                }
                else if (target.hasInterpretationType(InterpretationType.MATH_OPERATOR)) {
                    /* Check for special case of combiner being a '.', which signifies "no bracket" */
                    MathOperatorInterpretation operatorInterp = (MathOperatorInterpretation) target.getInterpretation(InterpretationType.MATH_OPERATOR);
                    if (operatorInterp.getOperator()==MathMLOperator.DOT) {
                        isAllowed = true;
                    }
                }
                return isAllowed;
            }
        };
        CMD_LEFT = map.addCombinerCommand("left", MATH_MODE_ONLY, bracketTargetMatcher, null, null);
        CMD_RIGHT = map.addCombinerCommand("right", MATH_MODE_ONLY, bracketTargetMatcher, null, null);
        
        /* Special bracket commands */
        map.addSimpleMathCommand("vert",
                new MathOperatorInterpretation(MathMLOperator.VERT_BRACKET),
                new MathBracketInterpretation(MathMLOperator.VERT_BRACKET, MathMLOperator.VERT_BRACKET, BracketType.OPENER_OR_CLOSER, true));
        map.addSimpleMathCommand("Vert",
                new MathOperatorInterpretation(MathMLOperator.DOUBLE_VERT_BRACKET),
                new MathBracketInterpretation(MathMLOperator.DOUBLE_VERT_BRACKET, MathMLOperator.DOUBLE_VERT_BRACKET, BracketType.OPENER_OR_CLOSER, true));

        /* This is a LaTeX-specific combiner macro that always comes before a
         * {@link MathRelationInterpretation} command.
         */
        CombinerTargetMatcher notTargetMatcher = new CombinerTargetMatcher() {
            public boolean isAllowed(FlowToken target) {
                return target.hasInterpretationType(InterpretationType.MATH_RELATION);
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
        
        /* =================================== ENVIRONMENTS ================================= */
        
        ENV_MATH = map.addEnvironment("math", TEXT_MODE_ONLY, MATH, null, new MathEnvironmentHandler(), ALLOW_INLINE);
        ENV_DISPLAYMATH = map.addEnvironment("displaymath", TEXT_MODE_ONLY, MATH, null, new MathEnvironmentHandler(), ALLOW_INLINE);
        ENV_VERBATIM = map.addEnvironment("verbatim", PARA_MODE_ONLY, VERBATIM, null, new VerbatimHandler(false), START_NEW_XHTML_BLOCK);
        ENV_ITEMIZE = map.addEnvironment("itemize", PARA_MODE_ONLY, null, null, new ListEnvironmentHandler(), START_NEW_XHTML_BLOCK);
        ENV_ENUMERATE = map.addEnvironment("enumerate", PARA_MODE_ONLY, null, null, new ListEnvironmentHandler(), START_NEW_XHTML_BLOCK);
        ENV_TABULAR = map.addEnvironment("tabular", false, 1, PARA_MODE_ONLY, PARAGRAPH, null, new TabularHandler(), START_NEW_XHTML_BLOCK);
        ENV_ARRAY = map.addEnvironment("array", false, 1, MATH_MODE_ONLY, MATH, null, new ArrayHandler(), null);
        ENV_EQNARRAY = map.addEnvironment("eqnarray", PARA_MODE_ONLY, MATH, null, new EqnArrayHandler(), START_NEW_XHTML_BLOCK);
        ENV_EQNARRAYSTAR = map.addEnvironment("eqnarray*", PARA_MODE_ONLY, MATH, null, new EqnArrayHandler(), START_NEW_XHTML_BLOCK);
        
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
