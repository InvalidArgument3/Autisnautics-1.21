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

package com.neep.neepmeat.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin
{
    @Shadow public ServerPlayer player;

//    @Inject(method = "onPlayerAction", at = @At("HEAD"))
//    void onApplyAction(PlayerActionC2SPacket packet, CallbackInfo ci)
//    {
//        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
//        if (stack.getItem() instanceof AssaultDrillItem drill)
//        {
//            switch(packet.getAction())
//            {
////                case START_DESTROY_BLOCK -> drill.onAttackBlock(player.world, stack, player);
////                case ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK -> drill.onFinishAttackBlock(player.world, stack, player);
//            }
//        }
//    }

//    @Shadow @Final private MinecraftClient client;
//
//    @Inject(method = "updateBlockBreakingProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getSoundGroup()Lnet/minecraft/sound/BlockSoundGroup;"))
//    void onThing(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir)
//    {
//        PlayerEntity player = client.player;
//        World world = client.world;
//        ItemStack active = player.getStackInHand(Hand.MAIN_HAND); // I think block breaking only uses the main hand
//        if (active.getItem() instanceof AssaultDrillItem drill)
//        {
//            drill.onUpdateBreak(world, active, player);
//        }
//    }


//    @Shadow @Final protected ServerPlayerEntity player;
//
//    @Shadow protected ServerWorld world;
//
//    @Shadow private int blockBreakingProgress;
//
//    @Inject(method = "continueMining", at = @At("HEAD"))
//    void onContinueMining(BlockState state, BlockPos pos, int failedStartMiningTime, CallbackInfoReturnable<Float> cir)
//    {
//        ItemStack active = player.getStackInHand(Hand.MAIN_HAND); // I think block breaking only uses the main hand
//        if (active.getItem() instanceof AssaultDrillItem drill)
//        {
//            drill.onUpdateBreak(world, active, player, blockBreakingProgress);
//        }
//    }
}
