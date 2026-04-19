(function () {
if (Platform.isLoaded("createaddition")) {
    if (typeof RecipeViewerEvents !== "undefined") RecipeViewerEvents.removeEntries("item", event => {
        const hide = (entry) => event.remove(entry)
        hide("createaddition:capacitor")

        hide("kubejs:incomplete_large_connector")
        hide("kubejs:incomplete_connector")

        hide("thermal:diamond_dust")
        hide("thermal:electrum_ingot")
        hide("thermal:electrum_nugget")
        hide("thermal:electrum_plate")
    })
    else if (typeof JEIEvents !== "undefined") JEIEvents.hideItems(event => {
        event.hide("createaddition:capacitor")

        event.hide("kubejs:incomplete_large_connector")
        event.hide("kubejs:incomplete_connector")

        event.hide("thermal:diamond_dust")
        event.hide("thermal:electrum_ingot")
        event.hide("thermal:electrum_nugget")
        event.hide("thermal:electrum_plate")
    })

    console.warn("[KubeJS] Skipping createaddition highPriorityAssets edits: ClientEvents.highPriorityAssets is unavailable in this KubeJS build.")
}
})();
