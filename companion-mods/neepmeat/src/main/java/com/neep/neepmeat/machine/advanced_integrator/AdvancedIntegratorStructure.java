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

package com.neep.neepmeat.machine.advanced_integrator;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.block.entity.AdvancedIntegratorStructureBlockEntity;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AdvancedIntegratorStructure extends BigBlockStructure<AdvancedIntegratorStructureBlockEntity> implements DataCable
{
    public AdvancedIntegratorStructure(BigBlock parent, Properties settings)
    {
        super(parent, settings);
    }

    @Override
    protected BlockEntityType<AdvancedIntegratorStructureBlockEntity> registerBlockEntity()
    {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "advanced_integrator_structure"),
                FabricBlockEntityTypeBuilder.create(
                        (p, s) -> new AdvancedIntegratorStructureBlockEntity(getBlockEntityType(), p, s),
                        this).build());
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!player.getItemInHand(hand).isEmpty())
            return super.useWithoutItem(state, world, pos, player, hit);

        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof AdvancedIntegratorStructureBlockEntity be)
        {
            var parent = be.getParent();
            if (parent != null)
            {
                parent.onUse(player);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
