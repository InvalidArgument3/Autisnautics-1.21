package dev.wyseman.moltensea.mixin;

import dev.wyseman.moltensea.MoltenSea;
import dev.wyseman.moltensea.NullSafeConcurrentMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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
 * {@code DimensionDataStorage.save}.
 *
 * <p>{@code get} also stores null when the data file is missing, and {@code containsKey} is how
 * it tells "not loaded yet" from "loaded, absent". A raw {@code ConcurrentHashMap} rejects that
 * null and crashes server startup. This map is concurrent and still returns null for a missing
 * file. Entries added during the save iteration are written on the next save.
 */
@Mixin(DimensionDataStorage.class)
public class DimensionDataStorageMixin {
    private static final AtomicBoolean LOGGED = new AtomicBoolean();

    @Shadow
    @Final
    @Mutable
    private Map<String, SavedData> cache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void autisnautics$concurrentSavedDataCache(CallbackInfo ci) {
        if (!(this.cache instanceof NullSafeConcurrentMap)) {
            NullSafeConcurrentMap<String, SavedData> next = new NullSafeConcurrentMap<>();
            next.putAll(this.cache);
            this.cache = next;
            if (LOGGED.compareAndSet(false, true)) {
                MoltenSea.LOG.info("Saved-data cache accepts a missing file");
            }
        }
    }
}
