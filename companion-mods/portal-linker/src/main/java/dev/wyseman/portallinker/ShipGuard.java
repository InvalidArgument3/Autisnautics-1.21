package dev.wyseman.portallinker;

import java.lang.reflect.Method;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import qouteall.imm_ptl.core.compat.sable_integration.IPSableCompatHelper;

/** Phase 1 refuses any end that sits on a Sable / Aeronautics ship. */
public final class ShipGuard {
    private static boolean lookedUp;
    private static boolean lookupFailed;
    private static Object helper;
    private static Method getContaining;

    private ShipGuard() {}

    public static boolean onShip(Level level, Collection<BlockPos> positions) {
        String name = level.getClass().getName();
        if (name.contains("SubLevel")) {
            return true;
        }
        boolean sable = ModList.get().isLoaded("sable");
        for (BlockPos pos : positions) {
            // Plot space is where Sable parks ships. Without Sable those coordinates are just far away.
            if (sable && IPSableCompatHelper.isPlotSpacePosition(pos.getX(), pos.getZ())) {
                return true;
            }
            if (contained(level, pos)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contained(Level level, BlockPos pos) {
        if (!ModList.get().isLoaded("sable")) {
            return false;
        }
        lookup();
        if (lookupFailed || getContaining == null) {
            return true;
        }
        try {
            Object ship = getContaining.invoke(helper, level, pos);
            if (ship == null) {
                return false;
            }
            Method removed = ship.getClass().getMethod("isRemoved");
            return !Boolean.TRUE.equals(removed.invoke(ship));
        } catch (ReflectiveOperationException e) {
            PortalLinker.LOG.warn("Could not ask Sable whether {} is on a ship; refusing the frame", pos, e);
            return true;
        }
    }

    private static void lookup() {
        if (lookedUp) {
            return;
        }
        lookedUp = true;
        try {
            Class<?> sable = Class.forName("dev.ryanhcode.sable.Sable");
            helper = sable.getField("HELPER").get(null);
            getContaining = helper.getClass().getMethod("getContaining", Level.class, net.minecraft.core.Vec3i.class);
        } catch (ReflectiveOperationException e) {
            PortalLinker.LOG.warn("Sable is loaded but its ship lookup is not the method Portal Linker knows", e);
            lookupFailed = true;
            getContaining = null;
        }
    }
}
