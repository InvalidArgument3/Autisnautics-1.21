package dev.wyseman.fwachainfix;

/**
 * Marker for the jar. NeoForge loads this mod from the toml and the client
 * mixin config. There is no client class reference here, so a dedicated server
 * can load the jar without the render hooks.
 */
public final class FwaChainFixMod {
    private FwaChainFixMod() {}
}
