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

package com.neep.neepmeat.entity.worm;

import com.neep.neepmeat.util.RandomMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class WormActions
{
    protected static RandomMap<ResourceLocation, Entry> MAP = new RandomMap<>();

    public static void put(ResourceLocation id, WormActionFactory factory, float weight)
    {
        Entry entry = new Entry(weight, factory);
        MAP.put(id, entry, weight);

        // Make corresponding animation available
//        AnimationBuilder animationBuilder = new AnimationBuilder().addAnimation(animation);
//        ANIMATION_MAP.put(id.toString(), animationBuilder);
    }
    public static Entry random()
    {
        return MAP.next();
    }

    public static void init()
    {
        Entry emptyEntry = new Entry(1, WormAction.EmptyAction::new);
        MAP.getById().defaultReturnValue(emptyEntry);
//        ANIMATION_MAP.defaultReturnValue(IDLE);

//        put(IdleWormAction.ID, IdleWormAction::new, 1);
//        put(FullSwingWormAction.ID, FullSwingWormAction::new, 1);
        put(WormBiteGoal.ID, WormBiteGoal::new, 1);
    }

    public static CompoundTag toNbt(WormAction action)
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", action.toString());
        action.writeNbt(nbt);
        return nbt;
    }

    public static WormAction fromNbt(CompoundTag nbt, WormEntity worm)
    {
        ResourceLocation id = ResourceLocation.tryParse(nbt.getString("id"));
        Entry entry = MAP.get(id);
        WormAction action = entry.create(worm);
        action.readNbt(nbt);
        return action;
    }

//    protected static Object2ObjectMap<String, AnimationBuilder> ANIMATION_MAP = new Object2ObjectOpenHashMap<>();
//    protected static final AnimationBuilder IDLE = new AnimationBuilder().addAnimation("animation.god_worm.idle");

//    public static AnimationBuilder getAnimation(String actionId)
//    {
//        return ANIMATION_MAP.getOrDefault(actionId, IDLE);
//    }

    public record Entry(float weight, WormActionFactory factory)
    {
        public WormAction create(WormEntity entity)
        {
            return factory.create(entity);
        }
    }

    @FunctionalInterface
    public interface WormActionFactory
    {
        WormAction create(WormEntity entity);
    }
}
