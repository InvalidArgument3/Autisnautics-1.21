// priority: 1
ServerEvents.recipes(event => {
    event.remove({ output: "ae2:vibration_chamber" })

    event.remove({ id: "ae2:transform/flawed_budding_quartz" })
    event.remove({ id: "ae2:transform/chipped_budding_quartz" })
    event.remove({ id: "ae2:transform/damaged_budding_quartz" })
})
