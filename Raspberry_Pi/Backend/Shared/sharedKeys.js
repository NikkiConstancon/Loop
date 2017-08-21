const crypto = require("crypto-js");

var key = "xP{}Lk.x#3V2S?F2p'q{kqd[Qu{7/S-d*bzt"
module.exports.encrypt = function (val) {
    return encodeURIComponent(crypto.AES.encrypt(val, key).toString())
}
module.exports.decrypt = function (value) {
    value = decodeURIComponent(value)
    return crypto.AES.decrypt(value, key).toString(crypto.enc.Utf8)
}