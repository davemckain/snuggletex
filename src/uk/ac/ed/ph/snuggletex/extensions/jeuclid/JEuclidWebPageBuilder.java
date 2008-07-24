/* $Id: MasterNoMathMLViewStrategy.java 2712 2008-03-10 17:01:01Z davemckain $
 *
 * Copyright (c) 2003 - 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.jeuclid;

import uk.ac.ed.ph.aardvark.commons.util.StringUtilities;
import uk.ac.ed.ph.snuggletex.CSSUtilities;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.SnuggleTeX;
import uk.ac.ed.ph.snuggletex.conversion.BaseWebPageBuilder;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilderFacade;
import uk.ac.ed.ph.snuggletex.conversion.SessionContext;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.conversion.XMLUtilities;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.extensions.jeuclid.JEuclidWebPageBuilderOptions.ImageSaver;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.awt.Dimension;
import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import net.sourceforge.jeuclid.MutableLayoutContext;
import net.sourceforge.jeuclid.LayoutContext.Parameter;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.converter.Converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * FIXME: Document this type
 * 
 * FIXME: Support different types of images via plugins, like in JEuclid.
 * 
 * 
 * NASTY BIT: Writing out transparent GIFs in Java requires Java 6 and is a bit hacky. See:
 * 
 * http://www.eichberger.de/2007/07/transparent-gifs-in-java.html
 * 
 * for a helpful blog post on it. JEuclid doesn't appreciate the difficulty so writes out transparent
 * GIFs as solid black squares.
 * 
 * @author  David McKain
 * @version $Revision: 2712 $
 */
public final class JEuclidWebPageBuilder extends BaseWebPageBuilder<JEuclidWebPageBuilderOptions> {
	
    public JEuclidWebPageBuilder(final SessionContext sessionContext, final JEuclidWebPageBuilderOptions options) {
    	super(sessionContext, options);
    }
    
    private void fixOptions() {
    	if (options.getImageSaver()==null) {
    		throw new SnuggleRuntimeException("No ImageSaver provided");
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
	public Document createWebPage(final List<FlowToken> fixedTokens) throws SnuggleParseException {
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
        MathMLConversionVisitor visitor = new MathMLConversionVisitor(options, document);
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
        serializer.setOutputProperty(OutputKeys.INDENT, StringUtilities.toYesNo(options.isIndenting()));
        serializer.setOutputProperty(OutputKeys.ENCODING, options.getEncoding());
        serializer.setOutputProperty(OutputKeys.METHOD, "html");
    }
    
    /**
     * Simple "visitor" class that traverses the given XHTML + MathML document, replacing
     * MathML <tt>math</tt> elements by an appropriate image rendition.
     *
     * @author  David McKain
     * @version $Revision: 2712 $
     */
    protected static class MathMLConversionVisitor {
    	
    	private final JEuclidWebPageBuilderOptions options;
        private final Document document;
        private final MutableLayoutContext layoutContext;
        
        /** Count of current <math/> element so as to ensure unique names for replaced files */
        private int mathMLIslandCount;
        
        public MathMLConversionVisitor(final JEuclidWebPageBuilderOptions options, final Document document) {
        	this.options = options;
            this.document = document;
            
        	this.layoutContext = new LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext());
            layoutContext.setParameter(Parameter.ANTIALIAS, Boolean.valueOf(options.isAntiAliasing()));
            layoutContext.setParameter(Parameter.MATHSIZE, Float.valueOf(options.getFontSize()));

        }
        
        public void run(final Element startElement) {
            this.mathMLIslandCount = 0;
            visitElement(startElement);
        }
        
        protected void visitElement(Element element) {
            if (Globals.MATHML_NAMESPACE.equals(element.getNamespaceURI()) && element.getLocalName().equals("math")) {
                replaceMathMLIsland(element);
            }
            else {
                /* Descend into child Nodes */
                NodeList childNodes = element.getChildNodes();
                Node childNode;
                for (int i=0, size=childNodes.getLength(); i<size; i++) {
                    childNode = childNodes.item(i);
                    if (childNode.getNodeType()==Node.ELEMENT_NODE) {
                        visitElement((Element) childNode);
                    }
                }
            }
        }
        
        protected void replaceMathMLIsland(Element mathMLElement) {
            /* First we use JEuclid to create image rendition of Node and replace this element with
             * an XHTML image link.
             */
            ImageSaver imageSaver = options.getImageSaver();
            File imagePNGFile = imageSaver.getImageOutputFile(mathMLIslandCount);
            Dimension imageDimension;
            try {
                imageDimension = Converter.getInstance().convert(mathMLElement, imagePNGFile, "image/png", layoutContext);
                if (imageDimension==null) {
                    throw new SnuggleRuntimeException("Could not convert MathML island #" + mathMLIslandCount + " to image");
                }
            }
            catch (Exception e) {
                throw new SnuggleRuntimeException("Unexpected Exception handling MathML island #" + mathMLIslandCount, e);
            }

            /* Next we extract the SnuggleTeX annotation within the MathML element, if applicable, which contains the
             * original LaTeX input for this math region. This is used to create an "alt" attribute.
             */
            String snuggleTeXEncoding = null;
            if (options.isAddingMathAnnotations()) {
            	snuggleTeXEncoding = SnuggleTeX.extractSnuggleTeXAnnotation(mathMLElement);
                if (snuggleTeXEncoding!=null) {
                    snuggleTeXEncoding = snuggleTeXEncoding
                        .replaceAll("%\\s+", "") /* Strip LaTeX comments */
                        .replaceAll("\\s+", " "); /* Normalise whitespace */
                }
            }
            
            /* Next we replace the <math/> element with a <span> or <div> to house its image replacement. */
            Node parentNode = mathMLElement.getParentNode();
            boolean isBlock = mathMLElement.getAttribute("display").equals("block");
            Element replacementContainer = document.createElementNS(Globals.XHTML_NAMESPACE, isBlock ? "div" : "span");
            replacementContainer.setAttribute("class", "mathml-math");
            parentNode.replaceChild(replacementContainer, mathMLElement);
            
            /* Replace MathML with XHTML <img/> element */
            Element imgElement = document.createElementNS(Globals.XHTML_NAMESPACE, "img");
            imgElement.setAttribute("src", imageSaver.getImageURL(mathMLIslandCount));
            imgElement.setAttribute("width", Integer.toString(imageDimension.width));
            imgElement.setAttribute("height", Integer.toString(imageDimension.height));
            if (snuggleTeXEncoding!=null) {
                imgElement.setAttribute("alt", snuggleTeXEncoding);
            }
            replacementContainer.appendChild(imgElement);
            
            /* Increment count of MathML islands for next time */
            mathMLIslandCount++;
        }

    }
}
