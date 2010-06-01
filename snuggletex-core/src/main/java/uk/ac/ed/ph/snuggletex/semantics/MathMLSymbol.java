/* $Id:MathMLOperator.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

/**
 * Defines the resulting content of various MathML "symbols" (i.e. identifiers and operators).
 * 
 * FIXME: This is on its way out for 1.3.0, or at least partially.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
public interface MathMLSymbol {
    
    public static final String OPEN_BRACKET = "(";
    public static final String CLOSE_BRACKET = ")";
    public static final String OPEN_CURLY_BRACKET = "{";
    public static final String CLOSE_CURLY_BRACKET = "}";
    public static final String OPEN_SQUARE_BRACKET = "[";
    public static final String CLOSE_SQUARE_BRACKET = "]";
    public static final String DIVIDES = "|";
    public static final String VERT_BRACKET = "|";

    public static final String APPLY_FUNCTION = "\u2061";
    public static final String INVISIBLE_TIMES = "\u2062";
    
    public static final String EXISTS = "\u2203";
    public static final String EMPTYSET = "\u2205";
    public static final String NABLA = "\u2207";
    public static final String IN = "\u2208";
    public static final String NOT_IN = "\u2209";
    public static final String NI = "\u220b";
    public static final String NOT_NI = "\u220c";
    public static final String PROD = "\u220f";
    public static final String COPROD = "\u2210";
    public static final String SUM = "\u2211";
    public static final String MP = "\u2213";
    public static final String SETMINUS = "\u2216";
    public static final String AST = "\u2217";
    public static final String CIRC = "\u2218";
    public static final String PROPTO = "\u221d";
    public static final String INFTY = "\u221e";
    public static final String MID = "\u2223";
    public static final String NOT_MID = "\u2224";
    public static final String DOUBLE_VERT_BRACKET = "\u2225";
    public static final String PARALLEL = "\u2225";
    public static final String NOT_PARALLEL = "\u2226";
    public static final String WEDGE = "\u2227";
    public static final String VEE = "\u2228";
    public static final String CAP = "\u2229";
    public static final String CUP = "\u222a";
    public static final String SURD = "\u221a";
    public static final String ANGLE = "\u2220";
    public static final String INTEGRAL = "\u222b";
    public static final String OINT = "\u222e";
    public static final String TOP = "\u22a4";
    public static final String BOT = "\u22a5";
    public static final String SIM = "\u223c";
    public static final String WR = "\u2240";
    public static final String NOT_SIM = "\u2241";
    public static final String SIMEQ = "\u2243";
    public static final String NOT_SIMEQ = "\u2244";
    public static final String CONG = "\u2245";
    public static final String NOT_CONG = "\u2246";
    public static final String APPROX = "\u2248";
    public static final String NOT_APPROX = "\u2249";
    public static final String ASYMP = "\u224d";
    public static final String DOTEQ = "\u2250";
    public static final String NOT_EQUALS = "\u2260";
    public static final String EQUIV = "\u2261";
    public static final String NOT_EQUIV = "\u2262";
    public static final String LEQ = "\u2264";
    public static final String GEQ = "\u2265";
    public static final String LL = "\u226a";
    public static final String GG = "\u226b";
    public static final String NOT_LESS_THAN = "\u226e";
    public static final String NOT_GREATER_THAN = "\u226f";
    public static final String NOT_LEQ = "\u2270";
    public static final String NOT_GEQ = "\u2271";
    public static final String PREC = "\u227a";
    public static final String SUCC = "\u227b";
    public static final String PRECEQ = "\u227c";
    public static final String SUCCEQ = "\u227d";
    public static final String NOT_PREC = "\u2280";
    public static final String NOT_SUCC = "\u2281";
    public static final String SUBSET = "\u2282";
    public static final String SUPSET = "\u2283";
    public static final String NOT_SUBSET = "\u2284";
    public static final String NOT_SUPSET = "\u2285";
    public static final String SUBSETEQ = "\u2286";
    public static final String SUPSETEQ = "\u2287";
    public static final String NOT_SUBSETEQ = "\u2288";
    public static final String NOT_SUPSETEQ = "\u2289";
    public static final String UPLUS = "\u228e";
    public static final String SQSUBSET = "\u228f";
    public static final String SQSUPSET = "\u2290";
    public static final String SQSUBSETEQ = "\u2291";
    public static final String SQSUPSETEQ = "\u2292";
    public static final String SQCAP = "\u2293";
    public static final String SQCUP = "\u2294";
    public static final String OPLUS = "\u2295";
    public static final String OMINUS = "\u2296";
    public static final String OTIMES = "\u2297";
    public static final String OSLASH = "\u2298";
    public static final String ODOT = "\u2299";
    public static final String VDASH = "\u22a2";
    public static final String DASHV = "\u22a3";
    public static final String PERP = "\u22a5";
    public static final String MODELS = "\u22a8";
    public static final String NOT_VDASH = "\u22ac";
    public static final String NOT_MODELS = "\u22ad";
    public static final String TRIANGLELEFT = "\u22b2";
    public static final String TRIANGLERIGHT = "\u22b3";
    public static final String BIGWEDGE = "\u22c0";
    public static final String BIGVEE = "\u22c1";
    public static final String DIAMOND = "\u22c4";
    public static final String CDOT = "\u22c5";
    public static final String STAR = "\u22c6";
    public static final String BOWTIE = "\u22c8";
    public static final String NOT_SQSUBSETEQ = "\u22e2";
    public static final String NOT_SQSUPSETEQ = "\u22e3";
    public static final String BIGCAP = "\u22c2";
    public static final String BIGCUP = "\u22c3";
    
    public static final String OPEN_ANGLE_BRACKET = "\u2329";
    public static final String CLOSE_ANGLE_BRACKET = "\u232a";

    public static final String BIGTRIANGLEUP = "\u25b3";
    public static final String TRIANGLE = "\u25b5";
    public static final String BIGTRIANGLEDOWN = "\u25bd";
    public static final String BIGCIRC = "\u25cb";
    
    public static final String BIGODOT = "\u2a00";
    public static final String BIGOPLUS = "\u2a01";
    public static final String BIGOTIMES = "\u2a02";
    public static final String BIGUPLUS = "\u2a04";
    public static final String BIGSQCUP = "\u2a06";
    public static final String AMALG = "\u2a3f";
}
