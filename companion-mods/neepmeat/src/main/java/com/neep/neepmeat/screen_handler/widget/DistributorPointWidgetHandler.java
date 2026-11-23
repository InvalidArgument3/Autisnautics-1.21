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

package com.neep.neepmeat.screen_handler.widget;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPoint;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPointBlockEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class DistributorPointWidgetHandler extends ConfigWidgetHandler<DistributorPointWidgetHandler.Values>
{
    @Nullable private final DistributorPoint be;

    public DistributorPointWidgetHandler(Player playerEntity, @Nullable DistributorPoint be)
    {
        super(playerEntity, Values.PARAM_CODEC);
        this.be = be;
    }

    @Override
    public void sendUpdates()
    {
        super.sendUpdates();
    }

    @Override
    protected Values getValues()
    {
        return new Values(be.getAddress(), be.getMode(), be.getMaxCooldown(), be.shouldAutoSend());
    }

    @Override
    protected void setValues(Values values)
    {
        be.setAddress(values.addr, values.mode, values.maxCooldown, values.autoSend);
    }

    @Override
    public void close()
    {
        super.close();
    }

    public record Values(String addr, DistributorPointBlockEntity.Mode mode, int maxCooldown, boolean autoSend)
    {
        public static ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class)
                .param(ParamCodec.STRING)
                .param(ParamCodec.ofEnum(DistributorPointBlockEntity.Mode.class))
                .param(ParamCodec.INT)
                .param(ParamCodec.BOOLEAN)
                .build();
    }
}
