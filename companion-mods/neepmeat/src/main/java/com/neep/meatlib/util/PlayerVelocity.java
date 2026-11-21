package com.neep.meatlib.util;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.GlobalChannelManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class PlayerVelocity
{
    public static final GlobalChannelManager<Velocity> CHANNEL = GlobalChannelManager.create(ResourceLocation.tryBuild(MeatLib.NAMESPACE, "add_player_velocity"),
            ChannelFormat.builder(Velocity.class).param(ParamCodecs.VEC3D).param(ParamCodec.BOOLEAN).build());

    public interface Velocity
    {
        void apply(Vec3 vel, boolean set);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Client
    {
        public static void init()
        {
            CHANNEL.receiver(Dist.CLIENT, (vel, set) ->
            {
                if (!set)
                    Minecraft.getInstance().player.addDeltaMovement(vel);
                else
                    Minecraft.getInstance().player.setDeltaMovement(vel);
            });
        }
    }
}
