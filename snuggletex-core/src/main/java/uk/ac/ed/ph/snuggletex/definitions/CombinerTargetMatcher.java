/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

/**
 * This interface is used to specify which {@link FlowToken}s are valid as combiner targets
 * for {@link Command}s of type {@link CommandType#COMBINER}
 *
 * @author  David McKain
 * @version $Revision$
 */
public interface CombinerTargetMatcher {
    
    boolean isAllowed(FlowToken target);

}
