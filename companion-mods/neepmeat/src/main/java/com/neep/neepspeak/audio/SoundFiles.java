package com.neep.neepspeak.audio;

import java.util.ArrayList;
import java.util.List;

public class SoundFiles
{
    public static final Entry DEBUG_SINE_1 = new Entry("debug_sine_1");
    public static final Entry DEBUG_SINE_2 = new Entry("debug_sine_2");

    // Uppercase vowel sounds that are otherwise named the same as lowercase ones must be 
    // changed to be distinct for windows to be able to work with them (case insensitive file system)
    // Thus `A_1` becomes `^a_1`, etc.

    // Vowels
    public static final Entry _a = new Entry("a_1");
    public static final Entry _A = new Entry("^a_1");
    public static final Entry _O = new Entry("^o_1");
    public static final Entry _o = new Entry("o_1");
    public static final Entry _u = new Entry("_u_1");
    public static final Entry _3 = new Entry("_3_1");
    public static final Entry _I = new Entry("^i_1");
    public static final Entry _E = new Entry("E_1");
    public static final Entry _V = new Entry("V_1");
    public static final Entry _U = new Entry("U_1");
    public static final Entry _at_1 = new Entry("@_1");
    public static final Entry _at_2 = new Entry("@_2");
    public static final Entry _i = new Entry("i_1");

    // Sonorant consonants
    public static final Entry _m_1 = new Entry("_m_1");
    public static final Entry _r$_1 = new Entry("_r$_1");
    public static final Entry _n_1 = new Entry("_n_1");

    // ?? consonants
    public static final Entry _f_1 = new Entry("_f_1");
    public static final Entry _v_2 = new Entry("_v_2");
    public static final Entry _s_1 = new Entry("_s_1");
    public static final Entry _S_1 = new Entry("_^s_1");
    public static final Entry _z_1 = new Entry("_z_1");
    public static final Entry _Z_1 = new Entry("_^z_1");
    public static final Entry _w_1 = new Entry("_w_1");
    public static final Entry _T_1 = new Entry("_T_1");
    public static final Entry _x_1 = new Entry("_x_1");
    public static final Entry _D_1 = new Entry("_D_1");
    public static final Entry _h_1 = new Entry("_h_1");
    public static final Entry _N_1 = new Entry("_^n_1");

    // Liquid
    public static final Entry _l_1 = new Entry("_l_1");

    // Obstruent consonants
    public static final Entry _b_2 = new Entry("_b_2");
    public static final Entry _t_2 = new Entry("_t_2");
    public static final Entry _p_2 = new Entry("_p_2");
    public static final Entry _g_2 = new Entry("_g_2");
    public static final Entry _dZ_2 = new Entry("_dZ_2");
    public static final Entry _d_2 = new Entry("_d_2");
    public static final Entry _k_2 = new Entry("_k_2");
    public static final Entry _tS_2 = new Entry("_tS_2");
    public static final Entry _j_2 = new Entry("_j_2");

    public static final List<Entry> VALUES;

    static
    {
        ArrayList<Entry> list = new ArrayList<>();
        list.add(_a);
        list.add(_A);
        list.add(_O);
        list.add(_o);
        list.add(_I);
        list.add(_E);
        list.add(_V);
        list.add(_U);
        list.add(_at_1);
        list.add(_at_2);
        list.add(_i);

        list.add(_m_1);
        list.add(_r$_1);
        list.add(_n_1);
        list.add(_f_1);
        list.add(_s_1);
        list.add(_S_1);
        list.add(_z_1);
        list.add(_Z_1);
        list.add(_w_1);
        list.add(_T_1);
        list.add(_x_1);
        list.add(_D_1);
        list.add(_h_1);
        list.add(_n_1);

        list.add(_b_2);
        list.add(_t_2);
        list.add(_p_2);
        list.add(_g_2);
        list.add(_dZ_2);
        list.add(_d_2);
        list.add(_k_2);
        list.add(_tS_2);
        list.add(_j_2);
        VALUES = list;
    }

    public static void preload()
    {
        for (var value : VALUES)
        {
            value.get();
        }
    }

    public static class Entry implements SoundDataProvider
    {
        public static final String EXTENSION = ".wav";

        public final String name;
        public final String resourcePath;

        private SoundData contents;

        public Entry(String name)
        {
            this.name = name;
            this.resourcePath = "phonemes/normal" + "/" + name + EXTENSION;
        }

        public SoundData get()
        {
            if (contents == null)
                contents = new SoundData(resourcePath);

            return contents;
        }

        @Override
        public String name()
        {
            return name;
        }
    }
}
