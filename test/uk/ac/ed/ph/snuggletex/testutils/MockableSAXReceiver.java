/* $Id: MockableSAXReceiver.java,v 1.2 2008/03/28 16:41:52 dmckain Exp $
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
 * @version $Revision: 1.2 $
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
