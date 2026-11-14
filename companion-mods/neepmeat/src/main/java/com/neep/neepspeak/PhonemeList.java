package com.neep.neepspeak;

import com.neep.neepspeak.audio.AudioComponent;
import com.neep.neepspeak.mouth.phoneme.Phonemes;
import com.neep.neepspeak.mouth.phoneme.PlayablePhoneme;
import com.neep.neepspeak.util.SampleInfo;

import java.util.List;

public class PhonemeList implements AudioComponent
{
    private final List<PlayablePhoneme> phonemes;

    public PhonemeList(List<PlayablePhoneme> phonemes)
    {
        this.phonemes = phonemes;
    }

    public PlayablePhoneme get(int idx)
    {
        if (idx >= 0 && idx < phonemes.size())
            return phonemes.get(idx);

        return Phonemes.END;
    }

    @Override
    public void init(SampleInfo info)
    {
        phonemes.forEach(p -> p.init(info));
    }
}
