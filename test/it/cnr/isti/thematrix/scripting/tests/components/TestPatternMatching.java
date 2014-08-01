/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.tests.components;

import it.cnr.isti.thematrix.scripting.filtermodule.support.MatchesFilterCondition;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author edoardovacchi
 */
public class TestPatternMatching extends TestCase {
    private final MatchesFilterCondition filter = new MatchesFilterCondition();
    @Test
    public void testMatchStar() {
        if (!filter.apply("STRINGtoMATCH123", "STR*m*123")) fail();
    }
    
    @Test
    public void testMatchStarEmpty() {
        if (!filter.apply("STRINGtoMATCH123", "STR*m*123*")) fail();
    }
    
    
    @Test
    public void testNotMatchStar() {
        if (filter.apply("STRINGtoMATCH123kk", "STR*m*123")) fail();
    }
    
    @Test
    public void testMatchQMark() {
        if (!filter.apply("STRINGtoMATCH123", "STR*t?m*123")) fail();
    }
}
