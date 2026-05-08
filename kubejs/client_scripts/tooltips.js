ItemEvents.modifyTooltips(tooltip => {
    let holds = (id, slots) => tooltip.add("metalbarrels:" + id + "_barrel", Text.translatable("item.metalbarrels.barrel.tooltip", slots));
    let main_assembly = (id, stage) => tooltip.add(id, Text.translatable("item.kubejs.main_assembly", stage == "4" ? "Finale" : "Chapter " + stage));
    let bonus_assembly = (id, stage) => tooltip.add(id, Text.translatable("item.kubejs.bonus_assembly", "Chapter " + stage));
    let not_consumed = (id) => tooltip.add(id, Text.translatable("item.kubejs.not_consumed_in_assembly"));

    tooltip.add("minecraft:redstone_ore", Text.translatable("item.minecraft.redstone_ore.tooltip"));
    tooltip.add("minecraft:deepslate_redstone_ore", Text.translatable("item.minecraft.deepslate_redstone_ore.tooltip"));
    tooltip.add("supplementaries:ash", Text.translatable("item.supplementaries.ash.tooltip"));
    tooltip.add("functionalstorage:storage_controller", Text.translatable("item.functionalstorage.controller.tooltip"));
    tooltip.add("functionalstorage:controller_extension", Text.translatable("item.functionalstorage.controller_extension.tooltip"));

    /*
    let wastelandOres = ["uranium", "boron", "thorium", "magnesium", "platinum"]
    wastelandOres.forEach(e => tooltip.add("nuclearcraft:" + e + "_ore", Text.red("This world's natural " + e + " reserves were depleted long ago.")));
    wastelandOres.forEach(e => tooltip.add("nuclearcraft:" + e + "_deepslate_ore", Text.red("This world's natural " + e + " reserves were depleted long ago.")));

    holds("copper", 5 * 9);
    holds("iron", 6 * 9);
    holds("silver", 8 * 9);
    holds("gold", 9 * 9);
    */

    tooltip.add("blazinghot:sturdy_casing", Text.darkGray("Cosmetic").italic());
    tooltip.add("blazinghot:blaze_casing", Text.darkGray("Cosmetic").italic());

    main_assembly("kubejs:kinetic_mechanism", "1");
    bonus_assembly("kubejs:sealed_mechanism", "1A");
    bonus_assembly("kubejs:reinforced_mechanism", "1B");
    main_assembly("create:precision_mechanism", "2");
    bonus_assembly("kubejs:infernal_mechanism", "2A");
    bonus_assembly("kubejs:logistic_mechanism", "2B");
    main_assembly("kubejs:inductive_mechanism", "3");
    bonus_assembly("kubejs:abstruse_mechanism", "3A");
    main_assembly("kubejs:calculation_mechanism", "4");
    /*
    not_consumed("cb_microblock:stone_saw");
    not_consumed("cb_microblock:iron_saw");
    not_consumed("cb_microblock:diamond_saw");
    not_consumed("projectred-core:screwdriver");
    */
    not_consumed("kubejs:chromatic_resonator");
    not_consumed("kubejs:flash_drive");

    global.substrates[0].forEach(e => tooltip.add(e.id, Text.translatable("item.kubejs.category.void")));
    global.substrates[1].forEach(e => tooltip.add(e.id, Text.translatable("item.kubejs.category.valkyriad")));
    global.substrates[2].forEach(e => tooltip.add(e.id, Text.translatable("item.kubejs.category.pandemoniac")));
    global.substrates[3].forEach(e => tooltip.add(e.id, Text.translatable("item.kubejs.category.lycanomorph")));
    global.substrates[4].forEach(e => tooltip.add(e.id, Text.translatable("item.kubejs.category.fractal")));
    global.substrates[5].forEach(e => tooltip.add(e.id, Text.translatable("item.kubejs.category.sloplike")));
    global.substrates[6].forEach(e => tooltip.add(e.id, Text.translatable("item.kubejs.category.memorial")));
    global.substrates[7].forEach(e => tooltip.add(e.id, Text.translatable("item.kubejs.category.catalyst")));
    tooltip.add("kubejs:substrate_void", Text.darkGray("\"yawn...\"").italic());
    tooltip.add("kubejs:substrate_void", Text.darkGray("- Blobbian").italic());
    tooltip.add("kubejs:substrate_valkyriad", Text.darkGray("Moored twenty million miles away, even as you hold it").italic());
    tooltip.add("kubejs:substrate_pandemoniac", Text.darkGray("Echoes with the clamor of an infernal mob").italic());
    tooltip.add("kubejs:substrate_lycanomorph", Text.darkGray("Bristles with disturbing geometry").italic());
    tooltip.add("kubejs:substrate_fractal", Text.darkGray("\"To make a machine casing from scratch, you must first invent the universe.\"").italic());
    tooltip.add("kubejs:substrate_fractal", Text.darkGray("- Unknown Engineer").italic());
    tooltip.add("kubejs:substrate_sloplike", Text.darkGray("Smells of fresh-sprayed foam").italic());
    tooltip.add("kubejs:substrate_memorial", Text.darkGray("Leaks profusely, but only when you aren't watching").italic());
    tooltip.add("kubejs:substrate_chaos", Text.darkPurple("The gate and the key to all that should not be").italic());

    // tooltip.add("structurescompass:structures_compass", Text.translatable("item.structurescompass.structures_compass.tooltip"));
    tooltip.add("reliquary:alkahestry_tome", Text.translatable("item.reliquary.alkahestry_tome.tooltip"));

    tooltip.add("kubejs:accellerator_redstone", Text.translatable("item.kubejs.accellerator_redstone.tooltip"));
    tooltip.add("kubejs:accellerator_glowstone", Text.translatable("item.kubejs.accellerator_glowstone.tooltip"));

    for (let i = 0; i < 15; i++) {
        tooltip.add(`kubejs:failed_alchemy_${i}`, Text.translatable("item.kubejs.failed_alchemy.tooltip"));
    }

    const pureore = ["minecraft:raw_iron", "minecraft:raw_copper", "minecraft:raw_gold",
        // 1.21: no thermal
        // "thermal:raw_lead", "thermal:raw_nickel", "create:raw_zinc",
        // A2: more pure ores
        "alexscaves:uranium",
        // "ad_astra:raw_desh", "ad_astra:raw_ostrum", "ad_astra:raw_calorite",
        // "nuclearcraft:boron_chunk", "nuclearcraft:magnesium_chunk", "nuclearcraft:thorium_chunk",
        // "tconstruct:raw_cobalt",
        // "immersiveengineering:raw_bauxite",
        // "tfmg:raw_lithium",
        // "nuclearcraft:lithium_chunk",
        // "scguns:raw_anthralite",
        // "thermal:raw_silver", "thermal:raw_tin"
    ];
    pureore.forEach(pureore => {
        tooltip.add(pureore, Text.translatable("item.kubejs.pureore.tooltip"));
    });

    /*
    tooltip.addAdvanced(["/^kubejs:.+machine$/", "thermal:machine_frame", "ae2:controller"], (item, adv, text) => {
        if (!tooltip.shift) {
            text.add(1, Text.translatable("item.kubejs.shift.summary"));
        } else {
            text.add(1, Text.translatable("item.kubejs.shift.summary"));
            text.add(2, Text.translatable("item.kubejs.machine.details"));
            text.add(3, Text.translatable("item.kubejs.machine.transformation"));
        }
    });
    tooltip.addAdvanced(["/^kubejs:trial.+$/"], (item, adv, text) => {
        if (!tooltip.shift) {
            text.add(1, Text.translatable("item.kubejs.shift.summary"));
        } else {
            text.add(1, Text.translatable("item.kubejs.shift.summary"));
            text.add(2, Text.translatable("item.kubejs.trial.details"));
            text.add(3, Text.translatable("item.kubejs.trial.special_info"));
        }
    });
    */

    // tooltip.addAdvanced(["occultism:spirit_fire"], (item, adv, text) => {
    //     if (!tooltip.shift) {
    //         text.add(1, Text.translatable("item.kubejs.shift.summary"));
    //     } else {
    //         text.add(1, Text.translatable("item.kubejs.shift.summary"));
    //         text.add(2, Text.translatable("item.spirit_fire.info"));
    //         text.add(3, Text.translatable("item.spirit_fire.creation"));
    //     }
    // });

    // tooltip.add("trials:crafter", Text.red(Text.translatable("cabin.trials.crafter.deprecation.tooltip")))
});
