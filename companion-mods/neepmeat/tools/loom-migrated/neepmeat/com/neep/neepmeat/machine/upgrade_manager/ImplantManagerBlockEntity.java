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

package com.neep.neepmeat.machine.upgrade_manager;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.implant.entity.ImplantManager;
import com.neep.neepmeat.implant.entity.ImplantRegistry;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.plc.component.MutateInPlace;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ImplantManagerBlockEntity extends SyncableBlockEntity implements ImplantManagerDelegate
{
    public ImplantManagerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Nullable
    @Override
    public ImplantManager getImplantManager()
    {
        MutateInPlace<?> mip = getMip();
        if (mip != null)
        {
            Object object = mip.get();
            if (object != null)
            {
                return NMComponents.IMPLANT_MANAGER.getNullable(object);
            }
        }
        return null;
    }

    public MutateInPlace<?> getMip()
    {
        MutateInPlace<?> mip = MutateInPlace.ITEM.find(level, worldPosition.relative(getBlockState().getValue(ImplantManagerBlock.FACING)), null);
        if (mip == null)
        {
            mip = MutateInPlace.ENTITY.find(level, worldPosition.relative(getBlockState().getValue(ImplantManagerBlock.FACING)), null);
        }
        return mip;
    }

    @Override
    @Nullable
    public ItemLike removeUpgrade(ResourceLocation identifier)
    {
        MutateInPlace<Object> mip = (MutateInPlace<Object>) getMip();
        if (mip != null)
        {
            Object object = mip.get();
            if (object != null)
            {
                ImplantManager manager = NMComponents.IMPLANT_MANAGER.getNullable(object);
                if (manager != null)
                {
                    if (manager.removeImplant(identifier))
                    {
                        mip.setResult(1, object);
                        return ImplantRegistry.getItem(identifier);
                    }
                }
            }
        }

        return null;
    }
}
