/* $Id:ErrorCode.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.SnugglePackage;
import uk.ac.ed.ph.snuggletex.upconversion.internal.UpConversionPackageDefinitions;

/**
 * Enumerates the various types of client-induced errors that can arise when using the SnuggleTeX
 * Up-Conversion process.
 * 
 * @since 1.2.0
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
public enum UpConversionErrorCode implements ErrorCode {
    
    /* ================ Failures in the Up-conversion Process, via extension (U) =============== */
    
    /* Presentation to Content MathML Failures */
    UCFG00(),
    UCFG01(),
    UCFG02(),
    UCFG03(),
    UCFOP0(),
    UCFOP1(),
    UCFOP2(),
    UCFOP3(),
    UCFOP4(),
    UCFOP5(),
    UCFFN0(),
    UCFFN1(),
    UCFFN2(),
    UCFFX0(),
    UCFFX1(),
    UCFFX2(),
    
    /* Content MathML to Maxima Failures */
    UMFG00(),
    UMFG01(),
    UMFG02(),
    UMFG03(),
    UMFG04(),
    UMFFX0(),
    UMFOP0(),
    
    /* Options and Assumption errors */
    UAEOP0(),
    UAEOP1(),
    UAEOP2(),
    UAESY0(),
    UAESY1(),
    UAESY2(),
    
    ;
    
    public String getName() {
        return name();
    }

    public SnugglePackage getPackage() {
        return UpConversionPackageDefinitions.getPackage();
    }
}
