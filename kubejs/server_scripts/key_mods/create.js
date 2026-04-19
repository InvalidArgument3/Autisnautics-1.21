// priority: 1
ServerEvents.recipes(event => {
    // casing recipe changes
    let tweak_casing = (name, mats) => {
        event.remove({ output: name + "_casing"})
        event.shapeless(Item.of(name + "_casing", 2), mats)
    }
    tweak_casing("create:andesite", ["create:andesite_alloy", "#minecraft:logs"])
    tweak_casing("create:copper", ["create:copper_sheet", "#minecraft:logs"])
    tweak_casing("create:railway", ["create:golden_sheet", "minecraft:deepslate"])
    tweak_casing("create:brass", ["create:brass_sheet", "#minecraft:logs"])
    tweak_casing("kubejs:zinc", ["create:zinc_ingot", "minecraft:stone"])
    tweak_casing("kubejs:lead", ["thermal:lead_plate", "minecraft:deepslate"])
    tweak_casing("kubejs:invar", ["thermal:invar_ingot", "minecraft:stone"])
    tweak_casing("kubejs:enderium", ["minecraft:ender_pearl", "minecraft:obsidian"])
    tweak_casing("kubejs:fluix", ["thermal:lead_plate", "minecraft:blackstone"])
    // tweak_casing('alloyed:steel', ["alloyed:steel_sheet", '#minecraft:logs'])
    tweak_casing("create:refined_radiance", ["create:refined_radiance", "#minecraft:logs"])
    tweak_casing("create:shadow_steel", ["create:shadow_steel", "#minecraft:logs"])
    // recipe changes
    event.replaceInput({ id: "create:crafting/kinetics/adjustable_chain_gearshift" }, "create:electron_tube", "minecraft:redstone")
    event.replaceInput({ id: "create:crafting/kinetics/rope_pulley" }, "#forge:wool", "#supplementaries:ropes")
    // windmill recipe tweaks
    event.remove({ id: "create:crafting/kinetics/white_sail" })
    event.shaped("2x create:white_sail", [
        "SSS",
        "NAN",
        "SSS"
    ], {
        A: "#forge:wool",
        N: "minecraft:iron_nugget",
        S: "minecraft:stick"
    })
    // tweak obsidian crushing recipe
    event.remove({ id: "create:crushing/obsidian" })
    event.recipes.create.crushing("create:powdered_obsidian", "minecraft:obsidian")
    // recompacting obsidian dust into its resource
    event.recipes.create.compacting("#forge:dusts/obsidian", "minecraft:obsidian")

    // Gravel and red sand washing buffs
    event.remove({ id: "create:splashing/gravel" })
    event.recipes.create.splashing([
        Item.of(Item.of("minecraft:iron_nugget", 2)).withChance(0.125),
        Item.of("minecraft:flint").withChance(0.25)
    ], "minecraft:gravel")

    event.remove({ id: "create:splashing/red_sand" })
    event.recipes.create.splashing([
        Item.of(Item.of("minecraft:gold_nugget", 2)).withChance(0.125),
        Item.of("minecraft:dead_bush").withChance(0.05)
    ], "minecraft:red_sand")

    // unify dough and allow the slime recipe to take dough from farmer's delight
    event.remove({ id: "create:crafting/appliances/dough" })
    event.replaceOutput({ id: "farmersdelight:wheat_dough_from_water" }, "farmersdelight:wheat_dough", "create:dough")
    event.replaceOutput({ id: "farmersdelight:wheat_dough_from_eggs" }, "farmersdelight:wheat_dough", "create:dough")
    event.replaceInput({ id: "create:crafting/appliances/slime_ball" }, "create:dough", "#forge:dough")
})
