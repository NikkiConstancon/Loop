const querystring = require('querystring')

var uuid1 = require('uuid/v1')
var nodemailer = require('nodemailer')
var logger = require('../revaLog')

var server = require('../webServer')



var pendingSends = 0
var transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'loop.reva.service@gmail.com',
        pass: 'mail-test-pass'
    }
})



var Mailer = module.exports = {
    mailEmialConfirmationUrlPath: '/email-confirmation',
    mailEmialConfirmationUrl: function (to, keyA, keyB, keyC, fullName) {
        pendingSends++
        
        var html = require('fs').readFileSync('./resources/email.html', 'utf8').toString()
        html = html.replace('{{fullName}}', fullName)
        html = html.replace('{{endUserAgreement}}', server.whoAmI() + '/end-user-agreement')
        html = html.replace('{{date}}', (new Date()).toString())
        html = html.replace('{{acceptURI}}', server.whoAmI(Mailer.mailEmialConfirmationUrlPath + '?a=' + querystring.escape(keyA) + '&b=' + querystring.escape(keyB)))
        html = html.replace('{{declineURI}}', server.whoAmI(Mailer.mailEmialConfirmationUrlPath + '?a=' + querystring.escape(keyA) + '&b=' + querystring.escape(keyC)))

        return new Promise(function (resolve, reject) {
            var mailOptions = {
                from: 'noreply.mailEmialConfirmationUrl@gmail.com',
                to: to,
                subject: 'ReVA email confirmation and activation',
                text: server.whoAmI(Mailer.mailEmialConfirmationUrlPath + '?a=' + querystring.escape(keyA) + '&b=' + querystring.escape(keyB)),
                html: html
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

