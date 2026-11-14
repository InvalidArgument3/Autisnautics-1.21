package com.neep.neepspeak.phoneme;

import com.neep.neepspeak.mouth.phoneme.*;

public class PhonemeSet
{
    public static final PhonemeSet NORMAL = new PhonemeSet(new PhonemeFileSet("phonemes/normal"));

    private final PhonemeFileSet fs;

    // X-SAMPA substitutions:
    // $ represents \ (r\ becomes r$)
    // 6 represents }
    // I'm aware X-SAMPA uses numbers for something else
    // Oh well

    // I've now realised that I've mixed SAMPA symbols in some places
    // help me please

    public final PlayablePhoneme SPACE = new SpacePhoneme(0.1f);
    public final PlayablePhoneme PAUSE = new SpacePhoneme(0.05f);

    public final PlayablePhoneme _A;
    public final PlayablePhoneme _a;
    public final PlayablePhoneme _E;
    public final PlayablePhoneme _I;
    public final PlayablePhoneme _U;
    public final PlayablePhoneme _i;
    public final PlayablePhoneme _V;
    public final PlayablePhoneme _o;
    public final PlayablePhoneme _O;

    public final PlayablePhoneme _u;
    public final PlayablePhoneme _3;
    public final PlayablePhoneme _at_1;
    public final PlayablePhoneme _at_2;
    public final PlayablePhoneme _r$;
    public final PlayablePhoneme _m;
    public final PlayablePhoneme _n;

    public final PlayablePhoneme _l;
    public final PlayablePhoneme _f;
    public final PlayablePhoneme _v;
    public final PlayablePhoneme _s;
    public final PlayablePhoneme _S;
    public final PlayablePhoneme _z;
    public final PlayablePhoneme _Z;
    public final PlayablePhoneme _w;
    public final PlayablePhoneme _T;
    public final PlayablePhoneme _x;
    public final PlayablePhoneme _D;
    public final PlayablePhoneme _h;
    public final PlayablePhoneme _N;

    public final PlayablePhoneme _b;
    public final PlayablePhoneme _p;
    public final PlayablePhoneme _g;
    public final PlayablePhoneme _dZ;
    public final PlayablePhoneme _d;
    public final PlayablePhoneme _t;
    public final PlayablePhoneme _k;
    public final PlayablePhoneme _tS;
    public final PlayablePhoneme _j;

    // Diphthongs made with monophthongs
    public final PlayablePhoneme _EI;
    public final PlayablePhoneme _AI;
    public final PlayablePhoneme _OI;
    public final PlayablePhoneme _V8;
    public final PlayablePhoneme _Vu;
    public final PlayablePhoneme _i6;
    public final PlayablePhoneme _Aw;
    public final PlayablePhoneme _oi;
    public final PlayablePhoneme _Vw;
    public final PlayablePhoneme _uA;
    public final PlayablePhoneme _uat;
    public final PlayablePhoneme _Ei;
    public final PlayablePhoneme _Ai;
    public final PlayablePhoneme _atU;
    public final PlayablePhoneme _uw;

    public PhonemeSet(PhonemeFileSet fileSet)
    {
        this.fs = fileSet;

        // This is really horrible, but I'm not sure if there's another way
        // I should put the phoneme names in an enum and associate the PlayablePhonemes with them using a map

        _A = new SonorantPhoneme(fs._A);
        _a = new SonorantPhoneme(fs._a);
        _E = new SonorantPhoneme(fs._E);
        _I = new SonorantPhoneme(fs._I);
        _U = new SonorantPhoneme(fs._U, 0.12f, true);
        _i = new SonorantPhoneme(fs._i);
        _V = new SonorantPhoneme(fs._V);
        _o = new SonorantPhoneme(fs._o);
        _O = new SonorantPhoneme(fs._O);
        _u = new SonorantPhoneme(fs._u);
        _3 = new SonorantPhoneme(fs._3);
        _at_1 = new SonorantPhoneme(fs._at_1);
        _at_2 = new ObstruentPhoneme(fs._at_2);
        _r$ = new SonorantPhoneme(fs._r$_1, 0.1f, false);
        _m = new SonorantPhoneme(fs._m_1, 0.1f, false);
        _n = new SonorantPhoneme(fs._n_1, 0.1f, false);
        _l = new SonorantPhoneme(fs._l_1, 0.13f, false);
        _f = new SonorantPhoneme(fs._f_1, 0.07f, false);
        _v = new SonorantPhoneme(fs._v_2, 0.1f, false);
        _s = new SonorantPhoneme(fs._s_1, 0.1f, false);
        _S = new SonorantPhoneme(fs._S_1, 0.1f, false);
        _z = new SonorantPhoneme(fs._z_1, 0.1f, false);
        _Z = new SonorantPhoneme(fs._Z_1, 0.1f, false);
        _w = new SonorantPhoneme(fs._w_1, 0.1f, false);
        _T = new SonorantPhoneme(fs._T_1, 0.1f, false);
        _x = new SonorantPhoneme(fs._x_1, 0.1f, false);
        _D = new SonorantPhoneme(fs._D_1, 0.1f, false);
        _h = new SonorantPhoneme(fs._h_1, 0.1f, false);
        _N = new SonorantPhoneme(fs._N_1, 0.1f, false);
        _b = new ObstruentPhoneme(fs._b_2, 0.03f);
        _p = new ObstruentPhoneme(fs._p_2, 0.05f);
        _g = new ObstruentPhoneme(fs._g_2);
        _dZ = new ObstruentPhoneme(fs._dZ_2, 0.05f);
        _d = new ObstruentPhoneme(fs._d_2, 0.05f);
        _t = new ObstruentPhoneme(fs._t_2, 0.05f);
        _k = new ObstruentPhoneme(fs._k_2, 0.05f);
        _tS = new ObstruentPhoneme(fs._tS_2, 0.05f);
        _j = new ObstruentPhoneme(fs._j_2, 0.05f);

        _EI = new DiphthongPhoneme(_E, _i);
        _AI = new DiphthongPhoneme(_A, _i);
        _OI = new DiphthongPhoneme(_O, _i);
        _V8 = new DiphthongPhoneme(_V, _U);
        _Vu = new DiphthongPhoneme(_V, _u);
        _i6 = new DiphthongPhoneme(_i, _u);
        _Aw = new DiphthongPhoneme(_A, _w);
        _oi = new DiphthongPhoneme(_o, _i);
        _Vw = new DiphthongPhoneme(_V, _w);
        _uA = new DiphthongPhoneme(_u, _A);
        _uat = new DiphthongPhoneme(_u, _at_1);
        _Ei = new DiphthongPhoneme(_E, _i);
        _Ai = new DiphthongPhoneme(_A, _i);
        _atU = new DiphthongPhoneme(_at_1, _U);
        _uw = new DiphthongPhoneme(_u, _w);
    }

