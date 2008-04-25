/* $Id$
 *
 * Copyright (c) 2003 - 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.aardvark.commons.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by {@link ObjectDumper} to specify how types or properties
 * should be dumped.
 * <p>
 * It can be applied to both types and properties (i.e. <tt>getP()</tt> methods).
 *
 * @author  David McKain
 * @version $Revision$
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ObjectDumperOptions {
    
    DumpMode value() default DumpMode.TO_STRING;

}