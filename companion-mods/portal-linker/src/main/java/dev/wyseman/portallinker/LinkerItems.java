package dev.wyseman.portallinker;

import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

public final class LinkerItems {
    private static final String KEY = "PortalLinker";

    private LinkerItems() {}

    public static UUID token(ItemStack stack) {
        CompoundTag tag = frame(stack);
        if (tag == null || !tag.hasUUID("token")) {
            return null;
        }
        return tag.getUUID("token");
    }

    public static void write(ItemStack stack, UUID token, int u, int v, String dimension) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag frame = new CompoundTag();
        frame.putUUID("token", token);
        frame.putInt("u", u);
        frame.putInt("v", v);
        frame.putString("dim", dimension);
        root.put(KEY, frame);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    public static void clear(ItemStack stack) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains(KEY)) {
            return;
        }
        root.remove(KEY);
        if (root.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        }
    }

    public static void tooltip(ItemStack stack, Item.TooltipContext context, java.util.List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.portal_linker.portal_linker.desc"));
        CompoundTag tag = frame(stack);
        if (tag != null) {
            lines.add(Component.translatable("item.portal_linker.portal_linker.stored", tag.getInt("u"), tag.getInt("v"), tag.getString("dim")));
        }
    }

    private static CompoundTag frame(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag root = data.copyTag();
        if (!root.contains(KEY)) {
            return null;
        }
        return root.getCompound(KEY);
    }
}
