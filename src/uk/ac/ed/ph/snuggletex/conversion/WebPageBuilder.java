/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.aardvark.commons.util.StringUtilities;
import uk.ac.ed.ph.snuggletex.CSSUtilities;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.WebPageBuilderOptions;
import uk.ac.ed.ph.snuggletex.WebPageBuilderOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This builds a simple web page representation of the LaTeX input, using the provided
 * {@link WebPageBuilderOptions} to provide a certain amount of control over the results.
 * <p>
 * This is a convenient way of bashing out working web pages if you're happy with the inherent
 * limitations of doing it this way.
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public final class WebPageBuilder {
    
    private final WebPageBuilderOptions options;
    private final SessionContext sessionContext;
    
    public WebPageBuilder(final SessionContext sessionContext, final WebPageBuilderOptions options) {
        this.options = options!=null ? (WebPageBuilderOptions) options.clone() : new WebPageBuilderOptions();
        this.sessionContext = sessionContext;
    }
    
    private void fixOptions() {
        /* 1. If MathPlayer HTML output is specified, then MathML MUST be prefixed and we won't allow
         * client-side XSLT either */
        if (options.getPageType()==WebPageType.MATHPLAYER_HTML) {
            options.setPrefixingMathML(true);
            options.setClientSideXSLTStylesheetURLs(StringUtilities.EMPTY_STRING_ARRAY);
        }
        /* 2. If client-side XSLT asked for then at least one URL must be specified. If not, we'll
         * do default output */
        String[] xsls = options.getClientSideXSLTStylesheetURLs();
        if (options.getPageType()==WebPageType.CLIENT_SIDE_XSLT_STYLESHEET && (xsls==null || xsls.length==0)) {
            options.setPageType(WebPageType.DEFAULT);
        }
        /* 3. Set content type */
        if (options.getPageType()==WebPageType.MATHPLAYER_HTML) {
            options.setContentType("text/html");
        }
        else {
            options.setContentType("application/xhtml+xml");
        }
    }

    /**
     * Computes the appropriate "Content-Type" string to be specified 
     * as an the XHTML <tt>meta</tt> element.
     * 
     * @see #computeContentTypeHeader()
     */
    public String computeMetaContentType() {
        /* Check options, making adjustments as required to ensure sanity */
        fixOptions();

        return options.getContentType() + "; charset=" + options.getEncoding();
    }
    
    /**
     * Computes the appropriate "Content-Type" string to be specified as an HTTP Header. (Note
     * that MathPlayer only sniffs a limited number of Content Types.)
     */
    public String computeContentTypeHeader() {
        /* Check options, making adjustments as required to ensure sanity */
        fixOptions();

        String result;
        if (options.getPageType()==WebPageType.CROSS_BROWSER_XHTML) {
            /* MathPlayer can only handle application/xhtml+xml without a "charset" clause */
            result = options.getContentType();
        }
        else {
            result = options.getContentType() + "; charset=" + options.getEncoding();
        }
        return result;
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
    public Document createWebPage(final List<FlowToken> fixedTokens) throws SnuggleParseException {
        Document document = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        
        /* Check options, making adjustments as required to ensure sanity */
        fixOptions();

        /* Create <body/> and maybe add title header */
        Element body = document.createElementNS(Globals.XHTML_NAMESPACE, "body");
        String title = options.getTitle();
        if (title!=null && options.isAddingTitleHeading()) {
            Element titleHeader = document.createElementNS(Globals.XHTML_NAMESPACE, "h1");
            titleHeader.appendChild(document.createTextNode(title));
            body.appendChild(titleHeader);
        }
        
        /* Build <body/> */
        DOMBuilder domBuilder = new DOMBuilder(sessionContext, body, options);
        domBuilder.buildDOMSubtree(fixedTokens);
        
        /* Build <head/> */
        Element head = document.createElementNS(Globals.XHTML_NAMESPACE, "head");
        
        /* Do template-y stuff */
        WebPageType pageType = options.getPageType();
        if (pageType==WebPageType.MATHPLAYER_HTML) {
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
        else if (pageType==WebPageType.CLIENT_SIDE_XSLT_STYLESHEET) {
            for (String url : options.getClientSideXSLTStylesheetURLs()) {
                /* (These go in at the top of the Document) */
                document.appendChild(document.createProcessingInstruction("xsl-stylesheet",
                        "type=\"text/xsl\" href=\"" + url + "\""));
            }
        }
        
        /* Add common relevant metadata */
        Element meta = document.createElementNS(Globals.XHTML_NAMESPACE, "meta");
        meta.setAttribute("http-equiv", "Content-Type");
        meta.setAttribute("content", computeMetaContentType());
        head.appendChild(meta);
        
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
        
        /* CSS will either go via a <link href="..."/> if specified or will be put inline using
         * <style/> and the default CSS.
         */
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
        else {
            /* No CSS link specified, so we'll embed stylesheet inline */
            Element style = document.createElementNS(Globals.XHTML_NAMESPACE, "style");
            style.setAttribute("type", "text/css");
            Properties cssProperties = CSSUtilities.readInlineCSSProperties(options);
            style.appendChild(document.createTextNode(CSSUtilities.writeStylesheet(cssProperties)));
            head.appendChild(style);
        }
        
        /* Create finished document */
        Element html = document.createElementNS(Globals.XHTML_NAMESPACE, "html");
        
        /* Set language either as 'xml:lang' or plain old 'lang' */
        if (options.getPageType()==WebPageType.MATHPLAYER_HTML) {
            html.setAttribute("lang", options.getLanguage());
        }
        else {
            html.setAttributeNS(Globals.XML_NAMESPACE, "xml:lang", options.getLanguage());
        }
        if (options.isPrefixingMathML()) {
            /* We'll try to explicitly set the MathML prefix on the root element */
            html.setAttributeNS(Globals.XMLNS_NAMESPACE, "xmlns:" + options.getMathMLPrefix(), Globals.MATHML_NAMESPACE);
        }
        html.appendChild(head);
        html.appendChild(body);
        document.appendChild(html);
        
        return document;
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
    public void writeWebPage(final List<FlowToken> fixedTokens, final Object contentTypeSettable,
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
    private Transformer createSerializer() {
        TransformerFactory transformerFactory = XMLUtilities.createTransformerFactory();
        Transformer result = options.getStylesheet();
        if (result==null) {
            try {
                result = transformerFactory.newTransformer();
            }
            catch (TransformerConfigurationException e) {
                throw new SnuggleRuntimeException("Could not create serializer", e);
            }
        }

        /* Set serialization properties as required for the type of output */
        WebPageType pageType = options.getPageType();
        result.setOutputProperty(OutputKeys.INDENT, StringUtilities.toYesNo(options.isIndenting()));
        result.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, pageType==WebPageType.CROSS_BROWSER_XHTML ? "no" : "yes");
        result.setOutputProperty(OutputKeys.ENCODING, options.getEncoding());
        result.setOutputProperty(OutputKeys.METHOD, pageType==WebPageType.MATHPLAYER_HTML ? "html" : "xml");
        if (pageType==WebPageType.CROSS_BROWSER_XHTML) {
            result.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.1 plus MathML 2.0 plus SVG 1.1//EN");
            result.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/Math/DTD/mathml2/xhtml-math11-f.dtd");
        }
        return result;
    }
}
