package com.neep.neepspeak.parser;

import com.neep.neepspeak.phoneme.PhonemeSet;

public class DoubleConsonantRule extends ConsonantRule
{
    @Override
    public int apply(PhonemeAppender append, PhonemeSet ps, String word, int i, int c, boolean start)
    {
        if (i + 1 >= word.length())
            return 0;

        var p1 = getConsonantPhoneme(ps, c);
        var p2 = getConsonantPhoneme(ps, word.charAt(i + 1));

        if (p1 != null && p1 == p2)
        {
            Phonemiser.debug(Character.toString(c));
            append.append(p1);
            return 2;
        }

        return 0;
    }
}
