package com.neep.neepspeak.parser;

import com.neep.neepspeak.phoneme.PhonemeSet;

public class VowelUtil
{
    public static void longVowel(PhonemeAppender append, PhonemeSet ps, String s, int i)
    {
        int c = s.codePointAt(i);
        switch (c)
        {
            case 'a' -> append.append(ps._E, ps._i);
            case 'e' -> append.append(ps._i);
            case 'i' -> append.append(ps._A, ps._i);
            case 'o' -> append.append(ps._V, ps._U);
            case 'u' -> append.append(ps._U);
        }
    }

    public static void shortVowel(PhonemeAppender append, PhonemeSet ps, String s, int i)
    {
        int c = s.codePointAt(i);
        switch (c)
        {
            case 'a' -> append.append(ps._a);
            case 'e' -> append.append(ps._E);
            case 'i' -> append.append(ps._I);
            case 'o' -> append.append(ps._O);
            case 'u' -> append.append(ps._at_1);
        }
    }
}
