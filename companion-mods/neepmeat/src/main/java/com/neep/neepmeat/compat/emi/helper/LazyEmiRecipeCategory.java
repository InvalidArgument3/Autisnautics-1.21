package com.neep.neepmeat.compat.emi.helper;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LazyEmiRecipeCategory extends EmiRecipeCategory {
    public LazyEmiRecipeCategory(ResourceLocation id, EmiRenderable icon) {
        super(id, icon);
    }

    @Override
    public Component getName() {
        return Component.translatable("category." + getId().getNamespace() + "." + getId().getPath().substring(getId().getPath().lastIndexOf('/') + 1));
    }
}
