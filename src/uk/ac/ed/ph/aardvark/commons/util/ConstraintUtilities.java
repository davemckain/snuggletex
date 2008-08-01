/* $Id$
 *
 * Copyright (c) 2003 - 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.aardvark.commons.util;

/**
 * Simple utility methods for enforcing various types of data constraints.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class ConstraintUtilities {

    public static void ensureNotNull(Object value) {
        ensureNotNull(value, "Object");
    }

    /**
     * Checks that the given object is non-null, throwing an
     * IllegalArgumentException if the check fails. If the check succeeds then
     * nothing happens.
     *
     * @param value object to test
     * @param objectName name to give to supplied Object when constructing Exception message.
     *
     * @throws IllegalArgumentException if an error occurs.
     */
    public static void ensureNotNull(Object value, String objectName) {
        if (value==null) {
            throw new IllegalArgumentException(objectName + " must not be null");
        }
    }
}
