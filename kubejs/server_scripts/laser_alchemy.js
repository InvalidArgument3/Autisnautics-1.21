// bonkers_chemistry port using simulated:laser_pointer

let LaserBehavior = Java.loadClass("dev.simulated_team.simulated.content.blocks.lasers.LaserBehaviour")

let cachedSeed = undefined
let cachedAlchemyData = {}

const SUBSTRATE_CATEGORIES = 8 // total number of substrate categories, including catalysts
const SUBSTRATES_PER_CATEGORY = 6 // number of substrates per category

const TOTAL_REAGENTS = 44 // number of reagents, including those like silver and silicon
const NORMAL_REAGENTS = 42 // number of reagents belonging to a category, excluding those like silver and silicon
const REAGENT_CATEGORIES = 7 // number of reagent categories


function shuffle(array, random) {
    for (let i = array.length - 1; i > 0; i--) {
        let j = random.nextInt(i + 1);
        let temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
    return array
}


let process = function (level, entity, laserColor) {

    // initialize cachedAlchemyData, runs only once per reload
    if (cachedSeed != level.getSeed()) {
        cachedSeed = level.getSeed()
        let random = Utils.newRandom(level.getSeed())
        /* For testing
        cachedSeed = level.getSeed() + (Math.random() * 9000000000)
        let random = Utils.newRandom(cachedSeed)
        */
        let next = () => random.nextInt(SUBSTRATES_PER_CATEGORY)
        let generateCode = () => [next(), next(), next(), next()]
        for (let cat = 0; cat < SUBSTRATE_CATEGORIES; cat++) {
            cachedAlchemyData[cat] = {
                code: generateCode(),
                result: cat == REAGENT_CATEGORIES ? "kubejs:substrate_chaos" : global.substrates[REAGENT_CATEGORIES][cat].id
            }
        }
        let total = []
        cachedAlchemyData["chaos_mapping"] = [] // chaos_mapping: array of each reagent with index as its pair
        for (let i = 0; i < TOTAL_REAGENTS; i++) {
            total.push(i)
            cachedAlchemyData["chaos_mapping"].push(0)
        }
        shuffle(total, random)
        for (let i = 0; i < TOTAL_REAGENTS; i += 2) { // avoid mapping any uncategorized reagent to another uncategorized reagent
            if (total[i] >= NORMAL_REAGENTS && total[i + 1] >= NORMAL_REAGENTS) {
                if (i == 0) {
                    let swap = total[i + 2]
                    total[i + 2] = total[i + 1]
                    total[i + 1] = swap
                } else {
                    let swap = total[i - 1]
                    total[i - 1] = total[i]
                    total[i] = swap
                }
            }
        }
        for (let i = 0; i < TOTAL_REAGENTS; i += 2) { // make chaos_mapping by scrambling total some more
            cachedAlchemyData["chaos_mapping"][total[i]] = total[i + 1]
            cachedAlchemyData["chaos_mapping"][total[i + 1]] = total[i]
        }
        // the index of each entry is its pair in the ordered list, shuffle() ensures they are never the same
        console.log("[laser_alchemy] Initialized chaos_mapping: " + cachedAlchemyData["chaos_mapping"])
    }

    let items = entity.getNbt().Items

    /*
    // Laser Recipe
    // Transmuting an item using a powered tool in place of a catalyst

    let validForProcessing = true
    let validTool = undefined//this will become the new item later
    let toProcess = undefined
    let processAmount = 0
    let magnet = "thermal:flux_magnet"
    let staff = "ae2:charged_staff"
    let entropy = "ae2:entropy_manipulator"

    items.forEach(e => {
        if (!validForProcessing)//cutoff switch
            return
        if (e.id.startsWith(magnet) || e.id.startsWith(staff) || e.id.startsWith(entropy)) {
            if (validTool)//if there's already a tool, we can't have a second one
                validForProcessing = false
            validTool = e
            return
        }
        if (toProcess && toProcess != e.id) {//we're already processing something, can't do it again
            validForProcessing = false
            return
        }
        toProcess = e.id
        processAmount += e.Count
    })

    if (validTool && validForProcessing && toProcess) {
        let resultItem = undefined//important
        let particle = "effect"

        if (!validTool.tag)//failsafe; probably different in 1.21 for item data changes
            return

        ////these can probably be made into a generic function taking toProcess, validTool, processAmount, resultItem, particle, name(+type?) of tool's energy tag, and an energy cost
        if (validTool.id.startsWith(magnet)) {
            if (!toProcess.equals("minecraft:basalt"))
                return
            let energy = validTool.tag.getInt("Energy") - 80 * processAmount
            if (energy < 0)
                return
            validTool.tag.putInt("Energy", energy)
            resultItem = "thermal:basalz_rod"
            particle = "flame"
        }
        // if (validTool.id.startsWith(staff)) {
        //     if (!toProcess.equals("kubejs:smoke_mote"))
        //         return
        //     let energy = validTool.tag.getDouble("internalCurrentPower") - 40 * processAmount
        //     if (energy < 0)
        //         return
        //     validTool.tag.putDouble("internalCurrentPower", energy)
        //     resultItem = "thermal:blitz_rod"
        //     particle = "firework"
        // }
        if (validTool.id.startsWith(entropy)) {
            if (!toProcess.equals("minecraft:snowball"))
                return
            let energy = validTool.tag.getDouble("internalCurrentPower") - 40 * processAmount
            if (energy < 0)
                return
            validTool.tag.putDouble("internalCurrentPower", energy)
            resultItem = "thermal:blizz_rod"
            particle = "spit"
        }

        if (!resultItem)//above blocks always set this at the end so this just means anything went wrong
            return

        level.server.runCommandSilent(`/particle minecraft:flash ${entity.x} ${entity.y + .5} ${entity.z} 0 0 0 .01 1`)
        level.server.runCommandSilent(`/particle ae2:matter_cannon_fx ${entity.x} ${entity.y + .5} ${entity.z}`)
        level.server.runCommandSilent(`/particle minecraft:${particle} ${entity.x} ${entity.y + .5} ${entity.z} .65 .65 .65 0 10`)
        level.server.runCommandSilent(`/playsound minecraft:block.enchantment_table.use block @a ${entity.x} ${entity.y} ${entity.z} 0.95 1.5`)
        attackNearby(level, entity.x, entity.y, entity.z)//deals damage in a small aoe around the cart. don't know if this has a point or it's just a meme

        let resultCount = Math.floor(processAmount / 2.0 + new Random().nextDouble())//output is input divided by [2...3]; careful if output > input due to space limit
        nbt.Items.clear()//delete all the items

        let actualIndex = 0
        for (let i = 0; i < 5; i++) {
            if (i == validTool.Slot) {//puts a new modified version of the tool back in its original slot
                nbt.Items.add(actualIndex, validTool)
                actualIndex++
                continue
            }
            if (resultCount <= 0)//if there's no more item outputs, just skip the slot
                continue

            let resultItemNBT = Utils.newMap();
            resultItemNBT.put("Slot", i)
            resultItemNBT.put("id", resultItem)
            resultItemNBT.put("Count", Math.min(64, resultCount))
            nbt.Items.add(actualIndex, resultItemNBT)
            actualIndex++

            resultCount = resultCount - 64//stack by stack
        }

        entity.setNbt(nbt)//set the minecart's nbt; again probably changed in 1.21
        return
    }
*/

    // Chaos Transmutation
    // Changing a reagent into its pair using the chaos catalyst

    let validForTransmutation = true
    let catalyst = undefined
    let toTransmute = undefined
    let transmuteAmount = 0

    items.forEach(e => {
        if (!validForTransmutation) {
            return
        }

        if (!e.getString("id").startsWith("kubejs:substrate_")) {
            validForTransmutation = false
            return
        }


        let mapping = global.substrate_mapping[e.getString("id").replace("kubejs:substrate_", "")]
        if (e.getString("id") != "kubejs:substrate_silicon" && e.getString("id") != "kubejs:substrate_silver" && (!mapping || mapping.category == REAGENT_CATEGORIES)) {
            if (catalyst || mapping) {// if unexpected catalyst, fail
                validForTransmutation = false
                return
            }
            catalyst = e
            return
        }

        if (toTransmute && toTransmute != e.getString("id")) {// only one type of reagent at a time
            validForTransmutation = false
            return
        }
        toTransmute = e.getString("id")// set reagent
        transmuteAmount += e.getInt("count")// and count
    })

    if (validForTransmutation && catalyst && toTransmute) {
        let categoryMapping = global.substrate_mapping[toTransmute.replace("kubejs:substrate_", "")]// get substrate_mapping for the reagent; silicon and silver do not have one!
        let resultItem

        if (toTransmute == "kubejs:substrate_silicon")// manually filling data in for weird reagents
            categoryMapping = { category: (REAGENT_CATEGORIES - 1), index: SUBSTRATES_PER_CATEGORY }
        if (toTransmute == "kubejs:substrate_silver")
            categoryMapping = { category: (REAGENT_CATEGORIES - 1), index: (SUBSTRATES_PER_CATEGORY + 1) }
        let data = cachedAlchemyData["chaos_mapping"]// get the map of reagent pairs
        let i1 = data[categoryMapping.category * SUBSTRATES_PER_CATEGORY + categoryMapping.index]// excludes last category which is catalysts
        resultItem = i1 == NORMAL_REAGENTS ? "kubejs:substrate_silicon" : // manually assign weird reagent results
            i1 == (NORMAL_REAGENTS + 1) ? "kubejs:substrate_silver" :
                global.substrates[Math.floor(i1 / SUBSTRATES_PER_CATEGORY)][i1 % SUBSTRATES_PER_CATEGORY].id

        // effects
        level.server.runCommandSilent(`/particle minecraft:flash ${entity.x} ${entity.y + .5} ${entity.z} 0 0 0 .01 1`)
        level.server.runCommandSilent(`/particle ae2:matter_cannon_fx ${entity.x} ${entity.y + .5} ${entity.z}`)
        level.server.runCommandSilent(`/particle minecraft:effect ${entity.x} ${entity.y + .5} ${entity.z} .75 .75 .75 .75 10`)
        level.server.runCommandSilent(`/playsound minecraft:block.enchantment_table.use block @a ${entity.x} ${entity.y} ${entity.z} 0.95 1.5`)
        // attackNearby(level, entity.x, entity.y, entity.z)

        let resultCount = 0
        for (let i = 0; i < transmuteAmount; i++) {
            if (Math.random() < 0.5) // individual 50% chance per item; output = inputd2 - input, bell curve distribution
                continue
            resultCount++
        }

        for (let i = 0; i < 5; i++) {
            if (i == catalyst.getShort("Slot")) { // leave the catalyst alone!
                continue
            }
            if (resultCount <= 0) {
                entity.extractItem(i, 256, false) // no more outputs: empty the slot and continue to the next slot
                continue
            }
            entity.extractItem(i, 256, false) // remove any old items
            entity.insertItem(i, Item.of(resultItem, Math.min(64, resultCount)), false) // add up to a stack of the output

            resultCount = resultCount - 64
        }
        return
    }

    // Laser Shifting
    // Transmuting a reagent into another reagent in a limited, lossy way using colored lasers

    let validForShifting = true
    let shiftCatalyst = undefined
    let toShift = undefined
    let shiftAmount = 0

    items.forEach(e => {
        if (!laserColor || (laserColor != "red" && laserColor != "green" && laserColor != "blue")) {
            console.log("invalid laser color")
            console.log(laserColor)
            return
        }

        if (!validForShifting) {
            return
        }

        if (!e.getString("id").startsWith("kubejs:substrate_")) {
            validForShifting = false
            return
        }

        let mapping = global.substrate_mapping[e.getString("id").replace("kubejs:substrate_", "")]
        if (!mapping || mapping.category == REAGENT_CATEGORIES) { // weird reagent, or a catalyst
            if (mapping && !shiftCatalyst) { // catalyst shifting
                shiftCatalyst = e
                return
            }
            validForTransmutation = false
        }

        if (toShift && toShift != e.getString("id")) {// only one type of reagent at a time
            validForShifting = false
            return
        }
        toShift = e.getString("id")// set reagent
        shiftAmount += e.getInt("count")// and count
    })

    if (validForShifting && toShift) {
        let categoryMapping = global.substrate_mapping[toShift.replace("kubejs:substrate_", "")]// get substrate_mapping for the reagent; silicon and silver do not have one!
        if (shiftCatalyst && global.substrate_mapping[shiftCatalyst.getString("id").replace("kubejs:substrate_", "")].index != categoryMapping.category) {
            console.log("wrong catalyst category: " + shiftCatalyst.getString("id"))
            return // mismatched catalyst category
        }
        let resultCategory = categoryMapping.category
        let resultIndex = categoryMapping.index
        let resultItem


        switch (laserColor) {
        case "red": // redshifting: move down a category or index
            if (shiftCatalyst && categoryMapping.index > 0) {
                resultIndex -= 1
                // resultItem = global.substrate_mapping[categoryMapping.category * SUBSTRATES_PER_CATEGORY + (categoryMapping.index - 1)].id
            }
            else if (!shiftCatalyst && categoryMapping.category > 0) {
                resultCategory -= 1
                // resultItem = global.substrate_mapping[(categoryMapping.category - 1) * SUBSTRATES_PER_CATEGORY + categoryMapping.index].id
            }
            break
        case "blue": // blueshifting: move up a category or index
            if (shiftCatalyst && categoryMapping.index < (SUBSTRATES_PER_CATEGORY - 1)) {
                resultIndex += 1
                // resultItem = global.substrate_mapping[categoryMapping.category * SUBSTRATES_PER_CATEGORY + (categoryMapping.index + 1)].id
            }
            else if (!shiftCatalyst && categoryMapping.category < (REAGENT_CATEGORIES - 1)) {
                resultCategory += 1
                // resultItem = global.substrate_mapping[(categoryMapping.category + 1) * SUBSTRATES_PER_CATEGORY + categoryMapping.index].id
            }
            break
        case "green": // greenshifting: move to category or index 0
            if (shiftCatalyst) {
                resultIndex = 0
            }
            else {
                resultCategory = 0
            }
            break
        default:
            console.log("i am error")
            return
        }
        if ( resultCategory == categoryMapping.category && resultIndex == categoryMapping.index ) {
            console.log("nothing doing!")
            return
        }
        resultItem = global.substrates[resultCategory][resultIndex].id

        // effects
        level.server.runCommandSilent(`/particle minecraft:flash ${entity.x} ${entity.y + .5} ${entity.z} 0 0 0 .01 1`)
        level.server.runCommandSilent(`/particle ae2:matter_cannon_fx ${entity.x} ${entity.y + .5} ${entity.z}`)
        level.server.runCommandSilent(`/particle minecraft:effect ${entity.x} ${entity.y + .5} ${entity.z} .75 .75 .75 .75 10`)
        level.server.runCommandSilent(`/playsound minecraft:block.enchantment_table.use block @a ${entity.x} ${entity.y} ${entity.z} 0.95 1.5`)
        // attackNearby(level, entity.x, entity.y, entity.z)

        let resultCount = Math.floor((shiftAmount / (4 - Math.random())))// output is input divided by 3...4

        for (let i = 0; i < 5; i++) {
            if (shiftCatalyst && i == shiftCatalyst.getShort("Slot")) { // leave the catalyst alone!
                continue
            }
            if (resultCount <= 0) {
                entity.extractItem(i, 256, false) // no more outputs: empty the slot and continue to the next slot
                continue
            }
            entity.extractItem(i, 256, false) // remove any old items
            entity.insertItem(i, Item.of(resultItem, Math.min(64, resultCount)), false) // add up to a stack of the output

            resultCount = resultCount - 64
        }
        return
    }

/*
    // Catalyst Mastermind
    //forming new stuff by combinations, including accelerators

    let catCode = -1;
    let guessedSet = []//set of indices within category e.g. [0,3,2,2] (never over 5)
    let reagents = []//same as above but it's the substrates' item ids
    let guessedString = ""
    let count = 0;
    let redstoneAccellerator = false
    let glowstoneAccellerator = false
    let valid = true

    if (items.length < 4)//need at least 4 items (can have 5 with accelerator)
        return

    items.forEach(e => {
        if (e.Count > 1) {//only one item allowed per stack
            valid = false
            return
        }
        if (e.id.startsWith("kubejs:accellerator_redstone")) {//redstone switch
            redstoneAccellerator = true
            return
        }
        if (e.id.startsWith("kubejs:accellerator_glowstone")) {//glowstone switch
            glowstoneAccellerator = true
            return
        }
        if (!e.id.startsWith("kubejs:substrate_")) {//if it's not an accelerator it has to be a substrate
            valid = false
            return
        }
        let mapping = global.substrate_mapping[e.id.replace("kubejs:substrate_", "")]//get substrate_mapping for the substrate
        if (!mapping)//if it's chaos, silicon, or silver, fail
            return
        if (catCode != -1 && catCode != mapping.category)//if we have a category and it doesn't match this one's category, fail
            return
        catCode = mapping.category//category is set to the reagent's category (including catalysts category)
        guessedSet.push(mapping.index)//pushes each item's category-specific index in order
        reagents.push(e.id)//push a list of ids to reagents set
        count++//number of valid substrates; this is not Item.Count
        guessedString = guessedString + "§6" + mapping.name + "§7" + (count < 4 ? ", " : "")//add substrate_mapping.name to guessedString with or without comma
    })

    if (!valid)//fail switch
        return
    if (count != 4)//if you put in 5 substrates, fail
        return
    if (!cachedAlchemyData[catCode])//failsafe for invalid category
        return

    let data = cachedAlchemyData[catCode]//get the object for our category
    let unmatchedCorrectSet = data.code.slice()//clone the correct code set up earlier
    let unmatchedGuessedSet = guessedSet.slice()//clone guessed code
    let result = [0, 0, 0]//amount of each item to give back: ash, redstone, glowstone
    let resultEval = [0, 0, 0, 0]//status of each guessed digit, 0=no match, 1=partial match, 2=exact match
    let trueFalse = [true, false]//used to distinguish between exact matches or partial matches later
    let retain = -1//whether one ingredient should be consumed (a random one)

    trueFalse.forEach(exact => {
        for (let digit = 0; digit < 4; digit++) {//for every digit in the guessed code...
            let guessed = unmatchedGuessedSet[digit]
            for (let digit2 = 0; digit2 < unmatchedCorrectSet.length; digit2++) {//for every digit in the correct code...
                let correct = unmatchedCorrectSet[digit2]
                if (correct != guessed)//if the two aren't the same number at all, quit
                    continue
                if (exact && digit != digit2)//if we're looking for exact matches and it's not an exact match, quit
                    continue

                resultEval[digit] = exact ? 2 : 1//write 2 if it's an exact match, 1 if it's not (0 means no match)
                result[exact ? 2 : 1] = result[exact ? 2 : 1] + 1//add a hint item for each match
                unmatchedGuessedSet[digit] = -2//these are to avoid processing the same thing twice I think?
                unmatchedCorrectSet[digit2] = -1
                break
            }
        }
    })

    if (glowstoneAccellerator || redstoneAccellerator) {
        let random = new Random()
        let shuffled = shuffle(Array(0, 1, 2, 3), random)//shuffle to ensure the same item doesn't get retained every time
        for (let i = 0; i < 4; i++) {
            let j = shuffled[i]
            if (glowstoneAccellerator && resultEval[j] == 2) {//exact matches
                retain = j
                break
            }
            if (redstoneAccellerator && resultEval[j] == 1) {//inexact matches
                retain = j
                break
            }
        }
    }

    result[0] = 4 - result[2] - result[1]//derive failed from total - exact - inexact, give that amount of ash

    // console.log("Correct: " + data.code)
    // console.log("Guessed: " + guessedSet)
    // console.log("Result: " + result)
    // console.log("Retained: " + retain)

    let errorId = -1
    //match one of the 14 different failed_alchemy items based on how and where you fucked up
    if (result[0] == 4)//oops, all ash!
        errorId = 0
    if (result[0] == 3) {//3 failed
        if (result[1] == 1)//a single partial match
            errorId = 1
        if (result[1] == 0)//zero partial matches, implying one true match
            errorId = 2
    }//etc etc...
    if (result[0] == 2) {
        if (result[1] == 2)
            errorId = 3
        if (result[1] == 0)
            errorId = 4
        if (result[1] == 1)
            errorId = 5
    }
    if (result[0] == 1) {
        if (result[1] == 3)
            errorId = 6
        if (result[1] == 0)
            errorId = 7
        if (result[1] == 2)
            errorId = 8
        if (result[1] == 1)
            errorId = 9
    }
    if (result[0] == 0) {
        if (result[1] == 4)
            errorId = 10
        if (result[1] == 3)
            errorId = 12
        if (result[1] == 1)
            errorId = 13
        if (result[1] == 2)
            errorId = 14
    }

    let success = errorId == -1//boolean alias for if errorid wasn't set i.e. 4 correct guesses
    let resultItem = success ? data.result : `kubejs:failed_alchemy_${errorId}`
    level.server.runCommandSilent(`/particle minecraft:flash ${entity.x} ${entity.y + .5} ${entity.z} 0 0 0 .01 1`)
    level.server.runCommandSilent(`/particle ae2:matter_cannon_fx ${entity.x} ${entity.y + .5} ${entity.z}`)
    level.server.runCommandSilent(`/particle minecraft:dust 0 1 1 1 ${entity.x} ${entity.y + .5} ${entity.z} .75 .75 .75 .75 ${success ? "80" : "6"}`)
    level.server.runCommandSilent(`/playsound minecraft:block.enchantment_table.use block @a ${entity.x} ${entity.y} ${entity.z} 0.95 ${success ? "2" : "1.25"}`)
    attackNearby(level, entity.x, entity.y, entity.z)
    if (success)
        level.server.runCommandSilent(`/playsound minecraft:block.beacon.activate block @a ${entity.x} ${entity.y} ${entity.z} 0.95 1.5`)//success noise, should add one for total failure
    nbt.Items.clear()//clear cart

    let resultItemNBT = {}
    // let resultItemTagNBT = {}
    // let resultItemLoreNBT = {}
    // let resultItemLoreList = []

    // resultItemLoreList.push({text: "' + guessedString + '", italic: false})
    // resultItemLoreNBT.Lore = resultItemLoreList
    // resultItemTagNBT.display = resultItemLoreNBT

    resultItemNBT.Slot = 0
    resultItemNBT.id = resultItem
    resultItemNBT.Count = 1
    // if (errorId != -1)
    //     resultItemNBT.tag = resultItemTagNBT
    nbt.Items.add(0, resultItemNBT)//always goes in first slot

    if (retain != -1) {
        resultItemNBT = {Slot: 1, id: reagents[retain], Count: 1};//a single retained item, always in second slot
        nbt.Items.add(1, resultItemNBT)
    }

    entity.setNbt(nbt)//add to cart
*/
}

BlockEvents.leftClicked(event => {
    let player = event.getPlayer()
    if (player.miningBlock) { return }

    let block = event.getBlock()
    if (!block.id.startsWith("simulated:laser_pointer") || block.getProperties().get("powered") != "true") { return }
    let color = "other"
    console.log(typeof block.getEntityData().getInt("LaserColor"))
    switch (block.getEntityData().getInt("LaserColor")) {
    case 16711680:
        color = "red"
        break
    case 255:
        color = "blue"
        break
    case 65280:
        color = "green"
        console.log("big green")
        break
    }
    let item = event.getItem()
    if (!item.empty) { return }


    console.log("BONK!")

    let level = event.getLevel()

    let laser = block.getEntity().getAllBehaviours().stream().filter((behaviour) => behaviour instanceof LaserBehavior ).findFirst().get()
    let target = laser.getClosestHitResult().entity ? laser.getClosestHitResult().entity : null

    if (target != null && target.type.equals("minecraft:hopper_minecart")) {
        target.attack(1)
        process(level, target, color)
    }
})
