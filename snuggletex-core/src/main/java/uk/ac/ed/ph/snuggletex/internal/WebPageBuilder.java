/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.snuggletex.SerializationMethod;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.SnuggleSession.EndOutputAction;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions;
import uk.ac.ed.ph.snuggletex.definitions.W3CConstants;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.utilities.CSSUtilities;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
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
    
    /**
     * Default URL and config to use when MathJax configuration option has been enabled.
     */
    public static final String DEFAULT_MATHJAX_URL = "http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=MML_HTMLorMML-full";
    
    private final SessionContext sessionContext;
    private final WebPageOutputOptions options;
    
    public WebPageBuilder(final SessionContext sessionContext, final WebPageOutputOptions options) {
        this.sessionContext = sessionContext;
        this.options = options;
    }

    public final Document buildWebPage(final List<FlowToken> fixedTokens) throws SnuggleParseException {
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
        Element body = createXHTMLElement(document, "body");
        String title = options.getTitle();

        if (title!=null && options.isAddingTitleHeading()) {
            Element titleHeader = createXHTMLElement(document, "h1");
            titleHeader.appendChild(document.createTextNode(title));
            body.appendChild(titleHeader);
        }
        
        /* Build <body/> */
        DOMBuildingController domBuildingController = new DOMBuildingController(sessionContext, options);
        domBuildingController.buildDOMSubtree(body, fixedTokens);
        
        /* Build <head/> */
        Element head = createXHTMLElement(document, "head");
        
        /* Do template-y stuff */
        if (options.isAddingMathPlayerImport()) {
            /* To trigger MathPlayer, we must declare the appropriate MathML prefix on the
             * <html/> element. Then add an <object/> followed by the appropriate PI to
             * the <head/> element. Getting any of this in the wrong order will fail
             * to make MathPlayer work.
             */
            Element object = createXHTMLElement(document, "object");
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
        Element meta = createXHTMLElement(document, "meta");
        if (options.isHtml5()) {
            /* Use 'charset' attribute in HTML5 */
            meta.setAttribute("charset", options.getEncoding());
        }
        else {
            /* Use traditional approach */
            meta.setAttribute("http-equiv", "Content-Type");
            meta.setAttribute("content", computeMetaContentType());
        }
        head.appendChild(meta); 

        
        /* Add common relevant metadata */
        meta = createXHTMLElement(document, "meta");
        meta.setAttribute("name", "Generator");
        meta.setAttribute("content", "SnuggleTeX");
        head.appendChild(meta);
        
        /* Add mandatory <title> element */
        Element titleElement = createXHTMLElement(document, "title");
        titleElement.appendChild(document.createTextNode(title!=null ? title : "SnuggleTeX Generated Page"));
        head.appendChild(titleElement);
        
        /* Add any external CSS links */
        String[] cssStylesheetURLs = options.getCSSStylesheetURLs();
        if (cssStylesheetURLs!=null) {
            Element link;
            for (String url : cssStylesheetURLs) {
                link = createXHTMLElement(document, "link");
                link.setAttribute("rel", "stylesheet");
                link.setAttribute("href", url);
                head.appendChild(link);
            }
        }
        
        /* Maybe add <style>...</style> section. */
        if (options.isIncludingStyleElement() && !options.isInliningCSS()) {
            Element style = createXHTMLElement(document, "style");
            style.setAttribute("type", "text/css");
            Properties cssProperties = CSSUtilities.readInlineCSSProperties(options);
            style.appendChild(document.createTextNode(CSSUtilities.writeStylesheet(cssProperties)));
            head.appendChild(style);
        }
        
        /* Add JS to bootstrap MathJax if requested */
        if (options.isMathJax()) {
            String mathJaxUrl = options.getCustomMathJaxUrl();
            if (mathJaxUrl==null) {
                mathJaxUrl = DEFAULT_MATHJAX_URL;
            }
            Element script = createXHTMLElement(document, "script");
            script.setAttribute("type", "text/javascript");
            script.setAttribute("src", mathJaxUrl);
            head.appendChild(script);
        }
        
        /* Create finished document */
        Element html = createXHTMLElement(document, "html");
        
        /* Add pref:renderer attribute if doing USS */
        String mathPrefRenderer = options.getMathPrefRenderer();
        if (mathPrefRenderer!=null) {
            html.setAttributeNS(W3CConstants.MATHML_PREF_NAMESPACE, "pref:renderer", "mathplayer-dl");
        }
        
        String lang = options.getLang();
        if (lang!=null) {
            /* Set language either as 'xml:lang' or plain old 'lang', or maybe both */
            if (options.getSerializationMethod()==SerializationMethod.HTML || options.getSerializationMethod()==SerializationMethod.STRICTLY_HTML) {
                html.setAttribute("lang", lang);
            }
            else {
                html.setAttributeNS(XMLConstants.XML_NS_URI, "xml:lang", lang);
            }
        }

        if (options.isPrefixingMathML() && options.getSerializationMethod()!=SerializationMethod.HTML && options.getSerializationMethod()!=SerializationMethod.STRICTLY_HTML) {
            /* We'll explicitly set the MathML prefix on the root element.
             * (MathPlayer needs it to be declared here too.)
             */
            html.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + options.getMathMLPrefix(), W3CConstants.MATHML_NAMESPACE);
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
     * Helper to create XHTML elements, setting the correct namespace prefix if required by the
     * underlying{@link WebPageOutputOptions}.
     */
    private Element createXHTMLElement(Document document, String elementLocalName) {
        return createXHTMLElement(document, elementLocalName, null);
    }
    
    /**
     * Helper to create XHTML text elements, setting the correct namespace prefix if required by the
     * underlying{@link WebPageOutputOptions}.
     */
    private Element createXHTMLElement(Document document, String elementLocalName, String content) {
        String qName;
        if (options.isPrefixingXHTML()) {
            qName = options.getXHTMLPrefix() + ":" + elementLocalName;
        }
        else {
            qName = elementLocalName;
        }
        Element result = document.createElementNS(W3CConstants.XHTML_NAMESPACE, qName);
        if (content!=null) {
            result.appendChild(document.createTextNode(content));
        }
        return result;
    }
    
    //-----------------------------------------------------------------
    
    public final String buildWebPageString(final List<FlowToken> fixedTokens) throws SnuggleParseException {
        /* Create resulting web page, including any client-specified XSLT */
        Document webPageDocument = buildWebPage(fixedTokens);
        
        /* Serialize as String */
        StringWriter resultBuilder = new StringWriter();
        if (options.isHtml5()) {
            /* Non-legacy HTML5 DOCTYPE can't be output using XSLT, so we add it here */ 
            resultBuilder.append(W3CConstants.HTML5_DOCTYPE_HEADER);
        }
        Transformer serializer = createSerializer();
        try {
            serializer.transform(new DOMSource(webPageDocument), new StreamResult(resultBuilder));
        }
        catch (TransformerException e) {
            throw new SnuggleRuntimeException("Could not serialize web page", e);
        }
        return resultBuilder.toString();
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
     * @param endOutputOptions specifies what to do with the outputStream once we've finished writing
     *   to it.
     * 
     * @throws SnuggleParseException
     * @throws IOException
     * @throws SnuggleRuntimeException if calling <tt>setContentType()</tt> on the contentTypeSettable
     *   Object failed, with the underlying Exception wrapped up.
     */
    public final void writeWebPage(final List<FlowToken> fixedTokens, Object contentTypeSettable,
            final OutputStream outputStream, final EndOutputAction endOutputOptions)
            throws SnuggleParseException, IOException {
        /* Set content type, if requested */
        if (contentTypeSettable!=null) {
            setWebPageContentType(contentTypeSettable);
        }
        
        /* Send HTML5 DOCTYPE, if required */
        if (options.isHtml5()) {
            OutputStreamWriter doctypeWriter = new OutputStreamWriter(outputStream, options.getEncoding());
            doctypeWriter.write(W3CConstants.HTML5_DOCTYPE_HEADER);
            doctypeWriter.flush();
        }
        
        /* Create resulting web page, including any client-specified XSLT */
        Document webPageDocument = buildWebPage(fixedTokens);
        
        /* Finally serialize */
        Transformer serializer = createSerializer();
        try {
            serializer.transform(new DOMSource(webPageDocument), new StreamResult(outputStream));
        }
        catch (TransformerException e) {
            throw new SnuggleRuntimeException("Could not serialize web page", e);
        }
        finally {
            if (endOutputOptions==EndOutputAction.CLOSE) {
                outputStream.close();
            }
            else if (endOutputOptions==EndOutputAction.FLUSH) {
                outputStream.flush();
            }
            else {
                /* (Do nothing!) */
            }
        }
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
     * @throws SnuggleRuntimeException if a serializer cannot be created.
     */
    private final Transformer createSerializer() {
        StylesheetManager stylesheetManager = sessionContext.getStylesheetManager();
        boolean supportsXSLT20 = stylesheetManager.supportsXSLT20();
        
        /* Get suitable serializer stylesheet */
        Transformer serializer = stylesheetManager.getSerializer(null, options);
        
        /* Set additional web-related properties */
        serializer.setOutputProperty(OutputKeys.MEDIA_TYPE, options.getContentType());
        Properties outputProperties = serializer.getOutputProperties();
        if (options.isHtml5()) {
            /* For HTML5, we have to add a DOCTYPE manually after serialization as
             * we can't output the non-legacy DOCTYPE using XSLT.
             */
            outputProperties.remove(OutputKeys.DOCTYPE_PUBLIC);
            outputProperties.remove(OutputKeys.DOCTYPE_SYSTEM);
        }
        if (options.getSerializationMethod()!=SerializationMethod.XML) {
            /* (Try to) suppress additional of a <meta content-type="..."> element in the output,
             * as we're adding our own one or doing something different in HTML5 output.
             * 
             * XSLT 2.0 lets you do this via a new output property, otherwise newer versions of
             * Xalan have a custom property.
             * 
             * BUG: This DOES NOT work with the Xalan bundled in the JDK (at least in Java 6)
             * and I can't find a way of fixing that.
             */
            if (supportsXSLT20) {
                /* (XSLT 2.0 way) */
                serializer.setOutputProperty("include-content-type", "no");
            }
            else {
                /* (Xalan only, but doesn't work on the version included in my Java 6) */
                serializer.setOutputProperty("{http://xml.apache.org/xalan}omit-meta-tag", "yes");
            }
        }
        return serializer;
    }
    
    /**
     * Computes the appropriate "Content-Type" string to be specified as an HTTP Header. (Note
     * that MathPlayer only sniffs a limited number of Content Types.)
     */
    private String computeContentTypeHeader() {
        String result;
        if (options.isNoCharsetInContentTypeHeader()) {
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
