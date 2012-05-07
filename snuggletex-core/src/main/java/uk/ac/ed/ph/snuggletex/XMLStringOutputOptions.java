/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.utilities.SerializationOptions;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;

/**
 * Builds on {@link DOMOutputOptions} to add in options for configuring how to build an
 * serialized output consisting of an XML (or possibly HTML) String using the relevant methods
 * in {@link SnuggleSession}
 * (e.g. {@link SnuggleSession#buildXMLString(XMLStringOutputOptions)}).
 * 
 * @see DOMOutputOptions
 * @see WebPageOutputOptions
 * @see WebPageOutputOptionsBuilder
 * 
 * @since 1.2.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public class XMLStringOutputOptions extends DOMOutputOptions implements SerializationSpecifier {
    
    private final SerializationOptions serializationOptions;
    
    public XMLStringOutputOptions() {
        super();
        this.serializationOptions = new SerializationOptions();
    }

    
    /**
     * Gets the {@link SerializationMethod} to use when generating the final output.
     * <p>
     * Default is {@link SerializationMethod#XML}.
     * This must not be null.
     * <p>
     * Note that {@link SerializationMethod#XHTML} is only supported properly if you are using
     * an XSLT 2.0 processor; otherwise it reverts to {@link SerializationMethod#XML}
     */
    public SerializationMethod getSerializationMethod() {
        return serializationOptions.getSerializationMethod();
    }

    /**
     * Sets the {@link SerializationMethod} to use when generating the final output.
     * This must not be null.
     * <p>
     * Note that {@link SerializationMethod#XHTML} is only supported properly if you are using
     * an XSLT 2.0 processor; otherwise it reverts to {@link SerializationMethod#XML}
     * 
     * @param serializationMethod {@link SerializationMethod} to use, which must not be null.
     */
    public void setSerializationMethod(SerializationMethod serializationMethod) {
        serializationOptions.setSerializationMethod(serializationMethod);
    }

    /** 
     * Gets the encoding for the resulting serialized XML.
     * <p>
     * Default is {@link SerializationSpecifier#DEFAULT_ENCODING}.
     */
    public String getEncoding() {
        return serializationOptions.getEncoding();
    }

    /** 
     * Sets the encoding for the resulting serialized XML.
     * <p>
     * Must not be null.
     * 
     * @param encoding encoding to use, which must be non-null and recognised by the XSLT
     *   {@link TransformerFactory} that will end up doing the serialization.
     */
    public void setEncoding(String encoding) {
        serializationOptions.setEncoding(encoding);
    }

    
    /**
     * Returns whether the resulting XML will be indented or not.
     * (This depends on how clever the underlying XSLT engine will be!)
     * <p>
     * Default is false.
     */
    public boolean isIndenting() {
        return serializationOptions.isIndenting();
    }

    /**
     * Sets whether the resulting XML will be indented or not.
     * (This depends on how clever the underlying XSLT engine will be!)
     * <p>
     * NOTE: The old version of Xalan that comes bundled with Java 6 (and below) appears to be
     * unable to indent output when using {@link XMLStringOutputOptions#setIndenting(boolean)}
     * with {@link SerializationMethod#HTML}.
     * I have not checked this behaviour with Java 7 (or above). I however recommend using Saxon, 
     * or including a newer version of Xalan in the ClassPath.
     * 
     * @param indenting true to indent, false otherwise.
     */
    public void setIndenting(boolean indenting) {
        serializationOptions.setIndenting(indenting);
    }
    
    
    /**
     * Returns the indentation level to use when {@link #isIndenting()} returns true.
     * (This is currently only supported if your underlying XSLT process is either Saxon
     * or Xalan. This will be the case by if you have chosen to either use Saxon or use
     * the default processor that ships with your Java platform.)
     * <p>
     * This must be a non-negative integer.
     * The default value is {@link SerializationSpecifier#DEFAULT_INDENT}.
     * 
     * @since 1.2.3
     */
    public int getIndent() {
        return serializationOptions.getIndent();
    }
    
    /**
     * Sets the indentation level to use when {@link #isIndenting()} returns true.
     * (This is currently only supported if your underlying XSLT process is either Saxon
     * or Xalan. This will be the case by if you have chosen to either use Saxon or use
     * the default processor that ships with your Java platform.)
     * <p>
     * This must be a non-negative integer.
     * The default value is {@link SerializationSpecifier#DEFAULT_INDENT}.
     * 
     * @since 1.2.3
     */
    public void setIndent(int indent) {
        serializationOptions.setIndent(indent);
    }

    
    /**
     * Gets whether to include an XML declaration on the resulting output.
     * Default is false.
     */
    public boolean isIncludingXMLDeclaration() {
        return serializationOptions.isIncludingXMLDeclaration();
    }

    /**
     * Sets whether to include an XML declaration on the resulting output.
     * 
     * @param includingXMLDeclaration true to include an XML declaration, false otherwise.
     */
    public void setIncludingXMLDeclaration(boolean includingXMLDeclaration) {
        serializationOptions.setIncludingXMLDeclaration(includingXMLDeclaration);
    }

    
    /**
     * Returns whether to use named entities for certain MathML symbols rather than
     * numeric character references.
     * <p>
     * Default is false.
     */
    public boolean isUsingNamedEntities() {
        return serializationOptions.isUsingNamedEntities();
    }

    /**
     * Sets whether to use named entities for certain MathML symbols rather than
     * numeric character references.
     * <p>
     * Note that this requires an XSLT 2.0-compliant engine (e.g. Saxon, which is in the
     * "full" SnuggleTeX distribution). This option will be silently ignored if you are
     * using an XSLT 1.0 engine, such as the one which typically comes bundled with your JDK.
     * <p>
     * (Also note that the resulting XML won't be parseable unless accompanied with a DTD
     * defining the MathML entities!)
     * 
     * @param usingNamedEntities true to use named entities, false to default to using
     *   numeric character references.
     */
    public void setUsingNamedEntities(boolean usingNamedEntities) {
        serializationOptions.setUsingNamedEntities(usingNamedEntities);
    }


    /**
     * Gets the public identifier to use in the resulting DOCTYPE declaration,
     * as described in {@link OutputKeys#DOCTYPE_PUBLIC}.
     * <p>
     * Default is null
     */
    public String getDoctypePublic() {
        return serializationOptions.getDoctypePublic();
    }

    /**
     * Sets the public identifier to use in the resulting DOCTYPE declaration,
     * as described in {@link OutputKeys#DOCTYPE_PUBLIC}.
     * 
     * @param doctypePublic public identifier to use, null for no identifier.
     */
    public void setDoctypePublic(String doctypePublic) {
        serializationOptions.setDoctypePublic(doctypePublic);
    }

    
    /**
     * Gets the system identifier to use in the resulting DOCTYPE declaration,
     * as described in {@link OutputKeys#DOCTYPE_SYSTEM}.
     * <p>
     * Default is null
     */
    public String getDoctypeSystem() {
        return serializationOptions.getDoctypeSystem();
    }

    /**
     * Sets the system identifier to use in the resulting DOCTYPE declaration,
     * as described in {@link OutputKeys#DOCTYPE_SYSTEM}.
     * 
     * @param doctypeSystem system identifier to use, null for no identifier.
     */
    public void setDoctypeSystem(String doctypeSystem) {
        serializationOptions.setDoctypeSystem(doctypeSystem);
    }
}
