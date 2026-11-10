package dev.wyseman.moltensea.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

final class DeepSeasTags {
    private DeepSeasTags() {}

    static boolean isWater(TagKey<?> tag) {
        if (tag == null) {
            return false;
        }
        ResourceLocation id = tag.location();
        return "create_submarine".equals(id.getNamespace()) && "water".equals(id.getPath());
    }
}
