/* $Id:MathMLOperator.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import static uk.ac.ed.ph.snuggletex.semantics.MathMLOperator.OperatorType.INFIX;
import static uk.ac.ed.ph.snuggletex.semantics.MathMLOperator.OperatorType.POSTFIX;
import static uk.ac.ed.ph.snuggletex.semantics.MathMLOperator.OperatorType.PREFIX;

/**
 * Enumerates the various MathML operators.
 * 
 * @author David McKain
 * @version $Revision:179 $
 */
public enum MathMLOperator {

    ADD(INFIX, "+"),
    SUBTRACT(INFIX, "-"),
    ASTERISK(INFIX, "*"),
    SLASH(INFIX, "/"),
    COMMA(INFIX, ","),
    EQUALS(INFIX, "="),
    FACTORIAL(POSTFIX, "!"),
    BACKSLASH(INFIX, "\\"),    
    NEG(PREFIX, "\u00ac"),
    
    LESS_THAN_OR_OPEN_ANGLE_BRACKET(INFIX, "<"),
    GREATER_THAN_OR_CLOSE_ANGLE_BRACKET(INFIX, ">"),
    NOT_LESS_THAN(INFIX, "\u226e"),
    NOT_GREATER_THAN(INFIX, "\u226f"),
    
    OPEN_BRACKET(PREFIX, "("),
    CLOSE_BRACKET(POSTFIX, ")"),
    OPEN_CURLY_BRACKET(PREFIX, "{"),
    CLOSE_CURLY_BRACKET(POSTFIX, "}"),
    OPEN_SQUARE_BRACKET(PREFIX, "["),
    CLOSE_SQUARE_BRACKET(POSTFIX, "]"),
    VERT_BRACKET(INFIX, "|"), /* This is both an opener and a closer! */
    DOUBLE_VERT_BRACKET(INFIX, "\u2225"), /* This is both an opener and a closer! */

    PM(INFIX, "\u00b1"),
    TIMES(INFIX, "\u00d7"),
    DIV(INFIX, "\u00f7"),
    
    DAGGER(INFIX, "\u2020"),
    DDAGGER(INFIX, "\u2021"),
    APPLY_FUNCTION(INFIX, "\u2061"),
    INVISIBLE_TIMES(INFIX, "\u2062"),

    LEFTARROW(INFIX, "\u2190"),
    UPARROW(INFIX, "\u2191"),
    RIGHTARROW(INFIX, "\u2192"),
    DOWNARROW(INFIX, "\u2193"),
    LEFTRIGHTARROW(INFIX, "\u2194"),
    UPDOWNARROW(INFIX, "\u2195"),
    NWARROW(INFIX, "\u2196"),
    NEARROW(INFIX, "\u2197"),
    SEARROW(INFIX, "\u2198"),
    SWARROW(INFIX, "\u2199"),
    MAPSTO(INFIX, "\u21a6"),
    HOOKLEFTARROW(INFIX, "\u21a9"),
    HOOKRIGHTARROW(INFIX, "\u21aa"),
    LEFTHARPOONUP(INFIX, "\u21bc"),
    LEFTHARPOONDOWN(INFIX, "\u21bd"),
    RIGHTHARPOONOUP(INFIX, "\u21c0"),
    RIGHTHARPOONDOWN(INFIX, "\u21c1"),
    RIGHTLEFTHARPOONS(INFIX, "\u21cc"),
    UC_LEFTARROW(INFIX, "\u21d0"),
    UC_UPARROW(INFIX, "\u21d1"),
    UC_RIGHTARROW(INFIX, "\u21d2"),
    UC_DOWNARROW(INFIX, "\u21d3"),
    UC_LEFTRIGHTARROW(INFIX, "\u21d4"),
    UC_UPDOWNARROW(INFIX, "\u21d5"),
    
