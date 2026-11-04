// priority: 1
// AE2 removed (swapped to Refined Storage): only run when AE2 is installed
if (Platform.isLoaded("ae2")) ServerEvents.recipes(event => {
    event.remove({ output: "ae2:vibration_chamber" })

    event.remove({ id: "ae2:transform/flawed_budding_quartz" })
    event.remove({ id: "ae2:transform/chipped_budding_quartz" })
    event.remove({ id: "ae2:transform/damaged_budding_quartz" })
})
