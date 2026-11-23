/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib;

import com.neep.meatlib.item.MeatlibItem;
import com.neep.meatlib.util.Wrenchable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class MeatlibEvents {
    public static final TagKey<Item> WRENCHES = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(MeatLib.ID, "wrenches"));
    public static final TagKey<Item> C_WRENCHES = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("c", "tools/wrenches"));

    private MeatlibEvents() {}

    @SubscribeEvent
    public static void onWrench(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        if (!(state.getBlock() instanceof Wrenchable wrenchable)) {
            return;
        }
        if (!event.getItemStack().is(WRENCHES) && !event.getItemStack().is(C_WRENCHES)) {
            return;
        }
        InteractionHand hand = event.getHand();
        boolean used = wrenchable.onWrench(level, event.getPos(), state, event.getEntity(), hand, event.getHitVec(), event.getItemStack());
        if (used) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof MeatlibItem item) {
            item.meatlibTooltip().apply(event.getItemStack(), event.getToolTip());
        }
    }

    @SubscribeEvent
    public static void afterDatapack(OnDatapackSyncEvent event) {
        MeatLib.LOG.debug("meatlib saw datapack sync for {}", event.getPlayer() == null ? "all players" : event.getPlayer().getScoreboardName());
    }
}
