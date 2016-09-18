var express = require('express');
var router = express.Router();
var base64Img = require('base64-img');
var fs = require("fs");

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
	res.render('index', { title: 'HeapSort' });
});


function getTags(){
	Clarifai.getTagsByUrl("hi.jpg").then((response) => {
		console.log('Got response', response);
	}).catch((err) => {
		console.error('Encountered error making request:', err);
	});
}


router.post('/image', function(req, res){
	var obj = JSON.stringify(req.body, function(){
		del = obj.slice(10, function(){
			console.log(del);

			fs.writeFile("hi.jpg", new Buffer(obj, "base64"), function(err) {});

			console.log(filepath+"====>");
			
			res.render('index', { title: 'Heap' });
		});
		
	});

	// obj = obj.slice(0, -2);
  //console.log("Got it! ==> ", obj);
  //var filepath = base64Img.imgSync(obj, '', '2');



})

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
