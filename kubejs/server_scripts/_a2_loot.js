LootJS.lootTables(event => {
    event.getLootTable("minecraft:entities/snow_golem").createPool(pool => {
        pool.name("a2_smoking_snow_golems")
        pool.when(conditions => {
            conditions.matchDamageSource({
                tags: [{
                    expected: true,
                    id: "a2:is_smoking" // custom since matchDamageSource only takes damageSourcePredicate which checks for tags
                }]
            })
        })
        pool.addEntry("kubejs:haunted_rind")
    })
})
