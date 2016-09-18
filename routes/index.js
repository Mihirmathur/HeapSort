var express = require('express');
var router = express.Router();
var base64Img = require('base64-img');
var fs = require("fs");
var firebase = require("firebase");

var json2csv = require('json2csv');
var config = {
	apiKey: "9o5B3crPY6sDuqbDRggbyFA695MK6m2cHiZ4vTDE",
	authDomain: "heapsort-9a89b.firebaseapp.com",
	databaseURL: "https://heapsort-9a89b.firebaseio.com/",
};
firebase.initializeApp(config);

// var cognitiveServices = require('cognitive-services');

// const computerVision = new cognitiveServices.computerVision({
//     API_KEY: '1230f40bf06a49d8b9ce67d8c64f3896'
// });

var Clarifai = require('clarifai');

Clarifai.initialize({
	'clientId': 'yy5JGA47Iydxvds2cHHiue1hxHLJdCkoIwbSma4x',
	'clientSecret': 'ZbwiIclnKgQuQc3s1bhGTYrg8d5_lz6lD-psizy7'
})

// const parameters = {
//     visualFeatures: "Categories"
// };

function ConvertToCSV(objArray) {
    var array = typeof objArray != 'object' ? JSON.parse(objArray) : objArray;
    var str = '';

    for (var i = 0; i < array.length; i++) {
      var line = '';
      for (var index in array[i]) {
        if (line != '') line += ','

          line += array[i][index];
      }

      str += line + '\r\n';
    }

    return str;
  }

/* GET home page. */
router.get('/', function(req, res) {
	var composition = {};
	var wordCloud = {};
	var history = firebase.database().ref('history/')
	.once('value').then( function(snapshot) {
		var allItems = snapshot.val();

		var compostCount = 0;
		var recycleCount = 0;
		var landfillCount = 0;

  //console.log(allItems);
  for(val in allItems){
  	//console.log(allItems[val]);
  	for(tag in allItems[val].tags){
  		var word = allItems[val].tags[tag];
  		console.log(word);
  		if(word!="indoor"){
  			if(wordCloud[word]===undefined){
  				wordCloud[word] = 1;
  			}
  			else {
  				wordCloud[word]++;	
  			}
  		}
  	}



  	if(allItems[val].category == "landfill")landfillCount++;
  	if(allItems[val].category == "compost")compostCount++;
  	if(allItems[val].category == "recycle")recycleCount++;
  }
  console.log(wordCloud);
  console.log("Landfill: "+ landfillCount);
  console.log("Compost: "+ compostCount);
  console.log("Recycle: "+ recycleCount);
  composition = [{
  	x:["Landfill", "Compost", "Recycle"],
  	y:[landfillCount, compostCount, recycleCount],
  	type: 'bar'
  }]
  // var wordCloudCSV = json2csv(wordCloud);
  // console.log(wordCloudCSV);

  var cloudCSV = ["Tag:Amount"];
  for(val in wordCloud){
  	cloudCSV.push((val + ":" + wordCloud[val]).toString());
  	
  }

  cloudCSV= cloudCSV.join(',');
  cloudCSV = cloudCSV.replace(/,/g, '\n');
  cloudCSV = cloudCSV.replace(/:/g, ',');
  // console.log(cloudCSV);
  //cloudCSV = ConvertToCSV(wordCloud);
  console.log(cloudCSV);

  fs.writeFile("public/test.csv", cloudCSV, function(err) {
  	if(err) {
  		return console.log(err);
  	}

  	console.log("The file was saved!");
  }); 

  res.render('index', { title: 'HeapSort', comp: composition, cloud: wordCloud  });
});

	
});


function getTags(){
	Clarifai.getTagsByUrl("hi.jpg").then((response) => {
		console.log('Got response', response);
	}).catch((err) => {
		console.error('Encountered error making request:', err);
	});
}


router.post('/image', function(req, res){
	var obj = req.body.body.slice(22);
	var tags;

	Clarifai.getTagsByImageBytes(obj).then((response) => {
		tags = response.results[0]["result"].tag.classes;
		console.log('Got response', tags);
		tags = JSON.stringify(tags);
		res.end(tags);

	}).catch((err) => {
		console.error('Encountered error making request:', err);
		tags = JSON.stringify(tags);
		res.end(tags);
	});



	// });

	// obj = obj.slice(0, -2);
  //console.log("Got it! ==> ", obj);
  //var filepath = base64Img.imgSync(obj, '', '2');



});

// router.get('/:bin?', function(req, res) {
//   console.log(req.params.data);
//   var body = req.params.bin;
//   console.log('Response From Router==> ' + body);
//   res.render('index', { title: 'HeapSort' });
// });



  // computerVision.analyzeImage({
  //       parameters,
  //       body
  //   })
  //   .then((response) => {
  //       console.log('Got response', response);
  //   })
  //   .catch((err) => {
  //       console.error('Encountered error making request:', err);
  //   });
  // res.render('index', { title: 'HeapSort' });
  // res.send(JSON);




  module.exports = router;
