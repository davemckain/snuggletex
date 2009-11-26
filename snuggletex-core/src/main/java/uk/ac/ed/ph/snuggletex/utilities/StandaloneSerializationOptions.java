/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.utilities;

import uk.ac.ed.ph.snuggletex.SerializationOptions;
import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;

/**
 * Implementation of {@link SerializationOptions} that can be used for some of the utility
 * methods in {@link MathMLUtilities}.
 * 
 * @since 1.2.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class StandaloneSerializationOptions implements SerializationOptions {

    private SerializationMethod serializationMethod;
    private String encoding;
    private boolean indenting;
    private boolean includingXMLDeclaration;
    private boolean usingNamedEntities;
    private String doctypePublic;
    private String doctypeSystem;
    
    public StandaloneSerializationOptions() {
        this.serializationMethod = SerializationMethod.XML;
        this.encoding = DEFAULT_ENCODING;
    }
    
    
    public SerializationMethod getSerializationMethod() {
        return serializationMethod;
    }

    public void setSerializationMethod(SerializationMethod serializationMethod) {
        ConstraintUtilities.ensureNotNull(serializationMethod, "serializationMethod");
        this.serializationMethod = serializationMethod;
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


    public String getDoctypePublic() {
        return doctypePublic;
    }

    public void setDoctypePublic(String doctypePublic) {
        this.doctypePublic = doctypePublic;
    }


    public String getDoctypeSystem() {
        return doctypeSystem;
    }

    public void setDoctypeSystem(String doctypeSystem) {
        this.doctypeSystem = doctypeSystem;
    }
    
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}