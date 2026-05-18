// MoreJS changed APIs between versions; only register when the event exists.
if (typeof MoreJSEvents !== "undefined" && MoreJSEvents.registerPotionBrewing) {
    MoreJSEvents.registerPotionBrewing((event) => {
        // Slow-falling potion that doesn't require ph*nt*ms
        event.addPotionBrewing("minecraft:wind_charge", "minecraft:awkward", "minecraft:slow_falling");
        event.removeByPotion(null, "minecraft:phantom_membrane", null);
        // Haste potion (chapter 2b)
        event.addPotionBrewing("minecraft:glow_berries", "minecraft:awkward", "kubejs:haste");
        event.addPotionBrewing("minecraft:redstone", "kubejs:haste", "kubejs:long_haste");
        event.addPotionBrewing("minecraft:glowstone_dust", "kubejs:haste", "kubejs:strong_haste");
        // remove duplicates, in case they have recipes
        event.removeByPotion(null, null, "apothic_attributes:haste");
        event.removeByPotion(null, null, "apothic_attributes:long_haste");
        event.removeByPotion(null, null, "apothic_attributes:strong_haste");
        event.removeByPotion(null, null, "alexscaves:haste");
        event.removeByPotion(null, null, "alexscaves:long_haste");
        event.removeByPotion(null, null, "alexscaves:strong_haste");
        event.removeByPotion(null, null, "galosphere:haste");
        event.removeByPotion(null, null, "galosphere:long_haste");
        event.removeByPotion(null, null, "galosphere:strong_haste");
        // New Potion of Glowing recipe: brewing Glow Lichen in an Awkward Potion.
        event.addPotionBrewing("minecraft:glow_lichen", "minecraft:awkward", "alexscaves:glowing")
    })
} else {
    console.warn("[KubeJS] Skipping potion brewing startup hooks: MoreJSEvents.registerPotionBrewing is unavailable on this build.")
}
