const app = require('express')();
const http = require('http').Server(app);
const io = require('socket.io')(http);

const PORT = process.env.PORT || 3000;

app.get('/', function(req, res){
  res.sendFile(__dirname+"/www/");
});

var couriers = [
	{
		latitude:41.087491,
		longitude:29.026163
	}
];

var farkX = -1;
var farkY = -1;

io.on('connection', function(socket){
  
  socket.emit("courierPositions", {CourierList: couriers});
  
  socket.on("yenimesaj", function(data) {
	  io.emit("yeni", data);
  });
  
  socket.on("newpos", function(data) {
	  io.emit("yeni", data);
	  
	  if (farkX < 0 || farkY < 0) {
		 var x = couriers[0].latitude - data.latitude;
		 var y = couriers[0].longitude - data.longitude;
		  
		  x = x/30;
		  y = y/30;
		  
		  farkX = x;
		  farkY = y;
	  }
	  
	  
	  
	  couriers[0].latitude -= farkX;
	  couriers[0].longitude -= farkY;
	  
	  if (data.latitude > couriers[0].latitude && data.longitude > couriers[0].longitude) {
		  socket.emit("reached");
		  couriers[0].latitude = 41.087491;
		  couriers[0].longitude = 29.026163;
	  }
	  
	  socket.emit("courierPositions", {CourierList: couriers});
  });
});


http.listen(PORT, function(){
  console.log('listening on *:'+PORT);
});





