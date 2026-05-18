// MoreJS changed APIs between versions; only register when the event exists.
if (typeof MoreJS !== "undefined" && MoreJS.registerPotionBrewing) {
    MoreJS.registerPotionBrewing((event) => {
        // Slow-falling potion that doesn't require ph*nt*ms
        event.addPotionBrewing(Ingredient.of("minecraft:wind_charge"), "minecraft:awkward", "minecraft:slow_falling");
        
        // Haste potion (chapter 2b)
        // removePotionBrewing is broken, used datapack to delete duplicates:
        // apothic_attributes, galosphere
        // event.removePotionBrewing({ output: /.*haste$/ });
        event.addPotionBrewing(Ingredient.of("minecraft:glow_berries"), "minecraft:awkward", "alexscaves:haste");
        event.addPotionBrewing(Ingredient.of("minecraft:redstone"), "kubejs:haste", "alexscaves:long_haste");
        event.addPotionBrewing(Ingredient.of("minecraft:glowstone_dust"), "kubejs:haste", "alexscaves:strong_haste");
        
        // New Potion of Glowing recipe: brewing Glow Lichen in an Awkward Potion.
        event.addPotionBrewing(Ingredient.of("minecraft:glow_lichen"), "minecraft:awkward", "alexscaves:glowing")
    })
} else {
    console.warn("[KubeJS] Skipping potion brewing startup hooks: MoreJSEvents.registerPotionBrewing is unavailable on this build.")
}
