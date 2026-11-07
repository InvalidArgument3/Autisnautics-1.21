var classes = {}
var Java = {
    loadClass: function (name) {
        if (!classes[name]) {
            var Ctor = function () {
                this.x = arguments[0]
                this.y = arguments[1]
                this.z = arguments[2]
            }
            classes[name] = Ctor
        }
        return classes[name]
    }
}
var handlers = []
var NativeEvents = { onEvent: function (type, fn) { handlers.push(fn) } }
var ServerEvents = { tick: function (fn) { handlers.push(fn) } }
var console = { warn: function () {}, info: function () {} }
