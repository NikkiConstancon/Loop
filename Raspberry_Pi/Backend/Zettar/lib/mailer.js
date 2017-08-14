
var uuid1 = require('uuid/v1')
var nodemailer = require('nodemailer')
var logger = require('../revaLog')

var server = require('./revaServer')

var transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'COS332.Marthinus@gmail.com',
        pass: 'mail-test-pass'
    }
})



var Mailer = module.exports = {
    mailEmialConfirmationUrl: function (user, hostUrl) {
        return new Promise(function (resolve, reject) {
            if (!hostUrl) {
                reject('hostUrl is undifined')
            }
            var key1 = uuid1()
            var key2 = uuid1()
            var mailOptions = {
                from: 'noreply.mailEmialConfirmationUrl@gmail.com',
                to: user.PatientEmail,
                subject: 'Sending Email using Node.js',
                text: hostUrl + '/emial-confirmation?a=' + key1 + '&b=' + key2
            }

            transporter.sendMail(mailOptions, function (error, info) {
                if (error) {
                    logger.error(error);
                    reject()
                } else {
                    logger.debug('Email sent: ' + info.response);
                    resolve()
                }
            })
        })
    }
}

