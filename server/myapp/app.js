var express = require('express');
var bodyParser = require("body-parser");
var curl = require('curlrequest');
var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
var curl_address = ('http://127.0.0.1:8293');
var Web3 = require('web3');
var web3 = new Web3(new Web3.providers.HttpProvider("http://localhost:8293"));
var eth = web3.eth;


app.get('/', function (req, res) {
  res.send('Hello!');
});
//var routes = require("./routes/routes.js")(app);

app.get('/nekonium-api/getbalance', function (req, res) {
	//var id = parseInt(req.params.address);
	//dirty trick here, use increasing nonce to prevent network/ http caching.  
	console.log(req.query);
	if(!(req.query.address) || !(req.query.nonce)) {
		res.send('error');
	}
	else{
		var address = req.query.address;
		//console.log(address);
                //res.send('test: '+address);
		//curl -X POST --data '{"jsonrpc":"2.0","method":"eth_protocolVersion","params":[],"id":67}'
		var curl_data = '{"jsonrpc":"2.0","method":"eth_getBalance","params":["'+ address + '", "latest"],"id":1}';
		curl.request({ url: curl_address, method:'POST', data: curl_data }, function (err, data) {
		    console.log(data);
			var temp = JSON.parse(data.toString());
			//console.log(temp["result"]);
			var result = {"status":"1","message":"OK","result":temp["result"]};
			res.send(result);	
		 });		
	}

});
app.get('/nekonium-api/gettxcount', function (req, res) {
        //var id = parseInt(req.params.address);
        console.log(req.query);
        if(!(req.query.address) || !(req.query.nonce) ) {
                res.send('error');
        }
        else{
                var address = req.query.address;
                //res.send('test: '+address);
                //curl -X POST --data '{"jsonrpc":"2.0","method":"eth_protocolVersion","params":[],"id":67}'
                var curl_data = '{"jsonrpc":"2.0","method":"eth_getTransactionCount","params":["'+address+'","latest"],"id":1}';
                curl.request({ url: curl_address, method:'POST', data: curl_data }, function (err, data) {
                    console.log(data);
                        var temp = JSON.parse(data.toString());
                        //console.log(temp);
                        //var result = {"status":"1","message":"OK","result":temp["result"]};
                        res.send(temp);
                 });
        }

});
app.get('/nekonium-api/getmultibalance', function (req, res) {
        //var id = parseInt(req.params.address);
        console.log(req.query);
        if(!(req.query.address) || !(req.query.nonce)) {
                res.send('error');
        }
        else{
                var address = req.query.address.toString().split(',');;
                //res.send('test: '+address);
                //curl -X POST --data '{"jsonrpc":"2.0","method":"eth_protocolVersion","params":[],"id":67}'
		var response = {"status":"1","message":"OK"};
              	var result = [];
		var count =0;
		 for (var i = 0; i < address.length; i++) {

		(function(i) {
			var curl_data = '{"jsonrpc":"2.0","method":"eth_getBalance","params":["'+ address[i] + '", "latest"],"id":1}';
                	curl.request({ url: curl_address, method:'POST', data: curl_data }, function (err, data) {
                	    //console.log(data);
				//console.log(i);
                        	var temp = JSON.parse(data.toString());
                        	//console.log(temp["result"]);
                        	result.push({"account":address[i], "balance":temp["result"]});
				console.log(result);
				count = count +1;
				//console.log(count);
				if(count==address.length){
					response = {"status":"1","message":"OK","result":result};
                			res.send(response);	
				}
                	      //  res.send(result);
	                 });
		})(i)
		}
		//response = {"status":"1","message":"OK","result":result};
		//res.send(response);
		
        }

});

app.get('/nekonium-api/sendtx', function (req, res) {
        //var id = parseInt(req.params.address);
        console.log(req.query);
        if(!(req.query.hex) || !(req.query.nonce)) {
                res.send('error');
        }       
        else{
                var hex_value = req.query.hex;
                //res.send('test: '+address);
                //curl -X POST --data '{"jsonrpc":"2.0","method":"eth_protocolVersion","params":[],"id":67}'
                var curl_data = '{"jsonrpc":"2.0","method":"eth_sendRawTransaction","params":["'+hex_value+'"],"id":1}';
                curl.request({ url: curl_address, method:'POST', data: curl_data }, function (err, data) {
                    console.log(data);
                        var temp = JSON.parse(data.toString());
                        //console.log(temp["result"]);
                        //var result = {"status":"1","message":"OK","result":temp["result"]};
                        res.send(temp);
                 });
        }
        
});

