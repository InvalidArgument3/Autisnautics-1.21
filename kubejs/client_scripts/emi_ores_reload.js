// EMI Ores reads placed features from a server packet on datapack sync, then registers
// "Ore Generation" recipes when EMI reloads. If EMI reloads first, the category stays empty.
// See: https://github.com/Abbie5/emi-ores (README + PR #13 deferred registration)
// EMI Ores requires both client AND server; only supports vanilla minecraft:ore features
// (IE native ores need datapack replacement — https://github.com/Abbie5/emi-ores/issues/32)

let emiOresReloadTicks = []
let lastFeatureCount = -1

const scheduleEmiOresReload = (delayTicks) => {
    if (!Platform.isLoaded("emi") || !Platform.isLoaded("emi_ores")) return
    emiOresReloadTicks.push(delayTicks)
}

const reloadEmiForOres = () => {
    try {
        Java.loadClass("dev.emi.emi.runtime.EmiReloadManager").reload()
    } catch (e) {
        console.warn("[KubeJS] EMI reload for ore gen failed: " + e)
    }
}

const getEmiOresFeatureCount = () => {
    try {
        return Java.loadClass("cc.abbie.emi_ores.client.FeaturesReciever").getFeatures().size()
    } catch (e) {
        return 0
    }
}

ClientEvents.loggedIn(event => {
    emiOresReloadTicks = []
    lastFeatureCount = -1
    scheduleEmiOresReload(40)
    scheduleEmiOresReload(100)
})

ClientEvents.loggedOut(event => {
    emiOresReloadTicks = []
    lastFeatureCount = -1
})

ClientEvents.tick(event => {
    if (Platform.isLoaded("emi") && Platform.isLoaded("emi_ores")) {
        let featureCount = getEmiOresFeatureCount()
        if (featureCount > 0 && featureCount !== lastFeatureCount) {
            lastFeatureCount = featureCount
            reloadEmiForOres()
        }
    }

    if (emiOresReloadTicks.length === 0) return
    for (let i = emiOresReloadTicks.length - 1; i >= 0; i--) {
        emiOresReloadTicks[i]--
        if (emiOresReloadTicks[i] <= 0) {
            emiOresReloadTicks.splice(i, 1)
            reloadEmiForOres()
        }
    }
})
