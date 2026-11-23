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

package com.neep.meatlib.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface HandledScreenAccessor
{
    @Accessor
    Slot getTouchDragSlotStart();

    @Accessor
    ItemStack getTouchDragStack();

    @Accessor
    boolean getTouchIsRightClickDrag();

    @Accessor
    int getDraggedStackRemainder();

    @Accessor
    void setDraggedStackRemainder(int remainder);

    @Accessor
    ItemStack getTouchDropReturningStack();

    @Accessor
    long getTouchDropTime();

    @Accessor
    void setTouchDropReturningStack(ItemStack stack);

    @Accessor
    int getHeldButtonType();
}
