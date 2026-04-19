// priority: 99
const POTION_BOTTLES = ["REGULAR", "SPLASH", "LINGERING"];
const POTIONS = [ // The order that we create these rei entries in is important!
    ["minecraft:mundane", []],
    ["minecraft:thick", []],
    ["minecraft:awkward", []],
    ["minecraft:night_vision", ["long"]],
    ["minecraft:invisibility", ["long"]],
    ["minecraft:leaping", ["long", "strong"]],
    ["minecraft:fire_resistance", ["long"]],
    ["minecraft:swiftness", ["long", "strong"]],
    ["minecraft:slowness", ["long", "strong"]],
    ["minecraft:turtle_master", ["long", "strong"]],
    ["minecraft:water_breathing", ["long"]],
    ["minecraft:healing", ["strong"]],
    ["minecraft:harming", ["strong"]],
    ["minecraft:poison", ["long", "strong"]],
    ["minecraft:regeneration", ["long", "strong"]],
    ["minecraft:strength", ["long", "strong"]],
    ["minecraft:weakness", ["long"]],
    ["minecraft:luck", []],
    ["minecraft:slow_falling", ["long"]],
    ["kubejs:haste", ["long", "strong"]]
];


// Add Potion fluids to REI (disabled on this runtime due missing ClientEvents.highPriorityAssets)
console.warn("[KubeJS] Skipping JEI_potions.js highPriorityAssets edits: ClientEvents.highPriorityAssets is unavailable in this KubeJS build.")
