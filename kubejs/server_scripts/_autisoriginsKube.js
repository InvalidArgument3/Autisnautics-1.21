
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
const etherMutationMessageArray = ["You are covered by a heavy carapace.","Your feet transformed into hooves.","Your neck is extremely thick.","Your back has grown a single feather.","Desire for violence arises within you.","Poison drips from your hands.","You generate gravity.","Clouds of rain follow you.","Your head has grown huge.","You have 3 eyes.","Your existence provokes living things.","You have dementia.","You suffer debilitation.","You have multiple mouths. Each bite of food feeds a quarter more.","Your knees are explosive.","You feel like everything is getting smaller.","You feel like everything is getting bigger.","You glow in the dark.","You swallow something bad.","Let's Ragnarok!!!","Your stomach can digest rotten things.","You are licked by a passing black cat. 'Umimyaa!'","You feel a lash on your back. 'You're such a slowpoke. Work instead of praying.'","You gain knowledge of a secret experience. 'I shall reward you...'","Your heart regenerates your body.","Your blood runs cold.","Your blood starts to boil.","Your arms become thin and long.","Your brain degenerates.","Your legs become lithe.","Your skin becomes pale.","You grow a second heart.","You have sores on your face.","You feel as though a stroke of good fortune passes by."]

let etherMutation = "autisorigins:ether__fallback"// fallback power
let mutationMessage = "You mutate imperceptibly."// fallback message

const getEtherMutation = function (event) {
    // NeoOrigins stores grants on the neoforge attachment, not the old ForgeCaps/apoli blob.
    let playerPowers = event.player.getNbt().getCompound("neoforge:attachments").getCompound("neoorigins:origin_data").toString()

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
    let etherBlob = event.player.getNbt().getCompound("neoforge:attachments").getCompound("neoorigins:origin_data").toString()
    let etherArray = etherBlob.match(/autisorigins:ether__[a-z_]+/gi) || []

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

// Sugary food is handled by autisorigins:sugar_replenishment_food (item tags, scaled).
// This command remains for debugging a flat refill.
ServerEvents.basicCommand('add_sugar', event => {
    event.server.runCommandSilent(`/resource change ${event.player.getName().getString()} autisorigins:sugar_addict 80`)
})

// debug command
ServerEvents.basicCommand('lower_sugar', event => {
    event.server.runCommand(`/resource change ${event.player.getName().getString()} autisorigins:sugar_addict -100`)
})

// Weak Flesh halves every status-effect duration. The datapack power
// autisorigins:weak_flesh_key1 asks NeoOrigins for that, and NeoOrigins applies
// it through MobEffectInstance.update, which keeps the longer time. A tick
// later, the effect that just landed is replaced with one at half the duration.
let $WeakFleshAdded = Java.loadClass('net.neoforged.neoforge.event.entity.living.MobEffectEvent$Added')
let $WeakFleshAttachments = Java.loadClass('com.cyberday1.neoorigins.attachment.OriginAttachments')
let $WeakFleshLayer = Java.loadClass('net.minecraft.resources.ResourceLocation').parse('origins:origin')
let $WeakFleshDaemon = Java.loadClass('net.minecraft.resources.ResourceLocation').parse('autisorigins:daemon')
let $WeakFleshPlayer = Java.loadClass('net.minecraft.server.level.ServerPlayer')
let $WeakFleshInstance = Java.loadClass('net.minecraft.world.effect.MobEffectInstance')

NativeEvents.onEvent($WeakFleshAdded, event => {
    try {
        let entity = event.getEntity()
        if (!(entity instanceof $WeakFleshPlayer)) return
        if (entity.level.isClientSide()) return
        let origin = entity.getData($WeakFleshAttachments.originData()).getOrigin($WeakFleshLayer)
        if (origin == null || !origin.equals($WeakFleshDaemon)) return
        let inst = event.getEffectInstance()
        if (inst == null || inst.getDuration() < 0) return
        let effect = inst.getEffect()
        let half = Math.floor(inst.getDuration() / 2)
        let amp = inst.getAmplifier()
        let ambient = inst.isAmbient()
        let visible = inst.isVisible()
        let show = inst.showIcon()
        entity.server.scheduleInTicks(1, () => {
            try {
                if (entity.getEffect(effect) == null) return
                entity.getActiveEffectsMap().put(effect, new $WeakFleshInstance(effect, half, amp, ambient, visible, show))
            } catch (err) {
                console.log('[weak flesh] ' + err)
            }
        })
    } catch (err) {
        console.log('[weak flesh] ' + err)
    }
})

// Fix NeoOrigins leaving players stuck invulnerable when all classes are disabled
// (hadAllOrigins stays false because origins:class is never selected).
function ensureHadAllOrigins(player) {
    try {
        if (!(player instanceof $WeakFleshPlayer)) return
        let data = player.getData($WeakFleshAttachments.originData())
        if (!data.isHadAllOrigins() && data.getOrigin($WeakFleshLayer) != null) {
            data.setHadAllOrigins(true)
        }
    } catch (err) {
        console.log('[origin invuln fix] ' + err)
    }
}

PlayerEvents.loggedIn(event => {
    ensureHadAllOrigins(event.player)
})

PlayerEvents.tick(event => {
    if (event.player.age % 20 === 0) {
        ensureHadAllOrigins(event.player)
    }
})
