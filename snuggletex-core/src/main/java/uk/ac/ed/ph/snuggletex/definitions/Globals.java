/* $Id:Globals.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.LR;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.MATH;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.PARAGRAPH;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.MathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathRelationInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketInterpretation.BracketType;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Lists various useful (but internal) constants and helper methods.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
public final class Globals {
    
    public static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    public static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";
    public static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
    public static final String MATHML_NAMESPACE = "http://www.w3.org/1998/Math/MathML";
    public static final String MATHML_PREF_NAMESPACE = "http://www.w3.org/2002/Math/preference";
    
    public static final String ERROR_MESSAGES_PROPERTIES_BASENAME = "uk/ac/ed/ph/snuggletex/error-messages";
    public static final String GENERAL_MESSAGES_PROPERTIES_BASENAME = "uk/ac/ed/ph/snuggletex/general-messages";
    public static final String CSS_PROPERTIES_NAME = "uk/ac/ed/ph/snuggletex/css.properties";
    
    /** Specifies the ClassPath location of the Stylesheet used by {@link XMLUtilities#serializeNodeChildren(org.w3c.dom.Node, boolean, boolean, uk.ac.ed.ph.snuggletex.utilities.StylesheetManager)} */
    public static final String EXTRACT_CHILD_NODES_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/extract-child-nodes.xsl";
    
    /** Specifies the ClassPath location of the XHTML -> HTML used for serializing as legacy HTML */
    public static final String XHTML_TO_HTML_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/xhtml-to-html.xsl";
    
    /** Specifies the ClassPath location of the MathML -> XHTML stylesheet to use for down-transforming */
    public static final String MATHML_TO_XHTML_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/mathml-to-xhtml.xsl";
    
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
    
    /** 
     * Literal Math characters, mapped to their resulting interpretation(s). This
     * has been flattened to make it easy to manage this code!
     */
    private static final Object[] mathCharacterData = new Object[] {
       '_', new MathOperatorInterpretation(MathMLOperator.SUB),
       '^', new MathOperatorInterpretation(MathMLOperator.SUPER),
       '+', new MathOperatorInterpretation(MathMLOperator.ADD),
       '-', new MathOperatorInterpretation(MathMLOperator.SUBTRACT),
       '=', new MathOperatorInterpretation(MathMLOperator.EQUALS), new MathRelationInterpretation(MathMLOperator.EQUALS, MathMLOperator.NOT_EQUALS),
       ',', new MathOperatorInterpretation(MathMLOperator.COMMA),
       '/', new MathOperatorInterpretation(MathMLOperator.SLASH),
       '*', new MathOperatorInterpretation(MathMLOperator.ASTERISK),
       '!', new MathOperatorInterpretation(MathMLOperator.FACTORIAL),
       '.', new MathOperatorInterpretation(MathMLOperator.DOT),
       '(', new MathOperatorInterpretation(MathMLOperator.OPEN_BRACKET), new MathBracketInterpretation(MathMLOperator.OPEN_BRACKET, MathMLOperator.CLOSE_BRACKET, BracketType.OPENER, true),
       ')', new MathOperatorInterpretation(MathMLOperator.CLOSE_BRACKET), new MathBracketInterpretation(MathMLOperator.CLOSE_BRACKET, MathMLOperator.OPEN_BRACKET, BracketType.CLOSER, true),
       '[', new MathOperatorInterpretation(MathMLOperator.OPEN_SQUARE_BRACKET), new MathBracketInterpretation(MathMLOperator.OPEN_SQUARE_BRACKET, MathMLOperator.CLOSE_SQUARE_BRACKET, BracketType.OPENER, true),
       ']', new MathOperatorInterpretation(MathMLOperator.CLOSE_SQUARE_BRACKET), new MathBracketInterpretation(MathMLOperator.CLOSE_SQUARE_BRACKET, MathMLOperator.OPEN_SQUARE_BRACKET, BracketType.CLOSER, true),
       '<', new MathOperatorInterpretation(MathMLOperator.LESS_THAN_OR_OPEN_ANGLE_BRACKET), new MathRelationInterpretation(MathMLOperator.LESS_THAN_OR_OPEN_ANGLE_BRACKET, MathMLOperator.NOT_LESS_THAN), new MathBracketInterpretation(MathMLOperator.LESS_THAN_OR_OPEN_ANGLE_BRACKET, MathMLOperator.GREATER_THAN_OR_CLOSE_ANGLE_BRACKET, BracketType.OPENER, false),
       '>', new MathOperatorInterpretation(MathMLOperator.GREATER_THAN_OR_CLOSE_ANGLE_BRACKET), new MathRelationInterpretation(MathMLOperator.GREATER_THAN_OR_CLOSE_ANGLE_BRACKET, MathMLOperator.NOT_GREATER_THAN), new MathBracketInterpretation(MathMLOperator.GREATER_THAN_OR_CLOSE_ANGLE_BRACKET, MathMLOperator.LESS_THAN_OR_OPEN_ANGLE_BRACKET, BracketType.OPENER, false),
       '|', new MathOperatorInterpretation(MathMLOperator.VERT_BRACKET), new MathBracketInterpretation(MathMLOperator.VERT_BRACKET, MathMLOperator.VERT_BRACKET, BracketType.OPENER_OR_CLOSER, false)
    };
    
    private static final Map<Character, EnumMap<InterpretationType, Interpretation>> mathCharacterMap;
    
    static {
        /* Initialises mathCharacterMap from the raw mathCharacterData */
        mathCharacterMap = new HashMap<Character, EnumMap<InterpretationType, Interpretation>>();
        Object object;
        Character currentCharacter = null;
        EnumMap<InterpretationType, Interpretation> currentMapBuilder = null;
        for (int i=0; i<mathCharacterData.length; i++) {
            object = mathCharacterData[i];
            if (object instanceof Character) {
                if (currentCharacter!=null) {
                    mathCharacterMap.put(currentCharacter, currentMapBuilder);
                }
                currentCharacter = (Character) object;
                currentMapBuilder = new EnumMap<InterpretationType, Interpretation>(InterpretationType.class);
            }
            else if (object instanceof MathInterpretation) {
                if (currentMapBuilder==null) {
                    throw new SnuggleLogicException("Expected a Character in data before this item " + object);
                }
                MathInterpretation currentInterpretation = (MathInterpretation) object;
                currentMapBuilder.put(currentInterpretation.getType(), currentInterpretation);
            }
            else {
                throw new SnuggleLogicException("Unexpected logic branch: got " + object);
            }
        }
        if (currentCharacter!=null) {
            mathCharacterMap.put(currentCharacter, currentMapBuilder);
        }
    }
    
    public static EnumMap<InterpretationType, Interpretation> getMathCharacterInterpretationMap(char c) {
        return mathCharacterMap.get(Character.valueOf(c));
    }

}
