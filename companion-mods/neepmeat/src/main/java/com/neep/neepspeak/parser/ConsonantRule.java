package com.neep.neepspeak.parser;

import com.neep.neepspeak.mouth.phoneme.Phonemes;
import com.neep.neepspeak.mouth.phoneme.PlayablePhoneme;
import com.neep.neepspeak.phoneme.PhonemeSet;

public class ConsonantRule implements Phonemiser.Rule, Phonemes
{
    @Override
    public int apply(PhonemeAppender append, PhonemeSet ps, String word, int i, int c, boolean start)
    {
        var p = getConsonantPhoneme(ps, c);
        if (p != null)
        {
            Phonemiser.debug(Character.toString(c));
            append.append(p);
            return 1;
        }

        return 0;
    }

    public static PlayablePhoneme getConsonantPhoneme(PhonemeSet ps, int c)
    {
        return switch (c)
        {
            case 'b' -> ps._b;
            case 'c' -> ps._k;
            case 'd' -> ps._d;
            case 'f' -> ps._f;
            case 'g' -> ps._g;
            case 'h' -> ps._h;
            case 'j' -> ps._dZ;
            case 'k' -> ps._k;
            case 'l' -> ps._l;
            case 'm' -> ps._m;
            case 'n' -> ps._n;
            case 'p' -> ps._p;
            case 'r' -> ps._r$;
            case 's' -> ps._s;
            case 't' -> ps._t;
            case 'v' -> ps._v;
            case 'w' -> ps._w;
            case 'z' -> ps._z;
            default -> null;
        };
    }
}
