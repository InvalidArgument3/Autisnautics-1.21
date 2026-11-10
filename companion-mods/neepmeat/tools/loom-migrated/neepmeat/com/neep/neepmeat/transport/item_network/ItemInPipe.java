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

package com.neep.neepmeat.transport.item_network;

import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.block.item_transport.machine.PipeItemMemory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

@SuppressWarnings("UnstableApiUsage")
public class ItemInPipe
{
    public double x;
    public double y;
    public double z;

    public Direction in;
    public Direction out;
    public float progress;
    public int pipeTicks;
    public long tickStart;
    public long tickEnd;
    public float speed;

    protected ItemVariant variant;
    protected int amount;

    protected ItemRoute route = ItemRoute.EMPTY;

    public final Vector3d clientPos = new Vector3d();

    @Nullable public PipeItemMemory callback;

    public ItemInPipe(ResourceAmount<ItemVariant> amount, long tickStart)
    {
        this(null, null, amount.resource(), (int) amount.amount(), tickStart);
    }

    public ItemInPipe(ItemVariant variant, int amount)
    {
        this(null, null, variant, amount, 0);
    }

    public ItemInPipe(Direction in, Direction out, ItemVariant variant, int amount, long tickStart)
    {
        this.in = in;
        this.out = out;
        this.progress = 0;
        this.variant = variant;
        this.amount = amount;
        this.speed = 0.1f;
        this.tickStart = tickStart;
        this.tickEnd = (long) (tickStart + 1 / speed);
    }

    public ItemVariant resource()
    {
        return variant;
    }

    public long amount()
    {
        return Math.max(amount, 0);
    }

    public void setRoute(ItemRoute route)
    {
        this.route = route;
    }

    public Direction getPreferredOutputDirection(BlockState state, Direction in, ItemPipe pipe, boolean nonTrivial)
    {
        var options = pipe.getConnections(state, in);
        if ((route != null && !route.isEmpty()) && (options.size() > 1 || nonTrivial))
        {
            return route.remove(0);
        }
        return null;
    }

    public static Vector3d directionUnit(Direction direction, Vector3d vec)
    {
        return vec.set(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    public ItemStack toItemStack()
    {
        return variant.toStack(amount);
    }

    public void set(Vec3 vec)
    {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public void tick()
    {
        ++pipeTicks;
        progress = (pipeTicks) * speed;
    }

    public void getPosition(float prog, Vector3d pos)
    {
        float inFactor = 1 - prog;
        if (prog <= 0.5)
        {
            directionUnit(in, pos).mul(inFactor - 0.5);
        }
        else
        {
            directionUnit(out, pos).mul(prog - 0.5);
        }
    }

    public void reset(Direction in, Direction out, long tickStart)
    {
        this.progress = 0;
        this.pipeTicks = 0;
        this.in = in;
        this.out = out;
        this.tickStart = tickStart;
        this.tickEnd = (long) (tickStart + 1 / speed);
        this.set(new Vec3(0, 0, 0));
    }

    public ResourceAmount<ItemVariant> toResourceAmount()
    {
        ResourceAmount<ItemVariant> itemVariantResourceAmount = new ResourceAmount<>(variant, amount);
        if (itemVariantResourceAmount.resource().isBlank())
        {
            itemVariantResourceAmount = new ResourceAmount<>(ItemVariant.of(Items.STONE.getDefaultInstance()), 1);
        }
        return itemVariantResourceAmount;
//        return new ResourceAmount<ItemVariant>(ItemVariant.of(itemStack), itemStack.getCount());
    }

    public void decrement(int i)
    {
        if (amount - i >= 0 && callback != null)
            callback.tickOff(variant, i);

        this.amount -= i;
    }

    public void zero()
    {
        if (amount > 0 && callback != null)
            callback.tickOff(variant, amount);

        this.amount = 0;
    }

    public CompoundTag toNbt(CompoundTag nbt)
    {
        nbt.putInt("in", in.get3DDataValue());
        nbt.putInt("out", out.get3DDataValue());
        nbt.putLong("tick_start", tickStart);
        nbt.putLong("tick_end", tickEnd);
        nbt.putFloat("speed", speed);

        CompoundTag item = new CompoundTag();
        nbt.put("variant", variant.toNbt());
        nbt.putInt("amount", amount);
        nbt.put("item", item);
        if (route != null)
            nbt.putByteArray("route", route.write());

        return nbt;
    }

    public static ItemInPipe fromNbt(CompoundTag nbt)
    {
        Direction in = Direction.from3DDataValue(nbt.getInt("in"));
        Direction out = Direction.from3DDataValue(nbt.getInt("out"));
        ItemVariant variant = ItemVariant.fromNbt(nbt.getCompound("variant"));

        // Prevents crashes and makes faults obvious
        if (variant.isBlank())
            variant = ItemVariant.of(Items.STONE);

        int amount = nbt.getInt("amount");
        long tickStart = nbt.getLong("tick_start");
        var route = ItemRoute.read((ByteArrayTag) nbt.get("route"));
        float speed = nbt.getFloat("speed");

        ItemInPipe item = new ItemInPipe(in, out, variant, amount, tickStart);
        item.setRoute(route);
        item.speed = speed;

        return item;
    }
//    public void setAmount(int amount)
//    {
//        this.amount = amount;

//    }

    public ItemInPipe split(int amount)
    {
        ItemInPipe newItem = new ItemInPipe(in, out, variant, amount, tickStart);

        newItem.x = x;
        newItem.y = y;
        newItem.z = z;
        newItem.speed = speed;
        newItem.tickEnd = tickEnd;
        newItem.pipeTicks = pipeTicks;

        newItem.route = new ItemRoute(route);
        newItem.callback = callback;

        return newItem;
    }

    public boolean isEmpty()
    {
        return amount == 0 || variant.isBlank();
    }

    public boolean hasRoute()
    {
        return route != null && !route.isEmpty();
    }

//    public ItemInPipe copyWith(int amount)
//    {
//        ItemInPipe newItem = new ItemInPipe(in, out, variant, amount, tickStart);
//        if (route != null)
//            newItem.setRoute((List<Direction>) route.clone());
//        return newItem;
//    }
}
