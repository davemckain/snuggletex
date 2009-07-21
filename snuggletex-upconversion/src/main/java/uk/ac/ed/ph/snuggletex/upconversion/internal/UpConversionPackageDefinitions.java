/* $Id: GlobalBuiltins.java 457 2009-07-17 16:56:57Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion.internal;

import static uk.ac.ed.ph.snuggletex.definitions.Globals.TEXT_MODE_ONLY;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.LR;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.MATH;
import static uk.ac.ed.ph.snuggletex.definitions.TextFlowContext.IGNORE;

import uk.ac.ed.ph.snuggletex.SnugglePackage;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionErrorCode;

import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This defines the {@link SnugglePackage} providing up-conversion functionality.
 *
 * @author  David McKain
 * @version $Revision: 457 $
 */
public final class UpConversionPackageDefinitions {
    
    /** Location of {@link ResourceBundle} providing error messages for this bundle */
    public static final String ERROR_MESSAGES_PROPERTIES_BASENAME = "uk/ac/ed/ph/snuggletex/upconversion/error-messages";
    
    private static final SnugglePackage upConversionPackage;
    
    public static final SnugglePackage getPackage() {
        return upConversionPackage;
    }
    
    static {
        upConversionPackage = new SnugglePackage("UpConversion");
        
        /* Set up error messages for this package */
        upConversionPackage.getErrorCodes().addAll(Arrays.asList(UpConversionErrorCode.values()));
        try {
            upConversionPackage.setErrorMessageBundle(ResourceBundle.getBundle(ERROR_MESSAGES_PROPERTIES_BASENAME));
        }
        catch (MissingResourceException e) {
            throw new SnuggleRuntimeException(e);
        }
        
         
        /* Special commands for making up-conversion assumptions. */
        upConversionPackage.addComplexCommand("assume", false, 2, TEXT_MODE_ONLY, new LaTeXMode[] { LR, MATH }, new AssumeHandler(), IGNORE);
     }
}
