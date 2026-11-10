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

package com.neep.neepmeat.machine.surgical_controller;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.network.plc.PLCRobotC2S;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import com.neep.neepmeat.plc.robot.PLCRobot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;

import java.util.Objects;

import static com.neep.neepmeat.machine.surgical_controller.SurgicalRobot.MovementState.*;

public class SurgicalRobot implements PLCRobot, NbtSerialisable
{
    private final PLCBlockEntity parent;
    private MovementState movementState = MovementState.STATE_IDLE;

    private boolean initial = true;

    public double cameraX;
    public double cameraY;
    public double cameraZ;
    private double x;
    private double y;
    private double z;
    public double clientX;
    public double clientY;
    public double clientZ;
    public float clientYaw;

    private double vx;
    private double vy;
    private double vz;

    public double prevX;
    public double prevY;
    public double prevZ;

    @Nullable private Vec3 targetPos;
    @Nullable private BlockPos target;

    private final BlockPos basePos;
    private final Vec3 dockingPos;
    private final Vec3 attachPos;

    @Nullable private Player controller;

    private float pitch;
    private float yaw;

    // I can't be bothered to do this properly
    public ResourceAmount<ItemVariant> stored;
    private boolean moved = true;

    public SurgicalRobot(PLCBlockEntity parent)
    {
        this.parent = parent;
        this.basePos = parent.getBlockPos();
        this.dockingPos = Vec3.upFromBottomCenterOf(basePos, 0.5);
        this.attachPos = Vec3.upFromBottomCenterOf(basePos, 1.4);
        this.x = dockingPos.x;
        this.y = dockingPos.y;
        this.z = dockingPos.z;
        this.clientX = x;
        this.clientY = y;
        this.clientZ = z;

        this.cameraX = x;
        this.cameraY = y;
        this.cameraZ = z;
    }

    public void setController(@Nullable Player player)
    {
        this.controller = player;
//        if (controller == null && shouldUpdatePosition(parent.getWorld()))
//        {
//            setTarget(new BlockPos(x, y, z));
//        }
    }

    @Nullable
    public Player getController()
    {
        return controller;
    }

    public boolean shouldUpdatePosition(Level world)
    {
        if (!world.isClientSide())
        {
            return (getController() == null || parent.actionBlocksController());
        }
        else
        {
            return getController() != null;
        }
    }

    public void tick()
    {
        if (shouldUpdatePosition(parent.getLevel()))
        {
            move();
        }
    }

    protected void move()
    {
        if (movementState == MovementState.STATE_IDLE)
        {
            if (!Objects.equals(target, basePos))
            {
                movementState = MovementState.STATE_LAUNCHING;
            }
        }
        else if (movementState == MovementState.STATE_LAUNCHING)
        {
            if (moveTo(attachPos)) movementState = STATE_ACTIVE;
        }
        else if (movementState == STATE_ACTIVE)
        {
            if (targetPos != null)
            {
                moveTo(targetPos);
            }
        }
        else if (movementState == MovementState.STATE_RETURNING)
        {
            if (moveTo(attachPos))
            {
                movementState = STATE_DOCKING;
            }
        }
        else if (movementState == STATE_DOCKING)
        {
            if (moveTo(dockingPos)) movementState = MovementState.STATE_IDLE;
        }
    }

    private boolean moveTo(Vec3 toPos)
    {
        if (toPos.distanceToSqr(x, y, z) >= 0.01 * 0.01)
        {
            double dx = (toPos.x - x);
            double dy = (toPos.y - y);
            double dz = (toPos.z - z);
            this.yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double vx = dx;
            double vy = dy;
            double vz = dz;

            // Normalise motion vector if distance is greater than base movement step
            if (dist > getSpeed())
            {
                vx = vx / dist * getSpeed();
                vy = vy / dist * getSpeed();
                vz = vz / dist * getSpeed();
            }

            // Step position
            x += vx;
            y += vy;
            z += vz;
            moved = true;
            return false;
        }
        else return true;
    }

