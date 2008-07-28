/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.aardvark.commons.util.StringUtilities;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * Template for a class that can build a web page from a {@link List} of fixed {@link FlowToken}s.
 * The default realisation of this template is {@link MathMLWebPageBuilder} that writes out nice
 * standards-compliant web pages.
 * <p>
 * SnuggleTeX comes optionally bundled with an extension that can create "legacy" HTML 4.0
 * pages with MathML converted to images.
 * 
 * @param <P> type of {@link AbstractWebPageBuilderOptions} supported by this builder
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public abstract class AbstractWebPageBuilder<P extends AbstractWebPageBuilderOptions> {
	
	protected final SessionContext sessionContext;
	
    protected final P options;
    
    public AbstractWebPageBuilder(final SessionContext sessionContext, final P options) {
        this.sessionContext = sessionContext;
        this.options = options;
    }
    
    public final Document createWebPage(final List<FlowToken> fixedTokens) throws SnuggleParseException {
        /* Get subclass to build resulting document */
        Document result = buildWebPage(fixedTokens);
        
        /* Apply any extra XSLT specified in the options */
        Transformer stylesheet = options.getStylesheet();
        if (stylesheet!=null) {
            DOMSource input = new DOMSource(result);
            result = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
            DOMResult output = new DOMResult(result);
            try {
                stylesheet.transform(input, output);
            }
            catch (TransformerException e) {
                throw new SnuggleRuntimeException("Could not apply stylesheet " + stylesheet);
            }
        }
        return result;
    }
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
        System.out.println("wWP!");
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
        /* Create resulting web page, including any client-specified XSLT */
        Document webPageDocument = createWebPage(fixedTokens);
        
        /* Finally serialize */
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
        /* Create either an identity transform (for XHTML) or one which converts XHTML to HTML
         * (for legacy HTML) output.
         */
        Transformer serializer;
        try {
            if (options.getContentType().equals("text/html")) {
                serializer = sessionContext.getStylesheet(Globals.XHTML_TO_HTML_XSL_RESOURCE_NAME).newTransformer();
            }
            else {
                serializer = XMLUtilities.createTransformerFactory().newTransformer();
            }
        }
        catch (TransformerConfigurationException e) {
            throw new SnuggleRuntimeException("Could not create serializer", e);
        }

        /* Set core serialization properties */
        serializer.setOutputProperty(OutputKeys.INDENT, StringUtilities.toYesNo(options.isIndenting()));
        serializer.setOutputProperty(OutputKeys.ENCODING, options.getEncoding());
        
        /* Let subclass further configure the Serializer as appropriate */
        configureSerializer(serializer);
        return serializer;
    }
    
    //------------------------------------------------------------
    
    /**
     * Subclasses should fill in to creates a DOM {@link Document} representing a complete
     * web page representing the given (fixed) Tokens.
     * 
     * @param fixedTokens fixed Tokens from earlier stages of parsing
     * @return full DOM Document (which must support namespaces)
     * 
     * @throws SnuggleParseException
     */
    protected abstract Document buildWebPage(final List<FlowToken> fixedTokens)
    		throws SnuggleParseException;
    
    /**
     * Subclasses should fill in the compute the appropriate HTTP <tt>Content-Type</tt>
     * header to use when using {@link #writeWebPage(List, Object, OutputStream)}.
     */
    protected abstract String computeContentTypeHeader();
    
    /**
     * Subclasses should fill in to configure the {@link Transformer} that will be used
     * to serialize the resulting web page. The {@link OutputKeys#INDENT} and
     * {@link OutputKeys#ENCODING} will have been set correctly already.
     */
    protected abstract void configureSerializer(Transformer serializer);
}
