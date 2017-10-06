var PatientManager = require('../patientManager');
var SubscriberManager = require('../subscriberManager');

var query = module.exports.query ={
    Username: 'nikki',
    Password: 'Password',
    Email: "COS332.Marthinus@gmail.com",
    Address: '42 Dale Avenue Hempton 1765',
    SubscriberList: ["nikki"],
    Age: 42,
    Weight: 23,
    Height: 32,
    Reason: 'Disability'
}

PatientManager
    .addPatient(query).catch(function (err) {
        //console.info(err)
    }).then(function () {
        PatientManager.getPatient({ Username: 'nikki' }).then(function (pat) {
            if (process.argv.indexOf('--verbose') != -1) {
                console.log(query)
                pat.printFields()
            }
        })
    }).catch(function (e) {

    })



query = module.exports.query = {
    Username: 'greg',
    Password: 'Password',
    Email: "COS332.Marthinus@gmail.com",
    Address: '42 Dale Avenue Hempton 1765',
    SubscriberList: ["greg"],
    Age: 42,
    Weight: 23,
    Height: 32,
    Reason: 'Disability'
}

PatientManager
    .addPatient(query).catch(function (err) {
        //console.info(err)
    }).then(function () {
        PatientManager.getPatient({ Username: 'greg' }).then(function (pat) {
            if (process.argv.indexOf('--verbose') != -1) {
                console.log(query)
                pat.printFields()
            }
        })
    }).catch(function (e) {

    })
    
    query = module.exports.query = {
    Username: 'rinus',
    Password: 'Password',
    SubscriberList: ["rinus"],
    Email: "COS332.Marthinus@gmail.com",
    Address: '42 Dale Avenue Hempton 1765',
    Age: 42,
    Weight: 23,
    Height: 32,
    Reason: 'Disability'
}

PatientManager
    .addPatient(query).catch(function (err) {
        //console.info(err)
    }).then(function () {
        PatientManager.getPatient({ Username: 'rinus' }).then(function (pat) {
            if (process.argv.indexOf('--verbose') != -1) {
                console.log(query)
                pat.printFields()
            }
        })
    }).catch(function (e) {

        })

    query = module.exports.query = {
    Username: 'juan',
    Password: 'Password',
    SubscriberList: ["juan"],
    Email: "COS332.Marthinus@gmail.com",
    Address: '42 Dale Avenue Hempton 1765',
    Age: 42,
    Weight: 23,
    Height: 32,
    Reason: 'Disability'
}

PatientManager
    .addPatient(query).catch(function (err) {
        //console.info(err)
    }).then(function () {
        PatientManager.getPatient({ Username: 'juan' }).then(function (pat) {
            if (process.argv.indexOf('--verbose') != -1) {
                console.log(query)
                pat.printFields()
            }
        })
    }).catch(function (e) {

        })
    
    
SubscriberManager
    .addSubscriber({
        Email: 'what@sub.com',
        Password: 'Password',
        Relation: "doctor",
        PatientList: ['nikki', 'greg', 'rinus']
    }).then(function (sub) {
        PatientManager.addToSubscriberList({ Username: 'nikki' }, 'what@sub.com')
        PatientManager.addToSubscriberList({ Username: 'greg' }, 'what@sub.com')
        PatientManager.addToSubscriberList({ Username: 'rinus' }, 'what@sub.com')
        /*
        SubscriberManager.addToPatientList({ Email: sub.Email }, 'nikki')
        SubscriberManager.addToPatientList({ Email: sub.Email }, 'greg')
        SubscriberManager.addToPatientList({ Email: sub.Email }, 'rinus')*/
    }).catch(function () { })

SubscriberManager
    .addSubscriber({
        Email: 'q@q.q',
        Password: 'Password',
        Relation: "doctor",
        PatientList: ['nikki', 'greg', 'rinus']
    }).then(function (sub) {
        PatientManager.addToSubscriberList({ Username: 'nikki' }, 'q@q.q')
        PatientManager.addToSubscriberList({ Username: 'greg' }, 'q@q.q')
        PatientManager.addToSubscriberList({ Username: 'rinus' }, 'q@q.q')
    }).catch(function () { })

SubscriberManager
    .addSubscriber({
        Email: 'what2@sub.com',
        Password: 'Password',
        Relation: "doctor",
        PatientList: []
    }).then(function (sub) {
        PatientManager.addToSubscriberList({ Username: 'nikki' }, 'what2@sub.com')
        PatientManager.addToSubscriberList({ Username: 'greg' }, 'what2@sub.com')
        PatientManager.addToSubscriberList({ Username: 'rinus' }, 'what2@sub.com')

        SubscriberManager.addToPatientList({ Email: sub.Email }, 'nikki')
        SubscriberManager.addToPatientList({ Email: sub.Email }, 'greg')
        SubscriberManager.addToPatientList({ Email: sub.Email }, 'rinus')
    }).catch(function () { })

SubscriberManager
    .addSubscriber({
        Email: 'a@a.a',
        Password: 'Password',
        Relation: "doctor",
        PatientList: []
    }).then(function (sub) {
        PatientManager.addToSubscriberList({ Username: 'nikki' }, 'a@a.a')
        PatientManager.addToSubscriberList({ Username: 'greg' }, 'a@a.a')
        PatientManager.addToSubscriberList({ Username: 'rinus' }, 'a@a.a')

        SubscriberManager.addToPatientList({ Email: sub.Email }, 'nikki')
        SubscriberManager.addToPatientList({ Email: sub.Email }, 'greg')
        SubscriberManager.addToPatientList({ Email: sub.Email }, 'rinus')
    }).catch(function () { })



