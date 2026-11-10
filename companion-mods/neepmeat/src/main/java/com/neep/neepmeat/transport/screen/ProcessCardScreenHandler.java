package com.neep.neepmeat.transport.screen;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.screen.WidgetHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import com.neep.neepmeat.transport.item.ProcessCardComponent;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ProcessCardScreenHandler extends BasicScreenHandler
{
    public final SlotsWidgetHandler slotsWidgetHandler;
    public final List<Slot> playerSlots;

    public ProcessCardScreenHandler(Inventory playerInventory, int syncId, ProcessCardComponent component)
    {
        super(TransportScreenHandlers.PROCESS_CARD, playerInventory, syncId);
        slotsWidgetHandler = addWidgetHandler(new SlotsWidgetHandler(component, playerInventory.player));

        this.playerSlots = createPlayerSlots(0, 0, playerInventory);
    }

    public ProcessCardScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, null);
    }

    public static class SlotsWidgetHandler implements WidgetHandler
    {
        @Nullable private final ProcessCardComponent component;
        private boolean dirty = true;
        
        public final ChannelManager<Consumer<CompoundTag>> updateItemsC2SS2C;
        public final ChannelManager<Consumer<ItemStack>> updateResultC2SS2C;

        public final ImplementedInventory inventory;
        public ItemStack resultStack = ItemStack.EMPTY;

        public SlotsWidgetHandler(@Nullable ProcessCardComponent component, Player player)
        {
            this.component = component;
            this.updateItemsC2SS2C = ChannelManager.create(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "process_card/items"),
                    ChannelFormat.builder(ParamCodecs.<Consumer<CompoundTag>>as(Consumer.class)).param(ParamCodec.NBT).build(), player);
            this.updateResultC2SS2C = ChannelManager.create(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "process_card/result"),
                    ChannelFormat.builder(ParamCodecs.<Consumer<ItemStack>>as(Consumer.class)).param(ParamCodecs.ITEM_STACK).build(), player);

            if (MeatLib.isClient())
            {
                updateItemsC2SS2C.receiver(this::receiveItemsNbtClient);
                updateResultC2SS2C.receiver(this::receiveResultClient);
                inventory = ImplementedInventory.ofSize(ProcessCardComponent.SIZE, this::updateToServer);
            }
            else
            {
                updateItemsC2SS2C.receiver(this::receiveItemsNbtServer);
                updateResultC2SS2C.receiver(this::receiveResultServer);
                inventory = ImplementedInventory.ofSize(ProcessCardComponent.SIZE);
            }
        }

        private void receiveResultServer(ItemStack stack)
        {
            if (component != null)
            {
                component.setResult(new ResourceAmount<>(ItemVariant.of(stack), stack.getCount()));
                dirty = true;
            }
        }

        private void receiveResultClient(ItemStack stack)
        {
            this.resultStack = stack;
        }

        private void updateToServer()
        {
            updateItemsC2SS2C.emitter().accept(inventory.writeNbt(new CompoundTag()));
        }

        private void receiveItemsNbtClient(CompoundTag nbt)
        {
            inventory.readNbt(nbt);
        }

        private void receiveItemsNbtServer(CompoundTag nbt)
        {
            inventory.readNbt(nbt);
            if (component != null)
                component.setItemsNbt(nbt);

            dirty = true;
        }

        @Override
        public void sendUpdates()
        {
            if (dirty && component != null)
            {
                dirty = false;
                updateItemsC2SS2C.emitter().accept(component.getItemsNbt());
                updateResultC2SS2C.emitter().accept(component.getResultStack());
            }
        }

        @Override
        public void close()
        {
            if (MeatLib.isClient())
            {
                updateToServer();
            }

            updateItemsC2SS2C.close();
            updateResultC2SS2C.close();
        }

        public void setResultStack(ItemStack s)
        {
            updateResultC2SS2C.emitter().accept(s);
        }
    }
}
