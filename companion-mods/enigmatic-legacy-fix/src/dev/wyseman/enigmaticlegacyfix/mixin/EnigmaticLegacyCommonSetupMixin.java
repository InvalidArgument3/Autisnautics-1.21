package dev.wyseman.enigmaticlegacyfix.mixin;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.proxy.CommonProxy;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * EnigmaticLegacy+ 1.21.1-1.1.2 handles common setup on a worker thread.
 * {@code onCommonSetup} calls {@code CommonProxy.init} immediately, and that
 * registers a dispenser behavior into the unsynchronized vanilla map. Another
 * mod doing the same put at the same time corrupts the fastutil table
 * ({@code ArrayIndexOutOfBoundsException} in {@code rehash}).
 *
 * <p>The call moves onto {@link FMLCommonSetupEvent#enqueueWork}, which
 * NeoForge runs on the main thread after every parallel common-setup listener
 * has returned. The init body is unchanged: the ichor spear dispenser behavior
 * and the ultimate-potion list.
 */
@Mixin(value = EnigmaticLegacy.class, remap = false)
public class EnigmaticLegacyCommonSetupMixin {
    @Redirect(
            method = "onCommonSetup",
            at = @At(
                    value = "INVOKE",
                    target = "Lauviotre/enigmatic/legacy/proxy/CommonProxy;init()V"
            ),
            remap = false
    )
    private void deferInit(CommonProxy proxy, FMLCommonSetupEvent event) {
        event.enqueueWork(proxy::init);
    }
}
