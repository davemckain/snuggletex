/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal.util;

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
 * This is used during the build process to create the SnuggleTeX <tt>all-math-characters.txt</tt>
 * from the <tt>unicode-math-table.tex</tt> from the <tt>unicode-math</tt> LaTeX package.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MakeCharacterDefinitions {
    
    public static void main(String[] args) throws Exception {
        if (args.length!=2) {
            throw new IllegalArgumentException("The unicode-math-table.tex input and all-math-characters.txt output text files must both be provided");
        }
        String inputFile = args[0];
        String outputFile = args[1];
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "US-ASCII"));
        BufferedWriter characterFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "US-ASCII"));
        characterFileWriter.write("# This was generated from " + inputFile + " during the SnuggleTeX build - do not edit!\n");
        
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
                throw new RuntimeException("Could not parse line " + line);
            }
        }
        reader.close();
        characterFileWriter.close();
        System.out.println("Types were " + typeFrequencyMap);
        System.out.println("Number of chars is " + count);
    }

}
