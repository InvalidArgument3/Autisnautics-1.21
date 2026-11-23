package com.neep.neepspeak.phoneme;

import com.neep.neepspeak.audio.SoundData;
import com.neep.neepspeak.audio.SoundDataProvider;

public class PhonemeFileSet
{
    private final String prefix;

    public final Entry DEBUG_SINE_1 = new Entry("debug_sine_1");
    public final Entry DEBUG_SINE_2 = new Entry("debug_sine_2");

    // Uppercase vowel sounds that are otherwise named the same as lowercase ones must be 
    // changed to be distinct for windows to be able to work with them (case insensitive file system)
    // Thus `A_1` becomes `^a_1`, etc.

    // Vowels
    public final Entry _a = new Entry("a_1");
    public final Entry _A = new Entry("^a_1");
    public final Entry _O = new Entry("^o_1");
    public final Entry _o = new Entry("o_1");
    public final Entry _u = new Entry("_u_1");
    public final Entry _3 = new Entry("_3_1");
    public final Entry _I = new Entry("^i_1");
    public final Entry _E = new Entry("E_1");
    public final Entry _V = new Entry("V_1");
    public final Entry _U = new Entry("U_1");
    public final Entry _at_1 = new Entry("@_1");
    public final Entry _at_2 = new Entry("@_2");
    public final Entry _i = new Entry("i_1");

    // Sonorant consonants
    public final Entry _m_1 = new Entry("_m_1");
    public final Entry _r$_1 = new Entry("_r$_1");
    public final Entry _n_1 = new Entry("_n_1");

    // ?? consonants
    public final Entry _f_1 = new Entry("_f_1");
    public final Entry _v_2 = new Entry("_v_2");
    public final Entry _s_1 = new Entry("_s_1");
    public final Entry _S_1 = new Entry("_^s_1");
    public final Entry _z_1 = new Entry("_z_1");
    public final Entry _Z_1 = new Entry("_^z_1");
    public final Entry _w_1 = new Entry("_w_1");
    public final Entry _T_1 = new Entry("_T_1");
    public final Entry _x_1 = new Entry("_x_1");
    public final Entry _D_1 = new Entry("_D_1");
    public final Entry _h_1 = new Entry("_h_1");
    public final Entry _N_1 = new Entry("_^n_1");

    // Liquid
    public final Entry _l_1 = new Entry("_l_1");

    // Obstruent consonants
    public final Entry _b_2 = new Entry("_b_2");
    public final Entry _t_2 = new Entry("_t_2");
    public final Entry _p_2 = new Entry("_p_2");
    public final Entry _g_2 = new Entry("_g_2");
    public final Entry _dZ_2 = new Entry("_dZ_2");
    public final Entry _d_2 = new Entry("_d_2");
    public final Entry _k_2 = new Entry("_k_2");
    public final Entry _tS_2 = new Entry("_tS_2");
    public final Entry _j_2 = new Entry("_j_2");

    public PhonemeFileSet(String prefix)
    {
        this.prefix = prefix;
    }

    public class Entry implements SoundDataProvider
    {
        public final String EXTENSION = ".wav";

        public final String name;

        private SoundData contents;

        public Entry(String name)
        {
            this.name = name;
        }

        public SoundData get()
        {
            if (contents == null)
                contents = new SoundData(prefix + "/" + name + EXTENSION);

            return contents;
        }

        @Override
        public String name()
        {
            return name;
        }
    }
}
