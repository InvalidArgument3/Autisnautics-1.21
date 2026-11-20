package dev.wyseman.fwachainfix;

import fr.madu59.fwa.utils.SwingingBlockHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Maps a block state onto {@link ChainColumn.Link}. Soul lanterns are their
 * own link so a soul lantern cannot fall through as "not a lantern", and they
 * join a column the same way a hanging lantern does. A standing lantern is not
 * a column link: Fancy World Animations does not swing it, and a leftover
 * animation is removed.
 */
public final class ColumnLinks {
    private ColumnLinks() {}

    public static ChainColumn.Link of(BlockState state) {
        if (state == null) {
            return ChainColumn.Link.NONE;
        }
        if (SwingingBlockHelper.isVerticalChain(state)) {
            return ChainColumn.Link.CHAIN;
        }
        if (!SwingingBlockHelper.isActiveHangingLantern(state)) {
            return ChainColumn.Link.NONE;
        }
        if (state.getBlock() == Blocks.SOUL_LANTERN) {
            return ChainColumn.Link.SOUL_LANTERN;
        }
        return ChainColumn.Link.HANGING_LANTERN;
    }
}
