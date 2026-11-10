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

package com.neep.neepmeat.machine.phage_ray;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.meatlib.util.MiscUtil;
import com.neep.neepbus.block.NeepBusProvider;
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.block.entity.MouthBlockEntity;
import com.neep.neepbus.screen.MouthScreenHandler;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.item.NMItemSettings;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.screen_handler.PhageRayScreenHandler;
import com.neep.neepmeat.transport.api.pipe.AbstractBloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import com.neep.neepmeat.transport.fluid_network.SimplePipeProperties;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.Util;
import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class PhageRayBlock extends BigBlock<PhageRayBlock.PhageRayStructureBlock> implements MeatlibBlock, EntityBlock, NeepBusProvider
{
    private final BigBlockPattern topVolume;
    private final BigBlockPattern bottomVolume;
    private final VoxelShape shape = Shapes.box(-1, 0, -1, 2, 0.5, 2);
    private final VoxelShape shapeBottom = Shapes.box(-1, 0.5, -1, 2, 1, 2);

    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    public PhageRayBlock(RegistrationContext ctx, Properties settings)
    {
        super(ctx, settings);
        ctx.appendItem(this, new BaseBlockItem(this, ctx,
                NMItemSettings.block().neepbus()
                        .tooltip(LivingMachineComponents.tooltip(LivingMachineComponents.PHAGE_RAY).append(TooltipSupplier.hidden(1)))));

        topVolume = BigBlockPattern.makeOddCylinder(1, 0, 0, getStructure().defaultBlockState().setValue(BOTTOM, false));
        bottomVolume = BigBlockPattern.makeOddCylinder(1, 0, 0, getStructure().defaultBlockState().setValue(BOTTOM, true));

        registerDefaultState(getStateDefinition().any().setValue(BOTTOM, false));
    }

    @Override
    protected PhageRayStructureBlock registerStructureBlock(RegistrationContext ctx)
    {
        return ctx.append(this, new PhageRayStructureBlock(this, MeatlibBlockSettings.copyOf(this)), "phage_ray_structure");
    }

    @Override
    public BigBlockPattern getVolume(BlockState blockState)
    {
        return blockState.getValue(BOTTOM) ? bottomVolume : topVolume;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return state.getValue(BOTTOM) ? shapeBottom : shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(BOTTOM);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof PhageRayBlockEntity be)
        {
            player.openMenu((SimpleScreenHandlerFactory) (syncId, playerInventory, player1) ->
                    new PhageRayScreenHandler(playerInventory, syncId, be));

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return defaultBlockState().setValue(BOTTOM, ctx.getClickedFace() == Direction.DOWN);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.PHAGE_RAY.create(pos,state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.PHAGE_RAY, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(), null, world);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {

    }

    public static class PhageRayStructureBlock extends BigBlockStructure<PhageRayStructureBlockEntity> implements LivingMachineStructure, NeepBusProvider, DataCable
    {
        public static final EnumProperty<ConnectionType> NORTH_CONNECTION = SimplePipeProperties.NORTH_CONNECTION;
        public static final EnumProperty<ConnectionType> EAST_CONNECTION = SimplePipeProperties.EAST_CONNECTION;
        public static final EnumProperty<ConnectionType> SOUTH_CONNECTION = SimplePipeProperties.SOUTH_CONNECTION;
        public static final EnumProperty<ConnectionType> WEST_CONNECTION = SimplePipeProperties.WEST_CONNECTION;

        public static final Map<Direction, EnumProperty<ConnectionType>> DIR_TO_CONNECTION = Util.make(new EnumMap<>(Direction.class), map ->
        {
            map.put(Direction.NORTH, NORTH_CONNECTION);
            map.put(Direction.EAST, EAST_CONNECTION);
            map.put(Direction.SOUTH, SOUTH_CONNECTION);
            map.put(Direction.WEST, WEST_CONNECTION);
        });

        public PhageRayStructureBlock(BigBlock<?> parent, Properties settings)
        {
            super(parent, settings);
            this.registerDefaultState(this.stateDefinition.any()
                    .setValue(NORTH_CONNECTION, ConnectionType.NONE)
                    .setValue(EAST_CONNECTION, ConnectionType.NONE)
                    .setValue(SOUTH_CONNECTION, ConnectionType.NONE)
                    .setValue(WEST_CONNECTION, ConnectionType.NONE)
                    .setValue(BOTTOM, false)
            );
        }

        @Override
        public RenderShape getRenderShape(BlockState state)
        {
            return RenderShape.MODEL;
        }

        @Override
        protected BlockEntityType<PhageRayStructureBlockEntity> registerBlockEntity()
        {
            return Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(NeepMeat.NAMESPACE, "phage_ray_structure"),
                    FabricBlockEntityTypeBuilder.create(
                            (p, s) -> new PhageRayStructureBlockEntity(getBlockEntityType(), p, s),
                            this).build());
        }

        @Override
        public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
        {
            if (world.getBlockEntity(pos) instanceof PhageRayStructureBlockEntity be)
            {
                var controllerPos = be.getControllerPos();
                if (controllerPos != null)
                {
                    return NMBlocks.PHAGE_RAY.use(world.getBlockState(controllerPos), world, controllerPos, player, hand, hit);
                }
            }
            return super.use(state, world, pos, player, hand, hit);
        }

        @Override
        public EnumMap<StructureProperty, PropertyValue> getProperties()
        {
            return StructureProperty.EMPTY;
        }

        @Override
        public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
        {
//            if (world.getBlockEntity(pos) instanceof PhageRayStructureBlockEntity be)
//            {
//                be.get
//            }
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
        {
            super.createBlockStateDefinition(builder);
            DIR_TO_CONNECTION.values().forEach(builder::add);
            builder.add(BOTTOM);
        }

        @Override
        public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
        {
            if (direction.getAxis().isVertical())
                return state;

            boolean connection = canConnectTo(neighborState, direction.getOpposite(), (Level) world, neighborPos);

            if (connection)
            {
                return state.setValue(DIR_TO_CONNECTION.get(direction), ConnectionType.SIDE);
            }

            return state.setValue(DIR_TO_CONNECTION.get(direction), ConnectionType.NONE);
        }

        public boolean canConnectTo(BlockState toState, Direction toFace, Level world, BlockPos toPos)
        {
            if (toState.getBlock() instanceof DataCable cable && !toState.is(this))
            {
                return cable.connectInDirection(world, toPos, toState, toFace);
            }
            return false;
        }
    }

    public static class PhageRayStructureBlockEntity extends BigBlockStructureEntity implements ConfigProvider
    {
        private final StructureBloodAcceptor acceptor = new StructureBloodAcceptor();

        public PhageRayStructureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }

        public BloodAcceptor getAcceptor(Direction direction)
        {
            return acceptor;
        }

        @Override
        public NeepBusConfig getConfig()
        {
            var controller = getControllerBE(PhageRayBlockEntity.class);
            if (controller != null)
            {
                return controller.getConfig();
            }
            return NeepBusConfig.EMPTY;
        }

        private class StructureBloodAcceptor extends AbstractBloodAcceptor
        {
            @Override
            public Mode getMode()
            {
                return Mode.ACTIVE_SINK;
            }

            @Override
            public float updateInflux(float influx)
            {
                var be =  getControllerBE(PhageRayBlockEntity.class);
                if (be != null)
                    return be.getBloodAcceptor().updateInflux(influx);

                return 0;
            }
        }

//        @Nullable
//        private PhageRayBlockEntity getControllerBE()
//        {
//            if (controllerPos == null)
//            {
//                return null;
//            }
//            else if (cache == null)
//            {
//                cache = BlockApiCache.create(MeatLib.VOID_LOOKUP, (ServerWorld) getWorld(), controllerPos);
//            }
//
//            return cache.getBlockEntity() instanceof PhageRayBlockEntity controller ? controller : null;
//        }
    }
}
