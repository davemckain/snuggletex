/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.testutil;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.utilities.ClassPathURIResolver;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import com.thaiopensource.resolver.Identifier;
import com.thaiopensource.resolver.Input;
import com.thaiopensource.resolver.Resolver;

/**
 * Implementation of Jing's {@link Resolver} that works in a similar fashion to
 * {@link ClassPathURIResolver}.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class ClassPathResolver implements Resolver {
    
    public static final String URI_SCHEME = "classpath";

    public void open(Input input) {
        input.setByteStream(uriToResource(parseUri(input.getUri())));
    }

    public void resolve(Identifier id, Input input) {
        URI baseUri = parseUri(id.getBase());
        URI resolved = baseUri.resolve(id.getUriReference());
        input.setUri(resolved.toString());
        input.setByteStream(uriToResource(resolved));
    }
    
    private URI parseUri(String uriString) {
        try {
            return new URI(uriString);
        }
        catch (URISyntaxException e) {
            throw new SnuggleLogicException("Unexpected bad URI " + uriString);
        }
    }
    
    private InputStream uriToResource(URI uri) {
        if (!URI_SCHEME.equals(uri.getScheme())) {
            throw new SnuggleLogicException("Only expect " + URI_SCHEME + ": URIs here");
        }
        /* Strip off the leading '/' from the path */
        String resourceLocation = uri.getPath().substring(1);
        InputStream result = getClass().getClassLoader().getResourceAsStream(resourceLocation);
        if (result==null) {
            throw new SnuggleLogicException("Could not located ClassPath resource at " + resourceLocation);
        }
        return result;
    }
}