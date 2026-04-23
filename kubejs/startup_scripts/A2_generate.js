StartupEvents.registry("item", event => {
    // 1.21: thermal rubber replacement
    event.create("rubber").texture("a2:item/rubber").displayName("Rubber")
    event.create("raw_rubber").texture("a2:item/raw_rubber").displayName("Raw Rubber")
})
