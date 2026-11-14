package com.neep.neepspeak.parser;

import com.neep.neepspeak.mouth.phoneme.PlayablePhoneme;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public record PhonemeAppender(List<PlayablePhoneme> list)
{
    public void append(PlayablePhoneme p)
    {
        list.add(p);
    }

    public void append(PlayablePhoneme... p)
    {
        list.addAll(Arrays.asList(p));
    }

    public void append(Collection<PlayablePhoneme> p)
    {
        list.addAll(p);
    }
}