    FORALL(PREFIX, "\u2200"),
    PARTIAL(PREFIX, "\u2202"),
    EXISTS(PREFIX, "\u2203"),
    IN(INFIX, "\u2208"),
    NABLA(PREFIX, "\u2207"),
    NOT_IN(INFIX, "\u2209"),
    NI(INFIX, "\u220b"),
    NOT_NI(INFIX, "\u220c"),
    PROD(PREFIX, "\u220f", true),
    COPROD(PREFIX, "\u2210", true),
    SUM(PREFIX, "\u2211", true),
    CIRC(INFIX, "\u2218"),
    BULLET(INFIX, "\u2219"),
    MP(INFIX, "\u2213"),
    SETMINUS(INFIX, "\u2216"),
    AST(INFIX, "\u2217"),
    PROPTO(INFIX, "\u221d"),
    MID(INFIX, "\u2223"),
    NOT_MID(INFIX, "\u2224"),
    PARALLEL(INFIX, "\u2225"),
    NOT_PARALLEL(INFIX, "\u2226"),
    WEDGE(INFIX, "\u2227"),
    VEE(INFIX, "\u2228"),
    CAP(INFIX, "\u2229"),
    CUP(INFIX, "\u222a"),
    SURD(PREFIX, "\u221a"),
    ANGLE(PREFIX, "\u2220"),
    INTEGRAL(PREFIX, "\u222b"),
    OINT(PREFIX, "\u222e"),
    TOP(INFIX, "\u22a4"),
    BOT(INFIX, "\u22a5"),
    SIM(INFIX, "\u223c"),
    WR(INFIX, "\u2240"),
    NOT_SIM(INFIX, "\u2241"),
    SIMEQ(INFIX, "\u2243"),
    NOT_SIMEQ(INFIX, "\u2244"),
    CONG(INFIX, "\u2245"),
    NOT_CONG(INFIX, "\u2246"),
    APPROX(INFIX, "\u2248"),
    NOT_APPROX(INFIX, "\u2249"),
    ASYMP(INFIX, "\u224d"),
    DOTEQ(INFIX, "\u2250"),
    NEQ(INFIX, "\u2260"),
    EQUIV(INFIX, "\u2261"),
    NOT_EQUIV(INFIX, "\u2262"),
    LEQ(INFIX, "\u2264"),
    GEQ(INFIX, "\u2265"),
    LL(INFIX, "\u226a"),
    GG(INFIX, "\u226b"),
    NOT_LEQ(INFIX, "\u2270"),
    NOT_GEQ(INFIX, "\u2271"),
    PREC(INFIX, "\u227a"),
    SUCC(INFIX, "\u227b"),
    PRECEQ(INFIX, "\u227c"),
    SUCCEQ(INFIX, "\u227d"),
    NOT_PREC(INFIX, "\u2280"),
    NOT_SUCC(INFIX, "\u2281"),
    SUBSET(INFIX, "\u2282"),
    SUPSET(INFIX, "\u2283"),
    NOT_SUBSET(INFIX, "\u2284"),
    NOT_SUPSET(INFIX, "\u2285"),
    SUBSETEQ(INFIX, "\u2286"),
    SUPSETEQ(INFIX, "\u2287"),
    NOT_SUBSETEQ(INFIX, "\u2288"),
    NOT_SUPSETEQ(INFIX, "\u2289"),
    UPLUS(INFIX, "\u228e"),
    SQSUBSET(INFIX, "\u228f"),
    SQSUPSET(INFIX, "\u2290"),
    SQSUBSETEQ(INFIX, "\u2291"),
    SQSUPSETEQ(INFIX, "\u2292"),
    SQCAP(INFIX, "\u2293"),
    SQCUP(INFIX, "\u2294"),
    OPLUS(INFIX, "\u2295"),
    OMINUS(INFIX, "\u2296"),
    OTIMES(INFIX, "\u2297"),
    OSLASH(INFIX, "\u2298"),
    ODOT(INFIX, "\u2299"),
    VDASH(INFIX, "\u22a2"),
    DASHV(INFIX, "\u22a3"),
    PERP(INFIX, "\u22a5"),
    MODELS(INFIX, "\u22a8"),
    NOT_VDASH(INFIX, "\u22ac"),
    NOT_MODELS(INFIX, "\u22ad"),
    TRIANGLELEFT(INFIX, "\u22b2"),
    TRIANGLERIGHT(INFIX, "\u22b3"),
    BIGWEDGE(PREFIX, "\u22c0", true),
    BIGVEE(PREFIX, "\u22c1", true),
    CDOT(INFIX, "\u22c5"),
    STAR(INFIX, "\u22c6"),
    BOWTIE(INFIX, "\u22c8"),
    NOT_SQSUBSETEQ(INFIX, "\u22e2"),
    NOT_SQSUPSETEQ(INFIX, "\u22e3"),
    BIGCAP(PREFIX, "\u22c2", true),
    BIGCUP(PREFIX, "\u22c3", true),
    
    SMILE(INFIX, "\u2323"),
    FROWN(INFIX, "\u2324"),
    
    BIGTRIANGLEUP(INFIX, "\u25b3"),
    BIGTRIANGLEDOWN(INFIX, "\u25bd"),
    DIAMOND(INFIX, "\u25c7"),
    BIGCIRC(INFIX, "\u25cb"),
    
    BIGODOT(PREFIX, "\u2a00", true),
    BIGOPLUS(PREFIX, "\u2a01", true),
    BIGOTIMES(PREFIX, "\u2a02", true),
    BIGUPLUS(PREFIX, "\u2a04", true),
    BIGSQCUP(PREFIX, "\u2a06", true),
    AMALG(INFIX, "\u2a3f"),    

    /* Placeholder for ^ and _. These are replaced by Commands during fixing */
    SUPER(INFIX, null),
    SUB(INFIX, null)

    ;

    // -------------------------------------------

    public static enum OperatorType {
        PREFIX,
        INFIX,
        POSTFIX;
    };

    private final OperatorType operatorType;
    private final String output;
    private final boolean limitsUnderOrOver;

    private MathMLOperator(final OperatorType operatorType, final String output) {
        this(operatorType, output, false);
    }
    
    private MathMLOperator(final OperatorType operatorType, final String output,
            final boolean limitsUnderOrOver) {
        this.operatorType = operatorType;
        this.output = output;
        this.limitsUnderOrOver = limitsUnderOrOver;
    }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public String getOutput() {
        return output;
    }

    public boolean isLimitsUnderOrOver() {
        return limitsUnderOrOver;
    }
    }
