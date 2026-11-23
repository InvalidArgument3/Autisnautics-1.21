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

package com.neep.meatweapons.screen;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.init.MWScreenHandlers;
import com.neep.meatweapons.item.meatgun.Meatgun;
import com.neep.meatweapons.item.meatgun.MeatgunModuleItem;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.meatgun.module.ModuleSlot;
import com.neep.neepmeat.implant.entity.ImplantManager;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.machine.upgrade_manager.ImplantManagerDelegate;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TinkerTableScreenHandler extends BasicScreenHandler
{
    public static final int BACKGROUND_WIDTH = 350;
    public static final int BACKGROUND_HEIGHT = 200;

    public static final ResourceLocation CHANNEL_ID = ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "chunnel");
    public static final ChannelFormat<SlotClick> CHANNEL_FORMAT = ChannelFormat.builder(SlotClick.class)
            .param(ParamCodec.UUID)
            .param(ParamCodec.INT)
            .build();

    public final ChannelManager<SlotClick> slotClick;
    public final ChannelManager<Runnable> openImplantConfigC2S;
    public final ChannelManager<SetName> setNameC2SS2C;

    private final Container blockInv;
    private final BlockPos pos;

    public final List<Slot> playerSlots;
    public final Slot itemSlot;
    private boolean nameDirty = true;

    public TinkerTableScreenHandler(int syncId, Inventory playerInventory)
    {
        this(syncId, playerInventory, new SimpleContainer(1), BlockPos.ZERO);
    }

    public TinkerTableScreenHandler(int syncId, Inventory playerInventory, Container blockInv, BlockPos pos)
    {
        super(MWScreenHandlers.MEATGUN, playerInventory, blockInv, syncId, null);
        this.slotClick = ChannelManager.create(CHANNEL_ID, CHANNEL_FORMAT, playerInventory.player);
        this.openImplantConfigC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "tinker_table/open_implant_config"), ChannelFormat.RUNNABLE, playerInventory.player);
        this.setNameC2SS2C = ChannelManager.create(ResourceLocation.tryBuild(MeatWeapons.NAMESPACE, "tinker_table/set_name"), ChannelFormat.builder(SetName.class).param(ParamCodec.STRING).build(), playerInventory.player);
        this.blockInv = blockInv;
        this.pos = pos;

        this.itemSlot = addSlot(new Slot(blockInv, 0, 8, BACKGROUND_HEIGHT - 2 - 21)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return stack.getItem() instanceof Meatgun;
            }

            @Override
            public void setByPlayer(ItemStack stack)
            {
                super.setByPlayer(stack);
                nameDirty = true;
            }
        });

        this.playerSlots = createPlayerSlots(0, 0, playerInventory);

        slotClick.receiver(this::onSlotClick);

        if (!isClient())
        {
            openImplantConfigC2S.receiver(this::openImplantConfig);
            setNameC2SS2C.receiver(this::receiveNewName);
        }
    }

    private void openImplantConfig()
    {
        ImplantManager manager = NMComponents.IMPLANT_MANAGER.getNullable(blockInv.getItem(0));
        if (manager != null)
        {
            playerInventory.player.openMenu(ImplantManagerScreenHandler.getFactory(
                    (ImplantManagerDelegate) playerInventory.player.level().getBlockEntity(pos),
                    pos,
                    false)
            );
        }
    }

    private void receiveNewName(String newName)
    {
        ItemStack stack = itemSlot.getItem();
        MeatgunComponent meatgun = MWComponents.MEATGUN.getNullable(stack);
        if (meatgun != null)
        {
            stack.setHoverName(Component.nullToEmpty(newName));
        }

        nameDirty = true;
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
        if (ready)
        {
            if (nameDirty)
            {
                ItemStack stack = itemSlot.getItem();
                MeatgunComponent meatgun = MWComponents.MEATGUN.getNullable(stack);
                if (meatgun != null)
                {
                    setNameC2SS2C.emitter().apply(stack.getHoverName().getString());
                }
                else
                {
                    setNameC2SS2C.emitter().apply("");
                }
                nameDirty = false;
            }
        }
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        slotClick.close();
        openImplantConfigC2S.close();
        setNameC2SS2C.close();
    }

    public void onSlotClick(UUID uuid, int slotIdx)
    {
        MeatgunComponent meatgun = MWComponents.MEATGUN.getNullable(itemSlot.getItem());
        if (meatgun != null)
        {
            MeatgunModule parent = meatgun.getRootHolder().moduleCache().findModule(uuid);
            if (parent != null)
            {
                ModuleSlot slot1 = parent.getChildren().get(slotIdx);

                boolean slotEmpty = slot1.get() == MeatgunModule.DEFAULT;
                boolean canChange = true;
                if (!slotEmpty)
                {
                    for (var childSlot : slot1.get().getChildren())
                    {
                        if (childSlot.get() != MeatgunModule.DEFAULT)
                        {
                            canChange = false;
                        }
                    }
                }

                boolean cursorEmpty = getCarried().isEmpty();

                if (canChange)
                {
                    RootModuleHolder holder = meatgun.getRootHolder();
                    if (!slotEmpty)
                    {
                        if (cursorEmpty)
                        {
                            // Take
                            ItemStack moduleStack = MeatgunModuleItem.get(slot1.get());

                            if (moduleStack.isEmpty())
                                return;

                            playSound(NMSounds.MEATGUN_MODULE_REMOVE.get());
                            setCarried(moduleStack);
                            slot1.set(MeatgunModule.DEFAULT);
                            sendAllDataToRemote();
                        }
                        else if (getCarried().getCount() == 1)
                        {
                            // Swap
                            ItemStack cursorStack = getCarried();
                            MeatgunModule.Type<?> cursorType = MeatgunModuleItem.get(cursorStack);
                            if (cursorType != MeatgunModule.DEFAULT_TYPE && slot1.applicable(cursorType))
                            {
                                MeatgunModule previous = slot1.get();
                                slot1.set(MeatgunModule.DEFAULT);
                                if (holder.canSupport(cursorType))
                                {
                                    playSound(NMSounds.MEATGUN_MODULE_PLACE.get());
                                    setCarried(MeatgunModuleItem.get(previous));

                                    slot1.set(cursorType.create(holder.getListener(), parent, cursorStack));
                                    sendAllDataToRemote();
                                }
                                else
                                {
                                    slot1.set(previous);
                                }
                            }
                        }
                    }
                    else if (!cursorEmpty)
                    {
                        // Set
                        ItemStack cursorStack = getCarried();
                        MeatgunModule.Type<?> cursorType = MeatgunModuleItem.get(cursorStack);
                        if (cursorType != MeatgunModule.DEFAULT_TYPE && slot1.applicable(cursorType))
                        {
                            if (holder.canSupport(cursorType))
                            {
                                playSound(NMSounds.MEATGUN_MODULE_PLACE.get());
                                slot1.set(cursorType.create(holder.getListener(), parent, cursorStack));
                                getCarried().shrink(1);
                                sendAllDataToRemote();
                            }
                        }
                    }
                }
            }
        }
        inventory.setChanged();
    }

    private void playSound(SoundEvent soundEvent)
    {
        if (playerInventory.player.level() instanceof ServerLevel serverWorld)
        {
            serverWorld.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 0.4f, 1f);
        }
    }

    public interface SlotClick
    {
        void apply(UUID uuid, int slot);
    }

    public interface SetName
    {
        void apply(String name);
    }
}
