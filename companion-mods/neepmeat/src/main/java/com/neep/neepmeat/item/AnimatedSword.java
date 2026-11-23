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

import com.neep.meatlib.item.BaseSwordItem;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.item.OverrideSwingItem;
import com.neep.neepmeat.entity.AnimationSyncable;
import net.minecraft.world.item.Tier;
import software.bernie.geckolib.animatable.GeoItem;

public abstract class AnimatedSword extends BaseSwordItem implements AnimationSyncable, OverrideSwingItem, GeoItem
{
    public static final int ANIM_SWING = 0;
    public static final int ANIM_STAB = 1;
    public static final int ANIM_CHOP = 2;

    public AnimatedSword(RegistrationContext ctx, Tier material, int attackDamage, float speed, Properties settings)
    {
        super(material, attackDamage, speed, settings);
    }

}
