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

package com.neep.neepmeat.client.screen.plc;

import com.neep.neepmeat.client.screen.ScreenSubElement;
import com.neep.neepmeat.client.screen.plc.edit.EditBoxWidget;
import com.neep.neepmeat.client.screen.plc.edit.InstructionBrowserWidget;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.network.plc.PLCSyncAction;
import com.neep.neepmeat.plc.compiler.PLCCompilePipeline;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.screen.PLCScreenHandler;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class PLCScreenEditorState extends ScreenSubElement implements Renderable, GuiEventListener, NarratableEntry, PLCScreenState
{
    private final PLCProgramScreen parent;
    private EditBoxWidget editorField;
    private final InstructionBrowserWidget browser;
    private final PLCStackViewer viewer;
    private boolean changed;

    // Has to be lazy as the initial value in the PropertyDelegate is incorrect.
    @Nullable private PLCCompilePipeline pipeline = null;

    public PLCScreenEditorState(PLCProgramScreen parent)
    {
        this.parent = parent;
        this.browser = new InstructionBrowserWidget(() -> null, p -> true, this::selectProvider);
        this.viewer = new PLCStackViewer(parent.getMenu().getPlc());
    }

    private void selectProvider(InstructionProvider provider)
    {
        editorField.insert("." + provider.getParseName().toLowerCase(Locale.ROOT));
    }

    @Override
    protected void init()
    {
        if (editorField == null)
        {
            editorField = new EditBoxWidget(x, y, 300, screenHeight, 0.8f, Component.nullToEmpty("Write your program here.\n\nClick a block in the world to insert its coordinates as a target.\n\nTo run the program, press the 'compile' button and then the 'run' button\n\nShortcuts:\nCTRL+S: Compile\nCTRL+R: Run/Pause\nCTRL+T: Stop\nCTRL+E: Compile and run.\nCTRL+F: Step"))
            {
                @Override
                public void setFocused(boolean focused)
                {
                    super.setFocused(focused);
                    updateEditorWidth();
                }
            };
            editorField.setText(parent.getMenu().getInitialText());
            editorField.setChangeListener(s -> this.changed = true);

        }

        updateEditorWidth();

        browser.init(screenWidth, screenHeight);

        viewer.init(screenWidth - 100 - 40, browser.getY(), 40, screenHeight - browser.getY());

        addDrawableChild(editorField);
        addDrawableChild(browser);
        addDrawable(viewer);

    }

    @Override
    public void setFocused(boolean focused)
    {
        super.setFocused(focused);
        updateEditorWidth();
    }

    private void updateEditorWidth()
    {
        if (editorField.isFocused())
            editorField.setWidth(300);
        else
            editorField.setWidth(100);

        editorField.setHeight(screenHeight);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double amount)
    {
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (editorField != null)
        {
            if (client.level.getGameTime() % 10 == 0 && changed)
            {
                String text = editorField.getText();

                if (pipeline == null)
                    pipeline = parent.getMenu().getLanguage().apply(() -> null);

                try
                {
                    if (text.length() >= FriendlyByteBuf.MAX_STRING_LENGTH)
                        throw new NeepASM.ProgramBuildException(-1, 0, "text too long (> " + FriendlyByteBuf.MAX_STRING_LENGTH + " chars)");

                    pipeline.parse(text);
                    setCompileMessage("Parsed Successfully", true, -1);
                }
                catch (NeepASM.ProgramBuildException e)
                {
                    setCompileMessage(e.getMessage(), false, e.line());
                }

                if (text.length() < FriendlyByteBuf.MAX_STRING_LENGTH)
                    parent.getMenu().updateText.emitter().accept(text);

                changed = false;
            }

            editorField.setDebugLine(parent.getMenu().debugLine());
        }
    }

    public void setCompileMessage(String message, boolean success, int line)
    {
        editorField.setError(message, success ? 0xFF44AA00 : 0xFFFF0000);
        editorField.setErrorLine(line);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (!editorField.isHovered() && getFocused() == editorField)
        {
            setFocused(null);
//            updateEditorWidth();
//            setFocused(null);
//            parent.focusOn(null);
//            return false;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void updateNarration(NarrationElementOutput builder)
    {

    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return NarrationPriority.FOCUSED;
    }

    public boolean isSelected()
    {
        return true;
    }

    public void argument(Argument argument)
    {
        editorField.insert("@(" + argument.pos().getX() + " " + argument.pos().getY() + " " + argument.pos().getZ() + " " + argument.face().name().toUpperCase().charAt(0) + ")");
    }

    @Override
    public void onKeyPressed(int keyCode, int scanCode, int modifiers)
    {
        keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        // Jank. Avert your eyes.
        // What are you doing here anyway? You don't need to see this.
        if (Screen.hasControlDown())
        {
            PLCScreenHandler handler = parent.getMenu();
            if (keyCode == GLFW.GLFW_KEY_R)
            {
                if (handler.isRunning())
                    handler.channel.emitter().apply(PLCSyncAction.PAUSE);
                else
                    handler.channel.emitter().apply(PLCSyncAction.RUN);

                return true;

            }
            else if (keyCode == GLFW.GLFW_KEY_S)
            {
                handler.channel.emitter().apply(PLCSyncAction.COMPILE);
            }
            else if (keyCode == GLFW.GLFW_KEY_F)
            {
                handler.channel.emitter().apply(PLCSyncAction.STEP);
            }
            else if (keyCode == GLFW.GLFW_KEY_E)
            {
                handler.channel.emitter().apply(PLCSyncAction.COMPILE_RUN);
            }
            else if (keyCode == GLFW.GLFW_KEY_T)
            {
                handler.channel.emitter().apply(PLCSyncAction.STOP);
                return true;
            }
        }

        if (keyCode == 258 && !isSelected())
        {
//            parent.setFocused(this);

//            boolean bl = !hasShiftDown();
//            if (!this.changeFocus(bl))
//            {
//                this.changeFocus(bl);
//            }

            return false;
        }
        else
        {
            return this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public boolean isEditFieldFocused()
    {
        return editorField.isFocused();
    }

    public void changeLanguage(LanguageMode language)
    {
        pipeline = language.apply(() -> null);
        changed = true;
    }
}
