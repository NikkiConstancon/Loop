var PatientManager = require('../patientManager');

var query = module.exports.query ={
    Username: 'Dummy Patient',
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
        PatientManager.getPatient({ Username: 'Dummy Patient' }).then(function (pat) {
            if (process.argv.indexOf('--verbose') != -1) {
                console.log(query)
                pat.printFields()
            }
        })
    }).catch(function (e) {

    })