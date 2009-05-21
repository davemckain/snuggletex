/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.BaseWebPageOptions.SerializationMethod;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.internal.util.IOUtilities;
import uk.ac.ed.ph.snuggletex.jeuclid.JEuclidWebPageOptions;
import uk.ac.ed.ph.snuggletex.jeuclid.SimpleMathMLImageSavingCallback;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple demo servlet that accepts some (displaymath mode) input and serves up an image rendition
 * of the result.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MathInputToImageServlet extends BaseServlet {

    private static final long serialVersionUID = 2349962200011540329L;
    
    private static final Logger logger = LoggerFactory.getLogger(MathInputToImageServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doRequest(request, response);
    }
    
    private void doRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        /* Read in input LaTeX */
        String inputLaTeX = request.getParameter("input");
        if (inputLaTeX==null || inputLaTeX.trim().length()==0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty input");
        }
        
        /* Parse the LaTeX */
        SnuggleEngine engine = new SnuggleEngine(getStylesheetCache());
        SnuggleSession session = engine.createSession();
        SnuggleInput input = new SnuggleInput("\\[ " + inputLaTeX + " \\]", "Form Input");
        session.parseInput(input);
        
        /* We'll save the image to a temporary File */
        final File tempFile = File.createTempFile("mathimage-", "png");
        ImageSavingCallback callback = new ImageSavingCallback(tempFile);
        try {
            /* Set up appropriate web output options */
            JEuclidWebPageOptions options = new JEuclidWebPageOptions();
            options.setSerializationMethod(SerializationMethod.XHTML);
            options.setErrorOutputOptions(ErrorOutputOptions.NO_OUTPUT);
            options.setMathVariantMapping(true);
            options.setAddingMathAnnotations(false);
            options.setIndenting(false);
            options.setImageSavingCallback(callback);
            
            /* Generate web page result, which we'll actually throw away! The important side effect
             * here is that an image should have been saved!
             */
            session.createWebPage(options);

            /* Generate appropriate result */
            List<InputError> errors = session.getErrors();
            if (!errors.isEmpty()) {
                logger.warn("Bad input: {}", inputLaTeX);
                logger.warn("Error count: {}", errors.size());
                for (InputError error : errors) {
                    logger.warn("Error: " + MessageFormatter.formatErrorAsString(error));
                }
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad LaTeX Input");
            }
            else if (callback.getFailure()!=null) {
                logger.warn("Could not generate image for input: " + inputLaTeX, callback.getFailure());
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not generate image for this input");
            }
            else {
                response.setContentType("image/png");
                response.setContentLength((int) tempFile.length());
                IOUtilities.transfer(new FileInputStream(tempFile), response.getOutputStream(), true, false);
            } 
        }
        finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    /**
     * Does the job of the saving the (first) image rendered to the given output file, recording
     * any {@link Throwable} for later retrieval.
     */
    private static class ImageSavingCallback extends SimpleMathMLImageSavingCallback {
        
        private final File outputFile;
        private Throwable failure;
        
        public Throwable getFailure() {
            return failure;
        }

        public ImageSavingCallback(final File outputFile) {
            this.outputFile = outputFile;
            this.failure = null;
        }
        
        @Override
        public File getImageOutputFile(int mathmlCounter) {
            /* Valid input only produces 1 image, so ignore all others */
            return mathmlCounter==0 ? outputFile : null;
        }
        
        @Override
        public OutputStream getImageOutputStream(int mathmlCounter) {
            /* Ignore anything but 1st image */
            return new NullOutputStream();
        }

        @Override
        public String getImageURL(int mathmlCounter) {
            /* Not needed here as we're throwing the resulting XML away */
            return "";
        }

        public void imageSavingFailed(Object imageOutputObject, int mathmlCounter,
                String contentType, Throwable exception) {
            this.failure = exception;
        }
    }
}