    // SAM's alphabet is almost a prefix code.
    public PlayablePhoneme toSound2(int c1, int c2)
    {
        return switch (c1)
        {
            case 'I' -> switch (c2)
            {
                case 'Y' -> _i;
                case 'H', 'X' -> _I;
                default -> SPACE;
            };
            case 'E' -> switch (c2)
            {
                case 'H' -> _E;
                case 'R' -> _3; // 3:
                case 'Y' -> _EI;
                default -> SPACE;
            };
            case 'A' -> switch (c2)
            {
                case 'E', 'X' -> _a;
                case 'A' -> _O;
                case 'H' -> _at_1;
                case 'O' -> _o;
                case 'W' -> _Aw;
                case 'Y' -> _AI;
                default -> SPACE;
            };
            case 'U' -> switch (c2)
            {
                case 'H' -> _U;
                case 'X' -> _u; // loot
                case 'W' -> _u; // crew
                default -> SPACE;
            };
            case 'O' -> switch (c2)
            {
                case 'H' -> _atU;
                case 'I' -> _OI;
                case 'W' -> _Vw;
                default -> SPACE;
            };

            case 'N' -> c2 == 'X' ? _N : _n;
            case 'Z' -> c2 == 'H' ? _Z : _z;
            case 'D' -> c2 == 'H' ? _D : _d;
            case 'S' -> c2 == 'H' ? _S : _s;
            case 'T' -> c2 == 'H' ? _T : _t;
            case 'C' -> c2 == 'H' ? _tS : SPACE;
//            case '/' -> c2 == 'H' ? _h : SPACE;

            case 'R' -> _r$;
            case 'L' -> _l;
            case 'W' -> _w;
//            case 'WH' -> _w; // We don't need this
            case 'Y' -> _j;
            case 'M' -> _m;
            case 'B' -> _b;
            case 'G' -> _g;
            case 'J' -> _dZ;
            case 'V' -> _v;

            case 'F' -> _f;
            case 'P' -> _p;
            case 'K' -> _k;
            case 'H' -> _h;
            default -> SPACE;
        };
    }

    public PlayablePhoneme toSound(String sam)
    {
        return switch (sam)
        {
            case "IY" -> _i;
            case "IH" -> _I;
            case "EH" -> _E;
            case "AE" -> _a;
            case "AA" -> _O;
            case "AH" -> _at_1;
            case "AO" -> _o;
            case "OH" -> _atU;
            case "UH" -> _U;
            case "UX" -> _u;
            case "ER" -> _3; // 3:
            case "AX" -> _a;
            case "IX" -> _I;

            case "EY" -> _EI;
            case "AY" -> _AI;
            case "OY" -> _OI;
            case "AW" -> _Aw;
            case "OW" -> _Vw;
            case "UW" -> _uw;
            case "UU" -> _at_1;

            case "R" -> _r$;
            case "L" -> _l;
            case "W" -> _w;
            case "WH" -> _w;
            case "Y" -> _j;
            case "M" -> _m;
            case "N" -> _n;
            case "NX" -> _N;
            case "B" -> _b;
            case "D" -> _d;
            case "G" -> _g;
            case "J" -> _dZ;
            case "Z" -> _z;
            case "ZH" -> _Z;
            case "V" -> _v;
            case "DH" -> _D;

            case "S" -> _s;
            case "SH" -> _S;
            case "F" -> _f;
            case "TH" -> _T;
            case "P" -> _p;
            case "T" -> _t;
            case "K" -> _k;
            case "X" -> _x;
            case "CH" -> _tS;
            case "H" -> _h;
            default -> null;
        };
    }
}
