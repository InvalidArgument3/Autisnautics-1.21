FTBQuestsEvents.customReward("641BC00F8A12FC5B", event => {
    event.player.give(Item.of('alexscaves:rainbounce_boots', "{Damage:0,RepairCost:1,display:{Lore:['{\"color\":\"purple\",\"text\":\"Stolen from the heavens.\"}']}}").enchant('minecraft:unbreaking', 4))
})
// Blobbian Ender Slimer power
FTBQuestsEvents.customReward("03991F0675E0FE1F", event => {
    event.server.runCommandSilent(`/power grant ${event.player.getName().getString()} autisorigins:ender_slimer`)
    event.server.runCommand(`/tellraw ${event.player.getName().getString()} ["",{"text":"after my big adventure, i feel like a ","italic":true},{"text":"whol new blobb","bold":true,"italic":true,"color":"light_purple"},{"text":". maybe I can press the ","italic":true},{"text":"secondary action key","bold":true,"italic":true},{"text":" to throw ","italic":true},{"text":"new blobbs","bold":true,"italic":true,"color":"light_purple"},{"text":"!","italic":true}]`)
})
