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

    public static LinkOrientation orientation(ItemStack stack) {
        CompoundTag tag = frame(stack);
        if (tag == null) {
            return LinkOrientation.FRONT;
        }
        return LinkOrientation.byName(tag.getString("orient"));
    }

    public static LinkOrientation cycle(ItemStack stack, int steps) {
        LinkOrientation next = orientation(stack).cycle(steps);
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag frame = root.contains(KEY) ? root.getCompound(KEY) : new CompoundTag();
        frame.putString("orient", next.name());
        root.put(KEY, frame);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return next;
    }

    public static void write(ItemStack stack, UUID token, int u, int v, String dimension) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag previous = root.contains(KEY) ? root.getCompound(KEY) : new CompoundTag();
        CompoundTag frame = new CompoundTag();
        frame.putUUID("token", token);
        frame.putInt("u", u);
        frame.putInt("v", v);
        frame.putString("dim", dimension);
        String orient = previous.getString("orient");
        if (!orient.isEmpty()) {
            frame.putString("orient", orient);
        }
        root.put(KEY, frame);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    /** Drops the stored frame. The exit mode stays on the item for the next link. */
    public static void clear(ItemStack stack) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains(KEY)) {
            return;
        }
        CompoundTag frame = root.getCompound(KEY);
        String orient = frame.getString("orient");
        frame.remove("token");
        frame.remove("u");
        frame.remove("v");
        frame.remove("dim");
        if (orient.isEmpty()) {
            root.remove(KEY);
        } else {
            CompoundTag kept = new CompoundTag();
            kept.putString("orient", orient);
            root.put(KEY, kept);
        }
        if (root.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        }
    }

    public static void tooltip(ItemStack stack, Item.TooltipContext context, java.util.List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.portal_linker.portal_linker.desc"));
        lines.add(Component.translatable("item.portal_linker.portal_linker.orient", orientation(stack).label()));
        CompoundTag tag = frame(stack);
        if (tag != null && tag.hasUUID("token")) {
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
