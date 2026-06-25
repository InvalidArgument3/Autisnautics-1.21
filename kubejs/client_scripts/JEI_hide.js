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

    __hideItems(event => {
        if (Item.exists("enderio:broken_spawner")) {
            event.hide("enderio:broken_spawner")
        }
        event.hide("chiselsandbits:block_bit")
        if (Item.exists("ae2:facade")) {
            event.hide("ae2:facade")
        }
    })
})();
