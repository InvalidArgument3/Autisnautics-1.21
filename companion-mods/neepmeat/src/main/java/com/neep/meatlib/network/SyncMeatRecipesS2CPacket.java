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

package com.neep.meatlib.network;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.recipe.MeatlibRecipe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import java.util.Collection;

@Deprecated
public class SyncMeatRecipesS2CPacket
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MeatLib.NAMESPACE, "recipe_sync");

    public static void send(ServerPlayer player, Collection<MeatlibRecipe<?>> recipes)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();

        buf.writeCollection(recipes, SyncMeatRecipesS2CPacket::writeRecipe);

        ServerPlayNetworking.send(player, ID, buf);
    }

    public static MeatlibRecipe<?> readRecipe(FriendlyByteBuf buf)
    {
        ResourceLocation identifier = buf.readResourceLocation();
        ResourceLocation identifier2 = buf.readResourceLocation();
        return null;
//        return RecipeRegistry.RECIPE_SERIALISER.getOrEmpty(value).orElseThrow(() -> new IllegalArgumentException("Unknown special recipe serializer " + value)).read(identifier2, buf);
    }

    public static <T extends MeatlibRecipe<?>> void writeRecipe(FriendlyByteBuf buf, T recipe)
    {
//        MeatRecipeSerialiser<T> serialiser = (MeatRecipeSerialiser<T>) recipe.getSerialiser();
//        buf.writeIdentifier(RecipeRegistry.RECIPE_SERIALISER.getId(serialiser));
//        buf.writeIdentifier(recipe.getId());
//        serialiser.write(buf, recipe);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client
    {
        public static void registerReceiver()
        {
//            ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) ->
//            {
//                Collection<MeatlibRecipe<?>> recipes = buf.readList(SyncMeatRecipesS2CPacket::readRecipe);
//
//                // We do not need to set the recipes on an integrated server
////                if (client.getServer() != null)
////                {
////                    return;
////                }
//
////                MeatRecipeManager.getInstance().setRecipes(recipes);
//            });
        }
    }
}
