/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
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

import uk.ac.ed.ph.snuggletex.dombuilding.AccentBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.AnchorHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.ArrayBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.BoxBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.CharacterCommandHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.DoNothingHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EnsureMathHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EqnArrayBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.HSpaceNodeBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.HrefBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.InterpretableSimpleMathBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.LineBreakHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.ListEnvironmentBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.LiteralBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.MathComplexCommandBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.MathEnvironmentBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.MathLimitsBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.MathNotBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.MathRootBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.MathStackrelBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.MathVariantMapHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.MfenceBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.ModeDelegatingBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.MrowBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.ParagraphBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.SimpleXHTMLContainerBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.SpaceNodeBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.StyleInterpretationNodeBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.TabularBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.TextSafeInterpretableMathIdentifierBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.VerbatimBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.XMLAttrHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.XMLBlockElementBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.XMLInlineElementBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.XMLNameOrIdHandler;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathFunctionIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.NottableMathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.SimpleMathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.StyleDeclarationInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketOperatorInterpretation.BracketType;

import java.util.EnumSet;

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
    public static BuiltinEnvironment ENV_FENCED;
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
        map.addSimpleCommand("{", ALL_MODES,
                new MathBracketOperatorInterpretation(MathMLOperator.OPEN_CURLY_BRACKET, MathMLOperator.CLOSE_CURLY_BRACKET, BracketType.OPENER),
                new ModeDelegatingBuilder(new CharacterCommandHandler("{"), new InterpretableSimpleMathBuilder()), null);
        map.addSimpleCommand("}", ALL_MODES,
                new MathBracketOperatorInterpretation(MathMLOperator.CLOSE_CURLY_BRACKET, MathMLOperator.OPEN_CURLY_BRACKET, BracketType.CLOSER),
                new ModeDelegatingBuilder(new CharacterCommandHandler("}"), new InterpretableSimpleMathBuilder()), null);
        map.addSimpleCommand(",", ALL_MODES, new SpaceNodeBuilder("\u2009", "0.167em"), ALLOW_INLINE); /* Thin space, all modes */
        map.addSimpleCommand(":", MATH_MODE_ONLY, new SpaceNodeBuilder(null, "0.222em"), null); /* Medium space, math only */
        map.addSimpleCommand(";", MATH_MODE_ONLY, new SpaceNodeBuilder(null, "0.278em"), null); /* Thick space, math only */
        map.addSimpleCommand("!", MATH_MODE_ONLY, new SpaceNodeBuilder(null, "-0.167em"), null); /* Negative thin space */
        map.addSimpleCommand(" ", ALL_MODES, new CharacterCommandHandler("\u00a0"), ALLOW_INLINE);
        
        /* Accents (non-math, complex) See Table 3.1, p. 38 on LaTeX companion.
         * (See also accents that apply in either MATH mode only or all modes, declared elsewhere!)
         */
        map.addComplexCommandSameArgMode("'", false, 1, TEXT_MODE_ONLY, new AccentBuilder(AccentMaps.ACCENT), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("`", false, 1, TEXT_MODE_ONLY, new AccentBuilder(AccentMaps.GRAVE), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("^", false, 1, TEXT_MODE_ONLY, new AccentBuilder(AccentMaps.CIRCUMFLEX), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("~", false, 1, TEXT_MODE_ONLY, new AccentBuilder(AccentMaps.TILDE), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("\"", false, 1, TEXT_MODE_ONLY, new AccentBuilder(AccentMaps.UMLAUT), ALLOW_INLINE);
        
        /* Traditional LaTeX commands */
        CMD_PAR = map.addSimpleCommand("par", TEXT_MODE_ONLY, null, null); /* (This is substituted during fixing) */
        map.addSimpleCommand("newline", ALL_MODES, new LineBreakHandler(), null);
        CMD_VERB = map.addSimpleCommand("verb", PARA_MODE_ONLY, new VerbatimBuilder(false), null);
        CMD_VERBSTAR = map.addSimpleCommand("verb*", PARA_MODE_ONLY, new VerbatimBuilder(true), null);
        CMD_ITEM = map.addSimpleCommand("item", PARA_MODE_ONLY, new ListEnvironmentBuilder(), null);
        map.addComplexCommandOneArg("ensuremath", false, ALL_MODES, MATH, new EnsureMathHandler(), null);
        
        /* TODO: Is there an equivalent of the following in LaTeX for doing "literal" input, sort of like \verb
         * but output using a normal typeface???
         */
        map.addComplexCommandOneArg("literal", false, TEXT_MODE_ONLY, VERBATIM, new LiteralBuilder(), null); 

        /* Tree version of a paragraph. The {@link TokenFixer} will create these, removing any
         * instances of {@link #PAR} and {@link TokenType#NEW_PARAGRAPH}.
         * <p>
         * I am not allowing this to be directly input, as this makes processing a bit easier
         * since it avoids the possibility of nested paragraphs.
         */
        CMD_PARAGRAPH = map.addComplexCommandSameArgMode("<paragraph>", false, 1, TEXT_MODE_ONLY, new ParagraphBuilder(), START_NEW_XHTML_BLOCK);
        
        /* Tree version of standard \item. Any \items are converted to these during token fixing.
         * I'm not allowing this to be directly input, which makes list handling a bit easier.
         */
        CMD_LIST_ITEM = map.addComplexCommandSameArgMode("<list item>", false, 1, PARA_MODE_ONLY, new ListEnvironmentBuilder(), START_NEW_XHTML_BLOCK);
        
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
        map.addComplexCommandOneArg("section", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuilder("h2"), START_NEW_XHTML_BLOCK);
        map.addComplexCommandOneArg("section*", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuilder("h2"), START_NEW_XHTML_BLOCK);
        map.addComplexCommandOneArg("subsection", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuilder("h3"), START_NEW_XHTML_BLOCK);
        map.addComplexCommandOneArg("subsection*", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuilder("h3"), START_NEW_XHTML_BLOCK);
        map.addComplexCommandOneArg("subsubsection", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuilder("h4"), START_NEW_XHTML_BLOCK);
        map.addComplexCommandOneArg("subsubsection*", false, PARA_MODE_ONLY, LR, new SimpleXHTMLContainerBuilder("h4"), START_NEW_XHTML_BLOCK);
        
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
        StyleInterpretationNodeBuilder styleInterpretationNodeBuilder = new StyleInterpretationNodeBuilder(); /* (Stateless so can share) */
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
        map.addSimpleCommand("ldots", ALL_MODES, new MathIdentifierInterpretation("\u2026"), new TextSafeInterpretableMathIdentifierBuilder(), ALLOW_INLINE);
        
        /* Other symbols valid in all modes */
        map.addSimpleCommand("pounds", ALL_MODES, new MathIdentifierInterpretation("\u00a3"), new TextSafeInterpretableMathIdentifierBuilder(), ALLOW_INLINE);
        
        //---------------------------------------------------------------
        // Math Mode stuff (see LaTeX Companion pp39-52)
        
        /* Semantic versions of MathML "&ApplyFunction;" and "&InvisibleTimes;" entities */
        CMD_APPLY_FUNCTION = map.addSimpleMathCommand("af", new SimpleMathOperatorInterpretation(MathMLOperator.APPLY_FUNCTION));
        CMD_INVISIBLE_TIMES = map.addSimpleMathCommand("itimes", new SimpleMathOperatorInterpretation(MathMLOperator.INVISIBLE_TIMES));
        
        /* Placeholders for corresponding MathML constructs. These are substituted from traditional LaTeX constructs
         * by {@link TokenFixer}.
         * 
         * Note that subscript/superscripts will either be converted to <msub/> et al or <munder/> et al
         * according to the type of operator being handled.
         */
        MathLimitsBuilder mathLimitsBuilder = new MathLimitsBuilder();
        CMD_MROW = map.addComplexCommandSameArgMode("<mrow>", false, 1, MATH_MODE_ONLY, new MrowBuilder(), null);
        CMD_MSUB_OR_MUNDER = map.addComplexCommandSameArgMode("<msubormunder>", false, 2, MATH_MODE_ONLY, mathLimitsBuilder, null);
        CMD_MSUP_OR_MOVER = map.addComplexCommandSameArgMode("<msupormover>", false, 2, MATH_MODE_ONLY, mathLimitsBuilder, null);
        CMD_MSUBSUP_OR_MUNDEROVER = map.addComplexCommandSameArgMode("<msubsupormunderover>", false, 3, MATH_MODE_ONLY, mathLimitsBuilder, null);
        
        /* A related idea to sub/super is \\stackrel */
        map.addComplexCommand("stackrel", false, 2, MATH_MODE_ONLY, null, new MathStackrelBuilder(), null);
        
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
        map.addSimpleMathCommand("arccos", new MathFunctionIdentifierInterpretation("arccos"));
        map.addSimpleMathCommand("arcsin", new MathFunctionIdentifierInterpretation("arcsin"));
        map.addSimpleMathCommand("arctan", new MathFunctionIdentifierInterpretation("arctan"));
        map.addSimpleMathCommand("arg", new MathFunctionIdentifierInterpretation("arg"));
        map.addSimpleMathCommand("cos", new MathFunctionIdentifierInterpretation("cos"));
        map.addSimpleMathCommand("cosh", new MathFunctionIdentifierInterpretation("cosh"));
        map.addSimpleMathCommand("cot", new MathFunctionIdentifierInterpretation("cot"));
        map.addSimpleMathCommand("coth", new MathFunctionIdentifierInterpretation("coth"));
        map.addSimpleMathCommand("csc", new MathFunctionIdentifierInterpretation("csc"));
        map.addSimpleMathCommand("deg", new MathFunctionIdentifierInterpretation("deg"));
        map.addSimpleMathCommand("det", new MathFunctionIdentifierInterpretation("det"));
        map.addSimpleMathCommand("dim", new MathFunctionIdentifierInterpretation("dim"));
        map.addSimpleMathCommand("exp", new MathFunctionIdentifierInterpretation("exp"));
        map.addSimpleMathCommand("gcd", new MathFunctionIdentifierInterpretation("gcd"));
        map.addSimpleMathCommand("hom", new MathFunctionIdentifierInterpretation("hom"));
        map.addSimpleMathCommand("inf", new MathFunctionIdentifierInterpretation("inf"));
        map.addSimpleMathCommand("ker", new MathFunctionIdentifierInterpretation("ker"));
        map.addSimpleMathCommand("lg", new MathFunctionIdentifierInterpretation("lg"));
        map.addSimpleMathCommand("lim", new MathFunctionIdentifierInterpretation("lim"));
        map.addSimpleMathCommand("liminf", new MathFunctionIdentifierInterpretation("lim inf"));
        map.addSimpleMathCommand("limsup", new MathFunctionIdentifierInterpretation("lim sup"));
        map.addSimpleMathCommand("ln", new MathFunctionIdentifierInterpretation("ln"));
        map.addSimpleMathCommand("log", new MathFunctionIdentifierInterpretation("log"));
        map.addSimpleMathCommand("max", new MathFunctionIdentifierInterpretation("max"));
        map.addSimpleMathCommand("min", new MathFunctionIdentifierInterpretation("min"));
        map.addSimpleMathCommand("Pr", new MathFunctionIdentifierInterpretation("Pr"));
        map.addSimpleMathCommand("sec", new MathFunctionIdentifierInterpretation("sec"));
        map.addSimpleMathCommand("sin", new MathFunctionIdentifierInterpretation("sin"));
        map.addSimpleMathCommand("sinh", new MathFunctionIdentifierInterpretation("sinh"));
        map.addSimpleMathCommand("sup", new MathFunctionIdentifierInterpretation("sup"));
        map.addSimpleMathCommand("tan", new MathFunctionIdentifierInterpretation("tan"));
        map.addSimpleMathCommand("tanh", new MathFunctionIdentifierInterpretation("tanh"));
        
        /* Variable-sized symbols */
        map.addSimpleMathCommand("sum", new SimpleMathOperatorInterpretation(MathMLOperator.SUM));
        map.addSimpleMathCommand("prod", new SimpleMathOperatorInterpretation(MathMLOperator.PROD));
        map.addSimpleMathCommand("coprod", new SimpleMathOperatorInterpretation(MathMLOperator.COPROD));
        map.addSimpleMathCommand("int", new SimpleMathOperatorInterpretation(MathMLOperator.INTEGRAL));
        map.addSimpleMathCommand("oint", new SimpleMathOperatorInterpretation(MathMLOperator.OINT));
        map.addSimpleMathCommand("bigcap", new SimpleMathOperatorInterpretation(MathMLOperator.BIGCAP));
        map.addSimpleMathCommand("bigcup", new SimpleMathOperatorInterpretation(MathMLOperator.BIGCUP));
        map.addSimpleMathCommand("bigsqcup", new SimpleMathOperatorInterpretation(MathMLOperator.BIGSQCUP));
        map.addSimpleMathCommand("bigvee", new SimpleMathOperatorInterpretation(MathMLOperator.BIGVEE));
        map.addSimpleMathCommand("bigwedge", new SimpleMathOperatorInterpretation(MathMLOperator.BIGWEDGE));
        map.addSimpleMathCommand("bigodot", new SimpleMathOperatorInterpretation(MathMLOperator.BIGODOT));
        map.addSimpleMathCommand("bigotimes", new SimpleMathOperatorInterpretation(MathMLOperator.BIGOTIMES));
        map.addSimpleMathCommand("bigoplus", new SimpleMathOperatorInterpretation(MathMLOperator.BIGOPLUS));
        map.addSimpleMathCommand("biguplus", new SimpleMathOperatorInterpretation(MathMLOperator.BIGUPLUS));
        
        /* Binary operators */
        map.addSimpleMathCommand("pm", new SimpleMathOperatorInterpretation(MathMLOperator.PM));
        map.addSimpleMathCommand("mp", new SimpleMathOperatorInterpretation(MathMLOperator.MP));
        map.addSimpleMathCommand("times", new SimpleMathOperatorInterpretation(MathMLOperator.TIMES));
        map.addSimpleMathCommand("div", new SimpleMathOperatorInterpretation(MathMLOperator.DIV));
        map.addSimpleMathCommand("ast", new SimpleMathOperatorInterpretation(MathMLOperator.AST));
        map.addSimpleMathCommand("star", new SimpleMathOperatorInterpretation(MathMLOperator.STAR));
        map.addSimpleMathCommand("circ", new SimpleMathOperatorInterpretation(MathMLOperator.CIRC));
        map.addSimpleMathCommand("bullet", new SimpleMathOperatorInterpretation(MathMLOperator.BULLET));
        map.addSimpleMathCommand("cdot", new SimpleMathOperatorInterpretation(MathMLOperator.CDOT));
        map.addSimpleMathCommand("cap", new SimpleMathOperatorInterpretation(MathMLOperator.CAP));
        map.addSimpleMathCommand("cup", new SimpleMathOperatorInterpretation(MathMLOperator.CUP));
        map.addSimpleMathCommand("uplus", new SimpleMathOperatorInterpretation(MathMLOperator.UPLUS));
        map.addSimpleMathCommand("sqcap", new SimpleMathOperatorInterpretation(MathMLOperator.SQCAP));
        map.addSimpleMathCommand("sqcup", new SimpleMathOperatorInterpretation(MathMLOperator.SQCUP));
        map.addSimpleMathCommand("vee", new SimpleMathOperatorInterpretation(MathMLOperator.VEE));
        map.addSimpleMathCommand("lor", new SimpleMathOperatorInterpretation(MathMLOperator.VEE));
        map.addSimpleMathCommand("wedge", new SimpleMathOperatorInterpretation(MathMLOperator.WEDGE));
        map.addSimpleMathCommand("land", new SimpleMathOperatorInterpretation(MathMLOperator.WEDGE));
        map.addSimpleMathCommand("setminus", new SimpleMathOperatorInterpretation(MathMLOperator.SETMINUS));
        map.addSimpleMathCommand("wr", new SimpleMathOperatorInterpretation(MathMLOperator.WR));
        map.addSimpleMathCommand("diamond", new SimpleMathOperatorInterpretation(MathMLOperator.DIAMOND));
        map.addSimpleMathCommand("bigtriangleup", new SimpleMathOperatorInterpretation(MathMLOperator.BIGTRIANGLEUP));
        map.addSimpleMathCommand("bigtriangledown", new SimpleMathOperatorInterpretation(MathMLOperator.BIGTRIANGLEDOWN));
        map.addSimpleMathCommand("triangleleft", new SimpleMathOperatorInterpretation(MathMLOperator.TRIANGLELEFT));
        map.addSimpleMathCommand("triangleright", new SimpleMathOperatorInterpretation(MathMLOperator.TRIANGLERIGHT));
        map.addSimpleMathCommand("oplus", new SimpleMathOperatorInterpretation(MathMLOperator.OPLUS));
        map.addSimpleMathCommand("ominus", new SimpleMathOperatorInterpretation(MathMLOperator.OMINUS));
        map.addSimpleMathCommand("otimes", new SimpleMathOperatorInterpretation(MathMLOperator.OTIMES));
        map.addSimpleMathCommand("oslash", new SimpleMathOperatorInterpretation(MathMLOperator.OSLASH));
        map.addSimpleMathCommand("odot", new SimpleMathOperatorInterpretation(MathMLOperator.ODOT));
        map.addSimpleMathCommand("bigcirc", new SimpleMathOperatorInterpretation(MathMLOperator.BIGCIRC));
        map.addSimpleMathCommand("dagger", new SimpleMathOperatorInterpretation(MathMLOperator.DAGGER));
        map.addSimpleMathCommand("ddagger", new SimpleMathOperatorInterpretation(MathMLOperator.DDAGGER));
        map.addSimpleMathCommand("amalg", new SimpleMathOperatorInterpretation(MathMLOperator.AMALG));
        map.addSimpleMathCommand("leq", new NottableMathOperatorInterpretation(MathMLOperator.LEQ, MathMLOperator.NOT_LEQ));
        map.addSimpleMathCommand("le", new NottableMathOperatorInterpretation(MathMLOperator.LEQ, MathMLOperator.NOT_LEQ));
        map.addSimpleMathCommand("prec", new NottableMathOperatorInterpretation(MathMLOperator.PREC, MathMLOperator.NOT_PREC));
        map.addSimpleMathCommand("preceq", new SimpleMathOperatorInterpretation(MathMLOperator.PRECEQ));
        map.addSimpleMathCommand("ll", new SimpleMathOperatorInterpretation(MathMLOperator.LL));
        map.addSimpleMathCommand("subset", new NottableMathOperatorInterpretation(MathMLOperator.SUBSET, MathMLOperator.NOT_SUBSET));
        map.addSimpleMathCommand("subseteq", new NottableMathOperatorInterpretation(MathMLOperator.SUBSETEQ, MathMLOperator.NOT_SUBSETEQ));
        map.addSimpleMathCommand("sqsubset", new SimpleMathOperatorInterpretation(MathMLOperator.SQSUBSET));
        map.addSimpleMathCommand("sqsubseteq", new NottableMathOperatorInterpretation(MathMLOperator.SQSUBSETEQ, MathMLOperator.NOT_SQSUBSETEQ));
        map.addSimpleMathCommand("in", new NottableMathOperatorInterpretation(MathMLOperator.IN, MathMLOperator.NOT_IN));
        map.addSimpleMathCommand("vdash", new NottableMathOperatorInterpretation(MathMLOperator.VDASH, MathMLOperator.NOT_VDASH));
        map.addSimpleMathCommand("geq", new NottableMathOperatorInterpretation(MathMLOperator.GEQ, MathMLOperator.NOT_GEQ));
        map.addSimpleMathCommand("ge", new NottableMathOperatorInterpretation(MathMLOperator.GEQ, MathMLOperator.NOT_GEQ));
        map.addSimpleMathCommand("succ", new NottableMathOperatorInterpretation(MathMLOperator.SUCC, MathMLOperator.NOT_SUCC));
        map.addSimpleMathCommand("succeq", new SimpleMathOperatorInterpretation(MathMLOperator.SUCCEQ));
        map.addSimpleMathCommand("gg", new SimpleMathOperatorInterpretation(MathMLOperator.GG));
        map.addSimpleMathCommand("supset", new NottableMathOperatorInterpretation(MathMLOperator.SUPSET, MathMLOperator.NOT_SUPSET));
        map.addSimpleMathCommand("supseteq", new NottableMathOperatorInterpretation(MathMLOperator.SUPSETEQ, MathMLOperator.NOT_SUPSETEQ));
        map.addSimpleMathCommand("sqsupset", new SimpleMathOperatorInterpretation(MathMLOperator.SQSUPSET));
        map.addSimpleMathCommand("sqsupseteq", new NottableMathOperatorInterpretation(MathMLOperator.SQSUPSETEQ, MathMLOperator.NOT_SQSUPSETEQ));
        map.addSimpleMathCommand("ni", new NottableMathOperatorInterpretation(MathMLOperator.NI, MathMLOperator.NOT_NI));
        map.addSimpleMathCommand("dashv", new SimpleMathOperatorInterpretation(MathMLOperator.DASHV));
        map.addSimpleMathCommand("equiv", new NottableMathOperatorInterpretation(MathMLOperator.EQUIV, MathMLOperator.NOT_EQUIV));
        map.addSimpleMathCommand("sim", new NottableMathOperatorInterpretation(MathMLOperator.SIM, MathMLOperator.NOT_SIM));
        map.addSimpleMathCommand("simeq", new NottableMathOperatorInterpretation(MathMLOperator.SIMEQ, MathMLOperator.NOT_SIMEQ));
        map.addSimpleMathCommand("asymp", new SimpleMathOperatorInterpretation(MathMLOperator.ASYMP));
        map.addSimpleMathCommand("approx", new NottableMathOperatorInterpretation(MathMLOperator.APPROX, MathMLOperator.NOT_APPROX));
        map.addSimpleMathCommand("cong", new NottableMathOperatorInterpretation(MathMLOperator.CONG, MathMLOperator.NOT_CONG));
        map.addSimpleMathCommand("neq", new SimpleMathOperatorInterpretation(MathMLOperator.NOT_IN));
        map.addSimpleMathCommand("doteq", new SimpleMathOperatorInterpretation(MathMLOperator.DOTEQ));
        map.addSimpleMathCommand("notin", new SimpleMathOperatorInterpretation(MathMLOperator.NOT_IN));
        map.addSimpleMathCommand("models", new SimpleMathOperatorInterpretation(MathMLOperator.MODELS));
        map.addSimpleMathCommand("perp", new SimpleMathOperatorInterpretation(MathMLOperator.PERP));
        map.addSimpleMathCommand("mid", new NottableMathOperatorInterpretation(MathMLOperator.MID, MathMLOperator.NOT_MID));
        map.addSimpleMathCommand("parallel", new SimpleMathOperatorInterpretation(MathMLOperator.PARALLEL));
        map.addSimpleMathCommand("bowtie", new SimpleMathOperatorInterpretation(MathMLOperator.BOWTIE));
        map.addSimpleMathCommand("smile", new SimpleMathOperatorInterpretation(MathMLOperator.SMILE));
        map.addSimpleMathCommand("frown", new SimpleMathOperatorInterpretation(MathMLOperator.FROWN));
        map.addSimpleMathCommand("propto", new SimpleMathOperatorInterpretation(MathMLOperator.PROPTO));
        
        /* Arrows */
        map.addSimpleMathCommand("leftarrow", new SimpleMathOperatorInterpretation(MathMLOperator.LEFTARROW));
        map.addSimpleMathCommand("Leftarrow", new SimpleMathOperatorInterpretation(MathMLOperator.UC_LEFTARROW));
        map.addSimpleMathCommand("rightarrow", new SimpleMathOperatorInterpretation(MathMLOperator.RIGHTARROW));
        map.addSimpleMathCommand("Rightarrow", new SimpleMathOperatorInterpretation(MathMLOperator.UC_RIGHTARROW));
        map.addSimpleMathCommand("leftrightarrow", new SimpleMathOperatorInterpretation(MathMLOperator.LEFTRIGHTARROW));
        map.addSimpleMathCommand("Leftrightarrow", new SimpleMathOperatorInterpretation(MathMLOperator.UC_LEFTRIGHTARROW));
        map.addSimpleMathCommand("mapsto", new SimpleMathOperatorInterpretation(MathMLOperator.MAPSTO));
        map.addSimpleMathCommand("hookleftarrow", new SimpleMathOperatorInterpretation(MathMLOperator.HOOKLEFTARROW));
        map.addSimpleMathCommand("leftharpoonup", new SimpleMathOperatorInterpretation(MathMLOperator.LEFTHARPOONUP));
        map.addSimpleMathCommand("leftharpoondown", new SimpleMathOperatorInterpretation(MathMLOperator.LEFTHARPOONDOWN));
        map.addSimpleMathCommand("rightleftharpoons", new SimpleMathOperatorInterpretation(MathMLOperator.RIGHTLEFTHARPOONS));
        map.addSimpleMathCommand("longleftarrow", new SimpleMathOperatorInterpretation(MathMLOperator.LEFTARROW)); /* NB: No appropriate Unicode symbols for long operators! */
        map.addSimpleMathCommand("Longleftarrow", new SimpleMathOperatorInterpretation(MathMLOperator.UC_LEFTARROW));
        map.addSimpleMathCommand("longrightarrow", new SimpleMathOperatorInterpretation(MathMLOperator.RIGHTARROW));
        map.addSimpleMathCommand("Longrightarrow", new SimpleMathOperatorInterpretation(MathMLOperator.UC_RIGHTARROW));
        map.addSimpleMathCommand("longleftrightarrow", new SimpleMathOperatorInterpretation(MathMLOperator.LEFTRIGHTARROW));
        map.addSimpleMathCommand("Longleftrightarrow", new SimpleMathOperatorInterpretation(MathMLOperator.UC_LEFTRIGHTARROW));
        map.addSimpleMathCommand("longmapsto", new SimpleMathOperatorInterpretation(MathMLOperator.MAPSTO));
        map.addSimpleMathCommand("hookrightarrow", new SimpleMathOperatorInterpretation(MathMLOperator.HOOKRIGHTARROW));
        map.addSimpleMathCommand("rightharpoonup", new SimpleMathOperatorInterpretation(MathMLOperator.RIGHTHARPOONOUP));
        map.addSimpleMathCommand("rightharpoondown", new SimpleMathOperatorInterpretation(MathMLOperator.RIGHTHARPOONDOWN));
        map.addSimpleMathCommand("uparrow", new SimpleMathOperatorInterpretation(MathMLOperator.UPARROW));
        map.addSimpleMathCommand("Uparrow", new SimpleMathOperatorInterpretation(MathMLOperator.UC_UPARROW));
        map.addSimpleMathCommand("downarrow", new SimpleMathOperatorInterpretation(MathMLOperator.DOWNARROW));
        map.addSimpleMathCommand("Downarrow", new SimpleMathOperatorInterpretation(MathMLOperator.UC_DOWNARROW));
        map.addSimpleMathCommand("updownarrow", new SimpleMathOperatorInterpretation(MathMLOperator.UPDOWNARROW));
        map.addSimpleMathCommand("Updownarrow", new SimpleMathOperatorInterpretation(MathMLOperator.UC_UPDOWNARROW));
        map.addSimpleMathCommand("nearrow", new SimpleMathOperatorInterpretation(MathMLOperator.NEARROW));
        map.addSimpleMathCommand("searrow", new SimpleMathOperatorInterpretation(MathMLOperator.SEARROW));
        map.addSimpleMathCommand("swarrow", new SimpleMathOperatorInterpretation(MathMLOperator.SWARROW));
        map.addSimpleMathCommand("nwarrow", new SimpleMathOperatorInterpretation(MathMLOperator.NWARROW));
        
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
        map.addSimpleMathCommand("nabla", new MathIdentifierInterpretation("\u2207"));
        map.addSimpleMathCommand("surd", new MathIdentifierInterpretation("\u221a"));
        map.addSimpleMathCommand("top", new MathIdentifierInterpretation("\u22a4"));
        map.addSimpleMathCommand("bot", new MathIdentifierInterpretation("\u22a5"));
        map.addSimpleMathCommand("|", new MathIdentifierInterpretation("\u2225"));
        map.addSimpleMathCommand("angle", new MathIdentifierInterpretation("\u2220"));
        map.addSimpleMathCommand("forall", new MathIdentifierInterpretation("\u2200"));
        map.addSimpleMathCommand("exists", new MathIdentifierInterpretation("\u2203"));
        map.addSimpleMathCommand("neg", new MathIdentifierInterpretation("\u00ac"));
        map.addSimpleMathCommand("lnot", new MathIdentifierInterpretation("\u00ac"));
        map.addSimpleMathCommand("flat", new MathIdentifierInterpretation("\u266d"));
        map.addSimpleMathCommand("natural", new MathIdentifierInterpretation("\u266e"));
        map.addSimpleMathCommand("sharp", new MathIdentifierInterpretation("\u266f"));
        map.addSimpleMathCommand("backslash", new MathIdentifierInterpretation("\\"));
        map.addSimpleMathCommand("partial", new MathIdentifierInterpretation("\u2202"));
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
        CMD_LEFT = map.addCombinerCommand("left", MATH_MODE_ONLY, EnumSet.of(InterpretationType.MATH_BRACKET_OPERATOR), null, null);
        CMD_RIGHT = map.addCombinerCommand("right", MATH_MODE_ONLY, EnumSet.of(InterpretationType.MATH_BRACKET_OPERATOR), null, null);
        
        /* Special bracket commands */
        map.addSimpleMathCommand("vert", new MathBracketOperatorInterpretation(MathMLOperator.VERT_BRACKET, MathMLOperator.VERT_BRACKET, BracketType.OPENER_OR_CLOSER));
        map.addSimpleMathCommand("Vert", new MathBracketOperatorInterpretation(MathMLOperator.DOUBLE_VERT_BRACKET, MathMLOperator.DOUBLE_VERT_BRACKET, BracketType.OPENER_OR_CLOSER));

        /* This is a LaTeX-specific combiner macro that always comes before a
         * {@link MathRelationOperatorInterpretation} command.
         */
        CMD_NOT = map.addCombinerCommand("not", MATH_MODE_ONLY, EnumSet.of(InterpretationType.MATH_RELATION_OPERATOR), new MathNotBuilder(), null);

        /* Complex math macros */
        map.addComplexCommandSameArgMode("sqrt", true, 1, MATH_MODE_ONLY, new MathRootBuilder(), null);
        CMD_FRAC = map.addComplexCommandSameArgMode("frac", false, 2, MATH_MODE_ONLY, new MathComplexCommandBuilder("mfrac"), null);
        CMD_OVER = map.addSimpleCommand("over", MATH_MODE_ONLY, null, null); /* TeX style fractions {... \over ...}, replaced during fixing *;
        
        /* Spacing */
        map.addSimpleCommand("quad", ALL_MODES, new SpaceNodeBuilder("\u00a0", "1em"), null);
        map.addSimpleCommand("qquad", ALL_MODES, new SpaceNodeBuilder("\u00a0\u00a0", "2em"), null);
        map.addComplexCommandSameArgMode("hspace", false, 1, ALL_MODES, new HSpaceNodeBuilder(), null);
        map.addComplexCommandSameArgMode("hspace*", false, 1, ALL_MODES, new HSpaceNodeBuilder(), null);

        /* Math accents */
        map.addComplexCommandSameArgMode("hat", false, 1, MATH_MODE_ONLY, new AccentBuilder(AccentMaps.CIRCUMFLEX, '\u0302', "mover"), null);
        map.addComplexCommandSameArgMode("bar", false, 1, MATH_MODE_ONLY, new AccentBuilder(null, '\u0304', "mover"), null);
        map.addComplexCommandSameArgMode("vec", false, 1, MATH_MODE_ONLY, new AccentBuilder(null, '\u2192', "mover"), null);
        map.addComplexCommandSameArgMode("dot", false, 1, MATH_MODE_ONLY, new AccentBuilder(null, '\u0307', "mover"), null);
        map.addComplexCommandSameArgMode("ddot", false, 1, MATH_MODE_ONLY, new AccentBuilder(null, '\u0308', "mover"), null);
        map.addComplexCommandSameArgMode("tilde", false, 1, MATH_MODE_ONLY, new AccentBuilder(AccentMaps.TILDE, '~', "mover"), null);
        map.addComplexCommandSameArgMode("widehat", false, 1, MATH_MODE_ONLY, new AccentBuilder(AccentMaps.CIRCUMFLEX, '\u0302', "mover"), null);
        map.addComplexCommandSameArgMode("widetilde", false, 1, MATH_MODE_ONLY, new AccentBuilder(AccentMaps.TILDE, '\u02dc', "mover"), null);
        map.addComplexCommandSameArgMode("overline", false, 1, MATH_MODE_ONLY, new AccentBuilder(null, '\u00af', "mover"), null);
        map.addComplexCommandSameArgMode("overbrace", false, 1, MATH_MODE_ONLY, new AccentBuilder(null, '\ufe37', "mover"), null);
        map.addComplexCommandSameArgMode("underbrace", false, 1, MATH_MODE_ONLY, new AccentBuilder(null, '\ufe38', "munder"), null);
        map.addComplexCommandSameArgMode("overrightarrow", false, 1, MATH_MODE_ONLY, new AccentBuilder(null, '\u20d7', "mover"), null);
        map.addComplexCommandSameArgMode("overleftarrow", false, 1, MATH_MODE_ONLY, new AccentBuilder(null, '\u20d6', "mover"), null);
        
        /* Dual-mode accents */
        map.addComplexCommandSameArgMode("underline", false, 1, ALL_MODES, StyleDeclarationInterpretation.UNDERLINE, new ModeDelegatingBuilder(styleInterpretationNodeBuilder, new AccentBuilder(null, '\u00af', "munder")), null);
        
        /* Complex multi-mode macros */
        map.addComplexCommandOneArg("mbox", false, ALL_MODES, LR, new BoxBuilder("mbox"), null);
        map.addComplexCommandOneArg("fbox", false, ALL_MODES, LR, new BoxBuilder("fbox"), null);
        
        /* Table stuff */
        CMD_HLINE = map.addSimpleCommand("hline", ALL_MODES, new TabularBuilder(), IGNORE);
        
        /* Commands for creating user-defined commands and environments */
        DoNothingHandler doNothingHandler = new DoNothingHandler();
        CMD_NEWCOMMAND = map.addComplexCommandSameArgMode("newcommand", false, 1, ALL_MODES, doNothingHandler, IGNORE);
        CMD_RENEWCOMMAND = map.addComplexCommandSameArgMode("renewcommand", false, 1, ALL_MODES, doNothingHandler, IGNORE);
        CMD_NEWENVIRONMENT = map.addComplexCommandSameArgMode("newenvironment", false, 2, ALL_MODES, doNothingHandler, IGNORE);
        CMD_RENEWENVIRONMENT = map.addComplexCommandSameArgMode("renewenvironment", false, 2, ALL_MODES, doNothingHandler, IGNORE);
        
        /* Special XHTML helpers */
        map.addComplexCommand("href", true, 1, TEXT_MODE_ONLY, new LaTeXMode[] { LR, VERBATIM }, new HrefBuilder(), ALLOW_INLINE);
        map.addComplexCommandOneArg("anchor", false, TEXT_MODE_ONLY, VERBATIM, new AnchorHandler(), ALLOW_INLINE);
        map.addComplexCommandOneArg("anchor*", false, TEXT_MODE_ONLY, LR, new AnchorHandler(), ALLOW_INLINE);
        
        /* Commands for creating custom XML (also see related environments) */
        CMD_XML_ATTR = map.addComplexCommand("xmlAttr", false, 3, ALL_MODES, new LaTeXMode[] { LR, LR, LR }, new XMLAttrHandler(), IGNORE);
        map.addComplexCommand("xmlBlockElement", true, 3, ALL_MODES, new LaTeXMode[] { LR, LR, LR, null }, new XMLBlockElementBuilder(), START_NEW_XHTML_BLOCK);
        map.addComplexCommand("xmlInlineElement", true, 3, ALL_MODES, new LaTeXMode[] { LR, LR, LR, null }, new XMLInlineElementBuilder(), ALLOW_INLINE);
        map.addComplexCommandOneArg("xmlName", false, ALL_MODES, VERBATIM, new XMLNameOrIdHandler(XMLNameOrIdHandler.NAME), IGNORE);
        map.addComplexCommandOneArg("xmlName*", false, ALL_MODES, LR, new XMLNameOrIdHandler(XMLNameOrIdHandler.NAME), IGNORE);
        map.addComplexCommandOneArg("xmlId", false, ALL_MODES, VERBATIM, new XMLNameOrIdHandler(XMLNameOrIdHandler.ID), IGNORE);
        map.addComplexCommandOneArg("xmlId*", false, ALL_MODES, LR, new XMLNameOrIdHandler(XMLNameOrIdHandler.ID), IGNORE);
        
        /* =================================== ENVIRONMENTS ================================= */
        
        ENV_MATH = map.addEnvironment("math", TEXT_MODE_ONLY, MATH, null, new MathEnvironmentBuilder(), ALLOW_INLINE);
        ENV_DISPLAYMATH = map.addEnvironment("displaymath", TEXT_MODE_ONLY, MATH, null, new MathEnvironmentBuilder(), ALLOW_INLINE);
        ENV_VERBATIM = map.addEnvironment("verbatim", PARA_MODE_ONLY, VERBATIM, null, new VerbatimBuilder(false), START_NEW_XHTML_BLOCK);
        ENV_ITEMIZE = map.addEnvironment("itemize", PARA_MODE_ONLY, null, null, new ListEnvironmentBuilder(), START_NEW_XHTML_BLOCK);
        ENV_ENUMERATE = map.addEnvironment("enumerate", PARA_MODE_ONLY, null, null, new ListEnvironmentBuilder(), START_NEW_XHTML_BLOCK);
        ENV_TABULAR = map.addEnvironment("tabular", false, 1, PARA_MODE_ONLY, PARAGRAPH, null, new TabularBuilder(), START_NEW_XHTML_BLOCK);
        ENV_ARRAY = map.addEnvironment("array", false, 1, MATH_MODE_ONLY, MATH, null, new ArrayBuilder(), null);
        ENV_EQNARRAY = map.addEnvironment("eqnarray", PARA_MODE_ONLY, MATH, null, new EqnArrayBuilder(), START_NEW_XHTML_BLOCK);
        ENV_EQNARRAYSTAR = map.addEnvironment("eqnarray*", PARA_MODE_ONLY, MATH, null, new EqnArrayBuilder(), START_NEW_XHTML_BLOCK);
        
        /* Simple text environments */
        map.addEnvironment("quote", PARA_MODE_ONLY, PARAGRAPH, null, new SimpleXHTMLContainerBuilder("blockquote"), START_NEW_XHTML_BLOCK);
        
        /* Text justification environments. (Note that each line is supposed to be delimited by '\\' */
        map.addEnvironment("center", PARA_MODE_ONLY, PARAGRAPH, null, new SimpleXHTMLContainerBuilder("div", "center"), START_NEW_XHTML_BLOCK);
        map.addEnvironment("flushleft", PARA_MODE_ONLY, PARAGRAPH, null, new SimpleXHTMLContainerBuilder("div", "flushleft"), START_NEW_XHTML_BLOCK);
        map.addEnvironment("flushright", PARA_MODE_ONLY, PARAGRAPH, null, new SimpleXHTMLContainerBuilder("div", "flushright"), START_NEW_XHTML_BLOCK);
        
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
        
        /* Special "fence" environment in Math mode */
        ENV_FENCED = map.addEnvironment("fenced", false, 2, MATH_MODE_ONLY, MATH, null, new MfenceBuilder(), null);

        /* Environments for generating custom XML islands (see corresponding command versions as well) */
        map.addEnvironment("xmlBlockElement", true, 2, ALL_MODES, null, null, new XMLBlockElementBuilder(), START_NEW_XHTML_BLOCK);
        map.addEnvironment("xmlInlineElement", true, 2, ALL_MODES, null, null, new XMLInlineElementBuilder(), ALLOW_INLINE);
    }
}
