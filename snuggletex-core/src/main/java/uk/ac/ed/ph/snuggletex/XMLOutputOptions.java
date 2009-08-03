/* $Id: WebPageOutputOptions.java 400 2009-06-14 21:26:31Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.utilities.StandaloneSerializationOptions;

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
public class XMLOutputOptions extends DOMOutputOptions implements SerializationOptions {
    
    private final StandaloneSerializationOptions serializationOptions;
    
    public XMLOutputOptions() {
        super();
        this.serializationOptions = new StandaloneSerializationOptions();
    }

    public SerializationMethod getSerializationMethod() {
        return serializationOptions.getSerializationMethod();
    }

    public void setSerializationMethod(SerializationMethod serializationMethod) {
        serializationOptions.setSerializationMethod(serializationMethod);
    }

    public String getEncoding() {
        return serializationOptions.getEncoding();
    }

    public void setEncoding(String encoding) {
        serializationOptions.setEncoding(encoding);
    }

    public boolean isIndenting() {
        return serializationOptions.isIndenting();
    }

    public void setIndenting(boolean identing) {
        serializationOptions.setIndenting(identing);
    }

    public boolean isIncludingXMLDeclaration() {
        return serializationOptions.isIncludingXMLDeclaration();
    }

    public void setIncludingXMLDeclaration(boolean includingXMLDeclaration) {
        serializationOptions.setIncludingXMLDeclaration(includingXMLDeclaration);
    }

    public boolean isUsingNamedEntities() {
        return serializationOptions.isUsingNamedEntities();
    }

    public void setUsingNamedEntities(boolean usingNamedEntities) {
        serializationOptions.setUsingNamedEntities(usingNamedEntities);
    }

    public String getDoctypePublic() {
        return serializationOptions.getDoctypePublic();
    }

    public void setDoctypePublic(String doctypePublic) {
        serializationOptions.setDoctypePublic(doctypePublic);
    }

    public String getDoctypeSystem() {
        return serializationOptions.getDoctypeSystem();
    }

    public void setDoctypeSystem(String doctypeSystem) {
        serializationOptions.setDoctypeSystem(doctypeSystem);
    }
}
