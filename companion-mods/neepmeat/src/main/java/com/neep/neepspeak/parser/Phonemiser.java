package com.neep.neepspeak.parser;

import com.neep.neepspeak.PhonemeList;
import com.neep.neepspeak.mouth.phoneme.Phonemes;
import com.neep.neepspeak.mouth.phoneme.PlayablePhoneme;
import com.neep.neepspeak.phoneme.PhonemeSet;

import java.util.ArrayList;
import java.util.List;

import static com.neep.neepspeak.parser.RuleBuilder.start;

public class Phonemiser implements Phonemes
{
    public static final String ANY_CONSONANT = "[bcdfghjklmnpqrstvwxz]"; // Hmm
    public static final String ANY_VOWEL = "[aeiouy]";
    public static final String ANY_FRONT_VOWEL = "[eiy]";
    public static final String ANY_LIQUID = "[rl]";
    public static final String NOT_VOWEL_PRE = "(\0|[^aeiouy])";
    public static final String NOT_VOWEL_SUF = "(\0|[^aeiouy])";
    public static final String END_OR_PLURAL = "(\0|s\0)";

    private final List<Rule> rules = new ArrayList<>();

    private final PhonemeSet ps;

    public static void debug(String string)
    {
//        System.out.println(string);
    }

    public Phonemiser(PhonemeSet phonemeSet)
    {
        this.ps = phonemeSet;
    }

    public <T extends Rule> void addRule(T t)
    {
        rules.add(t);
    }

    public void addRule(String rule, String phonemes)
    {
        addRule(RuleParser.parseRule(rule, phonemes, ps));
    }

    /// Rule syntax:
    /// PREFIX<CONSUME>SUFFIX
    /// CONSUME represents the characters that the parser will advance over
    /// All expressions support regex
    /// Use \0 to match the beginnings and ends of words rather than anchors
    /// Prepend the number of characters that will be matched if using regex syntax
    /// Expressions that match a variable number of characters are not allowed
    public void addRule(String[] rules)
    {
        if (rules.length % 2 != 0)
            throw new IllegalArgumentException("rule array must consist of rule and phoneme pairs");

        for (int i = 0; i < rules.length / 2; ++i)
        {
            addRule(rules[2 * i], rules[2 * i + 1]);
        }
    }

    public PhonemeList process(String input)
    {
//        input = "tables and chairs"; // chairs becomes tSEirs
//        input = "my brain is having thinky thoughts";
//        input = "lye"; // fix
//        input = "emergency  emergency";
//        input = "pressure";
//        input = "pleasure"; // maybe fix
//        input = "push";
//        input = "want";
//        input = "soften";
//        input = "mind boss dog";
//        input = "this is a test, this is only a test";
//        input = "good afternoon radio audience";
//        input = "cute";
//        input = "amplified";
//        input = "get on board join the troop";
//        input = "double engine each for you";
//        input = "maximum velocity";
//        input = "nuclear confusion";
//        input = "died";
//        input = "inconvenient";
//        input = "woman";
//        input = "sheep";
//        input = "exterminate";
//        input = "whole";
//        input = "you stink";
//        input = "I met a traveller in an antique land";
//        input = "who said two vast and trunkless legs of stone";
//        input = "one two three four five six seven eight";
//        input = "uwu owo";
//        input = "I'm the operator with my pocket calculator";
//        input = "ally";
//        input = "ores";

        String[] words = input.split(" ");
        List<PlayablePhoneme> phonemes = new ArrayList<>();
        PhonemeAppender appender = new PhonemeAppender(phonemes);
        for (var word : words)
        {
            word = "\0" + word.toLowerCase() + "\0";

            for (int i = 0; i < word.length(); i++)
            {
                boolean start = i == 0;
                int c = word.codePointAt(i);

                if (c == '\0')
                    continue;

//                String charString = Character.toString(c);
//                System.out.println("matching " + charString);
                for (var rule : rules)
                {
                    int advance = rule.apply(appender, ps, word, i, c, start);
                    if (advance > 0)
                    {
                        i += advance - 1;
                        break;
                    }
                }
            }

            appender.append(ps.SPACE);

            debug("");
        }

        phonemes.add(END);

        debug(phonemes.toString());

        return new PhonemeList(phonemes);
    }

