package com.neep.neepspeak.parser;

import com.neep.neepspeak.NEEPSpeakException;
import com.neep.neepspeak.mouth.phoneme.PlayablePhoneme;
import com.neep.neepspeak.phoneme.PhonemeSet;

import java.util.ArrayList;
import java.util.List;

public class RuleParser
{
    public static List<RuleBuilder.Match> parsePattern(String rule)
    {
        // Syntax: PREFIX<CAPTURE>SUFFIX
        // If regex is used, the length in characters of the section must be specified in an initial digit:
        // 3e.e<ooo>1.

        int prefixEnd = rule.indexOf('<');
        int matchEnd = rule.lastIndexOf('>');

        String prefix = prefixEnd != -1 ? rule.substring(0, prefixEnd) : "";
        String match = rule.substring(prefixEnd != -1 ? prefixEnd + 1 : 0, matchEnd != -1 ? matchEnd : rule.length());
        String suffix = matchEnd != -1 ? rule.substring(matchEnd + 1) : "";

        return List.of(
                readLength(prefix),
                readLength(match),
                readLength(suffix)
        );
    }

    public static List<PlayablePhoneme> parseSamPhonetic(String phonemes, PhonemeSet phonemeSet) throws NEEPSpeakException
    {
        phonemes = phonemes.toUpperCase();
        List<PlayablePhoneme> playablePhonemes = new ArrayList<>();
        for (int i = 0; i < phonemes.length(); ++i)
        {
            int c0 = phonemes.codePointAt(i);

            if (c0 == ' ')
                continue;

            int c1 = '\0';
            if (i + 1 < phonemes.length())
                c1 = phonemes.charAt(i + 1);

            String s0 = Character.toString(c0);
            String s1 = Character.toString(c1);
            int advance = 2;
            PlayablePhoneme result = phonemeSet.toSound(s0 + s1);
            if (result == null)
            {
                result = phonemeSet.toSound(s0);
                advance = 1;
            }

            i += advance - 1;

            if (result == null)
                throw new NEEPSpeakException("unknown phoneme " + s0 + (c1 != '\0' ? s1 : "") + " in " + phonemes);

            playablePhonemes.add(result);
        }

        return playablePhonemes;
    }

    public static Phonemiser.Rule parseRule(String rule, String phonemes, PhonemeSet phonemeSet)
    {
        List<PlayablePhoneme> playablePhonemes;
        try
        {
            playablePhonemes = parseSamPhonetic(phonemes, phonemeSet);
        }
        catch (NEEPSpeakException e)
        {
            throw new IllegalArgumentException(e);
        }

        List<RuleBuilder.Match> matches = parsePattern(rule);

        return RuleBuilder.build(matches.get(0), matches.get(1), matches.get(2), (a, ps, s, i) ->
                a.append(playablePhonemes));
    }

    public static RuleBuilder.Match readLength(String s)
    {
        if (s.isEmpty())
            return RuleBuilder.Match.EMPTY;

        String matchResult;
        int l;
        if (Character.isDigit(s.codePointAt(0)))
        {
            l = Character.getNumericValue(s.codePointAt(0));
            matchResult = s.substring(1);
        }
        else
        {
            l = s.length();
            matchResult = s;
        }

        return new RuleBuilder.Match(l, matchResult);
    }
}
