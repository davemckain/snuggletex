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
import uk.ac.ed.ph.snuggletex.SessionConfiguration;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.SnuggleSnapshot;
import uk.ac.ed.ph.snuggletex.BaseWebPageOptions.SerializationMethod;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.internal.util.IOUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.jeuclid.JEuclidWebPageOptions;
import uk.ac.ed.ph.snuggletex.jeuclid.SimpleMathMLImageSavingCallback;
import uk.ac.ed.ph.snuggletex.upconversion.UpConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

/**
 * Helper class to build the <tt>docs</tt> directory in the built webapp.
 * <p>
 * We now do this offline as it saves wasting processor cycles building everything
 * dynamically and also makes the management of image renditions of MathML much easier.
 * <p>
 * This class looks for each <tt>*.tex</tt> file in the specified (flat) {@link #sourceDirectory}
 * and passes it to SnuggleTeX to generate a number of different web outputs. Each output
 * is identified with a different file extension as follows:
 * <ul>
 *   <li>.xhtml -> {@link WebPageType#MOZILLA}</li>
 *   <li>.htm -> {@link WebPageType#MATHPLAYER_HTML}</li>
 *   <li>.xml -> {@link WebPageType#UNIVERSAL_STYLESHEET}</li>
 *   <li>.cxml -> {@link WebPageType#CROSS_BROWSER_XHTML}</li>
 *   <li>.html -> Legacy XHTML + images outout (created via {@link JEuclidWebPageOptions})</li>
 * </ul>
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class DocumentationBuilder {
    
    static final Logger log = Logger.getLogger(DocumentationBuilder.class.getName());

    /** Directory to process documentation in */
    private final File sourceDirectory;
    
    /** Location of <tt>macros.tex</tt> file */
    private final File macrosFile;
    
    /** Location of the XSLT to generate the final web pages */
    private final File formattingStylesheet;
    
    /** Post-processor which will up-convert the resulting MathML */
    private final UpConvertingPostProcessor upconverter;
    
    /** 
     * Base path for webapp. This can either be hard-coded "context path" (which is not portable)
     * or something relative to the {@link #sourceDirectory}.
     */
    private final String contextPath;
    
    /**
     * Name of directory inside {@link #sourceDirectory} that MathML image renditions will be
     * saved to.
     */
    final String mathMLImageDirectoryName;

    File imageOutputDirectory;
    private Transformer stylesheet;
    private SnuggleSnapshot snapshot;
    
    public DocumentationBuilder(final File sourceDirectory, final File macrosFile,
            final File formattingStylesheet, final String contextPath,
            final String mathMLImageDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.macrosFile = macrosFile;
        this.formattingStylesheet = formattingStylesheet;
        this.contextPath = contextPath;
        this.mathMLImageDirectoryName = mathMLImageDirectory;
        this.upconverter = new UpConvertingPostProcessor();
        
        /* These will be set on first use */
        this.stylesheet = null;
        this.snapshot = null;
        this.imageOutputDirectory = null;
    }
    
    public void run() throws IOException {
        /* Parse the macros and create a snapshot */
        snapshot = createPostMacrosSnapshot();
        
        /* Create image output directory */
        imageOutputDirectory = new File(sourceDirectory, mathMLImageDirectoryName);
        IOUtilities.ensureDirectoryCreated(imageOutputDirectory);
        
        /* Create stylesheet to format the resulting web page */
        try {
            stylesheet = XMLUtilities.createJAXPTransformerFactory().newTransformer(new StreamSource(formattingStylesheet));
            stylesheet.setParameter("context-path", contextPath);
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException("Could not compile stylesheet at " + formattingStylesheet, e);
        }
        
        /* Now process each .tex file in the sourceDirectory */
        FilenameFilter texFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".tex");
            }
        };
        for (File sourceFile : sourceDirectory.listFiles(texFilter)) {
            processFile(sourceFile);
        }
    }
    
    private void processFile(File sourceFile) throws IOException {
        log.info("Processing " + sourceFile);
        
        /* Recreate session based on parsed macros and parse input file */
        SnuggleSession session = snapshot.createSession();
        session.parseInput(new SnuggleInput(sourceFile));
        
        /* Log any errors found as they need fixed by me */
        logNewErrors(session);
        
        /* Get base name of the page we're building */
        String pageBaseName = sourceFile.getName().replaceFirst("\\.tex$", "");
        
        /* Build the normal web outputs */
        Map<WebPageType, String> typeToExtensionMap = new HashMap<WebPageType, String>();
        typeToExtensionMap.put(WebPageType.MOZILLA, ".xhtml");
        typeToExtensionMap.put(WebPageType.MATHPLAYER_HTML, ".htm");
        typeToExtensionMap.put(WebPageType.UNIVERSAL_STYLESHEET, ".xml");
        typeToExtensionMap.put(WebPageType.CROSS_BROWSER_XHTML, ".cxml");
        File targetFile;
        for (Entry<WebPageType, String> entry : typeToExtensionMap.entrySet()) {
            WebPageType pageType = entry.getKey();
            String fileExtension = entry.getValue();

            /* (Create new options each time as they might be altered by the writing process) */
            MathMLWebPageOptions mathOptions = new MathMLWebPageOptions();
            setupWebOptions(mathOptions);
            mathOptions.setPageType(pageType);
            
            /* Pass parameter to sylesheet providing view information */
            stylesheet.setParameter("page-type", pageType.name());
            
            /* Do tweaking if necessary */
            if (pageType==WebPageType.UNIVERSAL_STYLESHEET) {
                /* Point to our own version of the USS */
                mathOptions.setClientSideXSLTStylesheetURLs(contextPath + "/includes/pmathml.xsl");
            }

            targetFile = new File(sourceDirectory, pageBaseName + fileExtension);
            
            session.writeWebPage(mathOptions, new FileOutputStream(targetFile));
            logNewErrors(session);
        }
        
        /* Build compatibility output */
        JEuclidWebPageOptions jeuclidOptions = new JEuclidWebPageOptions();
        setupWebOptions(jeuclidOptions);
        stylesheet.setParameter("page-type", null); /* (Reset as it will have been set earlier) */
        jeuclidOptions.setDOMPostProcessor(new DownConvertingPostProcessor());
        jeuclidOptions.setSerializationMethod(SerializationMethod.XHTML);
        jeuclidOptions.setImageSavingCallback(new ImageSavingCallback(pageBaseName));
        targetFile = new File(sourceDirectory, pageBaseName + ".html");
        session.writeWebPage(jeuclidOptions, new FileOutputStream(targetFile));
    }
    
    /**
     * Trivial callback that saves MathML images into the {@link DocumentationBuilder#imageOutputDirectory}
     * in an obvious way.
     */
    class ImageSavingCallback extends SimpleMathMLImageSavingCallback {
        
        private final String pageBaseName;
        
        public ImageSavingCallback(final String pageBaseName) {
            this.pageBaseName = pageBaseName;
        }
        
        @Override
        public File getImageOutputFile(int mathmlCounter) {
            return new File(imageOutputDirectory, getImageName(mathmlCounter));
        }
        
        @Override
        public String getImageURL(int mathmlCounter) {
            return mathMLImageDirectoryName + "/" + getImageName(mathmlCounter);
        }
        
        /**
         * We just append the page base name with the image number, which will be unique
         * so is fine for us.
         */
        private String getImageName(int mathmlCounter) {
            return pageBaseName + "-" + mathmlCounter + ".png";
        }
        
        public void imageSavingFailed(File imageFile, int mathmlCounter, String contentType,
                Throwable exception) {
            DocumentationBuilder.log.log(Level.WARNING, "Image saving failed", exception);
        }
    }
    
    private void logNewErrors(SnuggleSession session) {
        List<InputError> errors = session.getErrors();
        if (!errors.isEmpty()) {
            for (InputError error : errors) {
                log.warning(MessageFormatter.formatErrorAsString(error));
            }
            errors.clear();
        }
    }
    
    private void setupWebOptions(BaseWebPageOptions options) {
        options.setMathVariantMapping(true);
        options.setAddingMathAnnotations(true);
        options.setStylesheet(stylesheet);
        options.setErrorOutputOptions(ErrorOutputOptions.XHTML);
        options.setIndenting(true);
        options.setDOMPostProcessor(upconverter);
    }
    
    /**
     * Uses SnuggleTeX to parse the {@link #macrosFile} and create a {@link SnuggleSnapshot}
     * of the results. This can be reused for every web request, which saves having to re-parse
     * the macros each time.
     * 
     * @throws IOException 
     */
    private SnuggleSnapshot createPostMacrosSnapshot() throws IOException {
        /* Create engine, read in macros and then create a snapshot to reuse for each request */
        SessionConfiguration configuration = new SessionConfiguration();
        
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession(configuration);
        session.parseInput(new SnuggleInput(macrosFile));
        return session.createSnapshot();
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length!=5) {
            System.err.println("Please supply: sourceDirectory macrosFile formattingStylesheet contextPath mathMLImageDirectoryName");
            System.exit(1);
        }
        File sourceDirectory = new File(args[0]);
        File macrosFile = new File(args[1]);
        File formattingStylesheet = new File(args[2]);
        String contextPath = args[3];
        String mathMLImageDirectoryName = args[4];
        
        DocumentationBuilder builder = new DocumentationBuilder(sourceDirectory, macrosFile, formattingStylesheet, contextPath, mathMLImageDirectoryName);
        builder.run();
    }
}
