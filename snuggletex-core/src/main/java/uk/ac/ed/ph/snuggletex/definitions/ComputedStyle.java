/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import uk.ac.ed.ph.snuggletex.internal.util.DumpMode;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectDumperOptions;
import uk.ac.ed.ph.snuggletex.tokens.Token;

/**
 * This represents the computed or runtime style of a {@link Token}.
 *
 * @author  David McKain
 * @version $Revision$
 */
@ObjectDumperOptions(DumpMode.TO_STRING)
public final class ComputedStyle {
    
    /** Default style */
    public static final ComputedStyle DEFAULT_STYLE = new ComputedStyle(null, FontFamily.NORMAL, FontSize.NORMALSIZE);
    
    /** TeX "Font Family" */
    public static enum FontFamily {
        NORMAL("div", "rm", "span", "rm", "normal"),
        BF("div", "bf", "b", null, "bold"),
        RM("div", "rm", "span", "rm", "normal"),
        EM("div", "em", "em", null, "italic"),
        IT("div", "it", "i", null, "italic"),
        TT("div", "tt", "tt", null, "monospace"),
        SC("div", "sc", "span", "sc", null),
        SL("div", "sl", "span", "sl", null),
        SF("div", "sf", "span", "sf", "sans-serif"),
        ;
        
        /** Name of resulting XHTML block element name */
        private final String targetBlockXHTMLElementName;
        
        /** Name of resulting CSS class for XHTML block elements */
        private final String targetBlockCSSClassName;
        
        /** Name of resulting XHTML inline element name */
        private final String targetInlineXHTMLElementName;
        
        /** Name of resulting CSS class for XHTML inline elements */
        private final String targetInlineCSSClassName;
        
        /** 
         * Name of 'variant' attribute in resulting MathML <mstyle/> element, if supported, or null
         * if this style cannot be used in Math mode.
         */
        private final String targetMathMLMathVariantName;
        
        private FontFamily(final String targetBlockXHTMLElementName,
                final String targetBlockCSSClassName, final String targetInlineXHTMLElementName,
                final String targetInlineCSSClassName, final String targetMathMLMathVariantName) {
            this.targetBlockXHTMLElementName = targetBlockXHTMLElementName;
            this.targetBlockCSSClassName = targetBlockCSSClassName;
            this.targetInlineXHTMLElementName = targetInlineXHTMLElementName;
            this.targetInlineCSSClassName = targetInlineCSSClassName;
            this.targetMathMLMathVariantName = targetMathMLMathVariantName;
        }
        
        public String getTargetBlockXHTMLElementName() {
            return targetBlockXHTMLElementName;
        }
        
        public String getTargetBlockCSSClassName() {
            return targetBlockCSSClassName;
        }
        
        public String getTargetInlineXHTMLElementName() {
            return targetInlineXHTMLElementName;
        }

        public String getTargetInlineCSSClassName() {
            return targetInlineCSSClassName;
        }
        
        public String getTargetMathMLMathVariantName() {
            return targetMathMLMathVariantName;
        }
    }
    
    /** TeX Font Size */
    public static enum FontSize {
        
        TINY("tiny"),
        SCRIPTSIZE("scriptsize"),
        FOOTNOTESIZE("footnotesize"),
        SMALL("small"),
        NORMALSIZE("normalsize"),
        LARGE("large"),
        LARGE_2("large2"),
        LARGE_3("large3"),
        HUGE("huge"),
        HUGE_2("huge2"),
        ;
        
        private final String targetCSSClassName;
        
        private FontSize(final String targetCSSClassName) {
            this.targetCSSClassName = targetCSSClassName;
        }
        
        public String getTargetCSSClassName() {
            return targetCSSClassName;
        }
    }
    
    private final ComputedStyle parentStyle;
    private final FontFamily fontFamily;
    private final FontSize fontSize;
    
    public ComputedStyle(ComputedStyle parentStyle, FontFamily fontFamily, FontSize fontSize) {
        this.parentStyle = parentStyle;
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
    }

    public ComputedStyle getParentStyle() {
        return parentStyle;
    }
    
    public FontFamily getFontFamily() {
        return fontFamily;
    }
    
    public FontSize getFontSize() {
        return fontSize;
    }
    
    
    public boolean isEquivalentTo(ComputedStyle other) {
        return fontFamily==other.fontFamily && fontSize==other.fontSize;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName()
            + "@" + Integer.toString(hashCode(), 16)
            + "(fontFamily=" + fontFamily
            + ",fontSize=" + fontSize
            + ",parentStyle=" + (parentStyle!=null ? Integer.toString(parentStyle.hashCode(), 16) : null)
            + ")";
    }
}
