/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import uk.ac.ed.ph.snuggletex.BaseWebPageOptions;
import uk.ac.ed.ph.snuggletex.DownConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.BaseWebPageOptions.SerializationMethod;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.internal.util.IOUtilities;
import uk.ac.ed.ph.snuggletex.jeuclid.JEuclidWebPageOptions;
import uk.ac.ed.ph.snuggletex.jeuclid.SimpleMathMLImageSavingCallback;
import uk.ac.ed.ph.snuggletex.upconversion.UpConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME: Document this class!
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class DocumentationServlet extends BaseServlet {
    
    private static final long serialVersionUID = -4091098938512353014L;

    static final Logger logger = LoggerFactory.getLogger(DocumentationServlet.class);
    
    public static final String CACHING_PARAM = "caching";
    
    private static final String FORMAT_OUTPUT_XSLT_URI = "classpath:/format-output.xsl";
    private static final String MACROS_RESOURCE_LOCATION = "/WEB-INF/macros.tex";
    private static final String TEX_SOURCE_BASE_RESOURCE = "/WEB-INF/docs";

    private final Map<String, WebPageType> extensionToWebPageTypeMap;
    private final Map<String, String> extensionToContentTypeMap;
    
    /** FIXME: Currently unused */
    private boolean caching;
    
    /** Directory in which Files created and cached by this servlet will be stored. */
    private File baseDirectory;
    
    @Override
    public void init() throws ServletException {
        /* Set up base directory */
        if (baseDirectory==null) {
            try {
                baseDirectory = File.createTempFile("snuggetex-", ".dir");
            }
            catch (IOException e) {
                throw new ServletException("Could not create initial tempfile for storing documentation", e);
            }
            if (!baseDirectory.delete()) {
                throw new ServletException("Could not delete tempfile at " + baseDirectory
                        + " for re-creating as a directory");
            }
            if (!baseDirectory.mkdir()) {
                throw new ServletException("Could not create base directory at " + baseDirectory
                        + " for storing documentation");
            }
            logger.info("Set base directory for documentation as {}", baseDirectory);
        }
        
        /* Check whether caching is turned on or not */
        caching = "true".equals(getServletConfig().getInitParameter(CACHING_PARAM));
    }
    
    public DocumentationServlet() {
        /* Define web page types by extension */
        this.extensionToWebPageTypeMap = new HashMap<String, WebPageType>();
        extensionToWebPageTypeMap.put("xhtml", WebPageType.MOZILLA);
        extensionToWebPageTypeMap.put("htm", WebPageType.MATHPLAYER_HTML);
        extensionToWebPageTypeMap.put("xml", WebPageType.UNIVERSAL_STYLESHEET);
        extensionToWebPageTypeMap.put("cxml", WebPageType.CROSS_BROWSER_XHTML);
        
        this.extensionToContentTypeMap = new HashMap<String, String>();
        extensionToContentTypeMap.put("xhtml", "application/xhtml+xml");
        extensionToContentTypeMap.put("xml", "application/xhtml+xml");
        extensionToContentTypeMap.put("cxml", "application/xhtml+xml");
        extensionToContentTypeMap.put("htm", "text/html");
        extensionToContentTypeMap.put("html", "text/html");
        extensionToContentTypeMap.put("png", "image/png");
    }
    
    private File mapResourcePath(final String resourcePath) {
        return new File(baseDirectory + File.separator + resourcePath.replace("/", File.separator));
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String resourcePath = request.getPathInfo();
        String[] splitResourcePath = splitResourcePath(resourcePath);
        String extension = splitResourcePath[1];
        String contentType = extensionToContentTypeMap.get(extension);
        File resourceFile = mapResourcePath(resourcePath);
        if (contentType==null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unsupported documentation resource extension " + extension);
            return;
        }
        if (!"png".equals(extension) && (!resourceFile.exists() || !caching)) {
            resourceFile = generateResource(resourcePath, request.getContextPath(), request.getServletPath());
            if (resourceFile==null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        response.setContentType(contentType);
        response.setContentLength((int) resourceFile.length());
        IOUtilities.transfer(new FileInputStream(resourceFile), response.getOutputStream(), true, false);
    }
    
    private String[] splitResourcePath(final String resourcePath) throws ServletException {
        /* Work out what is to be served using the file extension to determine what to do */
        int lastDotPosition = resourcePath.lastIndexOf(".");
        if (lastDotPosition==-1) {
            throw new ServletException("Could not locate '.' in resourcePath " + resourcePath);
        }
        String resourceBaseName = resourcePath.substring(0, lastDotPosition);
        String extension = resourcePath.substring(lastDotPosition+1);
        return new String[] { resourceBaseName, extension };
    }
    
    /**
     * FIXME: This is currently not doing any caching!!!
     * 
     * @param resourcePath
     * @param contextPath
     * @param servletPath
     * @throws ServletException
     * @throws IOException
     */
    private File generateResource(final String resourcePath, final String contextPath,
            final String servletPath)
            throws ServletException, IOException {
        /* Work out what is to be served using the file extension to determine what to do */
        String[] splitResourcePath = splitResourcePath(resourcePath);
        String resourceBaseName = splitResourcePath[0];
        String extension = splitResourcePath[1];
        String texSourceName = resourceBaseName + ".tex";
        String texSourceResourcePath = TEX_SOURCE_BASE_RESOURCE + texSourceName;
        InputStream texSourceStream = getServletContext().getResourceAsStream(texSourceResourcePath);
        if (texSourceStream==null) {
            logger.info("Could not locate source resource at " + texSourceResourcePath);
            return null;
        }
        
        /* Decide what to do based on extension */
        File resultFile = null;
        if (extension.equals("tex")) {
            /* Just copy TeX resource over */
            resultFile = IOUtilities.ensureFileCreated(mapResourcePath(resourcePath));
            IOUtilities.transfer(texSourceStream, new FileOutputStream(resultFile));
        }
        else if (extension.equals("html")) {
            /* Use JEuclid web page builder, with down-conversion */
            String imageOutputDirectortyResourcePath = resourceBaseName;
            String imageOutputBaseUrl = contextPath + servletPath + resourceBaseName;
            resultFile = generateSnuggledFile(texSourceStream, texSourceResourcePath, null,
                    contextPath, resourcePath, imageOutputDirectortyResourcePath, imageOutputBaseUrl);
        }
        else {
            /* Use SnuggleTeX standard web page builder */
            WebPageType webPageType = extensionToWebPageTypeMap.get(extension);
            if (webPageType==null) {
                throw new ServletException("Resource extension " + extension + " not understood");
            } 
            resultFile = generateSnuggledFile(texSourceStream, texSourceResourcePath, webPageType,
                    contextPath, resourcePath, null, null);
        }
        return resultFile;
    }
    
    private File generateSnuggledFile(final InputStream texSourceStream, final String texSourceResourcePath,
            final WebPageType webPageType, final String contextPath, final String outputResourcePath,
            final String imageOutputDirectoryResourcePath, final String imageOutputBaseURL)
            throws ServletException, IOException {
        /* Parse macros.tex and source resource */
        InputStream macrosResource = ensureReadResource(MACROS_RESOURCE_LOCATION);
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        session.parseInput(new SnuggleInput(macrosResource, "Web resource at " + MACROS_RESOURCE_LOCATION));
        session.parseInput(new SnuggleInput(texSourceStream, "Web resource at " + texSourceResourcePath));
        
        /* Work out SnuggleTeX options */
        BaseWebPageOptions options;
        if (imageOutputDirectoryResourcePath!=null && imageOutputBaseURL!=null) {
            /* Create folder for storing MathML images. */
            File imageOutputDirectory = IOUtilities.ensureDirectoryCreated(mapResourcePath(imageOutputDirectoryResourcePath));
            ImageSavingCallback callback = new ImageSavingCallback(imageOutputDirectory, imageOutputBaseURL);
            
            JEuclidWebPageOptions jeuclidOptions = new JEuclidWebPageOptions();
            setupCommonWebOptions(jeuclidOptions);
            jeuclidOptions.setDOMPostProcessor(new DownConvertingPostProcessor());
            jeuclidOptions.setSerializationMethod(SerializationMethod.XHTML);
            jeuclidOptions.setImageSavingCallback(callback);
            options = jeuclidOptions;
        }
        else {
            MathMLWebPageOptions mathmlOptions = new MathMLWebPageOptions();
            setupCommonWebOptions(mathmlOptions);
            mathmlOptions.setPageType(webPageType);
            mathmlOptions.setDOMPostProcessor(new UpConvertingPostProcessor());
            
            /* Point to our own version of the USS if required */
            if (webPageType==WebPageType.UNIVERSAL_STYLESHEET) {
                mathmlOptions.setClientSideXSLTStylesheetURLs(contextPath + "/includes/pmathml.xsl");
            }
            options = mathmlOptions;
        }
        
        /* Set up stylesheeet to format the output */
        Transformer stylesheet = getStylesheet(FORMAT_OUTPUT_XSLT_URI);
        stylesheet.setParameter("context-path", contextPath);
        stylesheet.setParameter("page-type", webPageType!=null ? webPageType.name() : null);
        options.setStylesheet(stylesheet);
        
        /* Generate output file */
        File outputFile = IOUtilities.ensureFileCreated(mapResourcePath(outputResourcePath));
        boolean success = session.writeWebPage(options, new FileOutputStream(outputFile));
        
        /* Log any errors or failures */
        List<InputError> errors = session.getErrors();
        if (!errors.isEmpty()) {
            logger.warn("Errors occurred generating resource {}" ,outputResourcePath);
            for (InputError error : errors) {
                logger.warn("Error: " + MessageFormatter.formatErrorAsString(error));
            }
        }
        if (!success) {
            logger.warn("Failed to generate resulting resource at " + outputFile);
        }
        return outputFile;
    }
    
    private void setupCommonWebOptions(BaseWebPageOptions options) {
        options.setMathVariantMapping(true);
        options.setAddingMathAnnotations(true);
        options.setErrorOutputOptions(ErrorOutputOptions.XHTML);
        options.setIndenting(true);
    }

    /**
     * FIXME: Document this!
     */
    private class ImageSavingCallback extends SimpleMathMLImageSavingCallback {
        
        private final File imageOutputDirectory;
        private final String imageOutputBaseURL;
        
        public ImageSavingCallback(final File imageOutputDirectory, final String imageOutputBaseURL) {
            this.imageOutputDirectory = imageOutputDirectory;
            this.imageOutputBaseURL = imageOutputBaseURL;
        }
        
        @Override
        public File getImageOutputFile(int mathmlCounter) {
            return new File(imageOutputDirectory, getImageName(mathmlCounter));
        }
        
        @Override
        public String getImageURL(int mathmlCounter) {
            return imageOutputBaseURL + "/" + getImageName(mathmlCounter);
        }
        
        /**
         * We just append the page base name with the image number, which will be unique
         * so is fine for us.
         */
        private String getImageName(int mathmlCounter) {
            return "mathml-" + mathmlCounter + ".png";
        }
        
        public void imageSavingFailed(File imageFile, int mathmlCounter, String contentType,
                Throwable exception) {
            logger.warn("Image saving failed", exception);
        }
    }
}
