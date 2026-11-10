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

package com.neep.neepmeat.datagen;

import com.neep.meatweapons.MWItems;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.transport.ItemTransport;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import java.util.function.Consumer;

public class NMAdvancements extends FabricAdvancementProvider
{
    protected NMAdvancements(FabricDataOutput output)
    {
        super(output);
    }

    @Override
    public void generateAdvancement(Consumer<Advancement> consumer)
    {
        Advancement root = Advancement.Builder.advancement()
                .display(
                        NMItems.RAW_MEAT_BRICK,
                        Component.translatable("advancements.neepmeat.main.root.title"),
                        Component.translatable("advancements.neepmeat.main.root.desc"),
                        new ResourceLocation("neepmeat:textures/gui/advancements/backgrounds/main.png"),
                        FrameType.TASK,
                        false,
                        false,
                        false

                )
                .addCriterion("immediate", PlayerTrigger.TriggerInstance.tick())
                .build(new ResourceLocation(NeepMeat.NAMESPACE, "root"));
        consumer.accept(root);

        Advancement obtain_guide = getItem(
                consumer, "main", "obtain_guide", root,
                NMItems.PROJECTOR
        );

        Advancement obtain_integrator = getItem(
                consumer, "main", "obtain_integrator", root,
                NMBlocks.INTEGRATOR_EGG
        );

        Advancement enlightenment = getItem(
                consumer, "main", "enlightenment", obtain_integrator,
                NMBlocks.PEDESTAL
        );

        Advancement obtain_asbestos = getItem(
                consumer, "main", "obtain_asbestos", obtain_integrator,
                NMBlocks.ASBESTOS
        );

        Advancement obtain_enlightened_brain = getItem(
                consumer, "main", "obtain_enlightened_brain", enlightenment,
                NMItems.ENLIGHTENED_BRAIN
            );

        Advancement place_grinder = placeBlock(
                consumer, "main", "place_grinder", obtain_integrator,
                NMBlocks.CRUSHER
        );

        Advancement place_hydraulic_press = getItem(
                consumer, "main", "place_hydraulic_press", place_grinder,
                NMBlocks.HYDRAULIC_PRESS
        );

        Advancement obtain_meat_bucket = getItem(
                consumer, "main", "obtain_meat_bucket", place_grinder,
                NMFluids.MEAT.getItem()
        );


        Advancement obtain_plc = placeBlock(
                consumer, "main", "obtain_plc", enlightenment,
                PLCBlocks.PLC
        );

        Advancement obtain_power_flower_seeds = getItem(
                consumer, "main", "obtain_power_flower_seeds", obtain_plc,
                NMBlocks.POWER_FLOWER_SEEDS
        );

        Advancement obtain_whisper_flour = getItem(
                consumer, "main", "obtain_whisper_flour", root,
                NMItems.WHISPER_FLOUR
        );

        Advancement obtain_whisper_brass = getItem(
                consumer, "main", "obtain_whisper_brass", obtain_whisper_flour,
                NMItems.WHISPER_BRASS_INGOT
        );

        Advancement place_ejector = getItem(
                consumer, "main", "place_ejector", obtain_whisper_brass,
                ItemTransport.EJECTOR
        );

        Advancement place_item_pipe = placeBlock(
                consumer, "main", "place_item_pipe", obtain_whisper_brass,
                ItemTransport.ITEM_PIPE
        );

        Advancement obtain_staff = getItem(
                consumer, "main", "obtain_staff", obtain_whisper_brass,
                MWItems.MEATGUN_STAFF
        );
    }

    private static Advancement getItem(Consumer<Advancement> consumer, String id, Advancement parent, Component name, Component desc, ItemLike... item)
    {
        Advancement advancement = Advancement.Builder.advancement()
                .parent(parent)
                .display(item[0], name, desc, null, FrameType.TASK, true, false, false)
                .addCriterion("get_item", InventoryChangeTrigger.TriggerInstance.hasItems(item))
                .build(new ResourceLocation(NeepMeat.NAMESPACE, id));

        consumer.accept(advancement);

        return advancement;
    }

    private static Advancement getItem(Consumer<Advancement> consumer, String prefix, String id, Advancement parent, ItemLike... item)
    {
        Advancement advancement = Advancement.Builder.advancement()
                .parent(parent)
                .display(item[0],
                        Component.translatable("advancements." + NeepMeat.NAMESPACE + "." + prefix + "." + id + ".title"),
                        Component.translatable("advancements." + NeepMeat.NAMESPACE + "." + prefix + "." + id + ".desc"),
                        null, FrameType.TASK, true, false, false)
                .addCriterion("get_item", InventoryChangeTrigger.TriggerInstance.hasItems(item))
                .build(new ResourceLocation(NeepMeat.NAMESPACE, id));

        consumer.accept(advancement);

        return advancement;
    }

    private static Advancement placeBlock(Consumer<Advancement> consumer, String prefix, String id, Advancement parent, Block block)
    {
        Advancement advancement = Advancement.Builder.advancement()
                .parent(parent)
                .display(block,
                        Component.translatable("advancements." + NeepMeat.NAMESPACE + "." + prefix + "." + id + ".title"),
                        Component.translatable("advancements." + NeepMeat.NAMESPACE + "." + prefix + "." + id + ".desc"),
                        null, FrameType.TASK, true, false, false)
                .addCriterion("get_item", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block))
                .build(new ResourceLocation(NeepMeat.NAMESPACE, id));

        consumer.accept(advancement);

        return advancement;
    }
}
