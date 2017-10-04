/**
 * @file
 * Cassandra database model that describes the PatientData schema
 **/

module.exports = {
    fields:{
        PatientUsername    : "text",
        DeviceID : "text",
        TimeStamp     : "double",
	Value : "float"
    },
    key:["TimeStamp","PatientUsername","DeviceID"]
}
