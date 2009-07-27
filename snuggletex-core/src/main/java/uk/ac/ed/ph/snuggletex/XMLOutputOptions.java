/* $Id: WebPageOutputOptions.java 400 2009-06-14 21:26:31Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;

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
     * <p>
     * Must not be null.
     */
    private String encoding;
    
    /**
     * Whether to indent the resulting XML or not.
     * (This depends on how clever the underlying XSLT engine will be!)
     * Default is false.
     */
    private boolean indenting;
    
    /**
     * Whether to include an XML declaration on the resulting output.
     * Default is false.
     */
    private boolean includingXMLDeclaration;
    
    /**
     * Specified whether to use named entities for certain MathML symbols rather than
     * numeric character references.
     * <p>
     * Note that this requires an XSLT 2.0-compliant engine (e.g. Saxon, which is in the
     * "full" SnuggleTeX distribution.)
     * <p>
     * (Also note that the resulting XML won't be parseable unless accompanied with a DTD
     * defining the MathML entities!)
     */
    private boolean usingNamedEntities;
    
    public XMLOutputOptions() {
        super();
        this.encoding = DEFAULT_ENCODING;
        this.usingNamedEntities = false;
        this.includingXMLDeclaration = false;
    }
    
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String encoding) {
        ConstraintUtilities.ensureNotNull(encoding, "encoding");
        this.encoding = encoding;
    }
    

    public boolean isIndenting() {
        return indenting;
    }
    
    public void setIndenting(boolean identing) {
        this.indenting = identing;
    }
    
    
    public boolean isIncludingXMLDeclaration() {
        return includingXMLDeclaration;
    }
    
    public void setIncludingXMLDeclaration(boolean includingXMLDeclaration) {
        this.includingXMLDeclaration = includingXMLDeclaration;
    }


    public boolean isUsingNamedEntities() {
        return usingNamedEntities;
    }

    public void setUsingNamedEntities(boolean usingNamedEntities) {
        this.usingNamedEntities = usingNamedEntities;
    }
}
