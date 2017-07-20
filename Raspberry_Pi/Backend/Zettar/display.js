module.exports = function(server){

		console.log('Outside');
		// server = server.httpServer.peers;
	var query = server.where({type: 'state_machine'});
	// 	console.log(query);


	  	setTimeout(function() {
			console.log(server.httpServer.peers);
		},3000);

	server.observe([query],function(object){
		console.log('Inside');
		console.log(object);
	});

}