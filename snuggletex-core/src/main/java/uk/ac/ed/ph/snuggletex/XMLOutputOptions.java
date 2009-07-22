/* $Id: WebPageOutputOptions.java 400 2009-06-14 21:26:31Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;


/**
 * Builds on {@link DOMOutputOptions} to add in options for configuring how to build a
 * web page using the relevant methods in {@link SnuggleSession}
 * (e.g. {@link SnuggleSession#buildXMLString(XMLOutputOptions)}).
 * 
 * @see DOMOutputOptions
 * @see WebPageOutputOptions
 * @see WebPageOutputOptionsTemplates
 *
 * @author  David McKain
 * @version $Revision: 400 $
 */
public class XMLOutputOptions extends DOMOutputOptions {
    
    /** Default encoding to use */
    public static final String DEFAULT_ENCODING = "UTF-8";
    
    /** 
     * Encoding for the resulting page.
     * Default is {@link XMLOutputOptions#DEFAULT_ENCODING}.
     * Must not be null.
     */
    private String encoding;
    
    /**
     * Whether to indent the resulting web page or not.
     * (This depends on how clever the underlying XSLT engine will be!)
     * Default is false.
     */
    private boolean indenting;
    
    private boolean mappingCharacters;
    
    public XMLOutputOptions() {
        super();
        this.encoding = DEFAULT_ENCODING;
        this.mappingCharacters = false;
    }
    
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    

    public boolean isIndenting() {
        return indenting;
    }
    
    public void setIndenting(boolean identing) {
        this.indenting = identing;
    }

    
    public boolean isMappingCharacters() {
        return mappingCharacters;
    }

    public void setMappingCharacters(boolean mappingCharacters) {
        this.mappingCharacters = mappingCharacters;
    }
}
