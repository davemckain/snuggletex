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

    /** Directory to process raw documentation from */
    private final File sourceDirectory;
    
    /** Directory in which to output finished documentation files */
    private final File outputDirectory;
    
    /** Location of <tt>macros.tex</tt> file */
    private final File macrosFile;
    
    /** XSLT File used to generate the final web pages */
    private final File formattingStylesheetFile;
    
    /** Post-processor which will up-convert the resulting MathML */
    private final UpConvertingPostProcessor upconverter;
    
    /** Resulting context path for this webapp. This is required to create some internal links. */
    private final String contextPath;
    
    private final Map<WebPageType, String> webPageTypeToExtensionMap;
    
    /**
     * Maximum last modified time for files like {@link #macrosFile} and {@link #formattingStylesheetFile}.
     */
    private final long helperFilesLastModified;
    
    /**
     * Name of directory inside {@link #outputDirectory} that MathML image renditions will be
     * saved to.
     */
    final String mathMLImageDirectoryName;

    File imageOutputDirectory;
    private Transformer stylesheet;
    private SnuggleSnapshot snapshot;
    
    public DocumentationBuilder(final File sourceDirectory, final File outputDirectory,
            final String contextPath, final File macrosFile,
            final File formattingStylesheetFile, final String mathMLImageDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.outputDirectory = outputDirectory;
        this.contextPath = contextPath;
        this.macrosFile = macrosFile;
        this.formattingStylesheetFile = formattingStylesheetFile;
        this.mathMLImageDirectoryName = mathMLImageDirectory;
        this.upconverter = new UpConvertingPostProcessor();
        
        this.helperFilesLastModified = Math.max(macrosFile.lastModified(), formattingStylesheetFile.lastModified());
        
        /* Define web page types */
        this.webPageTypeToExtensionMap = new HashMap<WebPageType, String>();
        webPageTypeToExtensionMap.put(WebPageType.MOZILLA, ".xhtml");
        webPageTypeToExtensionMap.put(WebPageType.MATHPLAYER_HTML, ".htm");
        webPageTypeToExtensionMap.put(WebPageType.UNIVERSAL_STYLESHEET, ".xml");
        webPageTypeToExtensionMap.put(WebPageType.CROSS_BROWSER_XHTML, ".cxml");
        
        /* These will be set on first use */
        this.stylesheet = null;
        this.snapshot = null;
        this.imageOutputDirectory = null;
    }
    
    public void run() throws IOException {
        /* Parse the macros and create a snapshot */
        snapshot = createPostMacrosSnapshot();
        
        /* Create output directories */
        IOUtilities.ensureDirectoryCreated(outputDirectory);
        imageOutputDirectory = new File(outputDirectory, mathMLImageDirectoryName);
        IOUtilities.ensureDirectoryCreated(imageOutputDirectory);
        
        /* Create stylesheet to format the resulting web page */
        try {
            stylesheet = XMLUtilities.createJAXPTransformerFactory().newTransformer(new StreamSource(formattingStylesheetFile));
            stylesheet.setParameter("context-path", contextPath);
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException("Could not compile stylesheet at " + formattingStylesheetFile, e);
        }
        
        /* Now process each .tex file in the sourceDirectory (and also in the outputDirectory
         * since the build process creates some dynamic .tex files that we need to turn into
         * documentation) */
        FilenameFilter texFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".tex");
            }
        };
        for (File sourceFile : sourceDirectory.listFiles(texFilter)) {
            processFile(sourceFile);
        }
        for (File sourceFile : outputDirectory.listFiles(texFilter)) {
            processFile(sourceFile);
        }
    }
    
    private void processFile(File sourceFile) throws IOException {
        /* Recreate session based on parsed macros and parse input file */
        SnuggleSession session = snapshot.createSession();
        session.parseInput(new SnuggleInput(sourceFile));
        
        /* Log any errors found as they need fixed by me */
        logNewErrors(session);
        
        /* Get base name of the page we're building */
        String pageBaseName = sourceFile.getName().replaceFirst("\\.tex$", "");
        
        /* Build the normal web outputs */
        File targetFile;
        for (Entry<WebPageType, String> entry : webPageTypeToExtensionMap.entrySet()) {
            WebPageType pageType = entry.getKey();
            String fileExtension = entry.getValue();
            targetFile = new File(outputDirectory, pageBaseName + fileExtension);
            
            /* Check if we actually need to recreate target. This will be true if any of the
             * following are newer than the targetFile:
             * 
             * sourceFile
             * formattingStylesheetFile
             * macrosFile
             */
            if (!targetFile.exists() || targetFile.lastModified() < Math.max(sourceFile.lastModified(), helperFilesLastModified)) {
                log.info("Generating " + pageType + " output for " + sourceFile);

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

                /* Create target file */
                session.writeWebPage(mathOptions, new FileOutputStream(targetFile));
                logNewErrors(session);
            }
        }
        
        /* Maybe rebuild compatibility output */
        targetFile = new File(outputDirectory, pageBaseName + ".html");
        if (!targetFile.exists() || targetFile.lastModified() < Math.max(sourceFile.lastModified(), helperFilesLastModified)) {
            JEuclidWebPageOptions jeuclidOptions = new JEuclidWebPageOptions();
            setupWebOptions(jeuclidOptions);
            stylesheet.setParameter("page-type", null); /* (Reset as it will have been set earlier) */
            jeuclidOptions.setDOMPostProcessor(new DownConvertingPostProcessor());
            jeuclidOptions.setSerializationMethod(SerializationMethod.XHTML);
            jeuclidOptions.setImageSavingCallback(new ImageSavingCallback(pageBaseName));
            session.writeWebPage(jeuclidOptions, new FileOutputStream(targetFile));
        }
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
        if (args.length!=6) {
            System.err.println("Please supply: sourceDirectory outputDirectory contextPath macrosFile formattingStylesheetFile mathMLImageDirectoryName");
            System.exit(1);
        }
        int i=0;
        File sourceDirectory = new File(args[i++]);
        File outputDirectory = new File(args[i++]);
        String contextPath = args[i++];
        File macrosFile = new File(args[i++]);
        File formattingStylesheetFile = new File(args[i++]);
        String mathMLImageDirectoryName = args[i++];
        new DocumentationBuilder(sourceDirectory, outputDirectory, contextPath, macrosFile,
                formattingStylesheetFile, mathMLImageDirectoryName).run();
    }
}
