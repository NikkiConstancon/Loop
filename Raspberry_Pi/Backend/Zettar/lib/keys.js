




var keys = module.exports  = require("crypto-js");

module.exports.userEmail = "xP{}Lk.x#3V2S?F2p'q{kqd[Qu{7/S-d*bzt"
module.exports.userEmailEncrypt = function (val) {
    return keys.AES.encrypt(val, keys.userEmail).toString()
}
module.exports.userEmailDecrypt = function (k) {
    try {
        return keys.AES.decrypt(k.toString(), keys.userEmail).toString(keys.enc.Utf8)
    } catch (e){
        return '-'
    }
}

