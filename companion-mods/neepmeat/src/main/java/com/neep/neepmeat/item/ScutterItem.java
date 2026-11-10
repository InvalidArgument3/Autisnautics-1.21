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

package com.neep.neepmeat.item;

import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.neepmeat.entity.scutter.ScutterEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class ScutterItem<T extends ScutterEntity> extends BaseItem
{
    protected final Supplier<EntityType<T>> entityType;

    public ScutterItem(Supplier<EntityType<T>> entityType, TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings);
        this.entityType = entityType;
    }

    @Nullable
    protected Vec3 canPlaceAt(Level world, BlockPos pos, Vec3 hit, Direction side)
    {
        BlockState state = world.getBlockState(pos);
        BlockPos offset = pos;
        if (state.blocksMotion())
        {
            offset = pos.relative(side);
        }
        Vec3 entityPos = Vec3.atCenterOf(offset);

        AABB box = entityType.get().getDimensions().makeBoundingBox(entityPos);
        if (world.noCollision(box))
        {
            return entityPos;
        }

        return null;
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Vec3 valid = canPlaceAt(world, pos, context.getClickLocation(), context.getClickedFace());

        if (valid != null)
        {
            if (world.isClientSide())
            {
                return InteractionResult.SUCCESS;
            }

            T scutter = entityType.get().create(world);
            if (scutter != null)
            {
                scutter.setPosRaw(valid.x, valid.y, valid.z);
                scutter.setPos(valid.x, valid.y, valid.z);
                processEntity(scutter, context);
                world.addFreshEntity(scutter);
            }

            context.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;

        }
        return super.useOn(context);
    }

    protected void processEntity(T entity, UseOnContext context)
    {
        ItemStack stack = context.getItemInHand();

        if (stack.hasCustomHoverName())
        {
            entity.setCustomName(context.getItemInHand().getHoverName());
        }

        if (stack.getTag() != null)
            entity.readStackNbt(stack.getTag());
    }
}
