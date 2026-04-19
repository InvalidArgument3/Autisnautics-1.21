if (Platform.isLoaded("createdieselgenerators")) {
    if (typeof RecipeViewerEvents !== "undefined") RecipeViewerEvents.removeEntries("item", event => {
        const hide = (entry) => event.remove(entry)
        hide("create_connected:control_chip")
        hide("create_connected:incomplete_control_chip")
    })
    else if (typeof JEIEvents !== "undefined") JEIEvents.hideItems(event => {
        event.hide("create_connected:control_chip")
        event.hide("create_connected:incomplete_control_chip")
    })
}
