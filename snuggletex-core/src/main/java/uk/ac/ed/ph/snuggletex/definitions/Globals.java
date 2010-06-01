/* $Id:Globals.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.LR;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.MATH;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.PARAGRAPH;

import uk.ac.ed.ph.snuggletex.SerializationSpecifier;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;

import java.util.EnumSet;

import javax.xml.XMLConstants;

/**
 * Lists various useful (but internal) constants and helper methods.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
public final class Globals {
    
    /**
     * @deprecated Use {@link XMLConstants#XML_NS_URI} instead
     */
    @Deprecated
    public static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    
    /**
     * @deprecated Use {@link XMLConstants#XMLNS_ATTRIBUTE_NS_URI} instead
     */
    @Deprecated
    public static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";
    
    /**
     * @deprecated Use {@link W3CConstants#XHTML_NAMESPACE} instead
     */
    @Deprecated
    public static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
    
    /**
     * @deprecated Use {@link W3CConstants#MATHML_NAMESPACE} instead
     */
    @Deprecated
    public static final String MATHML_NAMESPACE = "http://www.w3.org/1998/Math/MathML";
    
    /**
     * @deprecated Use {@link W3CConstants#MATHML_PREF_NAMESPACE} instead
     */
    @Deprecated
    public static final String MATHML_PREF_NAMESPACE = "http://www.w3.org/2002/Math/preference";
    
    public static final String GENERAL_MESSAGES_PROPERTIES_BASENAME = "uk/ac/ed/ph/snuggletex/general-messages";
    public static final String CSS_PROPERTIES_NAME = "uk/ac/ed/ph/snuggletex/css.properties";
    
    public static final String CORE_MATH_CHARACTER_DEFS_RESOURCE_NAME = "uk/ac/ed/ph/snuggletex/core-math-characters.txt";
    public static final String ALL_MATH_CHARACTER_DEFS_RESOURCE_NAME = "uk/ac/ed/ph/snuggletex/all-math-characters.txt";
    
    /** Specifies the ClassPath location of the Stylesheet used by {@link XMLUtilities#serializeNodeChildren(uk.ac.ed.ph.snuggletex.utilities.StylesheetManager, org.w3c.dom.Node, SerializationSpecifier)} */
    public static final String EXTRACT_CHILD_NODES_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/extract-child-nodes.xsl";
    
    /** Specifies the location of the XSLT that converts MathML symbol characters to named entities */
    public static final String MATHML_ENTITIES_MAP_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/mathml-entities-map.xsl";
    
    /** Specifies the location of the XSLT that serializes and converts MathML symbol characters to named entities */
    public static final String SERIALIZE_WITH_NAMED_ENTITIES_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/serialize-with-named-entities.xsl";
    
    /** Specifies the ClassPath location of the XHTML -> HTML used for serializing as legacy HTML */
    public static final String XHTML_TO_HTML_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/xhtml-to-html.xsl";
    
    /** Specifies the ClassPath location of the MathML -> XHTML stylesheet to use for down-transforming */
    public static final String MATHML_TO_XHTML_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/mathml-to-xhtml.xsl";
    
    /** URN used in MathML -> XHTML to call up XML containing current CSS Properties */
    public static final String CSS_PROPERTIES_DOCUMENT_URN = "urn:snuggletex-css-properties";

    /** Placeholder operator representing ^. This is replaced by an appropriate Command during token fixing */
    public static final String SUP_PLACEHOLDER = "^";
    
    /** Placeholder operator representing ^. This is replaced by an appropriate Command during token fixing */
    public static final String SUB_PLACEHOLDER = "_";
    
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
}
