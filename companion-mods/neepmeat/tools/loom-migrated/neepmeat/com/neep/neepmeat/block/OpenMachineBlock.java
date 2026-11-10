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

package com.neep.neepmeat.block;

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.block.PressureWashable;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.network.ParticleSpawnS2C;
import com.neep.meatlib.util.Wrenchable;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;

public class OpenMachineBlock extends BaseHorFacingBlock implements LivingMachineStructure, Wrenchable, PressureWashable
{
    private final MachineBlock parent;

    public OpenMachineBlock(RegistrationContext ctx, MachineBlock parent)
    {
        super(ctx, ItemSettings.block().factory(MachineBlock.MachineBlockItem::new),
                MeatlibBlockSettings.copyOf(parent).randomTicks());
        this.parent = parent;
    }

    @Override
    public ItemLike dropsLike()
    {
        return parent;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        if (context.getPlayer() == null)
            return defaultBlockState();

        return defaultBlockState().setValue(FACING, !context.getPlayer().isShiftKeyDown() ? context.getHorizontalDirection().getOpposite() : context.getHorizontalDirection());
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        // TODO: remove
//        onCreate(world, pos, state);
//        return ActionResult.SUCCESS;
        return super.use(state, world, pos, player, hand, hit);
    }

    public void heal(Level world, BlockPos pos, BlockState state)
    {
        world.setBlockAndUpdate(pos, parent.defaultBlockState());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        heal(world, pos, state);
    }

    public void onCreate(Level world, BlockPos pos, BlockState state)
    {
        BlockPos p = pos.relative(state.getValue(FACING));
        if (world.getBlockState(p).isAir())
        {
            int level = world.getRandom().nextInt(2) + 1;
            world.setBlockAndUpdate(p, NMFluids.SLUDGE.getBlock().defaultBlockState().setValue(LiquidBlock.LEVEL,  8 - level));
        }
    }

    @Override
    public EnumMap<StructureProperty, PropertyValue> getProperties()
    {
        return parent.getProperties();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag options)
    {
        parent.appendHoverText(stack, world, tooltip, options);
    }

    @Override
    public boolean onWrench(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, BlockHitResult hitResult, ItemStack stack)
    {
        heal(world, pos, state);
        playWrenchSound(world, pos);

        return true;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random)
    {
        if (random.nextDouble() < 0.1)
        {
            double d = (double) pos.getX() + 0.5;
            double e = pos.getY();
            double f = (double) pos.getZ() + 0.5;

            Direction direction = state.getValue(FACING);
            Direction.Axis axis = direction.getAxis();
            double h = random.nextDouble() * 0.6 - 0.3;
            double i = axis == Direction.Axis.X ? (double) direction.getStepX() * 0.52 : h;
            double j = random.nextDouble() * 0.8;
            double k = axis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.52 : h;
//        world.addParticle(NMParticles.BLOOD_DRIP, d, e, f, 0, 0, 0);
            world.addParticle(NMParticles.BLOOD_DRIP, d + i, e + j + 0.1, f + k, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public int wash(Level world, BlockPos pos, BlockState state, Vec3 velocity, int power, FluidVariant fluidVariant)
    {
        if (power >= 3 && fluidVariant.isOf(NMFluids.WORK_FLUID.still()))
        {
            heal(world, pos, state);

            if (world instanceof ServerLevel serverWorld)
            {
                PlayerLookup.around(serverWorld, pos, 10).forEach(p ->
                {
                    ParticleSpawnS2C.send(p, NMParticles.BODY_COMPOUND_SHOWER, pos.getCenter(), Vec3.ZERO, new Vec3(1.3, 1.3, 1.3), 10);
                });
            }
            return 3;
        }
        return 0;
    }
}
