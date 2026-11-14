package com.neep.neepmeat.api.storage;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.util.Objects;
import java.util.function.Supplier;

public class AutoInvalidatingLazyBlockApiCache<A, C> extends DynamicLazyBlockApiCache<A, C>
{
    private BlockPos prevPos = null;
    private C prevCtx = null;

    public AutoInvalidatingLazyBlockApiCache(Supplier<Level> worldSupplier, BlockApiLookup<A, C> lookup, Supplier<BlockPos> posSupplier, Supplier<C> ctxSupplier)
    {
        super(worldSupplier, lookup, posSupplier, ctxSupplier);
    }

    @Override
    protected void validate()
    {
        if (!Objects.equals(prevPos, pos()) || prevCtx != ctx())
        {
            prevPos = pos();
            prevCtx = ctx();
            invalidate();
        }

        super.validate();
    }
}