function getTransactionsByAccount(myaccount, startBlockNumber, endBlockNumber) {
  if (endBlockNumber == null) {
    endBlockNumber = eth.blockNumber;
    console.log("Using endBlockNumber: " + endBlockNumber);
  }
        var result = [];
  if (startBlockNumber == null) {
    startBlockNumber = endBlockNumber - 10;
    console.log("Using startBlockNumber: " + startBlockNumber);
  }
  console.log("Searching for transactions to/from account \"" + myaccount + "\" within blocks "  + startBlockNumber + " and " + endBlockNumber);

  for (var i = endBlockNumber; i >= startBlockNumber; i--) {
    if (i % 1000 == 0) {
      console.log("Searching block " + i);
    }
    if(i == startBlockNumber || result.length>=2)
        return result;
    var block = eth.getBlock(i, true);
    if (block != null && block.transactions != null) {
      block.transactions.forEach( function(e) {
        if (myaccount == "*" || myaccount == e.from || myaccount == e.to) {
//{"blockNumber":"65204","timeStamp":"1439232889","hash":"0x98beb27135aa0a25650557005ad962919d6a278c4b3dde7f4f6a3a1e65aa746c","nonce":"0","blockHash":"0x373d339e45a701447367d7b9c7cef84aab79c2b2714271b908cda0ab3ad0849b","transactionIndex":"0","from":"0x3fb1cd2cd96c6d5c0b5eb3322d807b34482481d4","to":"0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae","value":"0","gas":"122261","gasPrice":"50000000000","isError":"0","txreceipt_status":"","input":"0xf00d4b5d000000000000000000000000036c8cecce8d8bbf0831d840d7f29c9e3ddefa63000000000000000000000000c5a96db085dda36ffbe390f455315d30d6d3dc52","contractAddress":"","cumulativeGasUsed":"122207","gasUsed":"122207","confirmations":"4649350"}
                result.push({"blockNumber":e.blockNumber,
                                "hash":e.hash,
                                "from":e.from,
                                "to": e.to,
                                "value":e.value +"" ,
                                "confirmations":eth.blockNumber-e.blockNumber,
                                "timeStamp":block.timestamp,
                                "nonce":e.nonce,
                                "gasUsed":e.gas,
                                "gasPrice":e.gasPrice +""
                                });
		console.log(e.hash);
                if(result.length >= 2)
                        return result;
   

          }
      })
    }
  }
}

app.get('/nekonium-api/gettx', function (req, res) {
        //var id = parseInt(req.params.address);
        console.log(req.query);
        if(!(req.query.address) || !(req.query.nonce)) {
                res.send('error');
        }
        else{
                var address = req.query.address; 
		var result = getTransactionsByAccount(address);
                        var result = {"status":"1","message":"OK","result":result};
                        res.send(result);
           };
        

});

app.get('/nekonium-api/getmultitxinfo', function (req, res) {
        //var id = parseInt(req.params.address);
        console.log(req.query);
        if(!(req.query.tx) || !(req.query.nonce)) {
                res.send('error');
        }
        else{
                var tx = req.query.tx.toString().split(',');;
                //res.send('test: '+address);
                //curl -X POST --data '{"jsonrpc":"2.0","method":"eth_protocolVersion","params":[],"id":67}'
		//   curl -X POST --data '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":83}'

                var response = {"status":"1","message":"OK"};
                var result = [];
                var count =0;
                 for (var i = 0; i < tx.length; i++) {

                (function(i) {
                        var curl_data = '{"jsonrpc":"2.0","method":"eth_getTransactionByHash","params":["'+tx[i]+'"],"id":1}';

			//console.log(curl_data)
			 
                        curl.request({ url: curl_address, method:'POST', data: curl_data }, function (err, data) {       
		        var e = JSON.parse(data.toString())["result"];
			console.log(e)
        		var block = eth.getBlock(parseInt(e.blockNumber,16),false);
			if (!block || !block.number)
				block = eth.getBlock("latest");
			console.log(block.number);
			result.push({"blockNumber":parseInt(block.number),
                                	"hash":e.hash,
                                	"from":e.from,
                                	"to": e.to,
                                	"value":e.value ,
                                	"confirmations":eth.blockNumber-block.number,
                                	"timeStamp":parseInt(block.timestamp),
                                	"nonce":e.nonce,
                                	"gasUsed":parseInt(e.gas),
                                	"gasPrice":parseInt(e.gasPrice)
                                });

                                console.log(result);
                                count = count +1;
                                //console.log(count);
                                if(count==tx.length){
                                        response = {"status":"1","message":"OK","result":result};
                                        res.send(response);
                                }
                              //  res.send(result);
                         });
                })(i)
                }

        }

});

app.listen(8888, function () {
  console.log('Example app listening on port 8888!');
});
