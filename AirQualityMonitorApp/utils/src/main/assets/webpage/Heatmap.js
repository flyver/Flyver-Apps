(function () {
  var mapOptions = {
    zoom: 13,
    center: new google.maps.LatLng(42.645829, 23.376452),
    mapTypeId: google.maps.MapTypeId.SATELLITE
  };
  var initSocket = function(ws) {
     ws = new WebSocket("ws://"+document.location.host, "protocolOne");
     ws.onopen = function(event) {
       console.log("opened!");
     }
     ws.onmessage = function(event) {
       var strData = event.data;
       var sanitized = strData.replace(/[^ -~]+/g, "");
       var data = JSON.parse(sanitized);
//       var quality = ~~parseFloat(event.airQuality);
       var element = document.getElementById("scale-image");
       var latitudeTextbox = document.getElementById("latitude");
       var longitudeTextbox = document.getElementById("longitude");
       latitudeTextbox.innerText = "Latitude: " + data.droneLocation.location.mLatitude;
       longitudeTextbox.innerText = "Longitude: " + data.droneLocation.location.mLongitude;
       switch(data.airQuality / 16) {
         case 1: {
           element.className = "scale1";
         }
         break;
         case 2: {
           element.className = "scale2";
         }
         break;
         case 3: {
           element.className = "scale3";
         }
         break;
         case 4: {
           element.className = "scale4";
         }
         break;
         case 5: {
           element.className = "scale5";
         }
         break;
         case 6: {
           element.className = "scale6";
         }
         break;
       }
     }
     ws.onclose = function() {
       console.log("Closed");
       initSocket(ws);
     }
  }

  var element = document.getElementById("scale-image");
  element.className = "scale1";
  var xhttpRequest = new XMLHttpRequest();
  var websocket;
  initSocket(websocket);
  xhttpRequest.onreadystatechange = function() {
    if(xhttpRequest.readyState == 4) {
      var response = xhttpRequest.responseText;
      var data = response.split('|');
      var processed = [];
      for(var i = 0; i < data.length - 1; i++) {
        var str = data[i].split(',');
        var point = {location: new google.maps.LatLng(parseFloat(str[0]), parseFloat(str[1])), weight: parseFloat(str[3])};
        processed.push(point);
      }
      var pointArray = new google.maps.MVCArray(processed);
      var mapCanvas = document.getElementById('map-canvas');
      var map = new google.maps.Map(mapCanvas, mapOptions);
      var heatmap = new google.maps.visualization.HeatmapLayer({
        data: pointArray
      });
      heatmap.setMap(map);
    }
  }
  xhttpRequest.open('POST', '10.129.1.208', true);
  xhttpRequest.send('file');
})()
