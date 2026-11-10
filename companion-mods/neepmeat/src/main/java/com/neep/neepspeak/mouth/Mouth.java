package com.neep.neepspeak.mouth;

// This is the synthesiser part.
// It will take a phoneme list (and possibly a prosody map)

// Each phoneme type has instructions for:
// - Carrier waveform type
// - Amplitude envelope (time dependant)
// - Pitch envelope (time dependant)
// - Filter frequency and resonance(and number of filters)

// pitchMod[n] = intonation[n] + phoneme.pitch[n]

// Should consonants be treated the same as vowels, or should they modulate the vowel signal?

import com.neep.neepspeak.PhonemeList;
import com.neep.neepspeak.audio.AudioComponent;
import com.neep.neepspeak.mouth.phoneme.DiphthongPhoneme;
import com.neep.neepspeak.mouth.phoneme.Phonemes;
import com.neep.neepspeak.mouth.phoneme.PlayablePhoneme;
import com.neep.neepspeak.util.SampleBuffer;
import com.neep.neepspeak.util.SampleInfo;

public class Mouth implements AudioComponent
{
    private final PhonemeList list;

    private PlayingPhoneme currentPhoneme;
    private PlayingPhoneme nextPhoneme;

    int idx = 0;
    private SampleInfo info;
    private int crossfadeN;
    private SampleBuffer crossfadeBuffer;

    public Mouth(PhonemeList list)
    {
        this.list = list;
        this.currentPhoneme = new PlayingPhoneme(list.get(0));
        if (currentPhoneme.phoneme instanceof DiphthongPhoneme diphthong) // Very naughty and bad
        {
            currentPhoneme = new PlayingPhoneme(diphthong.getP1());
            nextPhoneme = new PlayingPhoneme(diphthong.getP2());
            idx--;
        }
        else
        {
            this.nextPhoneme = new PlayingPhoneme(list.get(1));
        }
    }

    public boolean isDone()
    {
        return currentPhoneme != null && currentPhoneme.phoneme == Phonemes.END;
    }

    public void processBuffer(SampleBuffer sb)
    {
        int samplesRemaining = sb.regionSize();

        // TODO: stop hardcoding
        // crossfadeN should reach this value just before the phoneme advances
        int crossfadeLength = 1500;

        while (samplesRemaining > 0)
        {
            // Each iteration plays one phoneme and optionally part of the following one

            if (currentPhoneme == null)
            {
                idx++;
                currentPhoneme = nextPhoneme;
                if (currentPhoneme.phoneme instanceof DiphthongPhoneme diphthong) // Very naughty and bad
                {
                    currentPhoneme = new PlayingPhoneme(diphthong.getP1());
                    nextPhoneme = new PlayingPhoneme(diphthong.getP2());
                    idx--;
                }
                else
                {
                    nextPhoneme = new PlayingPhoneme(list.get(idx + 1));
                }

                crossfadeN = 0;
            }

            if (currentPhoneme.phoneme == Phonemes.END)
                break;

            // This algorithm fills the buffer in chunks, limited by the frame size.
            // If a phoneme ends mid-frame, the chunk ends up shorter than the frame.
            // The rest of the frame is filled by the next phoneme in the next loop.

            int lengthN = (int) (currentPhoneme.length() * info.fs());
            int phonemeRemainingN = lengthN - currentPhoneme.clockN;

            // Value of phoneme clock at which the crossfade will start
            int crossfadeStartN = (int) (lengthN - crossfadeLength);

            int samplesThisTime = Math.min(samplesRemaining, phonemeRemainingN);
            int chunkStart = sb.endSample - samplesRemaining;

            // Distance from the end of the chunk to the crossfade start
            int cross = currentPhoneme.clockN + samplesThisTime - crossfadeStartN;

            // Distance from the end of the chunk to the crossfade start or the start of the chunk
            int cross1 = Math.min(cross, samplesThisTime);

            boolean crossfade = crossfadeLength > 0 && currentPhoneme.phoneme.tailOut() != 0 && nextPhoneme.phoneme.leadIn() != 0
                && cross > 0;
//            boolean transitionThisTime = currentPhoneme.clockN + sb.regionSize() >= phonemeRemainingN;
            boolean transitionThisTime = currentPhoneme.clockN + samplesThisTime >= lengthN;

            SampleBuffer subBuffer = new SampleBuffer(sb.data, chunkStart, chunkStart + samplesThisTime);
            currentPhoneme.processBuffer(subBuffer);

            if (crossfade)
            {
                int thisCrossfadeLength = Math.min(crossfadeLength, cross1);

                // TODO: preallocate crossfade buffer
                SampleBuffer sb1 = new SampleBuffer(new float[thisCrossfadeLength], 0, thisCrossfadeLength);
                nextPhoneme.processBuffer(sb1);
                nextPhoneme.clockN += thisCrossfadeLength;
                float a0 = (crossfadeN / (float) crossfadeLength);
                float a1 = (crossfadeN + thisCrossfadeLength) / (float) crossfadeLength;
                sb1.applyRamp(a0, a1, 0, thisCrossfadeLength);

                crossfadeN += thisCrossfadeLength;

                sb.applyRamp(1 - a0, 1 - a1, subBuffer.endSample - thisCrossfadeLength, subBuffer.endSample);
                sb1.addInto(sb, sb1.startSample, subBuffer.endSample - thisCrossfadeLength, thisCrossfadeLength);

            }

            samplesRemaining -= samplesThisTime;

            if (transitionThisTime)
            {
                currentPhoneme = null;
            }
            else
            {
                currentPhoneme.clockN += samplesThisTime;
            }
        }
    }

    @Override
    public void init(SampleInfo info)
    {
        this.info = info;
        list.init(info);

        this.crossfadeBuffer = new SampleBuffer(256);
    }

    private static class PlayingPhoneme implements PlayablePhoneme.PlayContext
    {
        public final PlayablePhoneme phoneme;
        public int clockN = 0;

        private PlayingPhoneme(PlayablePhoneme phoneme)
        {
            this.phoneme = phoneme;
        }

        public void processBuffer(SampleBuffer sb)
        {
            phoneme.processBuffer(sb, this);
        }

        @Override
        public int clockN()
        {
            return clockN;
        }

        public float length()
        {
            return phoneme.length(this);
        }

        @Override
        public String toString()
        {
            return phoneme.toString();
        }
    }
}