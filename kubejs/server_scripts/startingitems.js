/**
 * One-time give of items to new players
 */

PlayerEvents.loggedIn(event => {
    // Use persistent data instead of Game Stages for better reliability
    let data = event.player.persistentData
    if (!data.getBoolean("received_starting_items")) {
        data.putBoolean("received_starting_items", true)
        // Give quest book to player
        event.player.give("ftbquests:book")
    }
})
