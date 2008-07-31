/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.LR;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.MATH;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.PARAGRAPH;

import uk.ac.ed.ph.snuggletex.semantics.MathBracketOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.SimpleMathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketOperatorInterpretation.BracketType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Lists various useful (but internal) constants and helper methods.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class Globals {
    
    public static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    public static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";
    public static final String MATHML_NAMESPACE = "http://www.w3.org/1998/Math/MathML";
    public static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
    
    public static final String ERROR_MESSAGES_PROPERTIES_BASENAME = "uk/ac/ed/ph/snuggletex/error-messages";
    public static final String GENERAL_MESSAGES_PROPERTIES_BASENAME = "uk/ac/ed/ph/snuggletex/general-messages";
    public static final String CSS_PROPERTIES_NAME = "uk/ac/ed/ph/snuggletex/css.properties";
    
    /** Specifies the ClassPath location of the Stylesheet used by buildXMLString() */
    public static final String XML_STRING_XSL_RESOURCE_NAME = "uk/ac/ed/ph/snuggletex/to-xml-string.xsl";
    
    /** Specifies the ClassPath location of the XHTML -> HTML used for serializing as legacy HTML */
    public static final String XHTML_TO_HTML_XSL_RESOURCE_NAME = "uk/ac/ed/ph/snuggletex/xhtml-to-html.xsl";
    
    /** Specifies the ClassPath location of the MathML -> XHTML stylesheet to use for down-transforming */
    public static final String MATHML_TO_XHTML_XSL_RESOURCE_NAME = "uk/ac/ed/ph/snuggletex/mathml-to-xhtml.xsl";
    
    /** URN used in MathML -> XHTML to call up XML containing current CSS Properties */
    public static final String CSS_PROPERTIES_DOCUMENT_URN = "urn:snuggletex-css-properties";
    
    public static final EnumSet<LaTeXMode> MATH_MODE_ONLY = EnumSet.of(MATH);
    public static final EnumSet<LaTeXMode> PARA_MODE_ONLY = EnumSet.of(PARAGRAPH);
    public static final EnumSet<LaTeXMode> TEXT_MODE_ONLY = EnumSet.of(PARAGRAPH, LR);
    
    /**
     * This represents all modes that LaTeX commands can be used in, so is currently a bit
     * of a misnomer as it excludes {@link LaTeXMode#VERBATIM}!
     * 
     * TODO: Think of a more sensible name for this!!
     */
    public static final EnumSet<LaTeXMode> ALL_MODES = EnumSet.of(PARAGRAPH, MATH, LR);
    
    /** Literal Math characters, mapped to their resulting interpretations */
    private static final Object[] mathCharacterData = new Object[] {
       '_', new SimpleMathOperatorInterpretation(MathMLOperator.SUB),
       '^', new SimpleMathOperatorInterpretation(MathMLOperator.SUPER),
       '+', new SimpleMathOperatorInterpretation(MathMLOperator.ADD),
       '-', new SimpleMathOperatorInterpretation(MathMLOperator.SUBTRACT),
       '=', new SimpleMathOperatorInterpretation(MathMLOperator.EQUALS),
       ',', new SimpleMathOperatorInterpretation(MathMLOperator.COMMA),
       '(', new MathBracketOperatorInterpretation(MathMLOperator.OPEN_BRACKET, MathMLOperator.CLOSE_BRACKET, BracketType.OPENER),
       ')', new MathBracketOperatorInterpretation(MathMLOperator.CLOSE_BRACKET, MathMLOperator.OPEN_BRACKET, BracketType.CLOSER),
       '[', new MathBracketOperatorInterpretation(MathMLOperator.OPEN_SQUARE_BRACKET, MathMLOperator.CLOSE_SQUARE_BRACKET, BracketType.OPENER),
       ']', new MathBracketOperatorInterpretation(MathMLOperator.CLOSE_SQUARE_BRACKET, MathMLOperator.OPEN_SQUARE_BRACKET, BracketType.CLOSER),
       '<', new MathBracketOperatorInterpretation(MathMLOperator.OPEN_ANGLE_BRACKET, MathMLOperator.CLOSE_ANGLE_BRACKET, BracketType.OPENER),
       '>', new MathBracketOperatorInterpretation(MathMLOperator.CLOSE_ANGLE_BRACKET, MathMLOperator.OPEN_ANGLE_BRACKET, BracketType.CLOSER),
       '|', new MathBracketOperatorInterpretation(MathMLOperator.VERT_BRACKET, MathMLOperator.VERT_BRACKET, BracketType.OPENER_OR_CLOSER),
    };
    
    private static final Map<Character, MathInterpretation> mathCharacterMap;
    
    static {
        mathCharacterMap = new HashMap<Character, MathInterpretation>();
        for (int i=0; i<mathCharacterData.length; ) {
            Character character = (Character) mathCharacterData[i++];
            MathInterpretation interprestation = (MathInterpretation) mathCharacterData[i++];
            mathCharacterMap.put(Character.valueOf(character), interprestation);
        }
    }
    
    public static MathInterpretation getMathCharacter(char c) {
        return mathCharacterMap.get(Character.valueOf(c));
    }

}
