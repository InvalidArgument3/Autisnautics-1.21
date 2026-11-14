package com.neep.neepmeat.plc.screen;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.plc.block.entity.ProgramCabinetBlockEntity;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import com.neep.neepmeat.screen_handler.CardSlot;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ProgramCabinetScreenHandler extends BasicScreenHandler
{
    public final List<Slot> cabinetSlots;
    public final List<Slot> playerSlots;

    public final WidgetHandler handler;

    public ProgramCabinetScreenHandler(Inventory playerInventory, int syncId, Container blockInventory, ProgramCabinetBlockEntity be)
    {
        super(NMScreenHandlers.PROGRAM_CABINET, playerInventory, blockInventory, syncId, null);
        this.cabinetSlots = createSlotBlock(ProgramCabinetBlockEntity.SLOTS, blockInventory, 0, ProgramCardSlot::new);
        this.playerSlots = createPlayerSlots(0, 0, playerInventory);
        this.handler = addWidgetHandler(new WidgetHandler(playerInventory.player, be));
    }

    public ProgramCabinetScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, new SimpleContainer(5 * 6), null);
    }

    public ProgramCabinetScreenHandler(Inventory i, int s, ProgramCabinetBlockEntity be)
    {
        this(i, s, be.getInventory(), be);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack originalCopy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem())
        {
            ItemStack mutableStack = slot.getItem();
            originalCopy = mutableStack.copy();
            if (index < this.inventory.getContainerSize())
            {
                if (!this.moveItemStackTo(mutableStack, this.inventory.getContainerSize(), this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(mutableStack, originalCopy);
                slot.remove(64); // This 'fixes' a strange thing in WorkstationScreenHandler.
                slot.setChanged();
            }
            else
            {
                ItemStack nextMutable = mutableStack.copyWithCount(1);
                if (!this.moveItemStackTo(nextMutable, 0, this.inventory.getContainerSize(), false))
                {
                    return ItemStack.EMPTY;
                }

                if (nextMutable.isEmpty())
                {
                    mutableStack.shrink(1);
                    slot.setByPlayer(mutableStack);
                }
                else
                {
                    slot.setChanged();
                }

                return ItemStack.EMPTY;
            }
        }
        return originalCopy;
    }

    /// Default implementation does not account for the slot's max count.
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean fromLast)
    {
        boolean bl = false;
        int i = startIndex;
        if (fromLast)
        {
            i = endIndex - 1;
        }

        if (stack.isStackable())
        {
            while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex))
            {
                Slot slot = this.slots.get(i);
                ItemStack slotStack = slot.getItem();
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, slotStack))
                {
                    int j = slotStack.getCount() + stack.getCount();
                    int maxCount = Math.min(slot.getMaxStackSize(), slotStack.getMaxStackSize());
                    if (j <= maxCount)
                    {
                        stack.setCount(0);
                        slotStack.setCount(j);
                        slot.setChanged();
                        bl = true;
                    }
                    else if (slotStack.getCount() < maxCount)
                    {
                        stack.shrink(maxCount - slotStack.getCount());
                        slotStack.setCount(maxCount);
                        slot.setChanged();
                        bl = true;
                    }
                }

                if (fromLast)
                {
                    i--;
                }
                else
                {
                    i++;
                }
            }
        }

        if (!stack.isEmpty())
        {
            if (fromLast)
            {
                i = endIndex - 1;
            }
            else
            {
                i = startIndex;
            }

            while (fromLast ? i >= startIndex : i < endIndex)
            {
                Slot slotx = this.slots.get(i);
                ItemStack itemStackx = slotx.getItem();
                if (itemStackx.isEmpty() && slotx.mayPlace(stack))
                {
                    if (stack.getCount() > slotx.getMaxStackSize())
                    {
                        slotx.setByPlayer(stack.split(slotx.getMaxStackSize()));
                    }
                    else
                    {
                        slotx.setByPlayer(stack.split(stack.getCount()));
                    }

                    slotx.setChanged();
                    bl = true;
                    break;
                }

                if (fromLast)
                {
                    i--;
                }
                else
                {
                    i++;
                }
            }
        }

        return bl;
    }

    private static class ProgramCardSlot extends CardSlot
    {
        public ProgramCardSlot(Container inventory, int index, int x, int y)
        {
            super(inventory, index, x, y);
        }

        @Override
        public Item validItem()
        {
            return PLCBlocks.PROGRAM_CARD;
        }
    }

    public static class WidgetHandler extends ConfigWidgetHandler<WidgetHandler.Values>
    {
        private final ProgramCabinetBlockEntity be;

        protected WidgetHandler(Player player, ProgramCabinetBlockEntity be)
        {
            super(player, Values.PARAM_CODEC);
            this.be = be;
        }

        @Override
        protected Values getValues()
        {
            return new Values(be.segmentOffset());
        }

        @Override
        protected void setValues(Values values)
        {
            if (be != null)
                be.setMemoryOffset(values.offset);
        }

        public record Values(int offset)
        {
            public static final ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class).param(ParamCodec.INT).build();
        }
    }
}
