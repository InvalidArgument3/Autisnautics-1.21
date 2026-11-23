// priority: 50
// Portal Linker. GATE is on.
//
// The jar ships the same column under id portal_linker:portal_linker, with the
// middle key as the item tag portal_linker:linker_gate. That tag's only entry
// is kubejs:enderium_machine, and it is optional so a dev server without
// KubeJS still loads the recipe (an empty tag matches nothing).
//
// This script removes that copy and puts the machine item itself in the
// middle, so the recipe book names it. The machine is consumed. The column is:
//   crying obsidian
//   kubejs:enderium_machine
//   eye of ender
//
// That machine is the chapter 3 ender-age gate: an ender casing around an
// abstruse mechanism (kubejs/server_scripts/recipes/chapters/chapter_3.js,
// donutCraft of kubejs:enderium_machine). The abstruse mechanism is the heated
// mix above it. Set GATE false to take the craft back out.

const GATE = true

ServerEvents.recipes(event => {
    // The jar recipe stays (middle ingredient is the portal_linker:linker_gate tag).
    // This copy names the machine item in the recipe book.
    if (!GATE) {
        return
    }
    event.shaped("portal_linker:portal_linker", ["C", "M", "E"], {
        C: "minecraft:crying_obsidian",
        M: "create:precision_mechanism",
        E: "minecraft:ender_eye"
    }).id("kubejs:portal_linker_machine")
})
