package dev.wyseman.walkerswimfix;

public interface SwimToggleAccess {
    boolean walkerswimfix$isSwimEnabled();

    void walkerswimfix$setSwimEnabled(boolean enabled);

    int walkerswimfix$getCachedBlockCount();

    long walkerswimfix$getLastCountTick();

    void walkerswimfix$setCachedBlockCount(int count, long tick);
}
