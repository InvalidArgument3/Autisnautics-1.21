// MoreJS changed APIs between versions; only register when the event exists.
if (typeof MoreJS !== "undefined" && MoreJS.registerPotionBrewing) {
    MoreJS.registerPotionBrewing((event) => {
        // Slow-falling potion that doesn't require ph*nt*ms
        event.addPotionBrewing("minecraft:wind_charge", "minecraft:awkward", "minecraft:slow_falling");
        event.removePotionBrewing(null, "minecraft:phantom_membrane", null);
        // Haste potion (chapter 2b)
        event.addPotionBrewing("minecraft:glow_berries", "minecraft:awkward", "kubejs:haste");
        event.addPotionBrewing("minecraft:redstone", "kubejs:haste", "kubejs:long_haste");
        event.addPotionBrewing("minecraft:glowstone_dust", "kubejs:haste", "kubejs:strong_haste");
        // remove duplicates, in case they have recipes
        event.removePotionBrewing(null, null, "apothic_attributes:haste");
        event.removePotionBrewing(null, null, "apothic_attributes:long_haste");
        event.removePotionBrewing(null, null, "apothic_attributes:strong_haste");
        event.removePotionBrewing(null, null, "alexscaves:haste");
        event.removePotionBrewing(null, null, "alexscaves:long_haste");
        event.removePotionBrewing(null, null, "alexscaves:strong_haste");
        event.removePotionBrewing(null, null, "galosphere:haste");
        event.removePotionBrewing(null, null, "galosphere:long_haste");
        event.removePotionBrewing(null, null, "galosphere:strong_haste");
        // New Potion of Glowing recipe: brewing Glow Lichen in an Awkward Potion.
        event.addPotionBrewing("minecraft:glow_lichen", "minecraft:awkward", "alexscaves:glowing")
    })
} else {
    console.warn("[KubeJS] Skipping potion brewing startup hooks: MoreJSEvents.registerPotionBrewing is unavailable on this build.")
}
