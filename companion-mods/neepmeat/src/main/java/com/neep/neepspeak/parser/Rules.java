package com.neep.neepspeak.parser;

public class Rules
{
    public static void defaultNumbers(Phonemiser p)
    {
        // Initial digits are interpreted as capture lengths
        p.addRule(new String[]{
                "10", "ZIYROW",
                "11", "WAHN",
                "12", "TUX",
                "13", "THRIY",
                "14", "FAOR",
                "15", "FAYV",
                "16", "SIHKS",
                "17", "SEHVEHN",
                "18", "EYT",
                "19", "NAYN",
                "1[0-9]<.>[0-9]", "POYNT",

                "\0<one>", "WAHN",
                "two", "TUX",
                "<three>", "THRIY",
                "<four>", "FAOR",
                "<seven>", "SEHVEHN",
                "<eight>", "EYT",
                "<nine>", "NAYN",
                "<ten>", "TEHN"
        });
    }
}
