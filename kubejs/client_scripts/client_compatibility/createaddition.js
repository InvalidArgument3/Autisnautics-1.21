(function () {
    if (Platform.isLoaded("createaddition")) {
        if (typeof RecipeViewerEvents !== "undefined") RecipeViewerEvents.removeEntries("item", event => {
            const hide = (entry) => event.remove(entry)
            const hideIf = (id) => { if (Item.exists(id)) hide(id) }
            hide("createaddition:capacitor")

            hide("kubejs:incomplete_large_connector")
            hide("kubejs:incomplete_connector")

            hideIf("thermal:diamond_dust")
            hideIf("thermal:electrum_ingot")
            hideIf("thermal:electrum_nugget")
            hideIf("thermal:electrum_plate")
        })
        else if (typeof JEIEvents !== "undefined") JEIEvents.hideItems(event => {
            event.hide("createaddition:capacitor")

            event.hide("kubejs:incomplete_large_connector")
            event.hide("kubejs:incomplete_connector")

            if (Item.exists("thermal:diamond_dust")) event.hide("thermal:diamond_dust")
            if (Item.exists("thermal:electrum_ingot")) event.hide("thermal:electrum_ingot")
            if (Item.exists("thermal:electrum_nugget")) event.hide("thermal:electrum_nugget")
            if (Item.exists("thermal:electrum_plate")) event.hide("thermal:electrum_plate")
        })

        console.warn("[KubeJS] Skipping createaddition highPriorityAssets edits: ClientEvents.highPriorityAssets is unavailable in this KubeJS build.")
    }
})();
