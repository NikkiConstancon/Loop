/**
 * @file
 * This file contains modules and helper utility functions to enable mailing emails to new registered users.
 *
 * @todo add functionality to send notifications triggered by events
 */

const querystring = require('querystring')

var uuid1 = require('uuid/v1')
var nodemailer = require('nodemailer')
var logger = require('../revaLog')




var pendingSends = 0
var transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'loop.reva.service@gmail.com',
        pass: 'mail-test-pass'
    }
})


/**
 * @class wrapper for email related funcitons
 */
var Mailer = module.exports = {
    mailEmialConfirmationUrlPath: '/email-confirmation',
    /**
     *@brief send an account calidation email
     *
     *@param to the email to send to
     *@param keyA the necrypted usernam to validate
     *@param keyB the accept uuid
     *@parma keyC the decile uuid
     *@param fullName the name of the user
     **/
    mailEmialConfirmationUrl: function (to, keyA, keyB, keyC, fullName) {
        const server = require('../webServer')//moved here because of circular require
        pendingSends++
        
        var html
        try {
            html = require('fs').readFileSync('../resources/email.html', 'utf8').toString()
        } catch (e) {
            html = require('fs').readFileSync('./resources/email.html', 'utf8').toString()
        }
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

