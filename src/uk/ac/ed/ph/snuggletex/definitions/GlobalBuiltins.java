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
import static uk.ac.ed.ph.snuggletex.definitions.TextFlowContext.ALLOW_INLINE;
import static uk.ac.ed.ph.snuggletex.definitions.TextFlowContext.IGNORE;
import static uk.ac.ed.ph.snuggletex.definitions.TextFlowContext.START_NEW_XHTML_BLOCK;

import uk.ac.ed.ph.snuggletex.dombuilding.AccentBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.BoxBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.CharacterCommandHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.DoNothingHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EqnArrayBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.InterpretableSimpleMathBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.LineBreakHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.ListEnvironmentBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.MathComplexCommandBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.MathEnvironmentBuilder;
import uk.ac.ed.ph.snuggletex.dombuilding.MathNotBuilder;
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
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathFunctionIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.MathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathRelationOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.StyleDeclarationInterpretation;

import java.util.EnumSet;

/**
 * This defines the default set of {@link BuiltinCommand} and {@link BuiltinEnvironment}
 * supported by SnuggleTeX.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class GlobalBuiltins {
    
    public static BuiltinCommand CHAR_BACKSLASH;
    public static BuiltinCommand FRAC;
    public static BuiltinCommand NOT;
    public static BuiltinCommand APPLY_FUNCTION;
    public static BuiltinCommand INVISIBLE_TIMES;
    public static BuiltinCommand ITEM;
    public static BuiltinCommand LIST_ITEM;
    public static BuiltinCommand LEFT;
    public static BuiltinCommand RIGHT;
    public static BuiltinCommand MROW;
    public static BuiltinCommand MSUB;
    public static BuiltinCommand MSUP;
    public static BuiltinCommand MSUBSUP;
    public static BuiltinCommand NEWCOMMAND;
    public static BuiltinCommand RENEWCOMMAND;
    public static BuiltinCommand NEWENVIRONMENT;
    public static BuiltinCommand RENEWENVIRONMENT;
    public static BuiltinCommand OVER;
    public static BuiltinCommand PAR;
    public static BuiltinCommand PARAGRAPH;
    public static BuiltinCommand TABLE_COLUMN;
    public static BuiltinCommand TABLE_ROW;
    public static BuiltinCommand VERB;
    public static BuiltinCommand XML_ATTR;

    public static BuiltinEnvironment DISPLAYMATH;
    public static BuiltinEnvironment FENCED;
    public static BuiltinEnvironment MATH;
    public static BuiltinEnvironment VERBATIM;
    public static BuiltinEnvironment ITEMIZE;
    public static BuiltinEnvironment ENUMERATE;
    public static BuiltinEnvironment TABULAR;
    public static BuiltinEnvironment EQNARRAY;
    public static BuiltinEnvironment EQNARRAYSTAR;
    
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
        CHAR_BACKSLASH = map.addSimpleCommand("\\", ALL_MODES, new LineBreakHandler(), null);
        map.addSimpleCommand("$", ALL_MODES, new CharacterCommandHandler("$"), ALLOW_INLINE);
        map.addSimpleCommand("%", ALL_MODES, new CharacterCommandHandler("%"), ALLOW_INLINE);
        map.addSimpleCommand("#", ALL_MODES, new CharacterCommandHandler("#"), ALLOW_INLINE);
        map.addSimpleCommand("&", ALL_MODES, new CharacterCommandHandler("&"), ALLOW_INLINE);
        map.addSimpleCommand("_", ALL_MODES, new CharacterCommandHandler("_"), ALLOW_INLINE);
        map.addSimpleCommand("{", ALL_MODES,
                new MathBracketOperatorInterpretation(MathMLOperator.OPEN_CURLY_BRACKET, MathMLOperator.CLOSE_CURLY_BRACKET, true),
                new ModeDelegatingBuilder(new CharacterCommandHandler("{"), new InterpretableSimpleMathBuilder()), null);
        map.addSimpleCommand("}", ALL_MODES,
                new MathBracketOperatorInterpretation(MathMLOperator.CLOSE_CURLY_BRACKET, MathMLOperator.OPEN_CURLY_BRACKET, false),
                new ModeDelegatingBuilder(new CharacterCommandHandler("}"), new InterpretableSimpleMathBuilder()), null);
        map.addSimpleCommand(",", ALL_MODES, new SpaceNodeBuilder("\u2009", "0.167em"), ALLOW_INLINE); /* Thin space, all modes */
        map.addSimpleCommand(":", MATH_MODE_ONLY, new SpaceNodeBuilder(null, "0.222em"), null); /* Medium space, math only */
        map.addSimpleCommand(";", MATH_MODE_ONLY, new SpaceNodeBuilder(null, "0.278em"), null); /* Thick space, math only */
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
        PAR = map.addSimpleCommand("par", TEXT_MODE_ONLY, null, null); /* (This is substituted during fixing) */
        map.addSimpleCommand("newline", ALL_MODES, new LineBreakHandler(), null);
        VERB = map.addSimpleCommand("verb", PARA_MODE_ONLY, null, null);
        ITEM = map.addSimpleCommand("item", PARA_MODE_ONLY, new ListEnvironmentBuilder(), null);

        /* Tree version of a paragraph. The {@link TokenFixer} will create these, removing any
         * instances of {@link #PAR} and {@link TokenType#NEW_PARAGRAPH}.
         * <p>
         * I am not allowing this to be directly input, as this makes processing a bit easier
         * since it avoids the possibility of nested paragraphs.
         */
        PARAGRAPH = map.addComplexCommandSameArgMode(null, false, 1, TEXT_MODE_ONLY, new ParagraphBuilder(), START_NEW_XHTML_BLOCK);
        
        /* Tree version of standard \item. Any \items are converted to these during token fixing.
         * I'm not allowing this to be directly input, which makes list handling a bit easier.
         */
        LIST_ITEM = map.addComplexCommandSameArgMode(null, false, 1, PARA_MODE_ONLY, new ListEnvironmentBuilder(), START_NEW_XHTML_BLOCK);
        
        /* Tree-like placeholders for specifying columns and rows in environments such as 'tabular'.
         * We don't allow to be inputed as the containment requirements can make it awkward to ensure
         * that the input is valid. These tokens are produced during the fixing process and make it
         * easier to handle the table content further down the line.
         */
        TABLE_ROW = map.addComplexCommandSameArgMode(null, false, 1, ALL_MODES, null, null);
        TABLE_COLUMN = map.addComplexCommandSameArgMode(null, false, 1, ALL_MODES, null, null);
        
        /* Semantic versions of MathML "&ApplyFunction;" and "&InvisibleTimes;" entities */
        APPLY_FUNCTION = map.addSimpleMathCommand("af", new MathOperatorInterpretation(MathMLOperator.APPLY_FUNCTION));
        INVISIBLE_TIMES = map.addSimpleMathCommand("itimes", new MathOperatorInterpretation(MathMLOperator.INVISIBLE_TIMES));
        
        /* Variants of MathML constructs. These are substituted from traditional LaTeX constructs
         * by {@link TokenFixer}
         */
        MROW = map.addComplexCommandSameArgMode("mrow", false, 1, MATH_MODE_ONLY, new MrowBuilder(), null);
        MSUB = map.addComplexCommandSameArgMode("msub", false, 2, MATH_MODE_ONLY, new MathComplexCommandBuilder("msub"), null);
        MSUP = map.addComplexCommandSameArgMode("msup", false, 2, MATH_MODE_ONLY, new MathComplexCommandBuilder("msup"), null);
        MSUBSUP = map.addComplexCommandSameArgMode("msubsup", false, 3, MATH_MODE_ONLY, new MathComplexCommandBuilder("msubsup"), null);
        
        /* old-style P/LR mode style change macros, slightly complicated due to the way they
         * apply until the end of the current group, resulting in a lack of tree structure.
         * These are replaced by environments of the same name during token fixing, which agrees
         * with LaTeX spec. (see p28 of Lamport)
         * 
         * Note: each of these MUST be accompanied by a corresponding Environment definition.
         * (These are declared later in this file.)
         */
        map.addSimpleCommand("em", TEXT_MODE_ONLY, StyleDeclarationInterpretation.EM, null, null);
        map.addSimpleCommand("bf", TEXT_MODE_ONLY, StyleDeclarationInterpretation.BF, null, null);
        map.addSimpleCommand("rm", TEXT_MODE_ONLY, StyleDeclarationInterpretation.RM, null, null);
        map.addSimpleCommand("it", TEXT_MODE_ONLY, StyleDeclarationInterpretation.IT, null, null);
        map.addSimpleCommand("tt", TEXT_MODE_ONLY, StyleDeclarationInterpretation.TT, null, null);
        map.addSimpleCommand("sc", TEXT_MODE_ONLY, StyleDeclarationInterpretation.SC, null, null);
        map.addSimpleCommand("sl", TEXT_MODE_ONLY, StyleDeclarationInterpretation.SL, null, null);
        map.addSimpleCommand("sf", TEXT_MODE_ONLY, StyleDeclarationInterpretation.SF, null, null);
        
        /* New style P/LR mode style change macros. These take the text they are being applied to
         * as a single argument.
         */
        map.addComplexCommandSameArgMode("textrm", false, 1, TEXT_MODE_ONLY, StyleDeclarationInterpretation.RM, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("textsf", false, 1, TEXT_MODE_ONLY, StyleDeclarationInterpretation.SF, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("textit", false, 1, TEXT_MODE_ONLY, StyleDeclarationInterpretation.IT, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("textsl", false, 1, TEXT_MODE_ONLY, StyleDeclarationInterpretation.SL, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("textsc", false, 1, TEXT_MODE_ONLY, StyleDeclarationInterpretation.SC, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("textbf", false, 1, TEXT_MODE_ONLY, StyleDeclarationInterpretation.BF, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("texttt", false, 1, TEXT_MODE_ONLY, StyleDeclarationInterpretation.TT, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("emph", false, 1, TEXT_MODE_ONLY, StyleDeclarationInterpretation.EM, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        
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
        
        /* Styling (c.f. equivalents in text mode, listed above) */
        map.addComplexCommandSameArgMode("mathrm", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.RM, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("mathsf", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.SF, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("mathit", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.IT, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("mathbf", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.BF, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addComplexCommandSameArgMode("mathtt", false, 1, MATH_MODE_ONLY, StyleDeclarationInterpretation.TT, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        
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
        map.addSimpleMathCommand("varpi", new MathIdentifierInterpretation("\u03b1"));
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
        map.addSimpleMathCommand("Omega", new MathIdentifierInterpretation("\u03c9"));
        
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
        map.addSimpleMathCommand("liminf", new MathFunctionIdentifierInterpretation("lim inf")); /* TODO: Check spacing here! */
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
        map.addSimpleMathCommand("leq", new MathRelationOperatorInterpretation(MathMLOperator.LEQ, MathMLOperator.NOT_LEQ));
        map.addSimpleMathCommand("le", new MathRelationOperatorInterpretation(MathMLOperator.LEQ, MathMLOperator.NOT_LEQ));
        map.addSimpleMathCommand("prec", new MathRelationOperatorInterpretation(MathMLOperator.PREC, MathMLOperator.NOT_PREC));
        map.addSimpleMathCommand("preceq", new MathOperatorInterpretation(MathMLOperator.PRECEQ));
        map.addSimpleMathCommand("ll", new MathOperatorInterpretation(MathMLOperator.LL));
        map.addSimpleMathCommand("subset", new MathRelationOperatorInterpretation(MathMLOperator.SUBSET, MathMLOperator.NOT_SUBSET));
        map.addSimpleMathCommand("subseteq", new MathRelationOperatorInterpretation(MathMLOperator.SUBSETEQ, MathMLOperator.NOT_SUBSETEQ));
        map.addSimpleMathCommand("sqsubset", new MathOperatorInterpretation(MathMLOperator.SQSUBSET));
        map.addSimpleMathCommand("sqsubseteq", new MathRelationOperatorInterpretation(MathMLOperator.SQSUBSETEQ, MathMLOperator.NOT_SQSUBSETEQ));
        map.addSimpleMathCommand("in", new MathRelationOperatorInterpretation(MathMLOperator.IN, MathMLOperator.NOT_IN));
        map.addSimpleMathCommand("vdash", new MathRelationOperatorInterpretation(MathMLOperator.VDASH, MathMLOperator.NOT_VDASH));
        map.addSimpleMathCommand("geq", new MathRelationOperatorInterpretation(MathMLOperator.GEQ, MathMLOperator.NOT_GEQ));
        map.addSimpleMathCommand("ge", new MathRelationOperatorInterpretation(MathMLOperator.GEQ, MathMLOperator.NOT_GEQ));
        map.addSimpleMathCommand("succ", new MathRelationOperatorInterpretation(MathMLOperator.SUCC, MathMLOperator.NOT_SUCC));
        map.addSimpleMathCommand("succeq", new MathOperatorInterpretation(MathMLOperator.SUCCEQ));
        map.addSimpleMathCommand("gg", new MathOperatorInterpretation(MathMLOperator.GG));
        map.addSimpleMathCommand("supset", new MathRelationOperatorInterpretation(MathMLOperator.SUPSET, MathMLOperator.NOT_SUPSET));
        map.addSimpleMathCommand("supseteq", new MathRelationOperatorInterpretation(MathMLOperator.SUPSETEQ, MathMLOperator.NOT_SUPSETEQ));
        map.addSimpleMathCommand("sqsupset", new MathOperatorInterpretation(MathMLOperator.SQSUPSET));
        map.addSimpleMathCommand("sqsupseteq", new MathRelationOperatorInterpretation(MathMLOperator.SQSUPSETEQ, MathMLOperator.NOT_SQSUPSETEQ));
        map.addSimpleMathCommand("ni", new MathRelationOperatorInterpretation(MathMLOperator.NI, MathMLOperator.NOT_NI));
        map.addSimpleMathCommand("dashv", new MathOperatorInterpretation(MathMLOperator.DASHV));
        map.addSimpleMathCommand("equiv", new MathRelationOperatorInterpretation(MathMLOperator.EQUIV, MathMLOperator.NOT_EQUIV));
        map.addSimpleMathCommand("sim", new MathRelationOperatorInterpretation(MathMLOperator.SIM, MathMLOperator.NOT_SIM));
        map.addSimpleMathCommand("simeq", new MathRelationOperatorInterpretation(MathMLOperator.SIMEQ, MathMLOperator.NOT_SIMEQ));
        map.addSimpleMathCommand("asymp", new MathOperatorInterpretation(MathMLOperator.ASYMP));
        map.addSimpleMathCommand("approx", new MathRelationOperatorInterpretation(MathMLOperator.APPROX, MathMLOperator.NOT_APPROX));
        map.addSimpleMathCommand("cong", new MathRelationOperatorInterpretation(MathMLOperator.CONG, MathMLOperator.NOT_CONG));
        map.addSimpleMathCommand("neq", new MathOperatorInterpretation(MathMLOperator.NOT_IN));
        map.addSimpleMathCommand("doteq", new MathOperatorInterpretation(MathMLOperator.DOTEQ));
        map.addSimpleMathCommand("notin", new MathOperatorInterpretation(MathMLOperator.NOT_IN));
        map.addSimpleMathCommand("models", new MathOperatorInterpretation(MathMLOperator.MODELS));
        map.addSimpleMathCommand("perp", new MathOperatorInterpretation(MathMLOperator.PERP));
        map.addSimpleMathCommand("mid", new MathRelationOperatorInterpretation(MathMLOperator.MID, MathMLOperator.NOT_MID));
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

        /* Math combiner commands that absorb the (bracket) token immediately after. These are
         * converted to fences during token fixing.
         */
        LEFT = map.addCombinerCommand("left", MATH_MODE_ONLY, EnumSet.of(InterpretationType.MATH_BRACKET_OPERATOR), null, null);
        RIGHT = map.addCombinerCommand("right", MATH_MODE_ONLY, EnumSet.of(InterpretationType.MATH_BRACKET_OPERATOR), null, null);

        /* This is a LaTeX-specific combiner macro that always comes before a
         * {@link MathRelationOperatorInterpretation} command.
         */
        NOT = map.addCombinerCommand("not", MATH_MODE_ONLY, EnumSet.of(InterpretationType.MATH_RELATION_OPERATOR), new MathNotBuilder(), null);

        /* Complex math macros */
        map.addComplexCommandSameArgMode("sqrt", false, 1, MATH_MODE_ONLY, new MathComplexCommandBuilder("msqrt"), null);
        FRAC = map.addComplexCommandSameArgMode("frac", false, 2, MATH_MODE_ONLY, new MathComplexCommandBuilder("mfrac"), null);
        OVER = map.addSimpleCommand("over", MATH_MODE_ONLY, null, null); /* TeX style fractions {... \over ...}, replaced during fixing *;
        
        /* Spacing */
        map.addSimpleCommand("quad", ALL_MODES, new SpaceNodeBuilder("\u00a0", "1em"), null);
        map.addSimpleCommand("qquad", ALL_MODES, new SpaceNodeBuilder("\u00a0\u00a0", "2em"), null);

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
        map.addComplexCommandSameArgMode("underline", false, 1, ALL_MODES, StyleDeclarationInterpretation.UNDERLINE, new ModeDelegatingBuilder(new StyleInterpretationNodeBuilder(), new AccentBuilder(null, '\u00af', "munder")), null);
        
        /* Complex multi-mode macros */
        map.addComplexCommandOneArg("mbox", false, ALL_MODES, LR, new BoxBuilder("mbox"), null);
        map.addComplexCommandOneArg("fbox", false, ALL_MODES, LR, new BoxBuilder("fbox"), null);
        
        /* Commands for creating user-defined commands and environments */
        NEWCOMMAND = map.addComplexCommandSameArgMode("newcommand", false, 1, ALL_MODES, new DoNothingHandler(), IGNORE);
        RENEWCOMMAND = map.addComplexCommandSameArgMode("renewcommand", false, 1, ALL_MODES, new DoNothingHandler(), IGNORE);
        NEWENVIRONMENT = map.addComplexCommandSameArgMode("newenvironment", false, 2, ALL_MODES, new DoNothingHandler(), IGNORE);
        RENEWENVIRONMENT = map.addComplexCommandSameArgMode("renewenvironment", false, 2, ALL_MODES, new DoNothingHandler(), IGNORE);
        
        /* Commands for creating custom XML (also see related environments) */
        XML_ATTR = map.addComplexCommand("xmlAttr", false, 3, ALL_MODES, new LaTeXMode[] { LR, LR, LR }, new XMLAttrHandler(), IGNORE);
        map.addComplexCommand("xmlBlockElement", true, 3, ALL_MODES, new LaTeXMode[] { LR, LR, LR, null }, new XMLBlockElementBuilder(), START_NEW_XHTML_BLOCK);
        map.addComplexCommand("xmlInlineElement", true, 3, ALL_MODES, new LaTeXMode[] { LR, LR, LR, null }, new XMLInlineElementBuilder(), ALLOW_INLINE);
        
        /* =================================== ENVIRONMENTS ================================= */
        
        MATH = map.addEnvironment("math", TEXT_MODE_ONLY, LaTeXMode.MATH, null, new MathEnvironmentBuilder(), ALLOW_INLINE);
        DISPLAYMATH = map.addEnvironment("displaymath", TEXT_MODE_ONLY, LaTeXMode.MATH, null, new MathEnvironmentBuilder(), ALLOW_INLINE);
        VERBATIM = map.addEnvironment("verbatim", PARA_MODE_ONLY, LaTeXMode.VERBATIM, null, new VerbatimBuilder(), START_NEW_XHTML_BLOCK);
        ITEMIZE = map.addEnvironment("itemize", PARA_MODE_ONLY, null, null, new ListEnvironmentBuilder(), START_NEW_XHTML_BLOCK);
        ENUMERATE = map.addEnvironment("enumerate", PARA_MODE_ONLY, null, null, new ListEnvironmentBuilder(), START_NEW_XHTML_BLOCK);
        TABULAR = map.addEnvironment("tabular", false, 1, PARA_MODE_ONLY, LaTeXMode.PARAGRAPH, null, new TabularBuilder(), START_NEW_XHTML_BLOCK);
        EQNARRAY = map.addEnvironment("eqnarray", PARA_MODE_ONLY, LaTeXMode.MATH, null, new EqnArrayBuilder(), START_NEW_XHTML_BLOCK);
        EQNARRAYSTAR = map.addEnvironment("eqnarray*", PARA_MODE_ONLY, LaTeXMode.MATH, null, new EqnArrayBuilder(), START_NEW_XHTML_BLOCK);
        
        /* Simple text environments */
        map.addEnvironment("quote", PARA_MODE_ONLY, LaTeXMode.PARAGRAPH, null, new SimpleXHTMLContainerBuilder("blockquote"), START_NEW_XHTML_BLOCK);
        
        /* Text justification environments. (Note that each line is supposed to be delimited by '\\' */
        map.addEnvironment("center", PARA_MODE_ONLY, LaTeXMode.PARAGRAPH, null, new SimpleXHTMLContainerBuilder("div", "center"), START_NEW_XHTML_BLOCK);
        map.addEnvironment("flushleft", PARA_MODE_ONLY, LaTeXMode.PARAGRAPH, null, new SimpleXHTMLContainerBuilder("div", "flushleft"), START_NEW_XHTML_BLOCK);
        map.addEnvironment("flushright", PARA_MODE_ONLY, LaTeXMode.PARAGRAPH, null, new SimpleXHTMLContainerBuilder("div", "flushright"), START_NEW_XHTML_BLOCK);
        
        /* Alternative versions of \em and friends. These are converted internally to
         * environments as they're easier to deal with like that.
         */
        map.addEnvironment("em", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.EM, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("bf", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.BF, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("rm", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.RM, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("it", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.IT, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("tt", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.TT, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("sc", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SC, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("sl", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SL, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("sf", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SF, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        
        map.addEnvironment("tiny", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.TINY, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("scriptsize", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SCRIPTSIZE, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("footnotesize", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.FOOTNOTESIZE, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("small", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.SMALL, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("normalsize", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.NORMALSIZE, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("large", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.LARGE, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("Large", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.LARGE_2, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("LARGE", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.LARGE_3, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("huge", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.HUGE, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        map.addEnvironment("Huge", TEXT_MODE_ONLY, null, StyleDeclarationInterpretation.HUGE_2, new StyleInterpretationNodeBuilder(), ALLOW_INLINE);
        
        /* Special "fence" environment in Math mode */
        FENCED = map.addEnvironment("fenced", false, 2, MATH_MODE_ONLY, LaTeXMode.MATH, null, new MfenceBuilder(), null);

        /* Environments for generating custom XML islands (see corresponding command versions as well) */
        map.addEnvironment("xmlBlockElement", true, 2, ALL_MODES, null, null, new XMLBlockElementBuilder(), START_NEW_XHTML_BLOCK);
        map.addEnvironment("xmlInlineElement", true, 2, ALL_MODES, null, null, new XMLInlineElementBuilder(), ALLOW_INLINE);
    }
}
