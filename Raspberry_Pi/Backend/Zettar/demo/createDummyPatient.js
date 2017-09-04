var PatientManager = require('../patientManager');

var query = module.exports.query ={
    Username: 'nikki',
    Password: 'password',
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

