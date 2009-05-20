/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
@Deprecated
public class DocumentationFileManager {
    
    private final Logger logger = LoggerFactory.getLogger(DocumentationFileManager.class);
    
    private File baseDirectory;
    
    public void init() throws ServletException {
        if (baseDirectory==null) {
            try {
                baseDirectory = File.createTempFile("snuggetex", "dir");
            }
            catch (IOException e) {
                throw new ServletException("Could not create initial tempfile for storing documentation", e);
            }
            if (!baseDirectory.mkdir()) {
                throw new ServletException("Could not create base directory for storing documentation");
            }
            logger.info("Set base directory for documentation as {0}", baseDirectory);
        }
    }
    
    private File mapResourcePath(final String resourcePath) {
        return new File(baseDirectory + File.separator + resourcePath.replace("/", File.separator));
    }
    
    public File getResource(final String resourcePath) {
        File targetFile = mapResourcePath(resourcePath);
        return targetFile.exists() ? targetFile : null;
    }
    
}
