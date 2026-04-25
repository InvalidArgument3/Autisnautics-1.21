
const originId = function (event) {
    return event.player.getNbt().getCompound("neoforge:attachments").getCompound("neoorigins:origin_data").getCompound("origins").getString("origins:origin")
}

// Alchemist potion duping
ItemEvents.rightClicked((event) => {
    if (originId(event) == "autisorigins:alchemist" && event.item.id == "minecraft:potion" && event.item.getCustomData().getBoolean("alchemicalAllocated") != true) {
        // allocate_get
        let potionCopy = event.item.copy()
        let data = potionCopy.getCustomData()
        data.putBoolean("alchemicalAllocated", true)
        potionCopy.setCustomData(data)
        // cursed allocate_give
        event.server.scheduleInTicks(32, callback => {// surely this won't break if there's lag
            let oldVial = event.player.inventory.getSelected()
            if (oldVial.id == "minecraft:glass_bottle") {
                oldVial.shrink(1)
                event.server.runCommandSilent(`/execute at ${event.player.getName().getString()} run playsound minecraft:entity.llama.spit player @a`)
                event.player.give(potionCopy)
            }
        })
    }
})


// run by autisorigins:bomb/blastem
// not working yet (resources are broken)
ServerEvents.basicCommand('bomb', event => {
    let look = event.player.getLookAngle()
    event.server.runCommandSilent(`/execute at ${event.player.getName().getString()} run summon supplementaries:bomb ~ ~1.5 ~ {"Timer":200,"Active":1b,"Motion":[${look.x},${look.y},${look.z}]}`)
})

// //Ether Disease
const etherMutationArray = ["autisorigins:ether__carapace","autisorigins:ether__hooves","autisorigins:ether__neck","autisorigins:ether__wing","autisorigins:ether__slaughter","autisorigins:ether__hand","autisorigins:ether__gravity","autisorigins:ether__rain","autisorigins:ether__head","autisorigins:ether__eye","autisorigins:ether__enemy","autisorigins:ether__dementia","autisorigins:ether__weakening","autisorigins:ether__mouth","autisorigins:ether__explodingknees","autisorigins:ether__gigantism","autisorigins:ether__dwarfism","autisorigins:ether__fluorescent","autisorigins:ether__pregnant","autisorigins:ether__ragnarok","autisorigins:ether__stomach","autisorigins:ether__ehekatl","autisorigins:ether__lulwy","autisorigins:ether__kumiromi","autisorigins:ether__regeneration","autisorigins:ether__icyveins","autisorigins:ether__burningblood","autisorigins:ether__weakarm","autisorigins:ether__stupid","autisorigins:ether__litheleg","autisorigins:ether__vampirism","autisorigins:ether__secondheart","autisorigins:ether__hives","autisorigins:ether__placebo"]
const etherMutationMessageArray = ["You are covered by a heavy carapace.","Your feet transformed into hooves.","Your neck is extremely thick.","Your back has grown a single feather.","Desire for violence arises within you.","Poison drips from your hands.","You generate gravity.","Clouds of rain follow you.","Your head has grown huge.","You have 3 eyes.","Your existence provokes living things.","You have dementia.","You suffer debilitation.","You have multiple mouths.","Your knees are explosive.","You feel like everything is getting smaller.","You feel like everything is getting bigger.","You glow in the dark.","You swallow something bad.","Let's Ragnarok!!!","Your stomach can digest rotten things.","You are licked by a passing black cat. 'Umimyaa!'","You feel a lash on your back. 'You're such a slowpoke. Work instead of praying.'","You gain knowledge of a secret experience. 'I shall reward you...'","Your heart regenerates your body.","Your blood runs cold.","Your blood starts to boil.","Your arms become thin and long.","Your brain degenerates.","Your legs become lithe.","Your skin becomes pale.","You grow a second heart.","You have sores on your face.","You feel as though a stroke of good fortune passes by."]

let etherMutation = "autisorigins:ether__fallback"// fallback power
let mutationMessage = "You mutate imperceptibly."// fallback message

