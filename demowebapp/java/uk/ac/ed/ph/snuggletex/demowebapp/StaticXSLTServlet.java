/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.demowebapp;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * This trivial servlet takes static XHTML content and tarts it up slightly by applying the
 * {@link SnuggleTeXServlet#WEBPAGE_XSLT_RESOURCE_PATH} to it to pull in the correct CSS and add
 * in headers and footers.
 * <p>
 * This is not fully developed yet as there is only 1 static page so far...!
 * <p>
 * This reuses the {@link Templates} Object stored into the {@link ServletContext} by
 * {@link SnuggleTeXServlet}.
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public final class StaticXSLTServlet extends BaseServlet {
    
    private static final long serialVersionUID = 1372733519524963339L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /* Look up the HTML source */
        String path = request.getServletPath();
        InputStream sourceStream = getServletContext().getResourceAsStream(path);
        if (sourceStream==null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        /* Create stylesheet to format the resulting web page */
        Transformer stylesheet;
        try {
            stylesheet = getWebPageTemplates().newTransformer();
            stylesheet.setParameter("context-path", request.getContextPath());
        }
        catch (TransformerConfigurationException e) {
            throw new ServletException("Could not create stylesheet from Templates", e);
        }
        
        /* Then fire the results straight out.
         * (This is actually a bit nasty as doing it this way gives no opportunity to
         * fail on error.)
         */
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        ServletOutputStream servletOutputStream = response.getOutputStream();
        try {
            stylesheet.transform(new StreamSource(sourceStream), new StreamResult(servletOutputStream));
        }
        catch (TransformerException e) {
            throw new ServletException("Unexpected Exception performing transform", e);
        }
        finally {
            servletOutputStream.flush();
        }
    }
    
    private Templates getWebPageTemplates() throws ServletException {
        Templates result = (Templates) getServletContext().getAttribute(SnuggleTeXServlet.WEBPAGE_XSLT_ATTRIBUTE_NAME);
        if (result==null) {
            throw new ServletException("Could not read in stored Templates Object as ServletContext Attribute at "
                    + SnuggleTeXServlet.WEBPAGE_XSLT_ATTRIBUTE_NAME);
        }
        return result;
    }
}
