package dev.wyseman.harnessportals.mixin;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RegisterClientExtensionsEvent.class, remap = false)
public abstract class RegisterClientExtensionsEventMixin {

    @Shadow
    public abstract boolean isItemRegistered(Item item);

    @Inject(
            method = "registerItem(Lnet/neoforged/neoforge/client/extensions/common/IClientItemExtensions;[Lnet/minecraft/world/item/Item;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void harnessPortals$skipDuplicateItemExtension(IClientItemExtensions extensions, Item[] items, CallbackInfo ci) {
        if (items == null || items.length == 0) {
            return;
        }
        boolean anyRegistered = false;
        for (Item item : items) {
            if (isItemRegistered(item)) {
                anyRegistered = true;
                break;
            }
        }
        if (!anyRegistered) {
            return;
        }
        RegisterClientExtensionsEvent self = (RegisterClientExtensionsEvent) (Object) this;
        for (Item item : items) {
            if (!isItemRegistered(item)) {
                self.registerItem(extensions, item);
            }
        }
        ci.cancel();
    }
}
