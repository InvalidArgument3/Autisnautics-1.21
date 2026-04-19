if (Platform.isLoaded("integrated_stronghold")) {
    ServerEvents.tags("item", event => {
        event.get("kubejs:sellable_discs").add("integrated_stronghold:music_disc_sight", "integrated_stronghold:music_disc_forlorn");
    })
}
