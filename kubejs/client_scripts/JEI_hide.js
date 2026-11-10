/** /kjs inventory will be your friend. */
(() => {
    const __hasRecipeViewerEvents = typeof RecipeViewerEvents !== "undefined"
    const __hideItems = (handler) => {
        if (__hasRecipeViewerEvents) {
            RecipeViewerEvents.removeEntries("item", event => handler({ hide: (entry) => event.remove(entry) }))
        } else if (typeof JEIEvents !== "undefined") {
            JEIEvents.hideItems(handler)
        }
    }
    const __addItems = (handler) => {
        if (__hasRecipeViewerEvents) {
            RecipeViewerEvents.addEntries("item", event => handler({ add: (entry) => event.add(entry) }))
        } else if (typeof JEIEvents !== "undefined") {
            JEIEvents.addItems(handler)
        }
    }

    __hideItems(event => {
    // Cleanup
        if (Platform.isLoaded("enderio")) event.hide('enderio:broken_spawner')
        event.hide('chiselsandbits:block_bit')
        event.hide(/^ae2:facade$/)
    })

    __addItems(event => {
        if (Platform.isLoaded("ae2")) event.add(Item.of('ae2:facade', { item: "minecraft:iron_block" }))
        if (Platform.isLoaded("enderio")) {
            event.add('enderio:broken_spawner')
            event.add('enderio:reinforced_obsidian_block')
        }
    })
})();
