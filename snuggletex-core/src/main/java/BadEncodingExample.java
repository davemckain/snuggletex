import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class BadEncodingExample {
    
    public static void main(String[] args) throws Exception {
        /* Create a trivial XML file encoded as UTF-8 containing a single
         * element called "x" with character content consisting of a single
         * Greek beta character. 
         * 
         * (Beta is Unicode U+03B2, which gets encoded in UTF-8 as 2 bytes
         * CE B2.)
         */
        File tempFile = new File("temp.xml");
        try {
            int[] utf8Data = { 0x3c, 0x78, 0x3e, 0xce, 0xb2, 0x3c, 0x2f, 0x78, 0x3e };
            FileOutputStream xmlOutputStream = new FileOutputStream(tempFile);
            for (int b : utf8Data) {
                xmlOutputStream.write(b);
            }
            xmlOutputStream.close();
            
            /* Now read back, using the ISO-8859-1 encoding and see the mess that occurs.
             * 
             * You'd get the same result if you did new InputStreamReader() without the
             * 2nd argument and if the default charset of the machine you're running on
             * was ISO-8859-1.
             * 
             * The 1 argument InputStreamReader() will always bite your bum when dealing
             * with XML so never use it!
             */
            FileInputStream xmlInputStream = new FileInputStream(tempFile);
            InputStreamReader wrongEncodingReader = new InputStreamReader(xmlInputStream, "ISO-8859-1");
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = wrongEncodingReader.read())!=-1) {
                sb.append((char) c);
            }
            System.out.println("Read back :" + sb.toString());
        }
        finally {
            tempFile.delete();
        }
    }
}
