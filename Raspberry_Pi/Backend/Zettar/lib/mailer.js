const querystring = require('querystring')

var uuid1 = require('uuid/v1')
var nodemailer = require('nodemailer')
var logger = require('../revaLog')

var server = require('../webServer')



var pendingSends = 0
var transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'COS332.Marthinus@gmail.com',
        pass: 'mail-test-pass'
    }
})



var Mailer = module.exports = {
    mailEmialConfirmationUrlPath: '/email-confirmation',
    mailEmialConfirmationUrl: function (to, keyA, keyB) {
        pendingSends++
        return new Promise(function (resolve, reject) {
            var mailOptions = {
                from: 'noreply.mailEmialConfirmationUrl@gmail.com',
                to: to,
                subject: 'Test activation',
                text: server.whoAmI(Mailer.mailEmialConfirmationUrlPath + '?a=' + querystring.escape(keyA) + '&b=' + querystring.escape(keyB))
            }

            transporter.sendMail(mailOptions, function (error, info) {
                pendingSends--
                if (error) {
                    logger.error(error);
                    reject()
                } else {
                    logger.debug('Email sent: ' + info.response);
                    resolve()
                }
            })
        })
    },
    get pendingSends() {
        return pendingSends
    }
}

