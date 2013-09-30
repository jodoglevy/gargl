(function() {
	var shouldRecord = false;
	var recordingButtonText = {
		false: "Start Recording",
		true: "Stop Recording"
	};
	var requests = [];
	var nextId = 0;
	var removeButtonHtml = "<input type='button' value='Remove'";
	
	function toggleRecord() {
		shouldRecord = !shouldRecord;
		document.querySelector('#garglRecord').setAttribute("value",recordingButtonText[shouldRecord]);
	}
	
	function clearRequestsTable() {
		requests = [];
		nextId = 0;
		
		var trs = document.querySelectorAll(".garglTableEntry");
		for(var i = 0; i < trs.length; i ++){
			trs[i].parentNode.removeChild(trs[i]);
		}
	}
	
	function removeQueryStringFromUrl(url) {
		var questionMarkIndex = url.indexOf("?");
		return (questionMarkIndex !== -1 ? url.substr(0,questionMarkIndex) : url);
	}
	
	function getDomainOfUrl(url) {
		var domain = url.substring(url.indexOf('/')+2);
		var slashIndex = domain.indexOf("/");
		return (slashIndex !== -1 ? domain.substr(0,slashIndex) : domain);
	}
	
	function convertHarQueryStringObjectToString(harQueryStringObj) {
		var queryString = "";
		for(var i = 0; i < harQueryStringObj.length; i++) {
			var param = harQueryStringObj[i];
			
			if(i != 0) queryString += "&";
			queryString += param.name;
			if(param.value && param.value.length > 0) queryString += "=" + param.value;
		}
		
		return queryString;
	}
	
	function addRowToRequestsTable(idNumber, urlWithoutQueryString, method, queryString, postData) {
		var removeButtonId = "removeButton" + idNumber;
		var tr = document.createElement("tr");
		
		tr.setAttribute("class","garglTableEntry");
		tr.innerHTML = "<td>" + urlWithoutQueryString + "</td>";
		tr.innerHTML += "<td>" + method + "</td>";
		tr.innerHTML += "<td>" + decodeURIComponent(queryString) + "</td>";
		tr.innerHTML += "<td>" + decodeURIComponent(postData) + "</td>";
		tr.innerHTML += "<td>" + removeButtonHtml + " id='" + removeButtonId + "' /></td>";
		
		document.querySelector("#garglTable").appendChild(tr);
		
		var removeButton = document.querySelector("#" + removeButtonId);
		removeButton.addEventListener('click', function() {
			requests[idNumber] = null;
			
			var tr = removeButton.parentNode.parentNode;
			tr.parentNode.removeChild(tr);
		});
	}
	
	function trackRequest(request) {
		var urlWithoutQueryString = removeQueryStringFromUrl(request.request.url);
		var domain = getDomainOfUrl(urlWithoutQueryString);
		
		if(shouldRecord && !urlWithoutQueryString.match(/.gif$|.jpeg$|.jpg$|.png$|.js$|.css$/)) {
			var domainMustContain = document.querySelector("#garglDomainSearch").value;
			if(domainMustContain.length > 0 && domain.indexOf(domainMustContain) === -1) return;
			
			requests.push(request);			
			
			var queryString = convertHarQueryStringObjectToString(request.request.queryString);
			var postData = request.request.postData ? request.request.postData.text : ""
			
			addRowToRequestsTable(nextId, urlWithoutQueryString, request.request.method, queryString, postData);
			nextId ++;
		}
	}
	
	window.addEventListener('load', function() {
		document.querySelector('#garglRecord').addEventListener('click', toggleRecord);
		document.querySelector('#garglClear').addEventListener('click', clearRequestsTable);
	});
	
	chrome.devtools.network.onRequestFinished.addListener(trackRequest);
})();