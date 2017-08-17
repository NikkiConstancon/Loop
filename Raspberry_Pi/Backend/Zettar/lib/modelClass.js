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
    }
    get fields() { return this[fieldsKey] }
    set fields(obj) { Object.assign(this[fieldsKey], obj) }

    get key() { return this[keyKey] }
    set key(arr) { myArryConcat(this[keyKey], arr) }

    get methods() { return this[methodsKey] }
    set methods(obj) { Object.assign(this[methodsKey], obj) }
}

module.exports = Model