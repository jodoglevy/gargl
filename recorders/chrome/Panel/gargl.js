(function() {
	var shouldRecord = false;
	var recordingButtonText = {
		false: "Start Recording",
		true: "Stop Recording"
	};
	var requests = [];
	var removeButtonHtml = "<input type='button' value='Remove' />";

	window.addEventListener('load', function() {
		var recordButton = document.querySelector('#garglRecord');
		
		recordButton.addEventListener('click', function() {
			shouldRecord = !shouldRecord;
			recordButton.setAttribute("value",recordingButtonText[shouldRecord]);
		});
		
		document.querySelector('#garglClear').addEventListener('click', function() {
			var trs = document.querySelectorAll(".garglTableEntry");
			for(var i = 0; i < trs.length; i ++){
				trs[i].parentNode.removeChild(trs[i]);
			}
			
			requests = [];
		});
	});
		
	
	chrome.devtools.network.onRequestFinished.addListener(function(request) {
		var url = request.request.url;
		var questionMarkIndex = url.indexOf("?");
		var urlWithoutQueryString = questionMarkIndex !== -1 ? url.substr(0,questionMarkIndex) : url;
		
		var domain = urlWithoutQueryString.substring(urlWithoutQueryString.indexOf('/')+2);
		var slashIndex = domain.indexOf("/");
		domain = slashIndex !== -1 ? domain.substr(0,slashIndex) : domain;
		
		if(shouldRecord && !urlWithoutQueryString.match(/.gif$|.jpeg$|.jpg$|.png$|.js$|.css$/)) {
			var domainMustContain = document.querySelector("#garglDomainSearch").value;
			if(domainMustContain.length > 0 && domain.indexOf(domainMustContain) === -1) return;
			
			requests.push(request);			
			
			var queryString = "";
			for(var i = 0; i < request.request.queryString.length; i++) {
				var param = request.request.queryString[i];
				
				if(i != 0) queryString += "&";
				queryString += param.name;
				if(param.value && param.value.length > 0) queryString += "=" + param.value;
			}
			
			var postData = request.request.postData ? request.request.postData.text : ""
			
			var tr = document.createElement("tr");
			tr.setAttribute("class","garglTableEntry");
			tr.innerHTML = "<td>" + urlWithoutQueryString + "</td>";
			tr.innerHTML += "<td>" + request.request.method + "</td>";
			tr.innerHTML += "<td>" + decodeURIComponent(queryString) + "</td>";
			tr.innerHTML += "<td>" + decodeURIComponent(postData) + "</td>";
			tr.innerHTML += "<td>" + removeButtonHtml + "</td>";
			
			document.querySelector("#garglTable").appendChild(tr);
		}
	});
})();