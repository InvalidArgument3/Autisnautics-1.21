var loaded = []
var ServerEvents = { loaded: function (fn) { loaded.push(fn) } }
var JsonIO = { read: function () { return null }, write: function () {} }
var Item = { of: function (id) { return { getDisplayName: function () { return { getString: function () { return id } } } } } }
var Ingredient = {}
var console = { info: function () {}, warn: function () {}, error: function (m) { throw new Error(String(m)) } }
var Java = {
    loadClass: function (name) {
        if (name === 'java.nio.file.Files') {
            return {
                isRegularFile: function () { return false },
                deleteIfExists: function () { return false }
            }
        }
        if (name === 'java.nio.file.Paths') return { get: function () { return {} } }
        return function () {}
    }
}
var Utils = {
    newRandom: function () { return { nextInt: function () { return 0 } } },
    newMap: function () { return {} }
}
