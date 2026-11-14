package com.neep.neepspeak.parser;

import com.neep.neepspeak.NEEPSpeakException;
import com.neep.neepspeak.mouth.phoneme.PlayablePhoneme;
import com.neep.neepspeak.phoneme.PhonemeSet;

import java.util.regex.Pattern;

public class RuleBuilder
{
    private final PhonemeSet ps;

    private Match prefix;
    private Match match;
    private Match suffix;

    private Apply action;

    private RuleBuilder(PhonemeSet ps)
    {
        this.ps = ps;
    }

    public static RuleBuilder start(PhonemeSet ps)
    {
        return new RuleBuilder(ps);
    }

    public RuleBuilder prefix(int length, String pattern)
    {
        this.prefix = new Match(length, pattern);
        return this;
    }

    public RuleBuilder prefix(String pattern)
    {
        return prefix(pattern.length(), pattern);
    }

    public RuleBuilder suffix(int length, String pattern)
    {
        this.suffix = new Match(length, pattern);
        return this;
    }

    public RuleBuilder vowelSuffix()
    {
        suffix(1, Phonemiser.ANY_VOWEL);
        return this;
    }

    public RuleBuilder endWord()
    {
        suffix(1, "\0");
        return this;
    }

    public RuleBuilder endWordPlural()
    {
        suffix(2, Phonemiser.END_OR_PLURAL);
        return this;
    }

    public RuleBuilder suffix(String pattern)
    {
        return suffix(pattern.length(), pattern);
    }

    public RuleBuilder match(int length, String pattern)
    {
        this.match = new Match(length, pattern);

//        if (consume == -1)
//            consume = length;

        return this;
    }

    public RuleBuilder match(String pattern)
    {
        return match(pattern.length(), pattern);
    }

    public RuleBuilder phonemes(PlayablePhoneme... phonemes)
    {
        this.action = (a, ps, s, i) -> a.append(phonemes);
        return this;
    }

    public RuleBuilder phonemes(String phonemes)
    {
        try
        {
            var parsed = RuleParser.parseSamPhonetic(phonemes, ps);
            this.action = (a, ps, s, i) ->
            {
                a.append(parsed);
            };
        }
        catch (NEEPSpeakException e)
        {
            throw new IllegalArgumentException(e);
        }
        return this;
    }

    public RuleBuilder phonemes(Apply action)
    {
        this.action = action;
        return this;
    }

//    public RuleBuilder consume(int consume)
//    {
//        this.consume = consume;
//        return this;
//    }

    public static Phonemiser.Rule build(Match prefix, Match match, Match suffix, Apply action)
    {
        if (action == null || match == null)
            throw new IllegalStateException("No action specified");

        String regex = "^"
                + (prefix != null ? prefix.regex : "")
                + match.regex
                + (suffix != null ? suffix.regex : "")
                ;

        Pattern pattern = Pattern.compile(regex);
        Pattern matchPattern = Pattern.compile("^" + match.regex); // Anchor this to the beginning

        return new StandardRule(
                prefix != null ? prefix.length : 0,
                match.length,
                suffix != null ? suffix.length : 0,
                pattern,
                matchPattern,
                action);
    }

    public Phonemiser.Rule build()
    {
        return build(prefix, match, suffix, action);
    }

    public record StandardRule(
            int prefixLength,
            int matchLength,
            int suffixLength,
            Pattern pattern,
            Pattern match,
            Apply action
    ) implements Phonemiser.Rule
    {
        @Override
        public int apply(PhonemeAppender append, PhonemeSet ps, String word, int i, int c, boolean start)
        {
            if (i - prefixLength < 0 || i + matchLength > word.length())
            {
                return 0;
            }

            // The suffix can be omitted. This allows patterns that match both a word end and a character.
            // If a suffix is required but isn't present, the regex will fail.
            int substringEnd = Math.min(word.length(), i + matchLength + suffixLength);

            // Get a string that starts at the beginning of the prefix and ends at the end of the suffix
            String substring = word.substring(i - prefixLength, substringEnd);

            if (pattern.asPredicate().test(substring))
            {
                String matchString = word.substring(i, i + matchLength);
                if (match.asPredicate().test(matchString))
                {
                    Phonemiser.debug(pattern.pattern() + " - " + substring + " " + Character.toString(c));
                    action.apply(append, ps, word, i);
                    return matchLength;
                }
            }

            return 0;
        }
    }

    public record Match(int length, String regex)
    {
        public static final Match EMPTY = new Match(0, "");

    }

    public interface Apply
    {
        void apply(PhonemeAppender a, PhonemeSet ps, String s, int i);
    }
}
