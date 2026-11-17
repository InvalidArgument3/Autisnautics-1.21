package dev.wyseman.moltensea.mixin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Autosave iterates {@code cache} on the server thread. Immersive Petroleum's reservoir
 * feature calls {@code computeIfAbsent} from C2ME worker threads while Chunky is pregenerating.
 * The vanilla map is a HashMap, so that overlap throws ConcurrentModificationException out of
 * {@code DimensionDataStorage.save}. A concurrent map keeps the save from crashing. Entries
 * added during the iteration are written on the next save.
 */
@Mixin(DimensionDataStorage.class)
public class DimensionDataStorageMixin {
    @Shadow
    @Final
    @Mutable
    private Map<String, SavedData> cache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void autisnautics$concurrentSavedDataCache(CallbackInfo ci) {
        if (!(this.cache instanceof ConcurrentHashMap)) {
            this.cache = new ConcurrentHashMap<>(this.cache);
        }
    }
}
