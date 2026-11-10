package com.neep.neepmeat.plc.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.machine.surgical_controller.BuilderRobot;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.robot.PLCActuator;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class BuilderBlockEntity extends SyncableBlockEntity implements PLCActuator, PLCActuator.Provider
{
    public static int SLOTS = 27;
    private final BuilderRobot robot = new BuilderRobot(this);

    private final ImplementedInventory inventory = ImplementedInventory.ofSize(SLOTS, this::setChanged);
    private final Storage<ItemVariant> storage = InventoryStorage.of(inventory, null);

    private int selectedSlot = 0;
    private String selectedPattern = "";

    public BuilderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
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

    @Override
    public boolean actuatorRemoved()
    {
        return isRemoved();
    }

    public BuilderRobot getRobot()
    {
        return robot;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.put("robot", robot.writeNbt(new CompoundTag()));
        nbt.put("inventory", inventory.writeNbt(new CompoundTag()));
        nbt.putInt("selected_slot", selectedSlot);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.robot.readNbt(nbt.getCompound("robot"));
        this.inventory.readNbt(nbt.getCompound("inventory"));
        this.selectedSlot = nbt.getInt("selected_slot");
    }

    public Storage<ItemVariant> getStorage(Direction face)
    {
        return storage;
    }

    public void serverTick()
    {
        robot.tick();
        if (robot.posDirty)
        {
            sync();
            robot.posDirty = false;
        }

//        if (world.getTime() % 4 == 0)
//            selectedSlot = (selectedSlot + 1) % SLOTS;
    }

    public Container getInventory()
    {
        return inventory;
    }

    public int getSelectedSlot()
    {
        return selectedSlot;
    }

    public boolean placeSelected(BlockPos pos, Direction face)
    {
        if (selectedSlot == -1)
        {
            Pattern pattern = Pattern.compile(NeepAsmParser.convertToRegex(this.selectedPattern));
            for (int i = 0; i < inventory.getContainerSize(); i++)
            {
                if (Instructions.match(pattern, inventory.getItem(i).getItem()))
                {
                    return place((ServerLevel) level, inventory.getItem(i), pos, face);
                }
            }
            return false;
        }
        else if (selectedSlot < 0 || selectedSlot > inventory.getContainerSize())
        {
            return false;
        }
        else
        {
            return place((ServerLevel) level, inventory.getItem(selectedSlot), pos, face);
        }
    }

    public boolean place(ServerLevel serverWorld, ItemStack stack, BlockPos targetPos, Direction facing)
    {
        if (stack.getItem() instanceof BlockItem blockItem)
        {
            var fp = FakePlayer.get(serverWorld);

//            BlockPos targetPos = robot.getBlockPos();
//            Direction facing = robot.getFacing();
            Vec3 hitPos = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
            BlockHitResult hit = new BlockHitResult(hitPos, facing.getOpposite(), targetPos, true);

            BlockPlaceContext ipc = new BlockPlaceContext(fp, InteractionHand.MAIN_HAND, stack,
                    hit);
            var result = blockItem.place(ipc);

            return result.consumesAction();
        }

        return false;
    }

    public boolean breakBlock(BlockPos pos, Consumer<ItemStack> stackConsumer)
    {
        BlockState state = level.getBlockState(pos);
        if (
                level instanceof ServerLevel serverWorld
                && !state.isAir()
                && state.getDestroySpeed(level, pos) <= Blocks.OBSIDIAN.defaultDestroyTime())
        {
            var stacks = Block.getDrops(state, serverWorld, pos, level.getBlockEntity(pos));
            if (level.destroyBlock(pos, false))
            {
                stacks.forEach(stackConsumer);
                return true;
            }
        }

        return false;
    }

    @Override
    public void setTarget(PLC plc, @Nullable BlockPos target) throws NeepASM.RuntimeException
    {
        robot.setTarget(plc, target);
    }

    @Override
    public boolean reachedTarget(PLC plc)
    {
        return robot.reachedTarget(plc);
    }

    @Override
    public PLCActuator getPlcActuator()
    {
        return this;
    }

    public void setSlot(int i)
    {
        i = Mth.clamp(i, 0, SLOTS);
        selectedSlot = i;
        setChanged();
    }

    public void selectItem(String pattern)
    {
        this.selectedPattern = pattern;
        this.selectedSlot = -1;
        setChanged();
    }

    public String getSelectedPattern()
    {
        return selectedPattern;
    }
}
