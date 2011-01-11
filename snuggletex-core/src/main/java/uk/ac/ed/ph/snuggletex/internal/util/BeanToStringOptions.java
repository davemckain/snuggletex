/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Trivial annotation that can be applied to a <tt>getX()</tt> method to prevent its
 * details from being listed by {@link ObjectUtilities#beanToString(Object)}.
 * 
 * (This is copied from <tt>ph-commons-util</tt>.)
 *
 * @author  David McKain
 * @version $Revision$
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeanToStringOptions {
    
    PropertyOptions value() default PropertyOptions.SHOW_FULL;

}