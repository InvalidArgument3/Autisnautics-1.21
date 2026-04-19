// FTBQuestsEvents.completed("252B9DD5BFB8184A", event => {
// let runCommand = (cmd) => {
//     event.server.scheduleInTicks(10, event.server, function (callback) {
//         callback.data.runCommandSilent(cmd)
//     })
// }
//     let message;
//     let refund = false;

//     if (event.player.level.dimension = 'minecraft:nether') {
//         let structureName = Platform.isLoaded("betterfortresses") ? "betterfortresses:fortress" : "fortress";
//         let playerLevel = event.player.getLevel().getDimension().getPath();

//         let structure = TagKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, structureName);
//         let blockPos = new BlockPos(event.player.getX(), event.player.getY(), event.player.getZ());
//         let radius = 32;

//         let position = playerLevel.findNearestMapFeature(structure, blockPos, radius, false);

//         if (blockPos) {
//             message = `A Fortress was found at ${position.x} ${position.z}.`;

//         } else {
//             message = "No fortress could be found. Your gold has been refunded.";
//             refund = true;
//         }
//     } else {
//         message = "The Locator cannot be used here. Your gold has been refunded.";
//         refund = true;
//     }

//     event.server.scheduleInTicks(10, event.server, function (callback) {
//         if (refund) {
//             event.player.give(Item.of("thermal:gold_coin", 2))
//         }
//         callback.data.runCommand(`/tell ${event.player.name.text} ${message}`)
//     })
// })

// Accursed One Rainbounce Boots
FTBQuestsEvents.customReward("641BC00F8A12FC5B", event => {
    event.player.give(Item.of('alexscaves:rainbounce_boots', "{Damage:0,RepairCost:1,display:{Lore:['{\"color\":\"purple\",\"text\":\"Stolen from the heavens.\"}']}}").enchant('minecraft:unbreaking', 4))
})
// Blobbian Ender Slimer power
FTBQuestsEvents.customReward("03991F0675E0FE1F", event => {
    event.server.runCommandSilent(`/power grant ${event.player.getName().getString()} autisorigins:ender_slimer`)
    event.server.runCommand(`/tellraw ${event.player.getName().getString()} ["",{"text":"after my big adventure, i feel like a ","italic":true},{"text":"whol new blobb","bold":true,"italic":true,"color":"light_purple"},{"text":". maybe I can press the ","italic":true},{"text":"secondary action key","bold":true,"italic":true},{"text":" to throw ","italic":true},{"text":"new blobbs","bold":true,"italic":true,"color":"light_purple"},{"text":"!","italic":true}]`)
})
