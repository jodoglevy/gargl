(function() {
	var shouldRecord = false;
	var requests = [];
	var nextId = 0;
	
	const removeButtonHtml = "<input type='button' value='Remove'";
	const functionNameInputHtml = "<input type='text' name='funcName'";
	const functionDescriptionInputHtml = "<input type='text' name='funcDescription'";

	const garglRecordSelector = "#garglRecord";
	const garglTableEntrySelector = ".garglTableEntry";
	const garglTableSelector = "#garglTable";
	const garglDomainSearchSelector = "#garglDomainSearch";
	const garglSaveFileNameSelector = "#garglSaveFileName";
	const garglSaveHolderSelector = "#garglSaveHolder";
	const garglClearSelector = "#garglClear";
	const garglSaveSelector = "#garglSave";

	const recordingButtonText = {
		false: "Start Recording",
		true: "Stop Recording"
	};


	function toggleRecord() {
		shouldRecord = !shouldRecord;
		document.querySelector(garglRecordSelector).setAttribute("value",recordingButtonText[shouldRecord]);
	}
	
	function clearRequestsTable() {
		requests = [];
		nextId = 0;
		
		var trs = document.querySelectorAll(garglTableEntrySelector);
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
		var funcNameInputId = "funcNameInput" + idNumber;
		var funcDescriptionInputId = "funcDescriptionInput" + idNumber;
		
		var tr = document.createElement("tr");
		tr.setAttribute("class","garglTableEntry");
		tr.innerHTML = "<td>" + functionNameInputHtml + " id='" + funcNameInputId + "' /></td>";
		tr.innerHTML += "<td>" + functionDescriptionInputHtml + " id='" + funcDescriptionInputId + "' /></td>";
		tr.innerHTML += "<td>" + urlWithoutQueryString + "</td>";
		tr.innerHTML += "<td>" + method + "</td>";
		tr.innerHTML += "<td>" + decodeURIComponent(queryString) + "</td>";
		tr.innerHTML += "<td>" + decodeURIComponent(postData) + "</td>";
		tr.innerHTML += "<td>" + removeButtonHtml + " id='" + removeButtonId + "' /></td>";
		
		document.querySelector(garglTableSelector).appendChild(tr);
		
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
			var domainMustContain = document.querySelector(garglDomainSearchSelector).value;
			if(domainMustContain.length > 0 && domain.indexOf(domainMustContain) === -1) return;
			
			requests.push(request);			
			
			var queryString = convertHarQueryStringObjectToString(request.request.queryString);
			var postData = request.request.postData ? request.request.postData.text : ""
			
			addRowToRequestsTable(nextId, urlWithoutQueryString, request.request.method, queryString, postData);
			nextId ++;
		}
	}
	
	function cleanUpDownloadLink(a) {
		a.textContent = 'Downloaded';
		a.dataset.disabled = true;

		// Need a small delay for the revokeObjectURL to work properly.
		setTimeout(function() {
			window.URL.revokeObjectURL(a.href);
			a.parentNode.removeChild(a);
		}, 1500);
	}
	
	function createDownloadLink() {
		window.URL = window.webkitURL || window.URL;
		var prevLink = document.querySelector('a');
		var fileName = document.querySelector(garglSaveFileNameSelector).value;
		
		if (prevLink) window.URL.revokeObjectURL(prevLink.href);

		var garglFormattedRequests = convertHarArrayToGarglArray(requests);
		var fileContents = JSON.stringify(garglFormattedRequests, null, "\t");
		var bb = new Blob([fileContents], {type: 'text/plain'});

		var a = prevLink || document.createElement('a');
		a.download = ((fileName.length > 0 ? fileName : "gargl") + ".gtf");
		a.href = window.URL.createObjectURL(bb);
		a.textContent = 'Click to download';

		a.dataset.downloadurl = ['text/plain', a.download, a.href].join(':');

		document.querySelector(garglSaveHolderSelector).appendChild(a);

		a.onclick = function(e) {
			if ('disabled' in this.dataset) return false;
			cleanUpDownloadLink(this);
		};
	}

	function convertHarArrayToGarglArray(harArray) {
		var garglArray = [];

		harArray.forEach(function(harItem, itemIndex) {
			if(!harItem) return;

			delete(harItem.startedDateTime);
			delete(harItem.time);
			delete(harItem.cache);
			delete(harItem.timings);
			delete(harItem.pageref);

			delete(harItem.request.headersSize);
			delete(harItem.request.bodySize);

			if(harItem.request.postData) delete(harItem.request.postData.text);

			harItem.response = {
				headers: harItem.response.headers,
				cookies: harItem.response.cookies,
			};

			garglArray.push(harItem);
		});

		return garglArray;
	}
	
	window.addEventListener('load', function() {
		document.querySelector(garglRecordSelector).onclick = toggleRecord;
		document.querySelector(garglClearSelector).onclick = clearRequestsTable;
		document.querySelector(garglSaveSelector).onclick = createDownloadLink;
	});
	
	chrome.devtools.network.onRequestFinished.addListener(trackRequest);
})();