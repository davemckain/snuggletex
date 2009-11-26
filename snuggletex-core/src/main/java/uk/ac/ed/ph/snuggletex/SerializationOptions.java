/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.StandaloneSerializationOptions;

import javax.xml.transform.OutputKeys;

/**
 * Interface specifying various options for serializing XML produced by SnuggleTeX
 * and some of its utility classes.
 * <p>
 * The main implementation of this is {@link XMLStringOutputOptions}, which you can use
 * for normal SnuggleTeX functionality. There is also a {@link StandaloneSerializationOptions}
 * implementation that can be used for various utility classes, such as {@link MathMLUtilities}.
 * 
 * @see XMLStringOutputOptions
 * @see StandaloneSerializationOptions
 * 
 * @since 1.2.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public interface SerializationOptions {
    
    /** Default encoding to use */
    public static final String DEFAULT_ENCODING = "UTF-8";
    
    /**
     * Trivial enumeration of the 3 serialization methods we support.
     * <p>
     * <strong>NOTE:</strong> XHTML is only supported if you are using an XSLT 2.0
     * processor. If not supported, you will get XML output.
     */
    public static enum SerializationMethod {
        
        XML("xml"),
        XHTML("xhtml"),
        HTML("html"),
        ;
        
        private final String name;
        
        private SerializationMethod(final String name) {
            this.name = name;
        }
    
        public String getName() {
            return name;
        }
    }


    /**
     * {@link SerializationMethod} to use when generating the final output.
     * Default is {@link SerializationMethod#XML}.
     * This must not be null.
     * <p>
     * Note that {@link SerializationMethod#XHTML} is only supported properly if you are using
     * an XSLT 2.0 processor; otherwise it reverts to {@link SerializationMethod#XML}
     */
    SerializationMethod getSerializationMethod();

    void setSerializationMethod(SerializationMethod serializationMethod);
    
    /** 
     * Encoding for the resulting page.
     * Default is {@link SerializationOptions#DEFAULT_ENCODING}.
     * <p>
     * Must not be null.
     */
    String getEncoding();
    
    void setEncoding(String encoding);
    
    /**
     * Whether to indent the resulting XML or not.
     * (This depends on how clever the underlying XSLT engine will be!)
     * Default is false.
     */
    boolean isIndenting();
    
    void setIndenting(boolean identing);
    
    /**
     * Whether to include an XML declaration on the resulting output.
     * Default is false.
     */
    boolean isIncludingXMLDeclaration();
    
    void setIncludingXMLDeclaration(boolean includingXMLDeclaration);
    
    /**
     * Specifies whether to use named entities for certain MathML symbols rather than
     * numeric character references.
     * <p>
     * Note that this requires an XSLT 2.0-compliant engine (e.g. Saxon, which is in the
     * "full" SnuggleTeX distribution.)
     * <p>
     * (Also note that the resulting XML won't be parseable unless accompanied with a DTD
     * defining the MathML entities!)
     */
    boolean isUsingNamedEntities();

    void setUsingNamedEntities(boolean usingNamedEntities);

    /**
     * identifier for resulting document type declaration,
     * as described in {@link OutputKeys#DOCTYPE_PUBLIC}.
     */
    String getDoctypePublic();

    void setDoctypePublic(String doctypePublic);


    String getDoctypeSystem();

    void setDoctypeSystem(String doctypeSystem);
}
