/* $Id: TestFileHelper.java,v 1.2 2008/04/03 09:46:36 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.aardvark.commons.util.IOUtilities;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision: 1.2 $
 */
public final class TestFileHelper {

    /**
     * Reads in the given "single line" test file, assuming it is of the format
     * <pre>
     * single line input
     * 1 or more lines of output
     * ==== (divider token, at least 4 characters)
     * ...
     * </pre>
     * 
     * Returns a List of [input,output] pairs.
     * 
     * @throws Exception
     */
    public static Collection<String[]> readAndParseSingleLineInputTestResource(String resourceName) throws Exception {
        InputStream resourceStream = TestFileHelper.class.getClassLoader().getResourceAsStream(resourceName);
        String testData = IOUtilities.readUnicodeStream(resourceStream);
        testData = testData.replaceAll("(?m)^#.*$(\\s+)(^|$)", "");
        String[] testItems = testData.split("(?m)\\s*^={4,}\\s*");
        Collection<String[]> result = new ArrayList<String[]>(testItems.length);
        for (String testItem : testItems) {
            result.add(testItem.split("\n+", 2));
        }
        return result;
    }
    
    /**
     * Reads in the given "multiple line" test file, assuming it is of the format
     * <pre>
     * 1 or more lines of output input
     * ---- (divider token, at least 4 characters)
     * 1 or more lines of output
     * ==== (divider token, at least 4 characters)
     * ...
     * </pre>
     * 
     * Returns a List of [input,output] pairs.
     * 
     * @throws Exception
     */
    public static Collection<String[]> readAndParseMultiLineInputTestResource(String resourceName) throws Exception {
        InputStream resourceStream = TestFileHelper.class.getClassLoader().getResourceAsStream(resourceName);
        String testData = IOUtilities.readUnicodeStream(resourceStream);
        testData = testData.replaceAll("(?m)^#.*$(\\s+)(^|$)", "");
        String[] testItems = testData.split("(?m)\\s*^={4,}\\s*");
        Collection<String[]> result = new ArrayList<String[]>(testItems.length);
        for (String testItem : testItems) {
            result.add(testItem.split("(?m)\\s*-{4,}\\s*", 2));
        }
        return result;
    }
}