    @Override
    public void setTarget(@Nullable PLC plc, @Nullable BlockPos target)
    {
        if (target == null)
        {
            this.target = null;
            this.targetPos = null;
            return;
        }

        this.target = target.immutable();
        this.targetPos = getTarget(this.target);
    }

    private Vec3 getTarget(BlockPos target)
    {
        return Vec3.atCenterOf(target);
    }

    public double getSpeed()
    {
        return 0.1;
    }

    public Vec3 getPos()
    {
        return new Vec3(x, y, z);
    }

    public BlockPos getBlockPos()
    {
        return BlockPos.containing(x, y, z);
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    @Override
    public boolean actuatorRemoved()
    {
        return parent.isRemoved();
    }

    private static final String NBT_MOVEMENT_STATE = "movementState";

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        CompoundTag robotNbt = new CompoundTag();
        robotNbt.putInt(NBT_MOVEMENT_STATE, movementState.ordinal());
        robotNbt.putDouble("rx", x);
        robotNbt.putDouble("ry", y);
        robotNbt.putDouble("rz", z);

        robotNbt.putFloat("pitch", pitch);
        robotNbt.putFloat("yaw", yaw);
        nbt.put("robot", robotNbt);


        if (stored != null)
        {
            CompoundTag storedNbt = new CompoundTag();
            storedNbt.put("item", stored.resource().toNbt());
            storedNbt.putLong("amount", stored.amount());
            nbt.put("stored", storedNbt);
        }

        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        CompoundTag robotNbt = nbt.getCompound("robot");
        if (robotNbt.isEmpty())
        {
            // Migration from old format

        }
        else
        {
            this.movementState = MovementState.values()[robotNbt.getInt(NBT_MOVEMENT_STATE)];
            this.x = robotNbt.getDouble("rx");
            this.y = robotNbt.getDouble("ry");
            this.z = robotNbt.getDouble("rz");

            this.pitch = robotNbt.getFloat("pitch");
            this.yaw = robotNbt.getFloat("yaw");

            if (initial)
            {
                this.clientX = x;
                this.clientY = y;
                this.clientZ = z;
                initial = false;
            }

            this.clientYaw = yaw;
        }

        if (nbt.contains("stored"))
        {
            CompoundTag storedNbt = nbt.getCompound("stored");
            stored = new ResourceAmount<>(
                    ItemVariant.fromNbt(storedNbt.getCompound("item")),
                    storedNbt.getLong("amount")
            );
        }
    }

    public boolean isActive()
    {
        return movementState == STATE_ACTIVE;
    }

    @Override
    public boolean reachedTarget(PLC plc)
    {
        return targetPos == null || targetPos.distanceToSqr(x, y, z) <= 0.1 * 0.1;
    }

    public void returnToBase()
    {
        movementState = MovementState.STATE_RETURNING;
        setTarget(null, basePos);
    }

    public void setPos(double x, double y, double z)
    {
        // Do not accept update packets from the client when overriding the controller
        if (!parent.getLevel().isClientSide() && shouldUpdatePosition(parent.getLevel()))
            return;

        this.x = x;
        this.y = y;
        this.z = z;
        parent.setChanged();
    }

    public void stay()
    {
        this.setTarget(null, null);
    }

    public float getPitch()
    {
        return pitch;
    }

    public float getYaw()
    {
        return yaw;
    }

