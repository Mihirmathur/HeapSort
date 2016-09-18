var express = require('express');
var router = express.Router();
var base64Img = require('base64-img');
var fs = require("fs");
var firebase = require("firebase");
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

/* GET home page. */
router.get('/', function(req, res) {
	var composition = {};
	var wordCloud = {"name": "garbage"};
	var history = firebase.database().ref('history/')
	.once('value').then( function(snapshot) {
		var allItems = snapshot.val();

		var compostCount = 0;
		var recycleCount = 0;
		var landfillCount = 0;

  //console.log(allItems);
  for(val in allItems){
  	//console.log(allItems[val]);
  	if(allItems[val].category == "landfill")landfillCount++;
  	if(allItems[val].category == "compost")compostCount++;
  	if(allItems[val].category == "recycle")recycleCount++;
  }

  console.log("Landfill: "+ landfillCount);
  console.log("Compost: "+ compostCount);
  console.log("Recycle: "+ recycleCount);
  composition = [{
  	x:["Landfill", "Compost", "Recycle"],
  	y:[landfillCount, compostCount, recycleCount],
  	type: 'bar'
  }]
  res.render('index', { title: 'HeapSort', comp: composition  });
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
