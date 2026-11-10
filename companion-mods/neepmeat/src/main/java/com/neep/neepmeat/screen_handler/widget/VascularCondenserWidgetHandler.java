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

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.block.energy_transport.entity.VascularCondenserBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class VascularCondenserWidgetHandler extends ConfigWidgetHandler<VascularCondenserWidgetHandler.Values>
{
    public final ChannelManager<UpdateStoredCapacity> amountS2C;

    @Nullable private final VascularCondenserBlockEntity be;

    public VascularCondenserWidgetHandler(Player player, @Nullable VascularCondenserBlockEntity be)
    {
        super(player, Values.PARAM_CODEC);

        amountS2C = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "vc_amount"), UpdateStoredCapacity.FORMAT, player);
        this.be = be;
    }

    @Override
    protected Values getValues()
    {
        if (be != null)
        {
            return new Values(be.getTarget(), be.destroyExcess);
        }

        return new Values(0, false);
    }

    @Override
    protected void setValues(Values values)
    {
        if (be != null)
        {
            be.setTarget(Math.max(0, values.target));
            be.setDestroy(values.destroy);
        }
    }

    @Override
    public void sendUpdates()
    {
        super.sendUpdates();
        if (be != null)
        {
            amountS2C.emitter().apply(be.getStoredAbs(), be.getCapacity(), be.getNetworkInternal());
        }
    }

    @Override
    public void close()
    {
        super.close();
        amountS2C.close();
    }

    public interface UpdateStoredCapacity
    {
        ChannelFormat<UpdateStoredCapacity> FORMAT = ChannelFormat.builder(UpdateStoredCapacity.class).param(ParamCodec.LONG).param(ParamCodec.LONG).param(ParamCodec.LONG).build();

        void apply(long stored, long capacity, long networkInternal);
    }

    public record Values(int target, boolean destroy)
    {
        public static final ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class)
                .param(ParamCodec.INT).param(ParamCodec.BOOLEAN).build();
    }
}
