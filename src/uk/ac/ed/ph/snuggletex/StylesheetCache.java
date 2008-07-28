/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import javax.xml.transform.Templates;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public interface StylesheetCache {
    
    Templates getStylesheet(String resourceName);
    
    void putStylesheet(String resourceName, Templates stylesheet);

}
