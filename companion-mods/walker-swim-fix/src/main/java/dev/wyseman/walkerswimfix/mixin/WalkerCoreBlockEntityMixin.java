package dev.wyseman.walkerswimfix.mixin;

import dev.letitwalk.content.WalkerCoreBlockEntity;
import dev.wyseman.walkerswimfix.SwimToggleAccess;
import dev.wyseman.walkerswimfix.WalkerSwimFix;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WalkerCoreBlockEntity.class, remap = false)
public abstract class WalkerCoreBlockEntityMixin implements SwimToggleAccess {
    @Unique
    private static final String SWIM_NBT_KEY = "walkerswimfix:swimEnabled";

    @Unique
    private Boolean walkerswimfix$swimEnabled = null;

    @Unique
    private int walkerswimfix$cachedBlockCount = 0;

    @Unique
    private long walkerswimfix$lastCountTick = Long.MIN_VALUE;

    @Override
    public boolean walkerswimfix$isSwimEnabled() {
        if (walkerswimfix$swimEnabled == null) {
            return WalkerSwimFix.SPEC.isLoaded() && WalkerSwimFix.DEFAULT_SWIM_ENABLED.get();
        }
        return walkerswimfix$swimEnabled;
    }

    @Override
    public void walkerswimfix$setSwimEnabled(boolean enabled) {
        this.walkerswimfix$swimEnabled = enabled;
    }

    @Override
    public int walkerswimfix$getCachedBlockCount() {
        return walkerswimfix$cachedBlockCount;
    }

    @Override
    public long walkerswimfix$getLastCountTick() {
        return walkerswimfix$lastCountTick;
    }

    @Override
    public void walkerswimfix$setCachedBlockCount(int count, long tick) {
        this.walkerswimfix$cachedBlockCount = count;
        this.walkerswimfix$lastCountTick = tick;
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void walkerswimfix$save(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        tag.putBoolean(SWIM_NBT_KEY, this.walkerswimfix$isSwimEnabled());
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void walkerswimfix$load(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (tag.contains(SWIM_NBT_KEY)) {
            this.walkerswimfix$swimEnabled = tag.getBoolean(SWIM_NBT_KEY);
        } else {
            this.walkerswimfix$swimEnabled = null;
        }
    }
}
