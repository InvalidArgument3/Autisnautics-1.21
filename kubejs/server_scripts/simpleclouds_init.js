// This script automatically sets the Simple Clouds height to 381 for every new world.
// It uses Level persistent data to ensure the command only runs once per world save.

LevelEvents.loaded(event => {
    // Only process for the overworld
    if (event.level.dimension != 'minecraft:overworld') return

    let data = event.level.persistentData

    // Check if we've already initialized clouds for this world
    if (!data.getBoolean('simpleclouds_initialized_125')) {
        // Execute the command silently
        event.server.runCommandSilent('simpleclouds clouds height set 381')

        // Mark as initialized so it doesn't run again for this save
        data.putBoolean('simpleclouds_initialized_125', true)

        console.info('SimpleClouds: Automatically initialized cloud height to 381 for this world.')
    }
})
