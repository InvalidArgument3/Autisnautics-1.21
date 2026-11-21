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

package com.neep.neepmeat.machine;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.charnel_pump.CharnelPumpBlock;
import com.neep.neepmeat.machine.charnel_pump.CharnelPumpBlockEntity;
import com.neep.neepmeat.machine.phage_ray.PhageRayBlock;
import com.neep.neepmeat.machine.phage_ray.PhageRayBlockEntity;
import com.neep.neepmeat.machine.reactor.block.ReactionCoreBlock;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorNeedleBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorPortBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorSegmentBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.ReactionCoreBlockEntity;
import com.neep.neepmeat.machine.reactor.disruptor.DisruptorNeedleBlock;
import com.neep.neepmeat.machine.reactor.disruptor.DisruptorPartBlock;
import com.neep.neepmeat.machine.well_head.WellHeadBlock;
import com.neep.neepmeat.machine.well_head.WellHeadBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EffectMachines {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NeepMeat.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NeepMeat.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeepMeat.ID);

    public static final DeferredBlock<PhageRayBlock> PHAGE_RAY = BLOCKS.register("phage_ray", PhageRayBlock::new);
    public static final DeferredBlock<CharnelPumpBlock> CHARNEL_PUMP = BLOCKS.register("charnel_pump", CharnelPumpBlock::new);
    public static final DeferredBlock<WellHeadBlock> WELL_HEAD = BLOCKS.register("well_head", WellHeadBlock::new);
    public static final DeferredBlock<DisruptorPartBlock> DISRUPTOR_PORT = BLOCKS.register("disruptor_port", () -> new DisruptorPartBlock(DisruptorPortBlockEntity::new));
    public static final DeferredBlock<DisruptorPartBlock> DISRUPTOR_SEGMENT = BLOCKS.register("disruptor_segment", () -> new DisruptorPartBlock(DisruptorSegmentBlockEntity::new));
    public static final DeferredBlock<DisruptorNeedleBlock> DISRUPTOR_NEEDLE = BLOCKS.register("disruptor_needle", DisruptorNeedleBlock::new);
    public static final DeferredBlock<ReactionCoreBlock> REACTION_CORE = BLOCKS.register("reaction_core", ReactionCoreBlock::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PhageRayBlockEntity>> PHAGE_RAY_BE = BLOCK_ENTITIES.register("phage_ray",
            () -> BlockEntityType.Builder.of(PhageRayBlockEntity::new, PHAGE_RAY.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CharnelPumpBlockEntity>> CHARNEL_PUMP_BE = BLOCK_ENTITIES.register("charnel_pump",
            () -> BlockEntityType.Builder.of(CharnelPumpBlockEntity::new, CHARNEL_PUMP.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WellHeadBlockEntity>> WELL_HEAD_BE = BLOCK_ENTITIES.register("well_head",
            () -> BlockEntityType.Builder.of(WellHeadBlockEntity::new, WELL_HEAD.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DisruptorPortBlockEntity>> DISRUPTOR_PORT_BE = BLOCK_ENTITIES.register("disruptor_port",
            () -> BlockEntityType.Builder.of(DisruptorPortBlockEntity::new, DISRUPTOR_PORT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DisruptorSegmentBlockEntity>> DISRUPTOR_SEGMENT_BE = BLOCK_ENTITIES.register("disruptor_segment",
            () -> BlockEntityType.Builder.of(DisruptorSegmentBlockEntity::new, DISRUPTOR_SEGMENT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DisruptorNeedleBlockEntity>> DISRUPTOR_NEEDLE_BE = BLOCK_ENTITIES.register("disruptor_needle",
            () -> BlockEntityType.Builder.of(DisruptorNeedleBlockEntity::new, DISRUPTOR_NEEDLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactionCoreBlockEntity>> REACTION_CORE_BE = BLOCK_ENTITIES.register("reaction_core",
            () -> BlockEntityType.Builder.of(ReactionCoreBlockEntity::new, REACTION_CORE.get()).build(null));

    private EffectMachines() {}

    public static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of().strength(2f);
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
