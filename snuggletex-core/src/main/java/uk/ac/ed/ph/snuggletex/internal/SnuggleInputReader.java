/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.definitions.CoreErrorCode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that does the job of taking a {@link SnuggleInput}, checking its contents for
 * allowed Unicode characters, working out how to map absolute offsets into <tt>(line,column)</tt>
 * pairs and producing a {@link WorkingDocument} for later use.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class SnuggleInputReader implements WorkingDocument.SourceContext {
    
    private final SessionContext sessionContext;
    private final SnuggleInput input;
    
    private int[] newlineIndices = null;
    private int inputLength;
    private WorkingDocument resultingDocument = null;
    
    public SnuggleInputReader(SessionContext sessionContext, SnuggleInput input) {
        this.sessionContext = sessionContext;
        this.input = input;
    }
    
    public SnuggleInput getInput() {
        return input;
    }
    
    public SessionContext getSessionContext() {
        return sessionContext;
    }
    
    //---------------------------------------------
    // Public interface

    public WorkingDocument createWorkingDocument() throws IOException, SnuggleParseException {
        /* First of all, we read in the input document, returning a StringBuilder */
        StringBuilder inputData = readInputData();
        this.inputLength = inputData.length();
        
        /* Go through data, calculating indices of newlines */
        this.newlineIndices = calculateNewlineIndicesAndCheckCodePoints(inputData);
        
        /* Then create a WorkingDocument that can be passed to the LaTeX tokeniser for messing with */
        this.resultingDocument = new WorkingDocument(inputData, this);
        return resultingDocument;
    }
    
    public int[] getLineAndColumn(int index) {
        if (newlineIndices==null) {
            throw new IllegalStateException("Input has not yet been read");
        }
        if (index<0 || index>inputLength) {
            throw new IndexOutOfBoundsException();
        }
        int line, column;
        for (line=0; line<newlineIndices.length && newlineIndices[line]<index; line++)
            ;
        column = index - newlineIndices[line-1];
        return new int[] { line, column };
    }

    //---------------------------------------------
    
    private StringBuilder readInputData() throws IOException {
        switch (input.getType()) {
            case STRING:
                return new StringBuilder(input.getString());

            case FILE:
                return readCharacterStream(createReader(new FileInputStream(input.getFile()), input.getEncoding()));

            case INPUT_STREAM:
                return readCharacterStream(createReader(input.getInputStream(), input.getEncoding()));

            case READER:
                return readCharacterStream(input.getReader());

            default:
                throw new SnuggleLogicException("Unexpected switch case: " + input.getType());
        }
    }
    
    private Reader createReader(InputStream inputStream, String encoding) throws UnsupportedEncodingException {
        return encoding!=null ? new InputStreamReader(inputStream, encoding) : new InputStreamReader(inputStream);
    }
    
    private StringBuilder readCharacterStream(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        int size = 0;
        StringBuilder result = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            size += line.length() + 1;
            result.append(line).append("\n");
        }
        bufferedReader.close();
        return result;
    }

    private int[] calculateNewlineIndicesAndCheckCodePoints(StringBuilder inputData) throws SnuggleParseException {
        List<Integer> newlineIndicesBuilder = new ArrayList<Integer>();
        newlineIndicesBuilder.add(Integer.valueOf(-1));
        char lastChar = 0;
        char thisChar; /* (16 bit char only) */
        int codePoint; /* (Full Unicode code point */
        for (int i=0, length=inputData.length(); i<length; i++, lastChar=thisChar) {
            thisChar = inputData.charAt(i);
            if (thisChar=='\n') {
                newlineIndicesBuilder.add(Integer.valueOf(i));
            }
            if (Character.isHighSurrogate(lastChar)) {
                if (Character.isLowSurrogate(thisChar)) {
                    codePoint = Character.toCodePoint(lastChar, thisChar);
                }
                else {
                    /* Error: last was bad surrogate character */
                    InputError error = new InputError(CoreErrorCode.TTEG05, null,
                            Integer.toHexString(lastChar),
                            Integer.valueOf(i-1));
                    sessionContext.registerError(error);
                    inputData.setCharAt(i-1, ' ');
                    continue;
                }
            }
            else if (Character.isLowSurrogate(thisChar)) {
                /* Error: this is bad surrogate character */
                InputError error = new InputError(CoreErrorCode.TTEG05, null,
                        Integer.toHexString(thisChar),
                        Integer.valueOf(i));
                sessionContext.registerError(error);
                inputData.setCharAt(i, ' ');
                continue;
            }
            else {
                codePoint = thisChar;
            }
            /* Check that we allow this codepoint */
            if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                InputError error = new InputError(CoreErrorCode.TTEG02, null,
                        Integer.toHexString(codePoint),
                        Integer.valueOf(i));
                sessionContext.registerError(error);
                inputData.setCharAt(i, ' ');
            }
        }
        /* Make sure last character wasn't surrogate pair starter */
        if (Character.isHighSurrogate(lastChar)) {
            InputError error = new InputError(CoreErrorCode.TTEG05, null,
                    Integer.toHexString(lastChar),
                    Integer.valueOf(inputData.length()-1));
            sessionContext.registerError(error);
            inputData.setCharAt(inputData.length()-1, ' ');
        }
        
        /* Finally store newline information */
        int[] calculatedNewlineIndices = new int[newlineIndicesBuilder.size()];
        for (int i = 0; i < calculatedNewlineIndices.length; i++) {
            calculatedNewlineIndices[i] = newlineIndicesBuilder.get(i);
        }
        return calculatedNewlineIndices;
    }
}
