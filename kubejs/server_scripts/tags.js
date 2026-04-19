
ServerEvents.tags("item", event => {

    // clockwork wanderlite (and other ores that didnt get picked up in the forge:ores purge)
    event.add("forge:ores", "vs_clockwork:wanderlite_deepslate_ore")
    event.add("forge:ores", "vs_clockwork:wanderlite_end_ore")
    event.add("c:ores", "vs_clockwork:wanderlite_deepslate_ore")
    event.add("c:ores", "vs_clockwork:wanderlite_end_ore")

    event.add("forge:dusts/obsidian", "create:powdered_obsidian")
    event.add("forge:dusts", "create:powdered_obsidian")

    // Fixes copper dupe.
    event.remove("forge:storage_blocks/copper", "minecraft:cut_copper");

    colours.forEach(element => {
        event.get("forge:glazed_terracotta").add(`minecraft:${element}_glazed_terracotta`)
    });

    global.trades.forEach(element => {
        event.get("kubejs:transaction_cards/import").add(`kubejs:trade_card_${element}`)
    });

    global.professions.forEach(element => {
        event.get("kubejs:transaction_cards/profession").add(`kubejs:profession_card_${element}`)
    });

    unregistered_axes.forEach(axe => {
        event.get("forge:tools/axes").add(axe)
    });

    event.get("forge:vines").add("minecraft:vine")

    // These items aren't obtainable at the moment.
    // This tag lets these items be melted into iron using the smeltery

    // This tag lets these items be turned into circuit scraps and be melted into invar
    event.get("kubejs:circuit_press")
        .add("ae2:name_press")
        .add("ae2:silicon_press")
        .add("ae2:logic_processor_press")
        .add("ae2:engineering_processor_press")
        .add("ae2:calculation_processor_press")

    event.get("forbidden_arcanus:modifier/eternal_incompatible")
        .add(/exchangers:.*/)
        .add(/reliquary:.*/)
        // .add(/waterstrainer:.*/)
        // .add("#occultism:miners/ores")
        .add("projectred_core:draw_plate")
        .add("projectred_core:multimeter")

    // crafting tools for the chapters
    event.get("kubejs:saws").add("cb_microblock:stone_saw").add("cb_microblock:iron_saw").add("cb_microblock:diamond_saw")
    event.get("kubejs:screwdrivers").add("projectred_core:screwdriver")
    event.get("kubejs:chromatic_resonators").add("kubejs:chromatic_resonator")
    event.get("kubejs:flash_drives").add("kubejs:flash_drive")

    event.get("kubejs:machines").add("kubejs:andesite_machine").add("kubejs:copper_machine").add("kubejs:gold_machine").add("kubejs:brass_machine").add("kubejs:zinc_machine").add("kubejs:lead_machine").add("thermal:machine_frame").add("kubejs:enderium_machine").add("ae2:controller")

    event.get("kubejs:sellable_discs").add("minecraft:music_disc_13", "minecraft:music_disc_cat", "minecraft:music_disc_blocks", "minecraft:music_disc_chirp", "minecraft:music_disc_far", "minecraft:music_disc_mall", "minecraft:music_disc_mellohi", "minecraft:music_disc_stal", "minecraft:music_disc_strad", "minecraft:music_disc_ward", "minecraft:music_disc_11", "minecraft:music_disc_wait", "minecraft:music_disc_otherside", "minecraft:music_disc_5", "minecraft:music_disc_pigstep", "minecraft:music_disc_relic", "supplementaries:music_disc_heave_ho", "quark:music_disc_endermosh");
    event.get("kubejs:transaction_cards").add("#kubejs:transaction_cards/import")
    event.get("kubejs:transaction_cards").add("#kubejs:transaction_cards/profession")

    event.add("kubejs:strainer/sands", "minecraft:sand")

    event.get("kubejs:cake_slices")
        .add("farmersdelight:cake_slice")

    event.remove("tconstruct:anvil_metal", "thermal:bronze_block")

    // Create Deco laser lamps
    let decoLampColours = ["yellow", "red", "green", "blue"]
    let decoLampMaterials = ["andesite", "brass", "iron", "copper", "industrial_iron", "zinc"]
    for (let i = 0; i < decoLampColours.length; ++i) {
        for (let j = 0; j < decoLampMaterials.length; ++j) {
            let lamp = `createdeco:${decoLampColours[i]}_${decoLampMaterials[j]}_lamp`
            event.add("kubejs:alchemical_laser_lamps", lamp)
            event.add(`kubejs:alchemical_laser_lamps/${decoLampColours[i]}`, lamp)
        }
    }

    // Ad Astra laser lamps
    for (let i = 0; i < colours.length; ++i) {
        let lamp = `ad_astra:${colours[i]}_industrial_lamp`;
        event.add("kubejs:alchemical_laser_lamps", lamp)
        event.add(`kubejs:alchemical_laser_lamps/${colours[i]}`, lamp)
        lamp = `ad_astra:small_${colours[i]}_industrial_lamp`;
        event.add("kubejs:alchemical_laser_lamps", lamp)
        event.add(`kubejs:alchemical_laser_lamps/${colours[i]}`, lamp)
    }

    // This tag prevents items from being consumed in press (market) recipes
    event.get("thermal:crafting/dies").add("#kubejs:transaction_cards")
    event.get("thermal:crafting/dies").add("kubejs:missingno")

    event.get("thermal:crafting/casts").add("kubejs:three_cast").add("kubejs:eight_cast").add("kubejs:plus_cast").add("kubejs:minus_cast").add("kubejs:multiply_cast").add("kubejs:divide_cast").add("#kubejs:circuit_press")

    event.get("create:upright_on_belt")
        .add("ae2:red_paint_ball")
        .add("ae2:yellow_paint_ball")
        .add("ae2:green_paint_ball")
        .add("ae2:blue_paint_ball")
        .add("ae2:magenta_paint_ball")
        .add("ae2:black_paint_ball")

    // Items in the treasure tags are given as loot from treasure pots
    event.get("kubejs:treasure1")
        .add("minecraft:cobweb")
        .add("minecraft:dandelion")
        .add("minecraft:poppy")
        .add("minecraft:jungle_sapling")
        .add("minecraft:brown_mushroom")
        .add("minecraft:red_mushroom")
        .add("minecraft:bamboo")
        .add("minecraft:ladder")
        .add("minecraft:chain")
        .add("minecraft:flower_pot")
        .add("minecraft:painting")
        .add("minecraft:iron_nugget")
        .add("minecraft:gold_nugget")
        .add("create:copper_nugget")
        .add("create:zinc_nugget")
        .add("minecraft:charcoal")
        .add("minecraft:rotten_flesh")
        .add("minecraft:pumpkin_seeds")
        .add("minecraft:melon_seeds")
        .add("minecraft:bone_meal")
        .add("minecraft:paper")
        .add("farmersdelight:raw_pasta")
        .add("architects_palette:algal_blend")
        .add("farmersdelight:tree_bark")
        .add("create:cogwheel")
        .add("kubejs:sky_slimy_fern_leaf")
        .add("kubejs:earth_slimy_fern_leaf")
        .add("kubejs:ender_slimy_fern_leaf")
        .add("thermal:rubber")
        .add("thermal:phytogro")
        .add("create:andesite_alloy")
        .add("minecraft:poisonous_potato")

    event.get("kubejs:treasure2")
        .add("minecraft:lantern")
        .add("minecraft:redstone")
        .add("minecraft:bow")
        .add("farmersdelight:rice")
        .add("supplementaries:copper_lantern")
        .add("supplementaries:brass_lantern")
        .add("supplementaries:sconce")
        .add("supplementaries:rope_arrow")
        .add("supplementaries:slingshot")
        .add("supplementaries:flax_seeds")
        .add("supplementaries:bomb")
        .add("farmersdelight:sweet_berry_cookie")
        .add("farmersdelight:cabbage_seeds")
        .add("farmersdelight:tomato_seeds")
        .add("minecraft:scute")
        .add("minecraft:iron_ingot")
        .add("minecraft:copper_ingot")
        .add("create:zinc_ingot")
        .add("thermal:rosin")
        .add("minecraft:spider_eye")
        .add("minecraft:nether_brick")
        .add("minecraft:beetroot_seeds")
        .add("minecraft:book")
        .add("minecraft:name_tag")
        .add("farmersdelight:rope")
        .add("create:cinder_flour")
        .add("tconstruct:seared_brick")
        .add("farmersdelight:canvas")
        .add("thermal:cinnabar")
        .add("thermal:sulfur")
        .add("thermal:niter")
        .add("thermal:apatite")
        .add("minecraft:compass")
        .add("minecraft:experience_bottle")
        .add("minecraft:golden_carrot")
    // .add('antiqueatlas:empty_antique_atlas')

    // Treasure3 is only given from quartz pots
    event.get("kubejs:treasure3")
        .add("minecraft:skeleton_skull")
        .add("minecraft:clock")
        .add("minecraft:diamond")
        .add("minecraft:lapis_lazuli")
        .add("minecraft:zombie_head")
        .add("create:rose_quartz")
        .add("create:brass_hand")
        .add("minecraft:saddle")
        .add("ae2:certus_quartz_crystal")
        .add("ae2:fluix_crystal")
        .add("thermal:ice_charge")
        .add("thermal:lightning_charge")
        .add("thermal:earth_charge")
        .add("projectred_core:red_ingot")
        .add("thermal:ruby")
        .add("thermal:sapphire")
        .add("create:peculiar_bell")
        .add("minecraft:spectral_arrow")
        .add("minecraft:gold_ingot")
        .add("minecraft:magma_cream")
        .add("minecraft:ghast_tear")
        .add("minecraft:quartz")
        .add("minecraft:prismarine_shard")
        .add("minecraft:prismarine_crystals")
        .add("minecraft:chorus_fruit")
        .add("minecraft:blaze_powder")

    event.get("kubejs:ore_processing/metal/dusts")
        .add("#forge:dusts/copper")
        .add("#forge:dusts/iron")
        .add("#forge:dusts/gold")
        .add("#forge:dusts/zinc")
        .add("#forge:dusts/lead")
        .add("#forge:dusts/nickel")

    // This tag auto adds the beacon_payment_items tag which we don't want
    event.remove("create:create_ingots", "create:andesite_alloy")

    // A2
    // event.add("forge:slag", "tfmg:slag")
    event.add(`forge:dusts/wood`, "nuclearcraft:sawdust")// useless?
    event.get("kubejs:saws")
        .add("projectred_exploration:gold_saw")
        .add("projectred_exploration:ruby_saw")
        .add("projectred_exploration:sapphire_saw")
        .add("projectred_exploration:peridot_saw")
    event.add("forge:ingots/plastic", "nuclearcraft:bioplastic")
    event.add("forge:plates", "createdeco:zinc_sheet")
    event.add("forge:plates/zinc", "createdeco:zinc_sheet")

    // coal coke unification
    // event.add("forge:coal_coke", "tfmg:coal_coke")
    // event.add("forge:storage_blocks/coal_coke", "tfmg:coal_coke_block")
    // coal coke is not coal
    event.remove("forge:coal", "immersiveengineering:coal_coke")
    event.remove("minecraft:coals", "immersiveengineering:coal_coke")
    event.remove("c:coal", "immersiveengineering:coal_coke")

    // scguns didn't bother with item tags
    event.add("forge:ingots", "scguns:anthralite_ingot")
    event.add("forge:ingots/anthralite", "scguns:anthralite_ingot")
    event.add("forge:dusts", "scguns:anthralite_dust")
    event.add("forge:dusts/anthralite", "scguns:anthralite_dust")
    event.add("create:crushed_raw_materials", "scguns:crushed_raw_anthralite")
    event.add("create:crushed_raw_materials/anthralite", "scguns:crushed_raw_anthralite")
    event.add("forge:raw_materials", "scguns:raw_anthralite")
    event.add("forge:raw_materials/anthralite", "scguns:raw_anthralite")
    event.get("forge:ores")
        .add("scguns:anthralite_ore")
        .add("scguns:deepslate_anthralite_ore")
        .add("scguns:sulfur_ore")
        .add("scguns:deepslate_sulfur_ore")
        .add("scguns:nether_sulfur_ore")
    event.add("forge:ores/anthralite", "scguns:anthralite_ore")
    event.add("forge:ores/anthralite", "scguns:deepslate_anthralite_ore")
    event.get("forge:ores/sulfur")
        .add("scguns:sulfur_ore")
        .add("scguns:deepslate_sulfur_ore")
        .add("scguns:nether_sulfur_ore")
    event.add("forge:storage_blocks/anthralite", "scguns:anthralite_block")
    event.add("forge:storage_blocks/sulfur", "scguns:sulfur_block")
    event.add("forge:storage_blocks/treated_iron", "scguns:treated_iron_block")
    event.add("forge:storage_blocks", "scguns:treated_iron_block")

    // Plastic and Milk (moved from fluid block)
    event.add("forge:ingots/plastic", "nuclearcraft:bioplastic")
    event.get("kubejs:milk").add("minecraft:milk")

    // oredict simpleradios wire
    event.add("forge:wires/copper", "simpleradio:copper_wire")

    // tagging all chisels with durability
    event.get("forge:chisels")
        .add("chisel:iron_chisel")
        .add("chisel:diamond_chisel")
        .add("chisel:hitech_chisel")
        .add("tinkers_things:chisel")

    // meme stones (chisel, alexscaves, projectred_exploration)
    // limestone
    event.get("create:stone_types/limestone")// all block types
        .add(/alexscaves:limestone(?!_spear).*/)
        .add(/chisel:limestone.*/)
        .add(/v_slab_compat:.*limestone.*/)
    event.get("forge:stone/limestone")// full blocks only
        .add(/create:.*limestone(?!.*wall|.*slab|.*stair).*/)
        .add(/alexscaves:.*limestone(?!.*wall|.*slab|.*stair|_spear).*/)
        .add(/chisel:limestone.*/)
    event.get("chisel:carving/limestone")// full blocks only
        .add(/create:.*limestone(?!.*wall|.*slab|.*stair).*/)
        .add(/alexscaves:.*limestone(?!.*wall|.*slab|.*stair|_spear).*/)
    // marble
    event.get("forge:stone/marble")// full blocks only
        .add(/chisel:marble.*/)
    event.get("chisel:carving/marble")// full blocks only
        .add("projectred_exploration:marble")
        .add("projectred_exploration:marble_brick")
    // basalt
    event.get("chisel:carving/basalt")// full blocks only
        .add("projectred_exploration:basalt")
        .add("projectred_exploration:basalt_brick")
        .add("projectred_exploration:basalt_cobble")
})

