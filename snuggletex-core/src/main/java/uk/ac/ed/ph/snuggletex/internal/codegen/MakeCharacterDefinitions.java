/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal.codegen;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MakeCharacterDefinitions {
    
    private static final String CHARACTER_DATA_FILE = "snuggletex-core/src/main/resources/uk/ac/ed/ph/snuggletex/all-math-characters.txt";
    
    public static void main(String[] args) throws Exception {
        String fileName = "snuggletex-core/unicode-math-table.tex";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "US-ASCII"));
        BufferedWriter characterFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CHARACTER_DATA_FILE), "US-ASCII"));
        characterFileWriter.write("# This is generated from " + fileName + " - do not edit!\n");
        
        String line;
        Pattern linePattern = Pattern.compile("\\\\UnicodeMathSymbol\\{\"(.{5})\\}\\{\\\\(\\w+)\\s*\\}\\{\\\\(\\w+)\\}");
        Map<String, Integer> typeFrequencyMap = new HashMap<String, Integer>();
        int count = 0;
        while ((line = reader.readLine())!=null) {
            count++;
            Matcher matcher = linePattern.matcher(line);
            if (matcher.find()) {
                String codePointHex = matcher.group(1);
                String commandName = matcher.group(2);
                String type = matcher.group(3);
                
                Integer frequency = typeFrequencyMap.get(type);
                if (frequency==null) {
                    frequency = Integer.valueOf(1);
                }
                else {
                    frequency = Integer.valueOf(frequency.intValue() + 1);
                }
                typeFrequencyMap.put(type, frequency);
                
                if (type.equals("mathalpha") || type.equals("mathord")) {
                    /* These would be treated as identifiers */
                }
                else if (type.equals("mathop") || type.equals("mathpunct") || type.equals("mathrel") || type.equals("mathbin")) {
                    /* These would be operators */
                }
                else if (type.equals("mathaccent")) {
                    /* Accents need special handling */
                }
                else if (type.equals("mathopen") || type.equals("mathclose") || type.equals("mathfence")) {
                    /* Open/close operators */
                }
                
                /* Write character definition */
                characterFileWriter.write(codePointHex);
                characterFileWriter.write(':');
                characterFileWriter.write(commandName);
                characterFileWriter.write(':');
                characterFileWriter.write(type.substring(4).toUpperCase());
                characterFileWriter.write('\n');
            }
            else {
                throw new SnuggleLogicException("Could not parse line " + line);
            }
        }
        reader.close();
        characterFileWriter.close();
        System.out.println("Types were " + typeFrequencyMap);
        System.out.println("Number of chars is " + count);
    }

}
