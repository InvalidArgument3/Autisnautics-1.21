/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatlib.neepasm;

public class NeepASM
{
    private static final String SOURCE = "main:\n MOVE @(1 2 3 N) @(4 5 6) oggins=ooe # Cromment";

    public static void main(String[] args)
    {
//        Compiler compiler = new Compiler();
//        compiler.compile(SOURCE);
    }

    public abstract static class NeepAsmException extends Exception
    {
        public NeepAsmException(String message)
        {
            super(message);
        }
    }

    public static class RuntimeException extends NeepAsmException
    {
        public RuntimeException(String message)
        {
            super(message);
        }

        public static RuntimeException notSupported(String operation)
        {
            return new RuntimeException(operation + ": actuator cannot perform this operation");
        }
    }

    public static class ProgramBuildException extends NeepAsmException
    {
        public final int line;
        public final int pos;

        public ProgramBuildException(int line, int pos, String message)
        {
            super(message);
            this.line = line;
            this.pos = pos;
        }

        @Override
        public String getMessage()
        {
            return "At line " + line + ", char " + pos + ": " + super.getMessage();
        }

        public int line()
        {
            return line;
        }
    }

    public static class ParseException extends NeepAsmException
    {
        public ParseException(String message, String token)
        {
            super(message + ": " + token);
        }

        public ParseException(String message)
        {
            super(message);
        }

        public ParseException(String message, char token)
        {
            super(message + ": " + token);
        }
    }

    public static class CompilationException extends NeepAsmException
    {
        public CompilationException(String message)
        {
            super(message);
        }
    }
}
