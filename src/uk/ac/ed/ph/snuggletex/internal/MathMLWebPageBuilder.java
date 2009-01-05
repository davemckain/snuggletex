/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.commons.util.StringUtilities;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions;
import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.internal.AbstractWebPageOptions.SerializationMethod;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.utilities.CSSUtilities;

import java.util.List;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This builds a simple web page representation of the LaTeX input, using the provided
 * {@link MathMLWebPageOptions} to provide a certain amount of control over the results.
 * <p>
 * This is a convenient way of bashing out working web pages if you're happy with the inherent
 * limitations of doing it this way.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MathMLWebPageBuilder extends AbstractWebPageBuilder<MathMLWebPageOptions> {
    
    public MathMLWebPageBuilder(final SessionContext sessionContext, final MathMLWebPageOptions options) {
        super(sessionContext, options);
    }
    
    @Override
    protected void fixOptions() {
        /* 1. If MathPlayer HTML output is specified, then:
         * 
         * a. MathML MUST be prefixed
         * b. We want to generate plain old HTML in no namespace
         * c. We won't allow client-side XSLT either */
        if (options.getPageType()==WebPageType.MATHPLAYER_HTML) {
            options.setPrefixingMathML(true);
            options.setClientSideXSLTStylesheetURLs(StringUtilities.EMPTY_STRING_ARRAY);
        }
        /* 2. Set content type */
        if (options.getPageType()==WebPageType.MATHPLAYER_HTML) {
            options.setContentType("text/html");
        }
        else {
            options.setContentType("application/xhtml+xml");
        }
        /* 3. Set serialization method */
        SerializationMethod serializationMethod = null;
        switch (options.getPageType()) {
            case MOZILLA:
            case CROSS_BROWSER_XHTML:
                serializationMethod = SerializationMethod.XHTML;
                break;
                
            case MATHPLAYER_HTML:
                serializationMethod = SerializationMethod.HTML;
                break;
                
            case CLIENT_SIDE_XSLT_STYLESHEET:
            case UNIVERSAL_STYLESHEET:
                serializationMethod = SerializationMethod.XML;
                break;
                
            default:
                throw new SnuggleLogicException("Unexpected switch case" + options.getPageType());
        }
        options.setSerializationMethod(serializationMethod);
    }
    
    /**
     * Computes the appropriate "Content-Type" string to be specified as an HTTP Header. (Note
     * that MathPlayer only sniffs a limited number of Content Types.)
     */
    @Override
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
     * Creates a web page representing the given (fixed) Tokens, returning the result as a
     * DOM Document.
     * 
     * @param fixedTokens fixed Tokens from earlier stages of parsing
     * 
     * @return full DOM Document (with namespaces)
     * 
     * @throws SnuggleParseException
     */
    @Override
    public Document buildWebPage(final List<FlowToken> fixedTokens) throws SnuggleParseException {
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
        DOMBuilderFacade domBuilder = new DOMBuilderFacade(sessionContext, options);
        domBuilder.buildDOMSubtree(body, fixedTokens);
        
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
        /* Add in any client-side XSLT */
        for (String url : options.getClientSideXSLTStylesheetURLs()) {
            /* (These go in at the top of the Document) */
            document.appendChild(document.createProcessingInstruction("xml-stylesheet",
                    "type=\"text/xsl\" href=\"" + url + "\""));
        }

        /* Add content type (this may be redone by the serializer but let's be safe */
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
        
        /* Add pref:renderer attribute if doing USS */
        if (pageType==WebPageType.UNIVERSAL_STYLESHEET) {
            html.setAttributeNS(Globals.MATHML_PREF_NAMESPACE, "pref:renderer", "mathplayer-dl");
        }
        
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

    @Override
    protected void configureSerializer(Transformer serializer) {
        /* Set serialization properties as required for the type of output */
        WebPageType pageType = options.getPageType();
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, 
                pageType==WebPageType.CROSS_BROWSER_XHTML || pageType==WebPageType.UNIVERSAL_STYLESHEET
                ? "no" : "yes");
        if (pageType==WebPageType.CROSS_BROWSER_XHTML) {
            serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.1 plus MathML 2.0 plus SVG 1.1//EN");
            serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/Math/DTD/mathml2/xhtml-math11-f.dtd");
        }
    }
}
