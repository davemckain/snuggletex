/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;

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
        
        /** Input is read from a File using either a specified encoding or the platform default encoding */
        FILE,
        
        /** Input is read from an {@link InputStream} using either a specified encoding or the platform default encoding */
        INPUT_STREAM,
        
        /** Input is read from a {@link Reader} */
        READER;
    }
    
    /** The "type" of input encapsulated by an instance of this class. */
    private final InputType type;
    
    /** 
     * An "identifier" for this input. This is used when formulating error messages. Clients
     * can use as they require - e.g. as a kind of System ID, URL or File name.
     * <p>
     * This must not be null.
     */
    private String identifier;
    
    /**
     * An (optional) URI for this input. If provided, this can be used to help resolve links
     * to other Resources.
     */
    private URI uri;
    
    /**
     * The encoding for this input, used for {@link InputType#FILE} and {@link InputType#INPUT_STREAM}.
     * If null, we use the platform default encoding.
     */
    private String encoding;
    
    private final String string;
    private final File file;
    private final InputStream inputStream;
    private final Reader reader;
    
    private String stringRepresentation;
    
    public SnuggleInput(final String string) {
        this(string, "\"" + string + "\"");
    }
    
    public SnuggleInput(final String string, final String identifier) {
        this(InputType.STRING, string, null, null, null, identifier, null);
    }
    
    public SnuggleInput(final File file) {
        this(file, file.getPath());
    }
    
    public SnuggleInput(final File file, final String identifier) {
        this(InputType.FILE, null, file, null, null, identifier, null);
    }
    
    public SnuggleInput(final File file, final String identifier, String encoding) {
        this(InputType.FILE, null, file, null, null, identifier, encoding);
    }
    
    public SnuggleInput(final InputStream inputStream) {
        this(inputStream, "[Stream input @" + inputStream.hashCode() + "]");
    }
    
    public SnuggleInput(final InputStream inputStream, final String identifier) {
        this(InputType.INPUT_STREAM, null, null, inputStream, null, identifier, null);
    }
    
    public SnuggleInput(final InputStream inputStream, final String identifier, final String encoding) {
        this(InputType.INPUT_STREAM, null, null, inputStream, null, identifier, encoding);
    }
    
    public SnuggleInput(final Reader reader) {
        this(reader, "[Reader input @" + reader.hashCode() + "]");
    }
    
    public SnuggleInput(final Reader reader, final String identifier) {
        this(InputType.READER, null, null, null, reader, identifier, null);
    }
    
    private SnuggleInput(final InputType type, final String string, final File file,
            final InputStream inputStream, final Reader reader,
            final String identifier, final String encoding) {
        ConstraintUtilities.ensureNotNull(identifier, "identifier");
        switch (type) {
            case STRING:
                ConstraintUtilities.ensureNotNull(string, "string");
                break;
                
            case FILE:
                ConstraintUtilities.ensureNotNull(file, "file");
                break;
                
            case INPUT_STREAM:
                ConstraintUtilities.ensureNotNull(inputStream, "inputStream");
                break;
                
            case READER:
                ConstraintUtilities.ensureNotNull(reader, "reader");
                break;
                
            default:
                throw new SnuggleLogicException("Unexpected switch case " + type);
        }
        this.type = type;
        this.string = string;
        this.file = file;
        this.inputStream = inputStream;
        this.reader = reader;
        this.identifier = identifier;
        this.encoding = encoding;
    }
    
    /** 
     * Returns the "type" of input, represented by an instance of the {@link InputType} enumeration.
     */
    public InputType getType() {
        return type;
    }
    
    
    /** 
     * Returns the "identifier" for this input. This is used when formulating error messages.
     * You can use as you require - e.g. as a kind of System ID, URL or File name.
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /** 
     * Sets the "identifier" for this input. This is used when formulating error messages.
     * You can use as you require - e.g. as a kind of System ID, URL or File name.
     * 
     * @param identifier new identifier, which may be null.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    
    /**
     * Returns an (optional) URI for this input. If provided, this can be used to help resolve links
     * to other {@link SnuggleInput}s.
     */
    public URI getURI() {
        return uri;
    }
    
    /**
     * Sets a URI for this input. If provided, this can be used to help resolve links
     * to other {@link SnuggleInput}s.
     * 
     * @param uri URI for this input, which may be null.
     */
    public void setURI(URI uri) {
        this.uri = uri;
    }
    
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns the String that provided this input if type is {@link InputType#STRING}, null
     * otherwise.
     */
    public String getString() {
        return string;
    }
    
    /**
     * Returns the {@link File} that provided this input if type is {@link InputType#FILE}, null
     * otherwise.
     */
    public File getFile() {
        return file;
    }
    
    /**
     * Returns the {@link InputStream} that provided this input if type is {@link InputType#INPUT_STREAM}, null
     * otherwise.
     */
    public InputStream getInputStream() {
        return inputStream;
    }
    
    /**
     * Returns the {@link Reader} that provided this input if type is {@link InputType#READER}, null
     * otherwise.
     */
    public Reader getReader() {
        return reader;
    }
    
    @Override
    public String toString() {
        if (stringRepresentation==null) {
            stringRepresentation = buildStringRepresentation();
        }
        return stringRepresentation;
    }
    
    private String buildStringRepresentation() {
        return getClass().getSimpleName() + "(" + identifier + ")";
    }
}
