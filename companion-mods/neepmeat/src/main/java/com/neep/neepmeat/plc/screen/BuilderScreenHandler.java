package com.neep.neepmeat.plc.screen;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.plc.block.entity.BuilderBlockEntity;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class BuilderScreenHandler extends BasicScreenHandler
{
    public final ChannelManager<SyncSelection> selectedSlotS2C;

    private IntSupplier selected;
    private Supplier<String> patternSupplier;

    public final List<Slot> internalSlots;
    public final List<Slot> playerSlots;

    public int selectedSlot = -1;
    public String pattern = "";

    public BuilderScreenHandler(Inventory playerInventory, int syncId, Container inventory, IntSupplier selected, Supplier<String> patternSupplier)
    {
        super(NMScreenHandlers.BUILDER, playerInventory, inventory, syncId, null);
        internalSlots = createSlotBlock(BuilderBlockEntity.SLOTS, inventory, 0, Slot::new);
        playerSlots = createPlayerSlots(0, 0, playerInventory);

        selectedSlotS2C = addClosable(ChannelManager.create(
                ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "builder/selected_slot"),
                ChannelFormat.builder(SyncSelection.class)
                        .param(ParamCodec.INT)
                        .param(ParamCodec.STRING)
                        .build(), playerInventory.player));
        this.selected = selected;
        this.patternSupplier = patternSupplier;

        if (MeatLib.isClient())
        {
            selectedSlotS2C.receiver((s, p) ->
            {
                this.selectedSlot = s;
                this.pattern = p;
            });
        }
    }

    public BuilderScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, new SimpleContainer(BuilderBlockEntity.SLOTS), () -> -1, () -> "");
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
        if (ready)
        {
            selectedSlotS2C.emitter().apply(selected.getAsInt(), patternSupplier.get());
        }
    }

    public interface SyncSelection
    {
        void apply(int slot, @Nullable String pattern);
    }
}
