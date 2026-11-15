// priority: 50
// Portal Linker craft. The recipe id is portal_linker:portal_linker
// (crying obsidian, eye of ender, crying obsidian), shipped in the mod jar.
// Leave GATE false until a quest should hide the craft. Then set it true
// and give the item from that quest, or re-add the recipe in the chapter script.
const GATE = false

ServerEvents.recipes(event => {
    if (GATE) {
        event.remove({ id: "portal_linker:portal_linker" })
    }
})