    public void setPitchYaw(float pitch, float yaw)
    {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public void spawnItem(@Nullable ResourceAmount<ItemVariant> stored)
    {
        if (stored == null)
            return;
        Containers.dropItemStack(parent.getLevel(), x, y, z, stored.resource().toStack((int) stored.amount()));
    }

    public void dumpStored(PLC plc)
    {
        if (stored != null)
        {
            spawnItem(stored);
            stored = null;
        }
    }

    @Override
    public void setStored(PLC plc, @Nullable ResourceAmount<ItemVariant> stored)
    {
        this.stored = stored;
    }

    @Override
    public BlockPos getBasePos()
    {
        return parent.getBlockPos();
    }

    @Override
    public @Nullable ResourceAmount<ItemVariant> getStored(PLC plc)
    {
        return stored;
    }

    public void syncPosition(ServerLevel serverWorld)
    {
        if (!moved)
            return;

        PLCRobotC2S.send(parent, serverWorld);
        moved = false;
    }

    public boolean getState()
    {
        return movementState == STATE_RETURNING;
    }

    @Override
    public void setImplantTarget(PLC plc, Vec3 implantSite, Entity target)
    {
        setTarget(plc, new BlockPos(target.getBlockX(),
                (int) Math.ceil(target.getY() + target.getBbHeight()),
                target.getBlockZ()));
    }

    public enum MovementState
    {
        STATE_IDLE,
        STATE_LAUNCHING,
        STATE_ACTIVE,
        STATE_RETURNING,
        STATE_DOCKING
    }

    @Environment(value = Dist.CLIENT)
    public static class Client
    {
        private final SurgicalRobot robot;
        private final PLCBlockEntity be;

        public boolean forwardKey;
        public boolean backKey;
        public boolean leftKey;
        public boolean rightKey;
        public boolean upKey;
        public boolean downKey;

        public Client(SurgicalRobot robot, PLCBlockEntity be)
        {
            this.robot = robot;
            this.be = be;
        }

        public void tick()
        {
            motion();

            if (be.getLevel().getGameTime() % 4 == 0)
            {
                PLCRobotC2S.Client.send(be);
            }
        }

        public void motion()
        {
            if (!robot.parent.overrideController())
            {
                if (robot.controller == null)
                    return;

                Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

                boolean shouldTransformMotion = false;

                if (MeatLib.vsUtil != null)
                {
                    shouldTransformMotion = MeatLib.vsUtil.hasShipAtPosition(be.getBlockPos(), Minecraft.getInstance().level);
                }

                double speed = 0.2;
                float pitch = camera.getXRot();
                float yaw = camera.getYRot();
                double vx = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
                double vz = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
                Vec3 normal = new Vec3(-vz, 0, vx);

                double fvx = 0;
                double fvy = 0;
                double fvz = 0;

                if (forwardKey)
                {
                    fvx += vx;
                    fvz += vz;
                }
                if (backKey)
                {
                    fvx -= vx;
                    fvz -= vz;
                }

                if (leftKey)
                {
                    fvx -= normal.x;
                    fvz -= normal.z;
                }
                if (rightKey)
                {
                    fvx += normal.x;
                    fvz += normal.z;
                }

                if (upKey)
                {
                    fvy += speed;
                }
                if (downKey)
                {
                    fvy -= speed;
                }

                Quaterniondc rotation = new Quaterniond();

                if (shouldTransformMotion)
                {
                    rotation = MeatLib.vsUtil.getShipToWorldRotation(be.getBlockPos(), Minecraft.getInstance().level);
                }

                double l = Math.sqrt(fvx * fvx + fvz * fvz);
                if (l != 0)
                {
                    fvx = fvx / l * speed;
                    fvz = fvz / l * speed;

                    robot.vx = fvx;
                    robot.vz = fvz;
                }

                if (fvy != 0)
                {
                    robot.vy = fvy;
                }

                if (shouldTransformMotion && rotation != null)
                {
                    Vector3d holder = new Vector3d(fvx, fvy, fvz);
                    holder.rotate(rotation.invert(new Quaterniond()));
                    robot.vx = holder.x;
                    robot.vy = holder.y;
                    robot.vz = holder.z;
                }

                robot.x += robot.vx;
                robot.y += robot.vy;
                robot.z += robot.vz;

                robot.vz *= 0.05;
                robot.vy *= 0.05;
                robot.vx *= 0.05;

                // More awful kjank
                robot.movementState = STATE_ACTIVE;
                robot.setTarget(null, robot.getBlockPos());
            }
        }

        public void resetKeys()
        {
            forwardKey = false;
            backKey = false;
            leftKey = false;
            rightKey = false;
            upKey = false;
            downKey = false;
        }
    }
}
