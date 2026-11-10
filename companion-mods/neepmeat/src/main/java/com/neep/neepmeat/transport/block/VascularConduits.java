/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.transport.block;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.building.BuildingBlocks;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

public final class VascularConduits {
    private static final RegistrationContext CONTEXT = new RegistrationContext(NeepMeat.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeepMeat.ID);

    public static final DeferredBlock<VascularConduitBlock> CONDUIT = CONTEXT.block("vascular_conduit", VascularConduitBlock::new);
    public static final DeferredItem<BlockItem> CONDUIT_ITEM = CONTEXT.item("vascular_conduit",
            () -> new BlockItem(CONDUIT.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VascularConduitBlockEntity>> CONDUIT_BE =
            BLOCK_ENTITIES.register("vascular_conduit", () -> BlockEntityType.Builder
                    .of(VascularConduitBlockEntity::new, CONDUIT.get()).build(null));

    public static final DeferredBlock<EndBlock> SOURCE = CONTEXT.block("test_blood_source", () -> new EndBlock(BloodAcceptor.Mode.SOURCE, 1000));
    public static final DeferredBlock<EndBlock> SINK = CONTEXT.block("test_blood_sink", () -> new EndBlock(BloodAcceptor.Mode.SINK, 0));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EndBlock.EndEntity>> END_BE =
            BLOCK_ENTITIES.register("test_blood_end", () -> BlockEntityType.Builder
                    .of(EndBlock.EndEntity::new, SOURCE.get(), SINK.get()).build(null));

    private VascularConduits() {}

    public static void register(IEventBus bus) {
        CONTEXT.register(bus);
        BLOCK_ENTITIES.register(bus);
        NeoForge.EVENT_BUS.addListener(com.neep.neepmeat.transport.blood_network.BloodNetworkManager::onTick);
        bus.addListener(VascularConduits::creative);
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == BuildingBlocks.TAB.getKey()) {
            event.accept(CONDUIT_ITEM.get());
        }
    }

    public static final class EndBlock extends BaseEntityBlock {
        private final BloodAcceptor.Mode mode;
        private final long output;

        public EndBlock(BloodAcceptor.Mode mode, long output) {
            super(MeatlibBlockSettings.create().strength(1.0F).properties());
            this.mode = mode;
            this.output = output;
        }

        @Override
        protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
            return com.mojang.serialization.MapCodec.unit(this);
        }

        @Override
        public RenderShape getRenderShape(BlockState state) {
            return RenderShape.MODEL;
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new EndEntity(pos, state);
        }

        public static final class EndEntity extends BlockEntity implements BloodAcceptor {
            private final Mode mode;
            private final long produced;
            private float influx;

            public EndEntity(BlockPos pos, BlockState state) {
                super(END_BE.get(), pos, state);
                EndBlock block = (EndBlock) state.getBlock();
                this.mode = block.mode;
                this.produced = block.output;
            }

            @Override
            public long output() {
                return produced;
            }

            @Override
            public float updateInflux(float influx) {
                this.influx = influx;
                return influx;
            }

            public float influx() {
                return influx;
            }

            @Override
            public Mode mode() {
                return mode;
            }
        }
    }
}
