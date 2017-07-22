module.exports = function(server){

	console.log('Outside');
		
	var query = server.where({type : 'state_machine'});
		// console.log(query);
	server.observe([query],function(object){
		object.on('turn-on', function(){

			console.log("DONE");
		});
	});

	

}