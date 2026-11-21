package com.neep.neepspeech.parser;

import com.neep.neepspeak.parser.RuleBuilder;
import com.neep.neepspeak.parser.RuleParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRuleParser
{
    @Test
    public void test_parsePattern()
    {
        String rule = "1[abc]<wom>[cde]";
        var res = RuleParser.parsePattern(rule);
        assertEquals(new RuleBuilder.Match(1, "[abc]"), res.get(0));
        assertEquals(new RuleBuilder.Match(3, "wom"), res.get(1));
        assertEquals(new RuleBuilder.Match(5, "[cde]"), res.get(2));

        rule = "1[abc]<2wo[mu]>1[cde]";
        res = RuleParser.parsePattern(rule);
        assertEquals(new RuleBuilder.Match(1, "[abc]"), res.get(0));
        assertEquals(new RuleBuilder.Match(2, "wo[mu]"), res.get(1));
        assertEquals(new RuleBuilder.Match(1, "[cde]"), res.get(2));

        rule = "2wo[mu]";
        res = RuleParser.parsePattern(rule);
        assertEquals(RuleBuilder.Match.EMPTY, res.get(0));
        assertEquals(new RuleBuilder.Match(2, "wo[mu]"), res.get(1));
        assertEquals(RuleBuilder.Match.EMPTY, res.get(2));

        rule = "<wom>1[ae]";
        res = RuleParser.parsePattern(rule);
        assertEquals(RuleBuilder.Match.EMPTY, res.get(0));
        assertEquals(new RuleBuilder.Match(3, "wom"), res.get(1));
        assertEquals(new RuleBuilder.Match(1, "[ae]"), res.get(2));
    }
}
