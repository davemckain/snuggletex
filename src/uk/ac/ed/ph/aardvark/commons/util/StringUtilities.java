/* $Id: StringUtilities.java,v 1.1 2008/01/14 10:54:06 dmckain Exp $
 *
 * Copyright (c) 2003 - 2007 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.aardvark.commons.util;

import java.util.Iterator;

/**
 * (Cut-down version of Aardvark's StringUtilities class.)
 *
 * @author  David McKain
 * @version $Revision: 1.1 $
 */
public final class StringUtilities {
    
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Joins the given collection of Objects using the given
     * separator and each Object's normal toString() method.
     * <p>
     * For example, joining the collection "a", "b", "c" with "/"
     * gives "a/b/c".
     *
     * @param objects collection of Objects to join
     * @param separator separator to use
     * @return objects joined using the given separator.
     */
    public static String join(Iterable<? extends Object> objects, CharSequence separator) {
        StringBuilder result = new StringBuilder();
        for (Iterator<? extends Object> iter = objects.iterator(); iter.hasNext(); ) {
            result.append(iter.next().toString());
            if (iter.hasNext()) {
                result.append(separator);
            }
        }
        return result.toString();
    }

    /**
     * Same as {@link #join(Iterable, CharSequence)} but simply takes an array
     * of Objects.
     *
     * @param objects array of Objects to join
     * @param separator separator to use
     */
    public static String join(Object[] objects, CharSequence separator) {
        return join(objects, separator, 0, objects.length);
    }
    
    /**
     * Version of {@link #join(Object[], CharSequence)} that allows you to pass in
     * a {@link StringBuilder} that the result will be built up in. This is useful if you need
     * to do add in other stuff later on.
     *
     * @param resultBuilder StringBuilder to append results to
     * @param objects array of Objects to join
     * @param separator separator to use
     */
    public static void join(StringBuilder resultBuilder, Object[] objects, CharSequence separator) {
        join(resultBuilder, objects, separator, 0, objects.length);
    }
    
    /**
     * Version of {@link #join(Object[], CharSequence)} that allows you to specify a range of
     * indices in the array to join. This can be useful in some cases.
     *
     * @param objects array of Objects to join
     * @param separator separator to use
     * @param startIndex first index to join
     * @param endIndex index after last one to join
     */
    public static String join(Object[] objects, CharSequence separator, int startIndex, int endIndex) {
        StringBuilder result = new StringBuilder();
        join(result, objects, separator, startIndex, endIndex);
        return result.toString();
    }
    
    /**
     * Version of {@link #join(Object[], CharSequence, int, int)} that allows you to pass in
     * a {@link StringBuilder} that the result will be built up in. This is useful if you need
     * to do add in other stuff later on.
     *
     * @param resultBuilder StringBuilder to append results to
     * @param objects array of Objects to join
     * @param separator separator to use
     * @param startIndex first index to join
     * @param endIndex index after last one to join
     */
    public static void join(StringBuilder resultBuilder, Object[] objects, CharSequence separator, int startIndex, int endIndex) {
        boolean hasDoneFirst = false;
        for (int i=startIndex; i<endIndex; i++) {
            if (hasDoneFirst) {
                resultBuilder.append(separator);
            }
            resultBuilder.append(objects[i].toString());
            hasDoneFirst = true;
        }
    }
    
    //------------------------------------------------------------------------

    /**
     * Trivial helper method to convert a boolean into either
     * "yes" or "no" depending on its state.
     *
     * @param state boolean to convert
     * @return "yes" if true, "no" if false.
     */
    public static String toYesNo(boolean state) {
        return state ? "yes" : "no";
    }
    
    /**
     * Trivial helper method to convert a boolean into either
     * "true" or "false" depending on its state.
     *
     * @param state boolean to convert
     * @return "true" if true, "false" if false.
     */
    public static String toTrueFalse(boolean state) {
        return state ? "true" : "false";
    }
}
