/**
 * @file
 * This file is residual and may be removed from the repository
 * though it comes in handy at times to quickly test a new idea
 **/


// var dbManager = require("./databaseManager");
// var dbManager1 = require("./dataManager");
var patientManager = require("./patientManager");
var subscriberManager = require("./subscriberManager")
var dataManager = require("./patientDataManager")

var CryptoJS = require("crypto-js");

function compress(StartTime, EndTime, endResult){
    console.log(endResult);
    var result = {};
    for(var device in endResult){
        result[device] = [];
        var segment = (EndTime - StartTime ) / 600;
        var midEnd = StartTime;
        var midStart = StartTime;
        while(midStart < EndTime){ //for each segment that is there
            midEnd += segment;
            var avgX = 0;
            var avgY = 0;
            var count = 0;
            var minMaxAvg = {};
            for(var key in endResult[device]){
                if(endResult[device][key].x > midStart && endResult[device][key].x < midEnd){
                    count ++;
                    avgX += endResult[device][key].x;
                    avgY += endResult[device][key].y;
                }
                minMaxAvg = endResult[device][key];
                    
            }
            if(count == 0){
                avgX = (midEnd + midStart ) / 2
            } else{
                avgX /= count;
                avgY /= count;
                result[device].push({x: avgX, y:avgY});  
                
            } 
            midStart = midEnd;
        }
        result[device].push(minMaxAvg); 
                    
    }
    return result;
}
function get(msg) {
    var tmp = msg
    var params
    if(!tmp.DeviceID){
            params = {
            Username: tmp.Username  ,
            StartTime:  tmp.StartTime,
            EndTime:tmp.EndTime,
        }
    } else{
        params = {
            Username: tmp.Username  ,
            DeviceID: tmp.DeviceID,
            StartTime:  tmp.StartTime,
            EndTime:tmp.EndTime,
        }
        
    }
    console.log(params);
    patientManager.getDeviceMap({ Username: tmp.Username }).then(function (pat) { 
        dataManager.getGraphPoints(params).then(function(result){
             if(result == false){
                channel(false);
                
            }else{
                //console.log(result);
                var endResult = {};
                var minMax = {};
                for(var device in result){
                    console.log(result[device]);
                    var id = result[device].device;
                    if(tmp.DeviceID)
                        id = tmp.DeviceID
                    if(! endResult[id])
                        endResult[id] = [];
                    if(!minMax[id])
                        minMax[id] = [];
                    if(result[device])
                        minMax[id].push({Min: Number.POSITIVE_INFINITY, Max: Number.NEGATIVE_INFINITY, Avg: 0, count: 0});
                         
                        if(minMax[id][0].Min > result[device].y){
                            minMax[id][0].Min = result[device].y
                        }
                        if(minMax[id][0].Max < result[device].y){
                            minMax[id][0].Max = result[device].y
                        }
                        minMax[id][0].Avg += result[device].y
                        minMax[id][0].count ++;
                        endResult[id].push({x: result[device].x, y: result[device].y});  
                        console.log(endResult[id]);
                }
               var average;
                for(var device in minMax){
                    average =  minMax[device][0].Avg/ minMax[device][0].count
                    endResult[device].push({Min: minMax[device][0].Min, Max: minMax[device][0].Max, Avg: average});
                    
                }
               endResult = compress(tmp.StartTime, tmp.EndTime, endResult);
               console.log("\n\n");
                console.log(endResult);
                //channel(endResult);
            }
        }).catch(function (e) {
            console.log(e);
            //logger.error('GraphRetievalError', e)
           // channel(false)
        }) 
    }).catch(function (e) {
        console.log(e);
        //logger.error('DeviceMapRetievalError', e)
        //channel(false)
    }) 
}
setTimeout(function() {
    var d = new Date();
    var n = d.getTime();
    console.log(n);
    start = n - 10000000000;
    end = n ;
    console.log(start);
    get({ Username: 'greg',StartTime: start ,EndTime:end,})
    
}, 0);
// DeviceID:"Body_temperature",
 /*   subscriberManager.addSubscriber({
        Email: "nikkiconstancon@gmail.com",
        Password: CryptoJS.AES.encrypt('Password', 'secret key 123').toString(),
        Relation: "doctor",
        PatientList: []
    })
    subscriberManager.getsubscriber({Email: "nikkiconstancon@gmail.com"})

    PatientManager
                    .addPatient({ 
                        Username: 'nikki',
                        Password: CryptoJS.AES.encrypt('Password', 'secret key 123').toString(),
                        AccessPassword: CryptoJS.AES.encrypt('AccessPassword', 'secret key 123').toString(),
                        SubscriberList : [],
                        Email : "testPatient@test.co.za", 
                        Address : '42 Dale Avenue Hempton 1765',
                        Age : 42, 
                        Weight : 23,
                        Height : 32, 
                        Reason : 'Disability'});*/


     // dbManager1.addUser({name: 'Nikki2', surname: 'Con', age: 21});
     // dbManager2.addInstance({patientName: 'Nikki2', device: 'Con', time: 21});
    // patientDataManager.addInstance({PatientUsername : "name", DeviceID : "data[0].topic", TimeStamp : 0, Value : parseFloat(10.99)  });
   
/*    PatientManager
                    .addPatient({ 
                        Username: 'nikki',
                        Password: CryptoJS.AES.encrypt('Password', 'secret key 123').toString(),
                        AccessPassword: CryptoJS.AES.encrypt('AccessPassword', 'secret key 123').toString(),
                        SubscriberList : [],
                        Email : "testPatient@test.co.za", 
                        Address : '42 Dale Avenue Hempton 1765',
                        Age : 42, 
                        Weight : 23,
                        Height : 32, 
                        Reason : 'Disability'});
          PatientManager
                    .checkUsername("noone");   */        
 /*   subscriberManager.addSubscriber({
        Email: "nikkiconstancon@gmail.com",
        Password: CryptoJS.AES.encrypt('Password', 'secret key 123').toString(),
        Relation: "doctor",
        PatientList: {Username: "nikki", AccessPassword: "AccessPassword"}
    })*/

    // PatientManager.getPatient();
