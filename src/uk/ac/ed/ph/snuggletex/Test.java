/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.DOMBuilderOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.extensions.jeuclid.JEuclidWebPageBuilderOptions;
import uk.ac.ed.ph.snuggletex.extensions.jeuclid.JEuclidWebPageBuilderOptions.ImageSaver;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public class Test {
	
//	public static void main(String[] args) throws Exception {
//		SnuggleTeXEngine engine = new SnuggleTeXEngine();
//		SnuggleTeXSession session = engine.createSession();
//		session.parseInput(new SnuggleInput("$$1+x=3$$"));
//      
//		DOMBuilderOptions options = new DOMBuilderOptions();
//		options.setDownConverting(true);
//		options.setInliningCSS(false);
//		
//		Document d = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
//		Element e = d.createElement("bob");
//		d.appendChild(e);
//		
//		e.appendChild(d.createTextNode("Text?"));
//		
//		session.buildDOMSubtree(e, options);
//		
////		System.out.println(session.buildXMLString(options));
//		System.out.println(XMLUtilities.serializeXMLFragment(d));
//	}
    
//    public static void main(String[] args) throws Exception {
//        SnuggleTeXEngine engine = new SnuggleTeXEngine();
//        SnuggleTeXSession session = engine.createSession();
//        session.parseInput(new SnuggleInput(new File("temp/aardvark.tex")));
//        session.parseInput(new SnuggleInput(new File("temp/p1a.tex")));
//        session.parseInput(new SnuggleInput(new File("temp/maps.tex")));
//        
//        for (InputError error : session.getErrors()) {
//            System.out.println("Error raw: " + ObjectDumper.dumpObject(error, DumpMode.DEEP));
//            System.out.println("Error: " + MessageFormatter.formatErrorAsString(error));
//        }
        
//        System.out.println("Tokens are " + ObjectDumper.dumpObject(session.getParsedTokens(), DumpMode.DEEP));
        
//        String out = session.buildXMLString(new DOMBuilderOptions());
//        System.out.println("XML is " + out);
//    }
    
    public static void main(String[] args) throws Exception {
        SnuggleTeXEngine engine = new SnuggleTeXEngine();
        SnuggleTeXSession session = engine.createSession();
        session.parseInput(new SnuggleInput("Hello!\\par $1+1=2$ and $\frac{x}{y}=2$"));
        
        JEuclidWebPageBuilderOptions options = new JEuclidWebPageBuilderOptions();
        options.setTitle("My Page");
        options.setAddingTitleHeading(true);
        options.setDownConverting(true);
        options.setImageSaver(new ImageSaver() {
            public File getImageOutputFile(int mathmlCounter) {
                return new File("image.png");
            }
            
            public String getImageURL(int mathmlCounter) {
                return "image.png";
            }
            
        });
        options.setErrorOptions(ErrorOutputOptions.XHTML);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        session.writeWebPage(options, outputStream);
        
        System.out.println(new String(outputStream.toByteArray()));
    }
    
//    public static void main(String[] args) throws Exception {
//        SnuggleTeXEngine engine = new SnuggleTeXEngine();
//        SnuggleTeXSession session = engine.createSession();
//        session.parseInput(new SnuggleInput("$$1"));
//        
//        System.out.println("Errors were: " + session.getErrors());
//        System.out.println("Tokens are " + session.getParsedTokens());
//        
//        WebPageBuilderOptions options = new WebPageBuilderOptions();
//        options.setErrorOptions(ErrorOutputOptions.XHTML);
//        options.setPageType(WebPageType.CROSS_BROWSER_XHTML);
//        options.setMathMLPrefix("m");
//        
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        session.writeWebPage(options, outputStream);
//        System.out.println(new String(outputStream.toByteArray()));
//    }
}
