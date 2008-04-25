/* $Id: DefinitionMapTest.java,v 1.1 2008/04/17 16:45:39 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleTeXEngine;
import uk.ac.ed.ph.snuggletex.SnuggleTeXSession;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.DefinitionMap;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.definitions.TextFlowContext;
import uk.ac.ed.ph.snuggletex.dombuilding.DoNothingHandler;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision: 1.1 $
 */
public class DefinitionMapTest {
    
    @Test
    public void testCustomDefinition() throws Exception {
        /* We'll create a custom built-in command called \\bob that simply does nothing of interest */
        DefinitionMap definitionMap = new DefinitionMap();
        BuiltinCommand bobCommand = definitionMap.addSimpleCommand("bob", Globals.TEXT_MODE_ONLY,
                new DoNothingHandler(), TextFlowContext.ALLOW_INLINE);
        
        SnuggleTeXEngine engine = new SnuggleTeXEngine();
        engine.registerDefinitions(definitionMap);
        
        SnuggleTeXSession session = engine.createSession();
        session.parseInput(new SnuggleInput("\\bob"));

        /* Verify that we got exactly one command token for '\\bob' */
        List<FlowToken> tokens = session.getParsedTokens();
        Assert.assertEquals(1, tokens.size());
        Assert.assertTrue(tokens.get(0).isCommand(bobCommand));
    }
}
