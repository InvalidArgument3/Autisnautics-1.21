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

package com.neep.neepbus.client.item;

import com.mojang.blaze3d.platform.Window;
import com.neep.meatlib.client.event.CrosshairRenderEvent;
import com.neep.meatlib.client.event.ScrollEvents;
import com.neep.meatlib.client.event.UseAttackCallback;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.neepbus.NeepBus;
import com.neep.neepbus.block.NeepBusProvider;
import com.neep.neepbus.network.NeepBusNetwork;
import com.neep.neepbus.util.ConfigEntry;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.neep.meatlib.client.screen.GUIUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class NetworkingToolClient
{
    @Nullable private static StoredConfig CONFIG;

    private static Mode mode = Mode.SENDER;
    @Nullable private static StoredAddress address;

    private static int selected;

    public static void init()
    {
        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            if (client.level != null && client.player != null )
            {
                ItemStack mainStack = client.player.getMainHandItem();
                if (mainStack.is(NeepBus.NETWORKING_TOOL))
                {
                    tick(client);
                }
                else
                    CONFIG = null;
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
        {
            Minecraft client = Minecraft.getInstance();
            if (client.level != null && client.player != null )
            {
                ItemStack mainStack = client.player.getMainHandItem();
                if (mainStack.is(NeepBus.NETWORKING_TOOL))
                {
                    renderHud(drawContext, client, tickDelta);
                }
            }
        });

        ScrollEvents.PRE_INGAME_SCROLL.register((client, world, player, window, amount) ->
        {
            // Ignore if the player has a screen open.
            if (client.getOverlay() == null)
            {
                if (CONFIG != null)
                {
                    int newSelected = clampSelection(selected + (int) Math.round(amount), CONFIG.config());
                    if (newSelected != selected)
                    {
                        selected = newSelected;
                        client.getSoundManager().play(new SimpleSoundInstance(
                                MeatlibSounds.CLICK, SoundSource.PLAYERS,
                                1, 1, SoundInstance.createUnseededRandom(),
                                player.getX(), player.getY(), player.getZ()));
                    }
                    return true;
                }
            }
            return false;
        });

        UseAttackCallback.DO_USE.register((client, player) ->
        {
            if (override() && player.getMainHandItem().is(NeepBus.NETWORKING_TOOL))
            {
                if (player.isShiftKeyDown() && client.hitResult instanceof BlockHitResult blockHitResult)
                {
                    NeepBusNetwork.OPEN_CONFIG_SCREEN.emitter(player).open(blockHitResult.getBlockPos());
                    return true;
                }

                if (CONFIG == null && address != null)
                    clear();
                else if (CONFIG != null)
                    configClick(client, CONFIG.blockName(), CONFIG.config());

                UseAttackCallback.swingHand(client, InteractionHand.MAIN_HAND);

                return false;
            }
            return true;
        });

        CrosshairRenderEvent.EVENT.register((context, player) -> override() && player.getMainHandItem().is(NeepBus.NETWORKING_TOOL));
    }

    private static void clear()
    {
        mode = Mode.SENDER;
        address = null;
    }

    private static void configClick(Minecraft client, Component blockName, NeepBusConfig config)
    {
        selected = clampSelection(selected, config);

        if (address == null)
        {
            if (mode == Mode.SENDER)
            {
                var entries = config.getOutputs();
                if (!entries.isEmpty())
                {
                    ConfigEntry entry = config.getOutputs().get(selected);
                    address = new StoredAddress(blockName, entry);
                    mode = Mode.RECEIVER;

                    client.getSoundManager().play(new SimpleSoundInstance(
                            MeatlibSounds.UI_BEEP, SoundSource.PLAYERS,
                            1, 1, SoundInstance.createUnseededRandom(),
                            client.player.getX(), client.player.getY(), client.player.getZ()));
                }
            }
            // Add handling for RECEIVER
        }
        else
        {
            if (mode == Mode.RECEIVER)
            {
                var entries = config.getInputs();
                if (!entries.isEmpty())
                {
                    if (client.hitResult instanceof BlockHitResult blockHitResult)
                    {
                        NeepBusNetwork.NT_CONNECT.emitter(client.player)
                                .send(blockHitResult.getBlockPos(), false, selected, address.address());

                        client.getSoundManager().play(new SimpleSoundInstance(
                                MeatlibSounds.UI_BEEP, SoundSource.PLAYERS,
                                1, 1, SoundInstance.createUnseededRandom(),
                                client.player.getX(), client.player.getY(), client.player.getZ()));
                    }

                    mode = Mode.SENDER;
                    address = null;
                }
            }
            // Add handling for SENDER
        }
    }

    private static void renderHud(GuiGraphics context, Minecraft client, float tickDelta)
    {
        Window window = client.getWindow();
        int wWidth = window.getGuiScaledWidth();
        int wHeight = window.getGuiScaledHeight();
        Font textRenderer = client.font;

        int entryHeight = 20;
        int entryStride = entryHeight + 1;
        int entryWidth = 120;

        if (address != null)
        {
            if (CONFIG == null)
                renderInstructions(context, textRenderer, NeepMeat.translationKey("screen", "networking_tool.instructions.reset"));

            int xStart = wWidth / 2 - entryWidth - 10;
            int yStart = (wHeight - entryStride) / 2;
            renderEntry(context, textRenderer, address, true, xStart, yStart, entryWidth, entryHeight);

            renderHeader(context, textRenderer, address.blockName(), xStart, yStart - entryStride + 5, entryWidth, entryHeight);
        }


        if (address != null && CONFIG != null)
        {
            // Render right panel
            if (mode == Mode.RECEIVER)
            {
                renderInstructions(context, textRenderer, NeepMeat.translationKey("screen", "networking_tool.instructions.receiver"));

                List<? extends ConfigEntry> inputs = CONFIG.config().getInputs();

                int idx = Mth.clamp(selected, 0, inputs.size() - 1);

                int xStart = wWidth / 2 + 10; // Subtract to leave room for crosshairs
                int yStart = (wHeight - entryStride) / 2
                        + idx * entryStride;

                int i = 0;
                for (i = 0; i < inputs.size(); ++i)
                {
                    int y = yStart - i * entryStride;
                    ConfigEntry entry = inputs.get(i);
                    renderEntry(context, textRenderer, entry, i == idx, xStart, y, entryWidth, entryHeight);
                }

                int labelY = yStart - i * entryStride + 5;
                renderHeader(context, textRenderer, CONFIG.blockName(), xStart, labelY, entryWidth, entryHeight);
            }
        }

        if (address == null && CONFIG != null)
        {
            // Render left panel
            if (mode == Mode.SENDER)
            {
                renderInstructions(context, textRenderer, NeepMeat.translationKey("screen", "networking_tool.instructions.sender"));

                // Render the config entries
                List<? extends ConfigEntry> outputs = CONFIG.config().getOutputs();

                int idx = Mth.clamp(selected, 0, outputs.size() - 1);

                int xStart = wWidth / 2 - entryWidth - 10; // Subtract to leave room for crosshairs
                int yStart = (wHeight - entryStride) / 2
                        + idx * entryStride;

                int i = 0;
                for (i = 0; i < outputs.size(); ++i)
                {
                    int y = yStart - i * entryStride;
                    ConfigEntry entry = outputs.get(i);
                    renderEntry(context, textRenderer, entry, i == idx, xStart, y, entryWidth, entryHeight);
                }

                int labelY = yStart - i * entryStride + 5;
                renderHeader(context, textRenderer, CONFIG.blockName(), xStart, labelY, entryWidth, entryHeight);
            }
        }

        if (override())
        {
            String arrow = "→";
            GUIUtil.drawText(context, textRenderer, arrow,
                    (wWidth - textRenderer.width(arrow)) / 2f,
                    (wHeight - textRenderer.lineHeight) / 2f + 1,
                    PLCCols.TEXT.col, true);
        }

    }

    private static void renderInstructions(GuiGraphics context, Font textRenderer, Component instructions)
    {
        int yOff = 5;
        for (var text : textRenderer.split(instructions, 400))
        {
            GUIUtil.drawText(context, textRenderer, text, 5, yOff, PLCCols.TEXT.col, true);
            yOff += textRenderer.lineHeight;
        }
    }

    private static void renderEntry(GuiGraphics context, Font textRenderer, ConfigEntry entry, boolean selected, int x, int y, int w, int h)
    {
        int col = selected ? PLCCols.SELECTED.col : PLCCols.TEXT.col;
        GUIUtil.fill(context, x, y, x + w, y + h, 0x90000000);
        GUIUtil.renderBorderInner(context, x, y, w, h, col, 0);
        GUIUtil.drawText(context, textRenderer, entry.getName(), x + 2, y + 2, col, true);
        GUIUtil.drawText(context, textRenderer, "→", x + 2, y + 2 + textRenderer.lineHeight, PLCCols.INVALID.col, false);

        String address = entry.getAddress();
        int addressWidth = textRenderer.width(address);
        GUIUtil.drawText(context, textRenderer, address, x + w - addressWidth - 2, y + textRenderer.lineHeight + 2, PLCCols.TEXT.col, true);
    }

    private static void renderHeader(GuiGraphics context, Font textRenderer, Component name, int x, int y, int w, int h)
    {
        GUIUtil.drawCenteredText(context, textRenderer, name,
                x + w / 2f, y,
                PLCCols.TEXT.col, true);

        GUIUtil.drawHorizontalLine1(context, x, x + w, y + 10, PLCCols.BORDER.col);
    }

    private static void tick(Minecraft client)
    {
        updateConfig(client);

        updateSelection();
    }

    private static void updateSelection()
    {
        selected = clampSelection(selected, CONFIG != null ? CONFIG.config : null);
    }

    private static int clampSelection(int selected, NeepBusConfig config)
    {
        if (config != null)
        {
            return switch (mode)
            {
                case SENDER -> Mth.clamp(selected, 0, config.getOutputs().size() - 1);
                case RECEIVER -> Mth.clamp(selected, 0, config.getInputs().size() - 1);
            };
        }
        return 0;
    }

    private static void updateConfig(Minecraft client)
    {
        if (client.hitResult instanceof BlockHitResult hitResult)
        {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = client.level.getBlockState(pos);
            if (state.getBlock() instanceof NeepBusProvider provider)
            {
                @Nullable NeepBusConfig config = provider.getConfig(client.level, pos, state, null);
                if (config != null)
                {
                    CONFIG = new StoredConfig(state.getBlock().getName(), config);
                    return;
                }
            }
        }
        CONFIG = null;
    }

//    private static boolean renderOutline(WorldRenderContext context, @Nullable HitResult hitResult)
//    {
//        MinecraftClient client = MinecraftClient.getInstance();
//        Camera camera = client.gameRenderer.getCamera();
//
//        ItemStack stack = client.player.getMainHandStack();
//        if (stack.isOf(NeepBus.NETWORKING_TOOL))
//        {
//            NbtCompound nbt = stack.getSubNbt("networking");
//            if (nbt != null && nbt.contains("first"))
//            {
//                BlockPos first = NbtHelper.toBlockPos(nbt.getCompound("first"));
//
//                Vec3d camPos = camera.getPos();
//                BlockState targetState = client.world.getBlockState(first);
//                VoxelShape shape = targetState.getOutlineShape(client.world, first, ShapeContext.of(client.player));
//
//                drawCuboidShapeOutline(
//                        context.matrixStack(),
//                        context.consumers().getBuffer(RenderLayer.getLines()),
//                        shape,
//                        first.getX() - camPos.x,
//                        first.getY() - camPos.y,
//                        first.getZ() - camPos.z,
//                        1, 0.36f, 0.13f, 0.8f
//                );
//            }
//        }
//        return true;
//    }

    public static boolean override()
    {
        return CONFIG != null || address != null;
    }

    public enum Mode
    {
        SENDER,
        RECEIVER
    }

    private record StoredConfig(Component blockName, NeepBusConfig config)
    {

    }

    private record StoredAddress(Component blockName, String name, String address) implements ConfigEntry
    {
        public StoredAddress(Component blockName, ConfigEntry entry)
        {
            this(blockName, entry.getName(), entry.getAddress());
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public String getAddress()
        {
            return address;
        }

        @Override
        public void setAddress(String address)
        {

        }
    }
}
