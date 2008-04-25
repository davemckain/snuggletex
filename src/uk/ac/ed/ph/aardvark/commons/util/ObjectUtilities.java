/* $Id$
 *
 * Copyright (c) 2003 - 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.aardvark.commons.util;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Some random "macros" for doing common Object-based tasks.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class ObjectUtilities {
    
    /** Shared instance of an empty array of Objects, which is sometimes useful! */
    public final static Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * Convenience toString() method that can be applied safely to a null
     * Object, yielding null.
     *
     * @param object
     */
    public static String safeToString(Object object) {
        return object!=null ? object.toString() : null;
    }

    /**
     * Checks equality of two Objects, allowing the case where o1==o2==null
     * to return true. The equals() method on o2 should be compatible with
     * equality.
     *
     * @param o1
     * @param o2
     * @return true if either o1==o2 or (o1!=null and o1.equals(o2))
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        return (o1==o2) || (o1!=null && o1.equals(o2));
    }

    /**
     * Utility method to create a simple String representation of a JavaBean Object by
     * showing all properties <code>p</code> which can be called by <code>getP()</code>
     * on the Object.
     *
     * @param bean
     * @return String representation of the form <code>className(p1=value,p2=value,...)</code>
     */
    public static String beanToString(Object bean) {
        Class<?> beanClass = bean.getClass();
        
        /* Output bean's hashCode and start of property list */
        StringBuilder result = new StringBuilder(beanClass.getName())
            .append("@")
            .append(Integer.toHexString(bean.hashCode()))
            .append("(");
        
        /* Now show each property by calling relevant methods */
        Method [] methods = beanClass.getMethods();
        boolean outputMade = false;
        String methodName, rawPropertyName;
        for (Method method : methods) {
            /* See if we've got a getter method. If so, extract property name (with first char still
             * capitalised) */
            methodName = method.getName();
            if (methodName.startsWith("get") && method.getParameterTypes().length==0
                    && !methodName.equals("getClass") && methodName.length() > "get".length()) {
                rawPropertyName = methodName.substring("get".length());
            }
            else if (methodName.startsWith("is") && method.getParameterTypes().length==0
                    && methodName.length() > "is".length()) {
                rawPropertyName = methodName.substring("is".length());
            }
            else {
                continue;
            }
            /* See if there is an annotation controlling how the property should be displayed */
            BeanToStringOptions beanAnnotation = method.getAnnotation(BeanToStringOptions.class);
            PropertyOptions propertyOption = beanAnnotation!=null ? beanAnnotation.value() : null;
            if (propertyOption!=null && propertyOption==PropertyOptions.IGNORE_PROPERTY) {
                /* Ignore this property */
                continue;
            }
            /* It's a getP() or isP() method */
            if (outputMade) {
                result.append(",");
            }
            else {
                outputMade = true;
            }
            /* Make first character after 'get' or 'is' lower case so that it really
             * is the name of the property */
            result.append(Character.toLowerCase(rawPropertyName.charAt(0)));
            if (methodName.length()>1) {
                result.append(rawPropertyName.substring(1));
            }
            /* Now do the equals sign */
            result.append("=");
            /* Now get the value of the property */
            Object value = null;
            try {
                value = method.invoke(bean);
            }
            catch (Exception e) {
                result.append("[Caused Exception ").append(e).append("]");
                continue;
            }
            /* Now print something */
            if (propertyOption!=null && propertyOption==PropertyOptions.HIDE_VALUE) {
                /* Value is to be hidden */
                result.append(value!=null ? "[hidden]" : value);
            }
            else {
                if (value instanceof Object[]) {
                    /* Returned an array - in this case Arrays.toString() does something nice */
                    result.append(Arrays.toString((Object[]) value));
                }
                else {
                    /* Not an array, so handle as normal */
                    result.append(value);
                }
            }
        }
        result.append(")");
        return result.toString();
    }
}
