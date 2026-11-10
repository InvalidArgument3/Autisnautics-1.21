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

package com.neep.meatlib.recipe;

import com.neep.meatlib.MeatLib;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class MeatRecipeReloadListener implements SimpleSynchronousResourceReloadListener
{
    private static final MeatRecipeReloadListener INSTANCE = new MeatRecipeReloadListener();

    public static MeatRecipeReloadListener getInstance()
    {
        return INSTANCE;
    }

    @Override
    public ResourceLocation getFabricId()
    {
        return ResourceLocation.fromNamespaceAndPath(MeatLib.NAMESPACE, "recipe");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager)
    {
        for(ResourceLocation id : manager.listResources("recipes", path -> path.getPath().endsWith(".json")).keySet())
        {
//            try(InputStream stream = manager.getResource(id).getInputStream())
//            {
////                Reader reader = new InputStreamReader(stream);
////                JsonElement rootElement = JsonParser.parseReader(reader);
//            }
//            catch(Exception e)
//            {
//                MeatLib.LOGGER.error("Error occurred while loading resource json " + id.toString(), e);
//            }
        }
    }
}
