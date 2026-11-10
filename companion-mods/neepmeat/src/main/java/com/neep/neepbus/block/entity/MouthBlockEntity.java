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

package com.neep.neepbus.block.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.screen.MouthScreenHandler;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.SimpleEntry;
import com.neep.neepbus.util.WritePort;
import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.network.NEEPSpeakSayS2C;
import com.neep.neepspeak.NEEPSpeakException;
import com.neep.neepspeak.PhonemeList;
import com.neep.neepspeak.parser.Phonemiser;
import com.neep.neepspeak.parser.RuleParser;
import com.neep.neepspeak.phoneme.PhonemeSet;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MouthBlockEntity extends SyncableBlockEntity implements ConfigProvider
{
    public static final Phonemiser PHONEMISER = new Phonemiser(PhonemeSet.NORMAL);

    static
    {
        MouthBlockEntity.PHONEMISER.addRule(new String[]{
                "error", "EHRUXR",
                "minecraft", "MAYNKRAXFT",
                "neepmeat", "NIYPMIYT",
                "water", "WAATER",
                "<n>bread", "EHN",
                "bread", "BREHD",
                "cow", "KOW",
        });

        MouthBlockEntity.PHONEMISER.setupDefaultRules();
    }

    private String message = "";
    private boolean autoSay = true;
    private boolean phonetic = false;
    private int radius = 10;

    private long lastTrigger;

    private final WritePort setMessage = this::setMessage;
    private final WritePort trigger = dataVariant -> trigger();
    private final WritePort setPhonetic = this::setPhonetic;

    private void setPhonetic(DataVariant v)
    {
        this.phonetic = v.boolValue();
        setChanged();
    }

    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .input(new SimpleEntry("Set message"), setMessage)
            .input(new SimpleEntry("Set phonetic"), setPhonetic)
            .input(new SimpleEntry("Say"), trigger)
            .applyChanges(this)
            .build();

    private String error = "";

    public MouthBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public static PhonemeList parse(String msg, boolean phonetic) throws NEEPSpeakException
    {
        return phonetic ?
                new PhonemeList(RuleParser.parseSamPhonetic(msg, PhonemeSet.NORMAL))
                : PHONEMISER.process(msg);
    }

    private void setMessage(DataVariant dataVariant)
    {
        message = dataVariant.stringValue();
        sync();
        if (autoSay)
            trigger();
    }

    public void trigger()
    {
        if (level.getGameTime() - lastTrigger < 10)
            return;

        lastTrigger = level.getGameTime();

        error = "";
        try
        {
            parse(message, phonetic);

            PlayerLookup.around((ServerLevel) level, worldPosition, radius).forEach(p ->
            {
                NEEPSpeakSayS2C.CHANNEL.emitter(p).apply(message, worldPosition, phonetic);
            });
        }
        catch (NEEPSpeakException e)
        {
            error = e.getMessage();
        }
    }

    public void setValues(String message, boolean autoSay, boolean phonetic, int radius)
    {
        this.message = message.substring(0, Math.min(1000, message.length()));
        this.autoSay = autoSay;
        this.phonetic = phonetic;
        this.radius = Math.min(radius, 50);
        sync();
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.put("config", config.writeNbt(new CompoundTag()));
        nbt.putString("message", message);
        nbt.putBoolean("auto_say",  autoSay);
        nbt.putBoolean("phonetic", phonetic);
        nbt.putInt("radius", radius);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        config.readNbt(nbt.getCompound("config"));
        this.message = nbt.getString("message");
        this.autoSay = nbt.getBoolean("auto_say");
        this.phonetic = nbt.getBoolean("phonetic");
        this.radius = nbt.getInt("radius");
    }

    public static String getMessage(ItemStack stack)
    {
        String message = BlockItem.getBlockEntityData(stack).getString("message");

        if (message.length() > 25)
            message = message.substring(0, 25) + "...";

        return message;
    }

    public MouthScreenHandler.Values getValues()
    {
       return new MouthScreenHandler.Values(message, autoSay, phonetic, radius);
    }

    public String getError()
    {
        return error;
    }

    public boolean isEmpty()
    {
        return message.isEmpty();
    }
}
