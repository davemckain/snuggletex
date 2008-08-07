/* $Id$
 *
 * Copyright (c) 2003 - 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.jeuclid;

import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.internal.AbstractWebPageBuilder;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilderFacade;
import uk.ac.ed.ph.snuggletex.internal.SessionContext;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.internal.XMLUtilities;
import uk.ac.ed.ph.snuggletex.internal.AbstractWebPageOptions.SerializationMethod;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.utilities.CSSUtilities;

import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extension of {@link AbstractWebPageBuilder} that uses {@link JEuclidMathMLConversionVisitor}
 * to convert islands of MathML to images.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class JEuclidWebPageBuilder extends AbstractWebPageBuilder<JEuclidWebPageOptions> {
    
    public JEuclidWebPageBuilder(final SessionContext sessionContext, final JEuclidWebPageOptions options) {
        super(sessionContext, options);
    }
    
    @Override
    protected void fixOptions() {
        if (options.getImageSavingCallback()==null) {
            throw new SnuggleRuntimeException("No ImageSaver provided");
        }
        /* Content type will always be backward-compatible */
        options.setContentType("text/html");
        
        /* Use HTML serialization unless XHTML explicitly requested */
        if (options.getSerializationMethod()!=SerializationMethod.XHTML) {
            options.setSerializationMethod(SerializationMethod.HTML);
        }
    }
    
    /**
     * Creates a web page representing the given (fixed) Tokens, returning the result as a
     * DOM Document.
     * 
     * @return full DOM Document (with namespaces)
     * 
     * @throws SnuggleParseException
     */
    @Override
    public Document buildWebPage(final List<FlowToken> fixedTokens) throws SnuggleParseException {
        fixOptions();
        
        DocumentBuilder documentBuilder = XMLUtilities.createNSAwareDocumentBuilder();
        Document document = documentBuilder.newDocument();

        /* Create <body/> and maybe add title header */
        Element body = document.createElementNS(Globals.XHTML_NAMESPACE, "body");
        String title = options.getTitle();
        if (title!=null && options.isAddingTitleHeading()) {
            Element titleHeader = document.createElementNS(Globals.XHTML_NAMESPACE, "h1");
            titleHeader.appendChild(document.createTextNode(title));
            body.appendChild(titleHeader);
        }
        
        /* Build <body/> with XHTML + MathML */
        DOMBuilderFacade domBuilder = new DOMBuilderFacade(sessionContext, options);
        domBuilder.buildDOMSubtree(body, fixedTokens);
        
        /* Convert each MathML element to an image */
        JEuclidMathMLConversionVisitor visitor = new JEuclidMathMLConversionVisitor(options.getImageSavingCallback(), document);
        visitor.run(body);
        
        /* Build <head/> */
        Element head = document.createElementNS(Globals.XHTML_NAMESPACE, "head");
        
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
        html.setAttribute("lang", options.getLanguage());
        html.appendChild(head);
        html.appendChild(body);
        document.appendChild(html);
        
        return document;
    }
    
    @Override
    public String computeContentTypeHeader() {
        return computeMetaContentType();
    }
    
    /**
     * Computes the appropriate "Content-Type" string to be specified 
     * as an the XHTML <tt>meta</tt> element.
     */
    public String computeMetaContentType() {
        return "text/html; charset=" + options.getEncoding();
    }
    
    @Override
    protected void configureSerializer(Transformer serializer) {
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    }
}
