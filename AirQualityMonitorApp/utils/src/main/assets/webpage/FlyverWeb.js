function FlyverWeb(){function l(d,g,e){if(!(isNaN(d)&&isNaN(g)&&isNaN(e))){var a=vis.moment();f.add([{x:a,y:d,group:0},{x:a,y:g,group:1},{x:a,y:e,group:2}]);var b=k.getWindow(),c=b.end-b.start;d=f.getIds({filter:function(a){return a.x<b.start-c}});f.remove(d)}}var m=window.location.hostname,f=new vis.DataSet,h=new vis.DataSet;h.add({id:0,content:"pitch"});h.add({id:1,content:"roll"});h.add({id:2,content:"yaw"});var n=document.getElementById("chart"),p={start:vis.moment().add(-30,"seconds"),end:vis.moment(),
legend:{right:{position:"top-left"}},dataAxis:{customRange:{left:{min:-4,max:4}}},drawPoints:!1,shaded:{orientation:"bottom"}},k=new vis.Graph2d(n,f,h,p);setInterval(function(){var d=new XMLHttpRequest;d.onreadystatechange=function(){if(4==d.readyState){for(var g=d.responseText,e=g.split("]"),a=[],b=0;b<e.length;b++){var c=e[b],c=c.replace(/\[|\]|\"|,/g,""),c=c.split(" ");a.push(c)}for(var f=c=e=0,b=0;b<a.length-2;b++)e+=parseFloat(a[b][0]),c+=parseFloat(a[b][1]),f+=parseFloat(a[b][2]);l(e/(a.length-
2),c/(a.length-2),f/(a.length-2));a=vis.moment();b=k.getWindow();k.setWindow(a-(b.end-b.start),a,{animate:!1});console.log(g)}};d.open("POST",m,!0);d.send("sensors")},50)};