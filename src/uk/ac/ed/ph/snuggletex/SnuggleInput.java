/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.aardvark.commons.util.ObjectUtilities;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

/**
 * Simple container for an input into SnuggleTeX.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class SnuggleInput {

    /** Enumerates the various types of inputs we support. */
    public static enum InputType {
        
        /** Input is read from a String */
        STRING,
        
        /** Input is read from a File using the platform default encoding */
        FILE,
        
        /** Input is read from an {@link InputStream} using the platform default encoding */
        INPUT_STREAM,
        
        /** Input is read from a {@link Reader} */
        READER;
    }
    
    private final InputType type;
    private final String string;
    private final File file;
    private final InputStream inputStream;
    private final Reader reader;
    
    public SnuggleInput(final String string) {
        this(InputType.STRING, string, null, null, null);
    }
    
    public SnuggleInput(final File file) {
        this(InputType.FILE, null, file, null, null);
    }
    
    public SnuggleInput(final InputStream inputStream) {
        this(InputType.INPUT_STREAM, null, null, inputStream, null);
    }
    
    public SnuggleInput(final Reader reader) {
        this(InputType.READER, null, null, null, reader);
    }
    
    private SnuggleInput(final InputType type, final String string, final File file,
            final InputStream inputStream, final Reader reader) {
        this.type = type;
        this.string = string;
        this.file = file;
        this.inputStream = inputStream;
        this.reader = reader;
    }
    
    public InputType getType() {
        return type;
    }

    public String getString() {
        return string;
    }
    
    public File getFile() {
        return file;
    }
    
    public InputStream getInputStream() {
        return inputStream;
    }
    
    public Reader getReader() {
        return reader;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
