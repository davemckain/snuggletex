/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.SerializationMethod;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.StringUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.utilities.CSSUtilities;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Builds a web page from a {@link List} of fixed {@link FlowToken}s, using the supplied
 * {@link WebPageOutputOptions} to determine the exact form of the results.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class WebPageBuilder {
    
    private final SessionContext sessionContext;
    private final WebPageOutputOptions options;
    
    public WebPageBuilder(final SessionContext sessionContext, final WebPageOutputOptions options) {
        this.sessionContext = sessionContext;
        this.options = options;
    }
    

    public final Document createWebPage(final List<FlowToken> fixedTokens) throws SnuggleParseException {
        checkOptions();
        Document document = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        
        /* Add in any client-side XSLT */
        String[] clientXSLTURLs = options.getClientSideXSLTStylesheetURLs();
        if (clientXSLTURLs!=null) {
            for (String url : clientXSLTURLs) {
                /* (These go in at the top of the Document) */
                document.appendChild(document.createProcessingInstruction("xml-stylesheet",
                        "type=\"text/xsl\" href=\"" + url + "\""));
            }
        }
        
        /* Create <body/> and maybe add title header */
        Element body = document.createElementNS(Globals.XHTML_NAMESPACE, "body");
        String title = options.getTitle();
        if (title!=null && options.isAddingTitleHeading()) {
            Element titleHeader = document.createElementNS(Globals.XHTML_NAMESPACE, "h1");
            titleHeader.appendChild(document.createTextNode(title));
            body.appendChild(titleHeader);
        }
        
        /* Build <body/> */
        DOMBuildingController domBuildingController = new DOMBuildingController(sessionContext, options);
        domBuildingController.buildDOMSubtree(body, fixedTokens);
        
        /* Build <head/> */
        Element head = document.createElementNS(Globals.XHTML_NAMESPACE, "head");
        
        /* Do template-y stuff */
        WebPageType pageType = options.getWebPageType();
        if (pageType==WebPageType.MATHPLAYER_HTML) {
            /* To trigger MathPlayer, we must declare the appropriate MathML prefix on the
             * <html/> element. Then add an <object/> followed by the appropriate PI to
             * the <head/> element. Getting any of this in the wrong order will fail
             * to make MathPlayer work.
             */
            Element object = document.createElementNS(Globals.XHTML_NAMESPACE, "object");
            object.setAttribute("id", "MathPlayer");
            object.setAttribute("classid", "clsid:32F66A20-7614-11D4-BD11-00104BD3F987");
            head.appendChild(object);
            
            /* NOTE: We need to add the final '?' as we're outputting an HTML PI which normally
             * doesn't have one, even though the docs for MathPlayer seem to expect one!
             */
            head.appendChild(document.createProcessingInstruction("import",
                    "namespace=\"" + options.getMathMLPrefix() + "\" implementation=\"#MathPlayer\" ?"));
        }

        /* Add content type <meta/> element. (The serializer might add another of these but let's
         * be safe as we don't know what's going to happen at this point.) */
        Element meta = document.createElementNS(Globals.XHTML_NAMESPACE, "meta");
        meta.setAttribute("http-equiv", "Content-Type");
        meta.setAttribute("content", computeMetaContentType());
        head.appendChild(meta);
        
        /* Add common relevant metadata */
        meta = document.createElementNS(Globals.XHTML_NAMESPACE, "meta");
        meta.setAttribute("name", "Generator");
        meta.setAttribute("content", "SnuggleTeX");
        head.appendChild(meta);
        
        /* Add <title/>, if specified */
        if (title!=null) {
            Element titleElement = document.createElementNS(Globals.XHTML_NAMESPACE, "title");
            titleElement.appendChild(document.createTextNode(options.getTitle()));
            head.appendChild(titleElement);
        }
        
        /* Maybe add <style>...</style> section */
        if (options.isIncludingStyleElement()) {
            Element style = document.createElementNS(Globals.XHTML_NAMESPACE, "style");
            style.setAttribute("type", "text/css");
            Properties cssProperties = CSSUtilities.readInlineCSSProperties(options);
            style.appendChild(document.createTextNode(CSSUtilities.writeStylesheet(cssProperties)));
            head.appendChild(style);
        }
        
        /* Add any external CSS links */
        String[] cssStylesheetURLs = options.getCSSStylesheetURLs();
        if (cssStylesheetURLs!=null) {
            Element link;
            for (String url : cssStylesheetURLs) {
                link = document.createElementNS(Globals.XHTML_NAMESPACE, "link");
                link.setAttribute("rel", "stylesheet");
                link.setAttribute("href", url);
                head.appendChild(link);
            }
        }
        
        /* Create finished document */
        Element html = document.createElementNS(Globals.XHTML_NAMESPACE, "html");
        
        /* Add pref:renderer attribute if doing USS */
        if (pageType==WebPageType.UNIVERSAL_STYLESHEET) {
            html.setAttributeNS(Globals.MATHML_PREF_NAMESPACE, "pref:renderer", "mathplayer-dl");
        }
        
        String lang = options.getLang();
        if (lang!=null) {
            /* Set language either as 'xml:lang' or plain old 'lang' */
            if (pageType==WebPageType.MATHPLAYER_HTML || pageType==WebPageType.PROCESSED_HTML) {
                html.setAttribute("lang", lang);
            }
            else {
                html.setAttributeNS(Globals.XML_NAMESPACE, "xml:lang", lang);
            }
        }

        if (options.isPrefixingMathML()) {
            /* We'll explicitly set the MathML prefix on the root element.
             * (MathPlayer needs it to be declared here too.)
             */
            html.setAttributeNS(Globals.XMLNS_NAMESPACE, "xmlns:" + options.getMathMLPrefix(), Globals.MATHML_NAMESPACE);
        }
        html.appendChild(head);
        html.appendChild(body);
        document.appendChild(html);
        
        /* Apply any extra XSLT specified in the options */
        Transformer[] stylesheets = options.getStylesheets();
        if (!ObjectUtilities.isNullOrEmpty(stylesheets)) {
            for (Transformer stylesheet : stylesheets) {
                DOMSource input = new DOMSource(document);
                document = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
                try {
                    stylesheet.transform(input, new DOMResult(document));
                }
                catch (TransformerException e) {
                    throw new SnuggleRuntimeException("Could not apply stylesheet " + stylesheet);
                }
            }
        }
        return document;
    }
    
    /**
     * Calls the <tt>setContentType</tt> method for the given Object (by reflection) to something
     * appropriate for serving the types of web pages generated by this builder over HTTP.
     * <p>
     * (The main example for this would be passing a <tt>javax.servlet.http.HttpResponse</tt>
     * Object, which I want to avoid a compile-time dependency on.)
     * 
     * @param contentTypeSettable Object that will have its <tt>contentType</tt>
     *   property set if provided. 
     */
    public final void setWebPageContentType(Object contentTypeSettable) {
        checkOptions();
        try {
            Method setterMethod = contentTypeSettable.getClass().getMethod("setContentType",
                    new Class<?>[] { String.class });
            setterMethod.invoke(contentTypeSettable, computeContentTypeHeader());
        }
        catch (Exception e) {
            throw new SnuggleRuntimeException("Could not find and call setContentType() on Object "
                    + contentTypeSettable, e);
        }
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
     * @throws SnuggleRuntimeException if calling <tt>setContentType()</tt> on the contentTypeSettable
     *   Object failed, with the underlying Exception wrapped up.
     */
    public final void writeWebPage(final List<FlowToken> fixedTokens, Object contentTypeSettable,
            final OutputStream outputStream) throws SnuggleParseException, IOException {
        /* Set content type, if requested */
        if (contentTypeSettable!=null) {
            setWebPageContentType(contentTypeSettable);
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
         * for proper no-namespace HTML output.
         */
        TransformerFactory transformerFactory = XMLUtilities.createJAXPTransformerFactory();
        SerializationMethod serializationMethod = options.getSerializationMethod();
        if (serializationMethod==null) {
            serializationMethod = SerializationMethod.XML;
        }
        Transformer serializer;
        try {
            if (options.getSerializationMethod()==SerializationMethod.HTML) {
                serializer = sessionContext.getStylesheetManager()
                    .getStylesheet(Globals.XHTML_TO_HTML_XSL_RESOURCE_NAME)
                    .newTransformer();
            }
            else {
                serializer = transformerFactory.newTransformer();
            }
        }
        catch (TransformerConfigurationException e) {
            throw new SnuggleRuntimeException("Could not create serializer", e);
        }
        
        /* Decide on output method, using XSLT 2.0's "xhtml" method if requested and available,
         * falling back to "xml" otherwise.
         */
        boolean xslt20 = XMLUtilities.supportsXSLT20(serializer);
        SerializationMethod effectiveMethod = serializationMethod;
        if (effectiveMethod==SerializationMethod.XHTML && !xslt20) {
            effectiveMethod = SerializationMethod.XML;
        }
        serializer.setOutputProperty(OutputKeys.METHOD, effectiveMethod.getName());

        /* Set serialization properties as required for the type of output */
        WebPageType pageType = options.getWebPageType();
        serializer.setOutputProperty(OutputKeys.INDENT, StringUtilities.toYesNo(options.isIndenting()));
        serializer.setOutputProperty(OutputKeys.ENCODING, options.getEncoding());
        serializer.setOutputProperty(OutputKeys.MEDIA_TYPE, options.getContentType());
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                StringUtilities.toYesNo(!(pageType==WebPageType.CROSS_BROWSER_XHTML || pageType==WebPageType.UNIVERSAL_STYLESHEET)));
        if (pageType==WebPageType.CROSS_BROWSER_XHTML) {
            serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.1 plus MathML 2.0 plus SVG 1.1//EN");
            serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/Math/DTD/mathml2/xhtml-math11-f.dtd");
        }
        if (xslt20 && serializationMethod!=SerializationMethod.XML) {
            /* XSLT 2.0 allows us to explicitly stop serializer adding Content Type declaration,
             * which is something we've already done here.
             */
            serializer.setOutputProperty("include-content-type", "no");
        }
        return serializer;
    }
    
    /**
     * Checks that required fields in {@link WebPageOutputOptions} have been set.
     */
    private void checkOptions() {
        ConstraintUtilities.ensureNotNull(options.getWebPageType(), "WebPageOutputOptions.webPageType");
        ConstraintUtilities.ensureNotNull(options.getSerializationMethod(), "WebPageOutputOptions.serializationMethod");
        ConstraintUtilities.ensureNotNull(options.getEncoding(), "WebPageOutputOptions.encoding");
        ConstraintUtilities.ensureNotNull(options.getContentType(), "WebPageOutputOptions.contentType");
    }
    
    /**
     * Computes the appropriate "Content-Type" string to be specified as an HTTP Header. (Note
     * that MathPlayer only sniffs a limited number of Content Types.)
     */
    private String computeContentTypeHeader() {
        String result;
        if (options.getWebPageType()==WebPageType.CROSS_BROWSER_XHTML) {
            /* MathPlayer can only handle application/xhtml+xml without a "charset" clause */
            result = options.getContentType();
        }
        else {
            result = options.getContentType() + "; charset=" + options.getEncoding();
        }
        return result;
    }
    
    /**
     * Computes the appropriate "Content-Type" string to be specified 
     * as an the XHTML <tt>meta</tt> element.
     * 
     * @see #computeContentTypeHeader()
     */
    private String computeMetaContentType() {
        return options.getContentType() + "; charset=" + options.getEncoding();
    }
}
