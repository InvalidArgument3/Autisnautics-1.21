// priority: 2

ServerEvents.recipes(event => {
    // Pearlescent Levitite is the soul crystallization of Levitite Blend
    // (soul torch, soul campfire, soul fire, or a soul-fire base block beside
    // the pool). That path never writes a recipe, so the primer quest has
    // nothing to craft. Heated compacting uses one mixing batch of blend
    // (500 mB) and a soul-fire base block, the same inputs as the in-world
    // cast. Regular Levitite stays the heat crystallization.
    event.recipes.create.compacting("aeronautics:pearlescent_levitite", [
        Fluid.sizedIngredientOf("aeronautics:levitite_blend", 500),
        Ingredient.of("#minecraft:soul_fire_base_blocks")
    ]).heated().id("kubejs:pearlescent_levitite")
})
