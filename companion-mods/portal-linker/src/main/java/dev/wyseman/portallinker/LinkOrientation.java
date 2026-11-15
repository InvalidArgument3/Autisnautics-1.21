package dev.wyseman.portallinker;

import net.minecraft.network.chat.Component;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.my_util.DQuaternion;

/**
 * Extra turn applied on top of the upright link. Stored on the link and on the linker item.
 * The names are what the action bar says.
 */
public enum LinkOrientation {
    FRONT("portal_linker.orient.front"),
    BACK("portal_linker.orient.back"),
    ROLL_90("portal_linker.orient.roll_90"),
    ROLL_180("portal_linker.orient.roll_180"),
    ROLL_270("portal_linker.orient.roll_270"),
    FLIP("portal_linker.orient.flip");

    private final String key;

    LinkOrientation(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public Component label() {
        return Component.translatable(key);
    }

    public LinkOrientation cycle(int steps) {
        LinkOrientation[] values = values();
        int index = Math.floorMod(ordinal() + steps, values.length);
        return values[index];
    }

    public static LinkOrientation byName(String name) {
        if (name == null || name.isEmpty()) {
            return FRONT;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return FRONT;
        }
    }

    /**
     * World-space turn applied after the upright connection, in destination space.
     * Null means the upright connection is used as-is.
     */
    public DQuaternion extra(Portal destination) {
        return switch (this) {
            case FRONT -> null;
            case BACK -> DQuaternion.rotationByDegrees(destination.getAxisH(), 180.0);
            case ROLL_90 -> DQuaternion.rotationByDegrees(destination.getNormal(), 90.0);
            case ROLL_180 -> DQuaternion.rotationByDegrees(destination.getNormal(), 180.0);
            case ROLL_270 -> DQuaternion.rotationByDegrees(destination.getNormal(), 270.0);
            case FLIP -> DQuaternion.rotationByDegrees(destination.getAxisW(), 180.0);
        };
    }
}
