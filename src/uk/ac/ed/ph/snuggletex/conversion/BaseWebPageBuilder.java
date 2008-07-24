/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * FIXME: Document this type!
 * FIXME: Tidy this up!
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public abstract class BaseWebPageBuilder<P extends BaseWebPageBuilderOptions> {
	
	protected final SessionContext sessionContext;
    protected final P options;
    
    public BaseWebPageBuilder(final SessionContext sessionContext, final P options) {
        this.sessionContext = sessionContext;
        this.options = options;
    }
    
    /**
     * Creates a web page representing the given (fixed) Tokens, returning the result as a
     * DOM Document.
     * 
     * @param fixedTokens fixed Tokens from earlier stages of parsing
     * 
     * @return full DOM Document (with namespaces)
     * 
     * @throws SnuggleParseException
     */
    public abstract Document createWebPage(final List<FlowToken> fixedTokens)
    		throws SnuggleParseException;
    
    /**
     * Creates a web page representing the given (fixed) Tokens, and writes the results to
     * the given {@link OutputStream}.
     * 
     * @param fixedTokens fixed Tokens from earlier stages of parsing
     * @param contentTypeSettable optional bean Object that will have its <tt>contentType</tt>
     *   property set if provided. (This is generally useful as a proxy for the <tt>HttpResponse</tt>
     *   Object in the servlet API, which I want to avoid a compile-time dependency on.)
     * @param outputStream Stream to send the resulting page to, which will be closed afterwards.
     * 
     * @throws SnuggleParseException
     * @throws IOException
     */
    public final void writeWebPage(final List<FlowToken> fixedTokens, final Object contentTypeSettable,
            final OutputStream outputStream) throws SnuggleParseException, IOException {
        if (contentTypeSettable!=null) {
            /* Look for a Method called setContentType() and, if found, call it */
            try {
                Method setterMethod = contentTypeSettable.getClass().getMethod("setContentType", new Class<?>[] { String.class });
                setterMethod.invoke(contentTypeSettable, computeContentTypeHeader());
            }
            catch (Exception e) {
                throw new SnuggleRuntimeException("Could not find and call setContentType() on Object " + contentTypeSettable, e);
            }
        }
        Document webPageDocument = createWebPage(fixedTokens);
        Transformer serializer = createSerializer();
        try {
            serializer.transform(new DOMSource(webPageDocument), new StreamResult(outputStream));
        }
        catch (TransformerException e) {
            throw new SnuggleRuntimeException("Could not serialize web page", e);
        }
        finally {
            outputStream.close();
        }
    }
    
    /**
     * @throws SnuggleRuntimeException if a serializer cannot be created.
     */
    private final Transformer createSerializer() {
        TransformerFactory transformerFactory = XMLUtilities.createTransformerFactory();
        Transformer serializer = options.getStylesheet();
        if (serializer==null) {
            try {
                serializer = transformerFactory.newTransformer();
            }
            catch (TransformerConfigurationException e) {
                throw new SnuggleRuntimeException("Could not create serializer", e);
            }
        }
        
        /* Let subclass configure the Serializer as appropriate */
        configureSerializer(serializer);
        return serializer;
    }
    
    protected abstract String computeContentTypeHeader();
    
    protected abstract void configureSerializer(Transformer serializer);
}
