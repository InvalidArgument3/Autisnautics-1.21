package com.neep.neepspeak.mouth.phoneme;

import com.neep.neepspeak.audio.SoundFiles;
import com.neep.neepspeak.phoneme.Phoneme1;

public interface Phonemes
{
    // Phonetic alphabet from S.A.M.
    // I've since realised that this alphabet is optimised for american english rather than RP
    // Hence 'pot' is PAAT because that's how an american might transcribe it phonetically using the latin alphabet

    // Vowels
    Phoneme1 IY = new Phoneme1("IY"); // feet
    Phoneme1 IH = new Phoneme1("IH"); // pin
    Phoneme1 EH = new Phoneme1("EH"); // beg
    Phoneme1 AE = new Phoneme1("AE"); // sam
    Phoneme1 AA = new Phoneme1("AA"); // pot
    Phoneme1 AH = new Phoneme1("AH"); // budget
    Phoneme1 AO = new Phoneme1("AO"); // talk
    Phoneme1 OH = new Phoneme1("OH"); // cone
    Phoneme1 UH = new Phoneme1("UH"); // book
    Phoneme1 UX = new Phoneme1("UX"); // loot
    Phoneme1 ER = new Phoneme1("ER"); // bird
    Phoneme1 AX = new Phoneme1("AX"); // gallon
    Phoneme1 IX = new Phoneme1("IX"); // digit

    // Diphthongs
    Phoneme1 EY = new Phoneme1("EY"); // made
    Phoneme1 AY = new Phoneme1("AY"); // high
    Phoneme1 OY = new Phoneme1("OY"); // boy
    Phoneme1 AW = new Phoneme1("AW"); // how
    Phoneme1 OW = new Phoneme1("OW"); // slow
    Phoneme1 UW = new Phoneme1("UW"); // crew

    // Voiced consonants
    Phoneme1 R = new Phoneme1("R"); // red
    Phoneme1 L = new Phoneme1("L"); // allow
    Phoneme1 W = new Phoneme1("W"); // away
    Phoneme1 WH = new Phoneme1("WH"); // whale
    Phoneme1 Y = new Phoneme1("Y"); // you
    Phoneme1 M = new Phoneme1("M"); // sam
    Phoneme1 N = new Phoneme1("N"); // man
    Phoneme1 NX = new Phoneme1("NX"); // song
    Phoneme1 B = new Phoneme1("B"); // bad
    Phoneme1 D = new Phoneme1("D"); // dog
    Phoneme1 G = new Phoneme1("G"); // again
    Phoneme1 J = new Phoneme1("J"); // judge
    Phoneme1 Z = new Phoneme1("Z"); // zoo
    Phoneme1 ZH = new Phoneme1("ZH"); // pleasure
    Phoneme1 V = new Phoneme1("V"); // seven
    Phoneme1 DH = new Phoneme1("DH"); // these

    Phoneme1 S = new Phoneme1("S"); // seven
    Phoneme1 SH = new Phoneme1("SH"); // ghoti
    Phoneme1 F = new Phoneme1("F"); // ghoti
    Phoneme1 TH = new Phoneme1("TH"); // thin
    Phoneme1 P = new Phoneme1("P"); // poke
    Phoneme1 T = new Phoneme1("T"); // talk
    Phoneme1 K = new Phoneme1("K"); // cake
    Phoneme1 CH = new Phoneme1("CH"); // speech
    Phoneme1 H = new Phoneme1("H"); // ahead

    PlayablePhoneme DEBUG_SINE_1 = new SonorantPhoneme(SoundFiles.DEBUG_SINE_1, 0.5f, false);
    PlayablePhoneme DEBUG_SINE_2 = new SonorantPhoneme(SoundFiles.DEBUG_SINE_2, 0.5f, false);
    PlayablePhoneme END = new SpacePhoneme(0.1f);
}
