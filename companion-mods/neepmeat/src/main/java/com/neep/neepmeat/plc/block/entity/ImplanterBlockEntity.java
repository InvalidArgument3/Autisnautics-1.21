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

package com.neep.neepmeat.plc.block.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.neepbus.NeepBus;
import com.neep.neepbus.input.InputInterceptors;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.entity.goal.Action;
import com.neep.neepmeat.init.NMSounds;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.neepmeat.plc.instruction.ImplantInstruction;
import com.neep.neepmeat.plc.recipe.ImplantStep;
import com.neep.neepmeat.plc.robot.PLCActuator;
import com.neep.neepmeat.screen_handler.ImplanterScreenHandler;
import com.neep.neepmeat.util.NMMaths;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImplanterBlockEntity extends SyncableBlockEntity implements PLCActuator, PLCActuator.Provider, ImplanterActuator
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepBus.REGISTRY_NAMESPACE, "implanter_movement");

    private final ImplementedInventory inventory = new ImplementedInventory()
    {
        private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

        @Override
        public NonNullList<ItemStack> getItems()
        {
            return items;
        }

        @Override
        public int getMaxStackSize()
        {
            return 1;
        }

        @Override
        public void setChanged()
        {
            ImplanterBlockEntity.this.setChanged();
        }
    };

    private final ImplanterInputInterceptor interceptor = new ImplanterInputInterceptor();

    private final ImplantAction action = new ImplantAction();

    public float renderYaw;
    public float renderX;
    public float renderY;

    public float cameraYaw;
    public float cameraX;
    public float cameraY;

    private float yaw;
    private float x;
    private float y;

    @Nullable private Vec3 targetEndPos;

    public ImplanterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);

        yaw = 0;
        x = 1;
        y = 1;
    }

    @Override
    public void sync()
    {
        super.sync();
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
    }

    public void serverTick()
    {
        if (interceptor.controller != null)
        {
            float speed = 0.08f;

            float vYaw = 0;
            float vx = 0;
            float vy = 0;

            if (interceptor.forward)
                vx += 1;

            if (interceptor.back)
                vx -= 1;

            if (interceptor.left)
                vYaw += 1;

            if (interceptor.right)
                vYaw -= 1;

            if (interceptor.up)
                vy += 1;

            if (interceptor.down)
                vy -= 1;

//            float omega = speed / x;
            yaw += vYaw * 0.2f * speed;
            x += vx * speed;
            y += vy * speed;

//            System.out.printf("x: %s, y: %s, yaw: %s,%n", x, y, yaw);
            limitPosition();
            softSync();
        }
        else if (targetEndPos != null)
        {
            float speed = 0.08f;

            moveAuto(NMMaths.wrapRadians((float) targetEndPos.x), (float) targetEndPos.y, (float) targetEndPos.z, speed * 3);
            sync();
        }

        action.tick();
    }

    private void moveAuto(float tYaw, float tx, float ty, float speed)
    {
        this.yaw = NMMaths.moveTowardsAngle(yaw, tYaw, speed * 0.4f);
        if (yaw != tYaw)
        {
            this.x = NMMaths.moveTowards(x, 1, speed);
            this.y = NMMaths.moveTowards(y, 1, speed);
        }
        else
        {
            this.x = NMMaths.moveTowards(x, tx, speed);
            this.y = NMMaths.moveTowards(y, ty, speed);
        }

        // Automatically reset when target reached
        if (reachedTarget(null))
        {
            targetEndPos = null;
        }
    }

    public void clientTick()
    {
    }

    private void limitPosition()
    {
        float magnitude = Mth.sqrt(x * x + y * y);
        x = x / magnitude * Math.min(magnitude, 4.6f);
        y = y / magnitude * Math.min(magnitude, 4.6f);
        y = Math.max(y, -2);
        x = Math.max(x, 1f);
    }

    private boolean inBounds(float tx, float ty)
    {
        float magnitude = Mth.sqrt(tx * tx + ty * ty);
        return magnitude <= 4.6f && y >= -2 && tx >= 1;
    }

    public void targetRestPos()
    {
        targetEndPos = new Vec3(yaw, 1, 1);
    }

    public void onUse(Player user, InteractionHand hand)
    {
        if (interceptor.controller == null)
        {
            user.openMenu(new ExtendedScreenHandlerFactory()
            {
                @Override
                public void writeScreenOpeningData(ServerPlayer serverPlayerEntity, FriendlyByteBuf packetByteBuf)
                {
                    packetByteBuf.writeBlockPos(worldPosition);
                }

                @Override
                public Component getDisplayName()
                {
                    return Component.empty();
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
                {
                    return new ImplanterScreenHandler(playerInventory, inventory, syncId, ImplanterBlockEntity.this);
                }
            });
            InputInterceptors.activate(user, interceptor);
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putFloat("arm_x", x);
        nbt.putFloat("arm_y", y);
        nbt.putFloat("yaw", yaw);
        inventory.writeNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.x = nbt.getFloat("arm_x");
        this.y = nbt.getFloat("arm_y");
        this.yaw = nbt.getFloat("yaw");
        inventory.readNbt(nbt);
    }

    public float getYaw()
    {
        return yaw;
    }

    @Override
    public BlockPos getBasePos()
    {
        return worldPosition;
    }

    @Override
    public void spawnItem(@Nullable ResourceAmount<ItemVariant> stored)
    {
        if (stored == null)
            return;

        Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.25, worldPosition.getZ() + 0.5, stored.resource().toStack((int) stored.amount()));
    }

    public float getPolX()
    {
        return x;
    }

    public float getPolY()
    {
        return y;
    }

    @Override
    public boolean actuatorRemoved()
    {
        return false;
    }

    public Vec3 endOrigin()
    {
        double c = Mth.cos(yaw);
        double s = Mth.sin(yaw);
        double x1 = worldPosition.getX() + 0.5 + x * s;
        double z1 = worldPosition.getZ() + 0.5 + x * c;
        double y1 = worldPosition.getY() + y;

        return new Vec3(x1, y1, z1);
    }

    public Vec3 direction()
    {
        double dx = x * Mth.sin(yaw);
        double dz = x * Mth.cos(yaw);

        return new Vec3(dx, 0, dz).normalize();
    }

    @Nullable
    public Entity raycast()
    {
        Vec3 start = endOrigin();

        Vec3 direction = direction();
        Vec3 end = start.add(direction());
        AABB box = AABB.ofSize(start, 0.5, 0.5, 0.5).expandTowards(direction.x, 0, direction.z);
        var entities = level.getEntities(EntityTypeTest.forClass(Entity.class), box, e -> true);
        for (var entity : entities)
        {
            if (entity.getBoundingBox().clip(start, end).isPresent())
            {
                return entity;
            }
        }

        return null;
    }

    public Container getInventory()
    {
        return inventory;
    }

    @Override
    public double getX()
    {
        return worldPosition.getX() + 0.5 + x * Mth.sin(yaw);
    }

    @Override
    public double getY()
    {
        return worldPosition.getY() + y;
    }

    @Override
    public double getZ()
    {
        return worldPosition.getZ() + 0.5 + x * Mth.cos(yaw);
    }

    public void startApplySequenceManual()
    {
        action.start();
    }

    @Override
    public void setImplantTarget(PLC plc, Vec3 implantSite, Entity target)
    {
        Vec3 headToImplanter = new Vec3(worldPosition.getX() + 0.5, 0, worldPosition.getZ())
                .subtract(implantSite.x, 0, implantSite.z)
                .normalize()
                .scale(target.getBbWidth() / 2 + 0.3);

        try
        {
            setTargetPlatform(plc, implantSite.add(headToImplanter));
        }
        catch (NeepASM.RuntimeException ignored) { }
    }

    private void applyImplantManual()
    {
        Entity entity = raycast();
        ItemStack stack = inventory.getItem(0);
        if (entity != null && !stack.isEmpty())
        {
            MutateInPlace<Entity> mip = MutateInPlace.of(entity, level, worldPosition);
            ImplantStep step = new ImplantStep(stack.getItem(), 0);
            boolean success = ImplantInstruction.install(step, mip);

            if (success)
            {
                inventory.setItem(0, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public PLCActuator getPlcActuator()
    {
        return this;
    }

    @Override
    public void setStored(PLC plc, @Nullable ResourceAmount<ItemVariant> stored)
    {
        if (stored != null)
            inventory.setItem(0, new ItemStack(stored.resource().getItem(), (int) stored.amount()));
        else
            inventory.setItem(0, ItemStack.EMPTY);
    }

    @Override
    public @Nullable ResourceAmount<ItemVariant> getStored(PLC plc)
    {
        return new ResourceAmount<>(ItemVariant.of(inventory.getItem(0)), inventory.getItem(0).getCount());
    }

    @Override
    public void dumpStored(@Nullable PLC ignored)
    {
        if (!inventory.isEmpty())
            spawnItem(new ResourceAmount<>(ItemVariant.of(inventory.getItem(0)), inventory.getItem(0).getCount()));

        inventory.removeItemNoUpdate(0);
    }

    public void setTargetPlatform(PLC plc, @Nullable Vec3 target) throws NeepASM.RuntimeException
    {
        if (target == null)
        {
            this.targetEndPos = null;
        }
        else
        {
            float rx = (float) (target.x - worldPosition.getX() - 0.5f);
            float rz = (float) (target.z - worldPosition.getZ() - 0.5f);
            float tYaw = (float) Math.atan2(rx, rz);
            float tx = Mth.sqrt(rx * rx + rz * rz);
            float ty = (float) (target.y - worldPosition.getY());

            if (!inBounds(tx, ty))
            {
                throw new NeepASM.RuntimeException("implanter: position out of reach");
            }

            // I'd like to use Vector3f, but that's not available in 1.19
            this.targetEndPos = new Vec3(tYaw, tx, ty);
        }
    }

    @Override
    public void setTarget(PLC plc, @Nullable BlockPos target) throws NeepASM.RuntimeException
    {
        setTargetPlatform(plc, target != null ? Vec3.upFromBottomCenterOf(target, 0) : null);
    }

    @Override
    public boolean reachedTarget(@Nullable PLC unused)
    {
        if (targetEndPos == null)
            return true;

        return yaw == targetEndPos.x && x == targetEndPos.y && y == targetEndPos.z;
    }

    public void setYaw(float rotation)
    {
        this.yaw = Mth.DEG_TO_RAD * rotation;
    }

    private class ImplantAction extends Action.Simple<ImplantAction>
    {
        private final Sequence<ImplantAction> playSound = (parent, counter) ->
        {
            level.playSound(null, worldPosition, NMSounds.IMPLANT_INSTRUCTION_APPLY.get(), SoundSource.NEUTRAL, 1, 1);
            setSequence(this.wait);
        };

        private final Sequence<ImplantAction> wait = (parent, counter) ->
        {
            if (counter > 40)
                setSequence(this.implant);
        };

        private final Sequence<ImplantAction> implant = (parent, counter) ->
        {
            applyImplantManual();
            setSequence(null);
        };

        public void start()
        {
            setSequence(playSound);
        }
    }
}
