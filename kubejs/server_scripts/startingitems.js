/**
 * One-time give of items to new players
 */

PlayerEvents.loggedIn(event => {
    // Use persistent data instead of Game Stages for better reliability
    let data = event.player.persistentData
    if (!data.getBoolean("received_starting_items")) {
        data.putBoolean("received_starting_items", true)
        // Quest book stays its own item. Mod guidebooks are the Akashic Tome
        // from universal_tome.js, on a separate once-per-player flag.
        event.player.give("ftbquests:book")
    }
})
