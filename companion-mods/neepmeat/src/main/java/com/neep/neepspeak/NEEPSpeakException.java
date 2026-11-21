package com.neep.neepspeak;

public class NEEPSpeakException extends Exception
{
    public NEEPSpeakException(String message)
    {
        super(message);
    }

    public NEEPSpeakException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
