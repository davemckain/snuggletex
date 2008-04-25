/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.testutils;

import org.xml.sax.Attributes;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
interface MockableSAXReceiver {

    void startDocument();
    void skippedEntity(String name);
    void processingInstruction(String target, String data);
    void startElement(String uri, String localName, String qName, Attributes atts);
    void coalescedText(String text);
    void coalescedIgnorableWhitespace(String text);
    void endElement(String uri, String localName, String qName);
    void endDocument();

}
