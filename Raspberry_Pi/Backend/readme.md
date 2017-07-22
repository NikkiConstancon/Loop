At the moment, run both zettar and zettalet in seperate terminals.

Zettar will be deployed on the cloud and Zettlet will be deployed on each Raspberry Pi


Where to find what:
go Zettar/node_modules/zetta/lib

in http_server.js you will find the code where the connection to the raspberry pi's is established
	look at ZettaHttpServer.prototype.setupPeerSocket

in pubsub_service.js you will find where the streamed value is published.
	look at PubSub.prototype.publish


Instructions for it to work...

run the zettar and as many zettalets as you want, then go to
	http://browser.zettajs.io/#/overview?url=http:%2F%2F127.0.0.1:3009
This webpage essentially subscribes to the stream ( This will be replaced with the andriod app at some point)
then on the Zettar server the details are published.

TODO:
Extract the relevant informtion from the stream and then asynchronously store it in the database.
	
