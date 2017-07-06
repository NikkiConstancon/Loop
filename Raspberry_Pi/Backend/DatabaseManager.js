var cassandra = require('cassandra-driver');
// var async = require('async');
var uuid = require('uuid');


//Specify the database to be connected to
var databaseName = 'reva';

//connect to the excpected database
client = new cassandra.Client({contactPoints: ['127.0.0.1'], keyspace: databaseName});



function databaseManager(tableName){

	//set the name of the table -- users or patientData
	this.tableName = tableName;

	//when created this will be null
	this.columnNames = null;

	//prepared statements for both databases
	


	this.updateQuery = 'UPDATE ' + this.tableName + ' SET ? = ? WHERE ? = ?;';

	

	this.showSpecificQuery = 'SELECT ' + this.tableName + ' FROM ? WHERE ? = ?';

	//refernece to this in the databaseManager scope
	var self = this;



	/*
		Insert a single instace into the db
		Send an array of values as a paramenter
		users: [email,id,name,surname] in this order
		#TODO make the order irrelevant
		patient data: [..] --> to be confirmed
		

		Error handleing:
			error if user already exists
			error if query is not executed correctly 

	*/
	this.insertInstance = function(array){

		if(this.columnNames == null){
			//In this case the dbManager doesnt know what columns are in the table
			//This should only happen once when the dbManager is first used

			//Query to find the column names for the database selected
			var tableQuery = 'SELECT column_name FROM system_schema.columns WHERE keyspace_name = ' + "'" + databaseName + "'" + " AND table_name = '" + this.tableName + "';" 

	  		var list = '(';

			//Execute tableQuery
			client.execute(tableQuery, function(err, result) {

			  	//create a list of columns ( ?,?,?,...)
			  	//for each column
				for(var row in result.rows){
					//add column name
				    list += result.rows[row].column_name
				    //add comma if not the last to be added
				    if(row != result.rows.length-1){
				    	list +=',';
				    }
				}
				list += ')';
				//Store the column names
				self.columnNames = list;

				//these query values will now be consistent for the database;
				self.insertQuery = 'INSERT INTO ' + self.tableName + ' ' + self.columnNames + ' VALUES (?, ?, ?, ?) IF NOT EXISTS;';
			  	
				//call the insert function again
			  	self.insertInstance(array);
			});

		}else{

			//Query to insert -->columnNames needs to be set in the if section first

			client.execute(this.insertQuery, array, function(err, result) {
	  			console.log(err);
				
				//Error handeling
				if(err != null){
		  			console.log('Error: Query did not execute correctly');
				}
				else if(result.rows[0]['[applied]'] == false){
		  			console.log('Error: User ' + array  + ' already exists.');
				}else{

					//Can place validation here
					
		  			console.log('Success: ' + array + ' successfully added');
				}

			});
		}
	}
		

	this.deleteQuery = 'DELETE FROM ' + this.tableName + " WHERE ";


	function createWHERE(jsonString){

		var list = '';
		var counter = 0;
		var valueList = [];


		//for each object in the jsonstring
		for(var item in jsonString){
			//add column name = 'value'
			if (jsonString.hasOwnProperty(item)) {
				list += item + " = ?";
			}

			//dont know if the values will be numerical or string... so let the prepared statement deal wih that.
			valueList.push(jsonString[item]); 

			//if there is more than one condition then use AND-- but not on the last one
			if(counter !=  Object.keys(jsonString).length-1){
				list += ' AND ';
			}

			//just to see if it is the last
			counter ++;
		}	

		return {valueList,list};
	}



/*
	delete a single instace from the db
	Send an json object as a paramenter
	users: {id : '2314'} 
	patient data: {? : '?',....} --> to be confirmed
		
	NOTE: can only delete using the primary keys. only specify the primary key values -- how ever many there are
			using a non primary key will result in an error.

	Error handleing:
		error if instance already deleted then it warns you
		error if query is not executed correctly --usully as a result of incorrect Primary key

*/
	this.deleteInstance = function(jsonString){


		//create the where section of the query:
		var tuple = createWHERE(jsonString);

		//add conditions to the delete query;
		//result : DELETE FROM users WHERE id = ? AND name = ? ;
		self.deleteQuery += tuple['list'] + ' IF EXISTS;';

		console.log(self.deleteQuery);
		client.execute(self.deleteQuery, tuple['valueList'], function(err, result) {
				console.log(err);
				//Error handeling
				if(err != null){
		  			console.log('Error: Ensure that primary keys are used and that all are specified');
				}else if(result.rows[0]['[applied]'] == false){
		  			console.log('Error: ' + tuple['valueList']  + ' does not exist.');
				}else{
					//Can place validation here
					
		  			console.log('Success: ' + tuple['valueList'] + ' successfully deleted');
				}

			});
	}

	this.updateInstanceField = function(){
		
	}

	this.updateInstanceFields = function(){
		
	}


	this.fetchQuery = 'SELECT ' + this.tableName + ' FROM ';

	this.fetchInstance = function(array , jsonString){


		return 'hello';
	}

	this.betweenInstances = function(){
		
	}

}

//<<<<<<<<<<<<<<<<<<<<<<<<<<<TESTS<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



//---------------------------USERS------------------------------------

var userDatabaseManager = new databaseManager('users'); 

//---INSERT

// userDatabaseManager.insertInstance(['ni@g.com' , 'j?', 'NC', 'constancon']);
// userDatabaseManager.deleteInstance({'id' : 'j?'});
console.log(userDatabaseManager.fetchInstance({'id' : 'j?'}));
