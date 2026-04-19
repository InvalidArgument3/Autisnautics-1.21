// //Alchemist potion duping
let allocatedPotion = Item.of("minecraft:potion", 1)// default value should never appear

const saveAllocatedPotion = function (event, potion) {
    let potionCopy = potion.copy()
    let data = potionCopy.getNbt()
    data.putBoolean("alchemicalAllocated", true)
    potionCopy.setNbt(data)
    allocatedPotion = potionCopy
    // event.player.tell(Text.green(`allocatedPotion saved: ` + allocatedPotion.getId().toString() + `, nbt: ` + allocatedPotion.nbt.toString()))
}

// run by autisorigins:alchemical_allocation/allocatestart
ServerEvents.customCommand('allocate_get', event => {
    let jsMainItem = Item.of(event.player.getMainHandItem())
    let jsOffItem = Item.of(event.player.getOffhandItem())

    if (jsMainItem.getId() == "minecraft:potion") {
        saveAllocatedPotion(event, jsMainItem)
    }
    else if (jsOffItem.getId() == "minecraft:potion") {
        saveAllocatedPotion(event, jsOffItem)
    }
})


// run by autisorigins:alchemical_allocation/allocatefinish
ServerEvents.customCommand('allocate_give', event => {
    function callback (e) {
        event.server.scheduleInTicks(1, callback => {// if we don't delay this it deletes the original potion instead of the bottle
            let oldVial = e.player.inventory.getSelected()
            oldVial.shrink(1)
            e.player.give(allocatedPotion)
        })
    }
    callback(event)
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
ServerEvents.customCommand('ether_mutate', event => {
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
ServerEvents.customCommand('ether_cure', event => {
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
const DietApi = Java.loadClass("com.illusivesoulworks.diet.api.DietApi")
let nameGroupArray = []
let sugarsValue = 0

function nameGroups(value, key, map) {
    nameGroupArray.push(value.getName())
    if (value.getName() == "sugars") {
        sugarsValue = Math.floor(key * 1000)
    }
}

// run by autisorigins:sugar_replenishment_food
ServerEvents.customCommand('add_sugar', event => {
    nameGroupArray = []
    sugarsValue = 0

    let handDietResult = DietApi.getInstance().get(event.player, Item.of(event.player.getMainHandItem())).get()// Map<IDietGroup, Float>
    handDietResult.forEach(nameGroups)
    if (nameGroupArray.includes("sugars") && sugarsValue != 0) {
        event.server.runCommandSilent(`/resource change ${event.player.getName().getString()} autisorigins:sugar_addict ${sugarsValue.toString()}`)
    }
})

// debug command
ServerEvents.customCommand('lower_sugar', event => {
    event.server.runCommand(`/resource change ${event.player.getName().getString()} autisorigins:sugar_addict -100`)
})
