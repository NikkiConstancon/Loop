module.exports = {
    fields:{
        PatientUsername    : "text",
        DeviceID : "text",
        TimeStamp     : "timestamp",
	Value : "float"
    },
    key:["TimeStamp","PatientUsername","DeviceID"]
}
