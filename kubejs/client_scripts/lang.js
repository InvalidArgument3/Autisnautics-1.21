const LANGS = ["es_es", "fr_fr", "pt_br", "ru_ru", "zh_cn"]
LANGS.forEach(lang => {
    ClientEvents.lang(lang, event => {
        global.trades.forEach(id => {
            // raw may be "16x mod:custom_item" or "minecraft:dirt"
            let raw = global.transactions[id][0].out
            let itemId = String(raw).replace(/^\s*\d+x\s*/i, "").trim()
            let displayName = Item.exists(itemId)
                ? Text.translatable(Item.of(itemId).item.getDescriptionId())
                : Text.of(itemId)

            let cardKey = `item.kubejs.trade_card_${id}`

            event.add(cardKey, Text.translatable("cabin.import.prefix", displayName).getString())
        })

        global.professions.forEach(id => {
            let profKey = `cabin.profession.${id}`
            let display = Text.translatable("cabin.profession.prefix", Text.translatable(profKey)).getString()

            let cardKey = `item.kubejs.profession_card_${id}`

            event.add(cardKey, display)
        })
    })
})
