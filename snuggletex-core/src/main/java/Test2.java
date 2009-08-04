import java.io.StringReader;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class Test2 {
    
    public static void main(String[] args) throws Exception {
        String s = "<?xml version='1.0' encoding='ISO-8859-1'?><root>\u03b1</root>";
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setContentHandler(new DefaultHandler() {
            @Override
            public void characters(char[] ch, int start, int length) {
                System.out.println("Got characters: " + new String(ch, start, length));
            }
        });
        xmlReader.parse(new InputSource(new StringReader(s)));
    }

}