const getEtherMutation = function (event) {
    let playerData = event.player.getNbt()
    let playerPowerData = playerData.get("ForgeCaps").get("apoli:powers")
    let playerPowers = playerPowerData.toString()

    for (let i = 0; i < 3; i++) {// you get two rerolls in case you already had the mutation
        let randomIndex = Math.floor(Math.random() * etherMutationArray.length)
        let randomMutation = etherMutationArray[randomIndex]
        if (!playerPowers.includes(randomMutation)) {
            etherMutation = randomMutation
            mutationMessage = etherMutationMessageArray[randomIndex]
            break
        }
        else {
            etherMutation = "autisorigins:ether__fallback"
            mutationMessage = "You mutate imperceptibly."
        }
    }
}

// run by autisorigins:ether_disease
ServerEvents.basicCommand('ether_mutate', event => {
    getEtherMutation(event)
    function callback (e) {
        event.server.scheduleInTicks(1, callback => {// just in case
            if (etherMutation != "autisorigins:ether__fallback") {
                e.server.runCommandSilent(`/power grant ${e.player.getName().getString()} ${etherMutation}`)
            }
            if (mutationMessage != "You mutate imperceptibly.") {
                let playerX = e.player.getX()
                let playerY = e.player.getY()
                let playerZ = e.player.getZ()
                e.server.runCommandSilent(`/particle minecraft:poof ${e.player.getX()} ${e.player.getY()} ${e.player.getZ()} 0 1 0 0.2 100`)
                e.server.runCommandSilent(`/execute at ${e.player.getName().getString()} run playsound minecraft:entity.shulker.shoot neutral @a ~ ~ ~ 1 0.6`)
                e.server.runCommand(`/tellraw ${e.player.getName().getString()} ["",{"text":"Your ether disease has progressed.","italic":true,"color":"gray"},{"text":"\\n"},{"text":"${mutationMessage}","italic":true,"color":"aqua"}]`)
            }
        })
    }
    callback(event)
})

// debug command for now
ServerEvents.basicCommand('ether_cure', event => {
    let etherArray = event.player.getNbt().get("ForgeCaps").get("apoli:powers").toString().match(/autisorigins:ether__[a-z_]+(?=")/gi)// doesn't match subpowers

    for (let i = 0; i < etherArray.length; i++) {
        event.server.runCommandSilent(`/power revoke ${event.player.getName().getString()} ${etherArray[i]}`)
        event.server.runCommand(`/tellraw ${event.player.getName().getString()} ["",{"text":"Curing ${etherArray[i]}.","italic":true,"color":"gray"}]`)
    }
    if (etherArray.length > 0) {
        event.server.runCommandSilent(`/execute at ${event.player.getName().getString()} run playsound minecraft:entity.zombie_villager.cure neutral @a ~ ~ ~`)
        event.server.runCommand(`/tellraw ${event.player.getName().getString()} ["",{"text":"Your ether disease has been cured... for now.","italic":true,"color":"aqua"}]`)
    }
})


// //Angel Sugar Resource
/* 1.21: no DietAPI
const DietApi = Java.loadClass("com.illusivesoulworks.diet.api.DietApi")
*/
let nameGroupArray = []
let sugarsValue = 0

function nameGroups(value, key, map) {
    nameGroupArray.push(value.getName())
    if (value.getName() == "sugars") {
        sugarsValue = Math.floor(key * 1000)
    }
}

// run by autisorigins:sugar_replenishment_food
ServerEvents.basicCommand('add_sugar', event => {
    nameGroupArray = []
    sugarsValue = 0
    /* 1.21: no DietAPI
    let handDietResult = DietApi.getInstance().get(event.player, Item.of(event.player.getMainHandItem())).get()// Map<IDietGroup, Float>
    handDietResult.forEach(nameGroups)
    if (nameGroupArray.includes("sugars") && sugarsValue != 0) {
        event.server.runCommandSilent(`/resource change ${event.player.getName().getString()} autisorigins:sugar_addict ${sugarsValue.toString()}`)
    }
    */
    event.server.runCommandSilent(`/resource change ${event.player.getName().getString()} autisorigins:sugar_addict 1`)// todo
})

// debug command
ServerEvents.basicCommand('lower_sugar', event => {
    event.server.runCommand(`/resource change ${event.player.getName().getString()} autisorigins:sugar_addict -100`)
})
