(function () {
    if (Platform.isLoaded("createcasing")) {
        const casingParts = [
            "oak_shaft", "spruce_shaft", "birch_shaft", "jungle_shaft", "acacia_shaft", "dark_oak_shaft",
            "crimson_shaft", "warped_shaft", "glass_shaft", "brass_shaft",
            "oak_cogwheel", "spruce_cogwheel", "birch_cogwheel", "jungle_cogwheel", "acacia_cogwheel",
            "dark_oak_cogwheel", "crimson_cogwheel", "warped_cogwheel",
            "oak_large_cogwheel", "spruce_large_cogwheel", "birch_large_cogwheel", "jungle_large_cogwheel",
            "acacia_large_cogwheel", "dark_oak_large_cogwheel", "crimson_large_cogwheel", "warped_large_cogwheel"
        ]
        const hideCasing = (event) => {
            casingParts.forEach(part => {
                let id = "createcasing:" + part
                if (Item.exists(id)) event.hide(id)
            })
        }
        if (typeof RecipeViewerEvents !== "undefined") RecipeViewerEvents.removeEntries("item", hideCasing)
        else if (typeof JEIEvents !== "undefined") JEIEvents.hideItems(hideCasing)
    }
})();
