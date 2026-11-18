package dev.wyseman.dimstackguard.mixin;

import dev.wyseman.dimstackguard.DimStackGuard;
import dev.wyseman.dimstackguard.ModLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stops {@code O_O.getModName} throwing for a namespace that is not a mod.
 *
 * <p>The method is {@code getModFileById(modId).getMods()...} with no null
 * check, so a datapack dimension namespace throws out of
 * {@code getModFileById}. Its only caller is
 * {@code McHelper.getDimensionName}, which already guards the result with
 * {@code modName != null ? modName : namespace}, so returning null here is the
 * behaviour that caller was written for.
 *
 * <p>Injecting at HEAD and cancelling keeps the method untouched for real mods.
 * {@code remap = false} because the target is a mod class compiled against
 * Mojmap, and the target is named rather than imported so the build does not
 * need Immersive Portals on the compile classpath.
 */
@Mixin(targets = "qouteall.imm_ptl.core.platform_specific.O_O", remap = false)
public class OOModNameMixin {
    @Inject(method = "getModName", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dimstack$missingMod(String modId, CallbackInfoReturnable<String> cir) {
        if (DimStackGuard.isMissingMod(modId, ModLookup::present)) {
            cir.setReturnValue(null);
        }
    }
}
