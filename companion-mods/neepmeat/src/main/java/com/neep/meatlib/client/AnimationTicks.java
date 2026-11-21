package com.neep.meatlib.client;

import org.lwjgl.glfw.GLFW;

public class AnimationTicks
{
    public static float getRenderTime()
    {
        return (float) GLFW.glfwGetTime() * 20;
    }
}
