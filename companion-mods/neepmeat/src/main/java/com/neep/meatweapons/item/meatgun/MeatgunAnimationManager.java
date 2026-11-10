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

package com.neep.meatweapons.item.meatgun;

import com.neep.meatweapons.client.meatgun.animation.MeatgunAnimation;
import com.neep.meatweapons.component.MeatgunComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.network.FriendlyByteBuf;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class MeatgunAnimationManager
{
    private static final Map<UUID, MeatgunAnimationManager> INSTANCES = CacheBuilder.newBuilder().softValues().build().asMap();

    public static MeatgunAnimationManager getOrCreate(UUID uuid, Meatgun meatgun)
    {
        return INSTANCES.computeIfAbsent(uuid, u -> (MeatgunAnimationManager) meatgun.createAnimationManager().get());
    }

    private final MeatgunAnimation idle;
    private MeatgunAnimation activeAnimation = MeatgunAnimation.EMPTY;

    private final Map<String, MeatgunAnimation> animations = new HashMap<>();

    public MeatgunAnimationManager(MeatgunAnimation idle)
    {
        this.idle = idle;
        this.activeAnimation = idle;
        this.activeAnimation.start(null);
    }

    public void queue(String name, @Nullable FriendlyByteBuf buf)
    {
        @Nullable MeatgunAnimation animation = animations.get(name);
        if (animation != null)
            queue(animation, buf);
    }

    public void queue(MeatgunAnimation animation, @Nullable FriendlyByteBuf buf)
    {
        if (activeAnimation.canStop(animation))
        {
            activeAnimation = animation;
            activeAnimation.start(buf);
        }
    }

    public void tick(MeatgunComponent component)
    {
        if (activeAnimation.finished())
        {
            activeAnimation = idle;
            activeAnimation.start(null);
        }

        activeAnimation.tick(component);
    }

    public MeatgunAnimation getActive()
    {
        return activeAnimation;
    }

    public MeatgunAnimationManager add(String name, MeatgunAnimation animation)
    {
        animations.put(name, animation);
        return this;
    }
}
