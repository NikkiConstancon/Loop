function myArryConcat(arr1, arr2) {
    for (var i in arr2) {
        if (arr1.indexOf(arr2[i]) === -1) {
            arr1.push(arr2[i])
        }
    }
}
//NOTE: do not use Symbol for hiddn keys, as this will missalign express schema
var fieldsKey = 'fieldsKey'//Symbol();
var keyKey = 'keyKey'//Symbol();
var methodsKey = 'methodsKey'
class Model {
    constructor() {
        this[fieldsKey] = {}
        this[keyKey] = []
        this[methodsKey] = {}

        Object.defineProperty(this, 'fields', {
            enumerable: true,
            get :function() { return this[fieldsKey] },
            set: function(obj) { Object.assign(this[fieldsKey], obj) }
        })
        Object.defineProperty(this, 'key', {
            enumerable: true,
            get: function() { return this[keyKey] },
            set: function(arr) { myArryConcat(this[keyKey], arr) },
        })
        Object.defineProperty(this, 'methods', {
            enumerable: true,
            get: function() { return this[methodsKey] },
            set: function(obj) { Object.assign(this[methodsKey], obj) }
        })
    }
}

module.exports = Model