    public void setupDefaultRules()
    {
        // Special rule for 'table' as it's pronounced different to 'raffle'
        addRule("<ta>ble", "TEY");

        // Not sure how to distinguish between D and T, so special rule for the most obvious
        addRule(start(ps).prefix("\0").match("th").suffix("(is|ey|em|ese|at|ose|ou|ee|y|ine|an|ere|ence|ither|ough|us)\0")
                .phonemes("DH").build());

        addRule("\0<my>\0", "MAY");
        addRule("<get>\0", "GEHT");
        addRule("<fu>", "FIYUX");
        addRule("<4(sion|tion)>", "SHAHN");
        addRule("<nu>clear", "NIYUX");
        addRule("<ied>\0", "AEIYD");

        addRule(new String[]{
                "\0<i>\0", "AY",
                "\0<i'm>", "AYM",
                "<air>", "EHR",
                "\0<the>\0", "DHAH",
                "<wom>1[ae]", "WUHM",
                "<air>", "EHR",
                "<board>", "BAARD",
                "<hav>1[ei]", "HAEV",
                "<you>", "YUX",
                "<i>nate", "IH",
                "<i>fied", "IH",
                "\0<cu>2t" + ANY_VOWEL, "KIYUW",
                "<ie>nce", "IY",
                "<who>", "HUX",
                "<ever>", "EHVR",
                "<ghost>", "GOHST",
                "2[wt]h<ere>", "EHR",
                "\0h<ere>", "IHR",
                "<said>", "SEHD",
                "\0d<ou>", "AH",
                "g<i>n", "IH",
                "<max>", "MAHKS",
                "<enien>2[tc]", "IYNIYEHN",
                "<con>", "KAAN",
                "<ocity>", "AASIHTIY",
                "<ique>", "IYK",

                "<uwu>", "UWWUW",
                "<owo>", "OWWOW",

                "<blood>", "BLAHD",
                "<ally>", "AELAY",
                "<above>", "AHBAHV",

                "<ore>2" + END_OR_PLURAL, "AOR",
                "<ivity>", "IHVIHTIY",
        });

        Rules.defaultNumbers(this);

        // Rules from https://www.zompist.com/spell.html
        addRule(new String[]{
                "<ch>", "CH",
                "<sh>", "SH",
                "<ph>", "F",
                "<th>", "TH",
                "<qu>", "KW",
                "<wr>", "R",
                "<wh>o", "H",
                "<wh>", "W",
                "<xh>", "X",
                "<rh>", "R",
                "e<x>1" + ANY_VOWEL, "GZ",
                "<x>1", "KS",
        });

        // GH
        addRule("<gh>1" + ANY_VOWEL, "G");
        addRule(start(ps).prefix(1, NOT_VOWEL_PRE)
                .match(3, ANY_VOWEL + "gh").phonemes(VowelUtil::longVowel).build());
        addRule("<gh>", "");

        addRule("<5[ao]ught>\0", "AOT");
        addRule("<ough>", "OW");

        addRule(new String[]{
                "<kn>", "N",
                "<gn>", "N",
                "<mn>", "N",
                "<pt>", "T",
                "<ps>", "S",
                "<tm>", "M",
        });

        // Ai at end of short words (using 3 letters as a heuristic for monosyllabic)
        addRule(start(ps).prefix(3, "\0..").match("y").phonemes("EY").build());

        addRule("<tm>", "M");
        addRule(start(ps).match("ey").phonemes("IY").build());
        addRule(start(ps).match("oy").phonemes("OY").build());
        addRule(start(ps).match("ay").phonemes("EY").build());

        // TODO: replace instances of y that are not adjacent to vowels with i
        addRule(start(ps).prefix(1, NOT_VOWEL_PRE).match("y").suffix(1, NOT_VOWEL_SUF).phonemes("IY").build());

        addRule(start(ps).match("stl").vowelSuffix().phonemes("SL").build());

        addRule(start(ps).match(2, "[ct]i").vowelSuffix().phonemes("SL").build());
        // The Rules say that tu -> chu, I don't say 'choob' and 'chuna'
        addRule(start(ps).match("tu").vowelSuffix().phonemes("TUW").build());
        addRule(start(ps).match("tu").suffix(1, ANY_LIQUID).phonemes("TUW").build());

        // Passion, vision
        addRule(start(ps).match("si").suffix("o").phonemes("CH").build());
        addRule(start(ps).match("ssi").suffix("o").phonemes("CH").build());

        // Assure, leisure
        addRule(start(ps).match("s").suffix("ur").phonemes("ZH").build());
        addRule(start(ps).match("ss").suffix("ur").phonemes("ZH").build());
        // tODO: after k (including substituted from x)

        // 17. Voiced s (but not after a) prison, amuse, (base, parasite)
        addRule(start(ps).match("s").prefix(1, "[eiouy]").vowelSuffix().phonemes("Z").build());

        // 18-19. al
        addRule(start(ps).match("al").suffix(1, "[rsm]").phonemes("AOL").build());
        addRule(start(ps).match("al").suffix(1, "l\0").phonemes("AOL").build());
        addRule(start(ps).match("alk").prefix(1, "[^\0]").phonemes("AOK").build());

        // 22. changeable
        addRule(start(ps).match("ge").prefix(1, "[^\0]").suffix(1, "[oa]")
                .phonemes("J").build());

        // 20. softening of velars
        addRule(start(ps).match("c").suffix(1, ANY_FRONT_VOWEL).phonemes("S").build());
        addRule(start(ps).match("c").suffix(1, ANY_VOWEL).phonemes("K").build());
        addRule(start(ps).match("g").suffix(1, ANY_FRONT_VOWEL).phonemes("J").build());
        addRule(start(ps).match("g").suffix(1, ANY_VOWEL).phonemes("G").build());

        // 23.
        addRule(start(ps).match(2, "gu").prefix("\0").phonemes("G").build());
        addRule(start(ps).match(3, "gue\0").phonemes("G").build());

        // 24. reverse written final liquids
        addRule(start(ps).match(2, "le").prefix(1, ANY_CONSONANT).suffix(2, END_OR_PLURAL).phonemes("AHL").build());
        addRule(start(ps).match(2, "re").prefix(1, ANY_CONSONANT).suffix(2, END_OR_PLURAL).phonemes("AHR").build());

        // 29.
        addRule(start(ps).match("ind").endWordPlural().phonemes("AYND").build()); // mind
        addRule(start(ps).match("o").suffix(2, "f" + ANY_CONSONANT).phonemes("AA").build()); // offer, soften
        addRule(start(ps).match("wa").suffix(2, "([tdns]|th)").phonemes("WAA").build()); // want
        addRule(start(ps).match("u").suffix(3, "([l]|sh|tch)").phonemes("UH").build()); // push

        // 47. this should be applied to phonemes after conversion
        addRule(start(ps).match(2, "[eiu]r").phonemes("R").build());

        // 26. short vowels before double consonants
        addRule(start(ps).match(1, ANY_VOWEL).suffix(2, ANY_CONSONANT + "{2}")
                .phonemes(VowelUtil::shortVowel).build());
        // short vowels before final consonant
        addRule(start(ps).match(1, ANY_VOWEL).suffix(2, ANY_CONSONANT + "\0")
                .phonemes(VowelUtil::shortVowel).build());
        // 25. long vowels before intervocalic consonants
        addRule(start(ps).match(1, ANY_VOWEL).suffix(3, NOT_VOWEL_SUF + ANY_VOWEL)
                .phonemes(VowelUtil::longVowel).build());

        // 27.
//        addRule(RuleBuilder.start().match(1, ANY_VOWEL).prefix(1, ANY_CONSONANT).phonemes(GhAfterVowelRule::longVowel).build());

        // 32. remove silent e
        addRule(start(ps).match("e").suffix("\0").phonemes().build());

        // 35. iVowel in initial syllable
        addRule(start(ps).match(2, "i[aeiou]").prefix(2, "\0.").phonemes("AYAH").build());
        addRule(start(ps).match(2, "i[aeiou]").prefix(3, "\0..").phonemes("AYAH").build());
        // 36.
        addRule(start(ps).match(2, "ow\0").phonemes("OW").build());
        addRule("<oo>k", "UH");
        addRule("s<ei>", "IY");
        addRule("<ie>\0", "AY");
        addRule("<oul>d", "UH");
        // 37
        addRule(start(ps).match("eau").phonemes("OW").build()); // beau
        addRule("<ai>", "EY"); // brain
        addRule("<2aw|au>", "AO"); // auto, awful
        addRule("<2ee|ea>", "IY"); // eeeeeee
        addRule(start(ps).match("eo").phonemes("IYAH").build());
        addRule(start(ps).match(2, "eu|ew").phonemes("IYUW").build()); // eutrophication, eww
        addRule(start(ps).match("ei").phonemes("IY").build()); //
        addRule(start(ps).match(2, "oa|oe").phonemes("OW").build()); // boat
        addRule(start(ps).match("oo").phonemes("UX").build()); // zoo
        addRule(start(ps).match(2, "ou|aw").phonemes("AW").build()); // out, away
        addRule(start(ps).match(2, "oi").phonemes("OY").build()); // oi there
        addRule(start(ps).match("ua").phonemes("IYUW").build()); // usual
        addRule(start(ps).match(2, "ue|ui").phonemes("IYUW").build()); // value

        addRule("\0<y>", "Y");

        // 40. suffices
        addRule(start(ps).match("[ai]ble").prefix(1, "[^\0]").endWord().phonemes("AHBL").build());
        addRule(start(ps).match("lion").prefix(1, ".").endWord().phonemes("LAYAHN").build());
        addRule(start(ps).match("nion").prefix(1, ".").endWord().phonemes("NAYAHN").build());
        // 41. final unpronounceable
        addRule(start(ps).match(1, "[bn]").suffix("\0").prefix(1, "m").phonemes().build());

        // 42. final vowel colouration
//        addRule(start().match(1, "a").prefix("\0").suffix("\0").phonemes(_E, _i).build());
        addRule(start(ps).match(1, "a").endWord().phonemes("AH").build());
        addRule(start(ps).match(1, "e").endWord().phonemes("AH").build());
        addRule(start(ps).match(1, "i").endWord().phonemes("IY").build());
        addRule(start(ps).match(1, "o").endWord().phonemes("OW").build());
        addRule(start(ps).match(1, "u").endWord().phonemes("UX").build());

        addRule(start(ps).match("io").phonemes("IYOW").build()); // radio

        // 44
        addRule(start(ps).match("wor").vowelSuffix().phonemes("WUXR").build());
        addRule(start(ps).match("wor").phonemes("WAO").build());

        addRule(new DoubleConsonantRule());
        addRule(new ConsonantRule());
    }

    public PhonemeSet getPhonemeSet()
    {
        return ps;
    }

    public interface Rule
    {
        int apply(PhonemeAppender append, PhonemeSet ps, String word, int i, int c, boolean start);
    }
}
