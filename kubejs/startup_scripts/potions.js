// MoreJS changed APIs between versions; only register when the event exists.
if (typeof MoreJSEvents !== "undefined" && MoreJSEvents.registerPotionBrewing) {
    MoreJSEvents.registerPotionBrewing((event) => {
        event.addPotionBrewing("thermal:blitz_powder", "minecraft:awkward", "minecraft:slow_falling");
        event.removeByPotion(null, "minecraft:phantom_membrane", null);
        event.addPotionBrewing("minecraft:glow_berries", "minecraft:awkward", "kubejs:haste");
        event.addPotionBrewing("minecraft:redstone", "kubejs:haste", "kubejs:long_haste");
        event.addPotionBrewing("minecraft:glowstone_dust", "kubejs:haste", "kubejs:strong_haste");
        // New Potion of Glowing recipe: brewing Glow Lichen in an Awkward Potion.
        event.addPotionBrewing("minecraft:glow_lichen", "minecraft:awkward", "alexscaves:glowing")
    })
} else {
    console.warn("[KubeJS] Skipping potion brewing startup hooks: MoreJSEvents.registerPotionBrewing is unavailable on this build.")
}