ServerEvents.tags("block", event => {

    // Create Deco laser lamps
    let decoLampColours = ["yellow", "red", "green", "blue"]
    let decoLampMaterials = ["andesite", "brass", "iron", "copper", "industrial_iron", "zinc"]
    for (let i = 0; i < decoLampColours.length; ++i) {
        for (let j = 0; j < decoLampMaterials.length; ++j) {
            let lamp = `createdeco:${decoLampColours[i]}_${decoLampMaterials[j]}_lamp`
            event.add("kubejs:alchemical_laser_lamps", lamp)
            event.add(`kubejs:alchemical_laser_lamps/${decoLampColours[i]}`, lamp)
        }
    }

    // Ad Astra laser lamps
    for (let i = 0; i < colours.length; ++i) {
        let lamp = `ad_astra:${colours[i]}_industrial_lamp`;
        event.add("kubejs:alchemical_laser_lamps", lamp)
        event.add(`kubejs:alchemical_laser_lamps/${colours[i]}`, lamp)
        lamp = `ad_astra:small_${colours[i]}_industrial_lamp`;
        event.add("kubejs:alchemical_laser_lamps", lamp)
        event.add(`kubejs:alchemical_laser_lamps/${colours[i]}`, lamp)
    }

    event.remove("tconstruct:anvil_metal", "thermal:bronze_block")

    // Not sure if anything checks for this block tag but don't want to risk it.
    event.remove("forge:storage_blocks/copper", "minecraft:cut_copper");

    // I don't know why this isn't wrenchable by default
    event.add("create:wrench_pickup", "minecraft:note_block")

    event.add("create:wrench_pickup", "mbd2:strainer")

    event.add("create:wrench_pickup", /thermal:machine/)
    event.add("create:wrench_pickup", /thermal:device/)
    event.add("create:wrench_pickup", /thermal:dynamo/)
    event.add("create:wrench_pickup", "thermal:tinker_bench")
    event.add("create:wrench_pickup", "thermal:charge_bench")
    event.add("create:wrench_pickup", "thermal:energy_cell_frame")
    event.add("create:wrench_pickup", "thermal:energy_cell")
    event.add("create:wrench_pickup", "thermal:fluid_cell_frame")
    event.add("create:wrench_pickup", "thermal:fluid_cell")
    event.add("create:wrench_pickup", "thermal:energy_duct")
    event.add("create:wrench_pickup", "thermal:fluid_duct")
    event.add("create:wrench_pickup", "thermal:fluid_duct_windowed")

    /* // Supplementaries blocks added to wrench pickup - possible source of world load crash
    event.add("create:wrench_pickup", "supplementaries:cog_block")
    event.add("create:wrench_pickup", "supplementaries:relayer")
    event.add("create:wrench_pickup", "supplementaries:spring_launcher")
    event.add("create:wrench_pickup", "supplementaries:speaker_block")
    event.add("create:wrench_pickup", "supplementaries:turn_table")
    // event.add("create:wrench_pickup", 'supplementaries:pulley_block')
    // event.add("create:wrench_pickup", 'supplementaries:hourglass')
    event.add("create:wrench_pickup", "supplementaries:bellows")
    event.add("create:wrench_pickup", "supplementaries:clock_block")
    event.add("create:wrench_pickup", "supplementaries:crystal_display")
    event.add("create:wrench_pickup", "supplementaries:sconce_lever")
    event.add("create:wrench_pickup", "supplementaries:crank")
    event.add("create:wrench_pickup", "supplementaries:wind_vane")
    event.add("create:wrench_pickup", "supplementaries:faucet")
    */

    event.add("create:wrench_pickup", "#kubejs:alchemical_laser_lamps")

    // event.add("create:wrench_pickup", "cb_multipart:multipart")

    // Add tags to copper grates to allow fans to process through them
    event.get("create:fan_transparent")
        .add(/trials:copper_grate.*/)
        .add(/trials:waxed_copper_grate.*/)
        .add(/kubejs:trial_copper_grate*/)

        .add("everythingcopper:copper_grate")
        .add("everythingcopper:oxidized_copper_grate")

    // Add tags to basic vanilla-like chests and inventories to allow function with create contraptions
    event.get("create:chest_mounted_storage")
        .add(/^quark:.*_chest$|^everycomp:q.*_chest$/)

    event.get("create:simple_mounted_storage")
        .add(/^farmersdelight:.*_cabinet$|^everycomp:fd.*_cabinet$/)
        // AE2 Sky stone chests (These don't work with the create:chest_mounted_storage tag for some reason so they are here instead)
        .add("ae2:sky_stone_chest")
        .add("ae2:smooth_sky_stone_chest")

    // why?
    event.remove("forge:storage_blocks/iron", "scguns:treated_iron_block")

    // meme stones (chisel, alexscaves, projectred_exploration)
    // limestone
    event.get("forge:stone/limestone")// full blocks only
        .add(/create:.*limestone(?!.*wall|.*slab|.*stair).*/)
        .add(/alexscaves:.*limestone(?!.*wall|.*slab|.*stair|_spear).*/)
        .add(/chisel:limestone.*/)
    event.get("chisel:carving/limestone")// full blocks only
        .add(/create:.*limestone(?!.*wall|.*slab|.*stair).*/)
        .add(/alexscaves:.*limestone(?!.*wall|.*slab|.*stair|_spear).*/)
    // marble
    event.get("forge:stone/marble")// full blocks only
        .add(/chisel:marble.*/)
    event.get("chisel:carving/marble")// full blocks only
        .add("projectred_exploration:marble")
        .add("projectred_exploration:marble_brick")
    // basalt
    event.get("chisel:carving/basalt")// full blocks only
        .add("projectred_exploration:basalt")
        .add("projectred_exploration:basalt_brick")
        .add("projectred_exploration:basalt_cobble")

})

