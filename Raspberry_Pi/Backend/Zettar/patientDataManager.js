'use strict';

var dbMan = require('./databaseManager');
var util = require('util');
var logger = require('./revaLog');


function addMinutes(date, minutes) {
    
}


var patientDataManager = module.exports = {
    addInstance: function (user){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                var newUser = new dbMan.models.instance.patientData(user)
                newUser.save(function (err) {
                    if (err) {
                        logger.error(err)
                        reject(err)
                    } else {
                        //logger.debug('Instance Added Successfully!')
                        resolve(newUser)
                    }
                })
            }).catch((err)=>{
                reject(err)
            })
        })
    },

    getInstance: function (name) {
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                dbMan.models.instance.patientData.findOne({ PatientUsername: name }, function (err, found) {
                    if (err) {
                        logger.error(err)
                        reject(err)
                    } else {
                        //Note that returned variable john here is an instance of your model,
                        //so you can also do john.delete(), john.save() type operations on the instance.
                        logger.debug('Found Instance: ' + found.PatientUsername)
                        resolve(found)
                    }
                })
            }).catch((err) => {
                reject(err)
            })
        })
    },


    /*
    *Required parameter:
    *{
    *   Username: 
    *   DeviceId:
    *   StartTime:
    *   EndTime:
    *   Interval:
    *}
    * Username is the zettalet server name that will be connected to.
    * DeviceId is the id f the device whose data will be pulled
    * StartTime is a timestamp of the time from which the info will be collected
    * EndTime is a timestamp of the time till when info will be collected.
    * Interval is an integer of the number of minutes that will be compressed into one data point
    *
    * Expection: 
    * StartTime is before EndTime 
    * Date format : 2017-09-12 18:14:57.503000+0000
    *
    *Excpected response:
    * [{x: , y: }, {x: ,y: }, ...]
    *
    *
    */
    /*PatientUsername
DeviceID
TimeStamp*/
    getGraphPoints: function(_info){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
console.log("Getting graphPoints");
console.log(_info);
                var minimum = Number.POSITIVE_INFINITY;
                var maximum = Number.NEGATIVE_INFINITY;
                var avg = 0.0;
                var count = 0;
                var result = []
                var start = _info.StartTime;
                var end = _info.EndTime;
                var query;
                console.log(_info.DeviceID);
                console.log(_info.DeviceID);
                if(!_info.DeviceID){
                    query = {
                        PatientUsername: _info.Username,
                        TimeStamp : { '$gt':start, '$lte':end}
                    }
                } else{
                    query = {
                        PatientUsername: _info.Username,
                        DeviceID: _info.DeviceID,
                        TimeStamp : { '$gt':start, '$lte':end}
                    }
                }
                console.log(query);
                dbMan.models.instance.patientData.stream(query, {raw: true, allow_filtering: true}, function(data){
                    var row;
                    while (row = data.readRow()) {
                        count ++;
                        avg += row.Value;
                        if(row.Value > maximum)
                            maximum = row.Value;
                        
                        if(row.Value < minimum)
                            minimum = row.Value;

                        result.push({ x: row.TimeStamp , y : row.Value, device: row.DeviceID})
                    }

                    result.sort(function(first, second) {
                        return first.x - second.x;
                    });
                    result.push({Min: minimum, Max: maximum, Avg: (avg/count) })
                    if(minimum == Number.POSITIVE_INFINITY){
                     result = false;   
                    }
                    if(maximum == Number.NEGATIVE_INFINITY){
                     result = false;   
                    }
                    //console.log(result);
                    resolve(result);

                }, function(err){
                    reject(err);
                });
            }).catch((err) => {
                reject(err)
            })
        })


    },
 /*   getCompressedGraphPoints: function(_info){
        var start = _info.StartTime;
        var end = _info.EndTime;
        var segment = (_info.EndTime - _info.StartTime ) / 60 000;
        var midEnd = start + segment;
        var newInfo = _info;
        newInfo.EndTime = midEnd;
        this.getGraphPoints(newInfo).then(function(){
            
            
        });
        
    }*/
}