ServerEvents.tags("block_entity_type", event => {

    // Add tags to basic vanilla-like chests and inventories to allow function with tinker's side inventory feature on crafting stations
    event.get("tconstruct:side_inventories")
        .add("quark:variant_chest")
        .add("quark:variant_trapped_chest")
        .add(/^everycomp:q.*_chest$/)
        .add("farmersdelight:cabinet")
        .add("ae2:sky_stone_chest")
})

ServerEvents.tags("fluid", event => {
    // seed oils are not a substitute for water, please seek medical attention
    event.remove("minecraft:water", "createaddition:seed_oil")
    event.remove("minecraft:water", "createaddition:flowing_seed_oil")
    event.remove("minecraft:water", "createaddition:bioethanol")
    event.remove("minecraft:water", "createaddition:flowing_bioethanol")

    // event.add("kubejs:liquid_plastic", "tfmg:liquid_plastic")
    // ^ deprecated, fluid tag was required for NC recipe

    // kubejs molten anthralite
    event.add("forge:molten_anthralite", "kubejs:molten_anthralite")
    event.add("forge:anthralite", "kubejs:molten_anthralite")
    event.add("tconstruct:molten_anthralite", "kubejs:molten_anthralite")

    // kubejs liquid plastic
    event.add("forge:liquid_plastic", "kubejs:liquid_plastic")
    event.add("forge:molten_plastic", "kubejs:liquid_plastic")
    event.add("forge:plastic", "kubejs:liquid_plastic")

    // creosote tagging
    // event.add("forge:creosote", "tfmg:creosote")
})
