(function() {
	var shouldRecord = false;
	var requests = [];
	var nextId = 0;
	
	const garglSchemaVersion = "1.0";

	const removeButtonHtml = "<input type='button' value='Remove'";
	const functionNameInputHtml = "<input type='text' name='funcName'";
	const functionDescriptionInputHtml = "<input type='text' name='funcDescription'";

	const garglRecordSelector = "#garglRecord";
	const garglTableEntrySelector = ".garglTableEntry";
	const garglTableSelector = "#garglTable";
	const garglDomainSearchSelector = "#garglDomainSearch";
	const garglModuleNameSelector = "#garglModuleName";
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
		var moduleName = document.querySelector(garglModuleNameSelector).value;
		
		if (prevLink) window.URL.revokeObjectURL(prevLink.href);

		var garglFormattedRequests = {
			garglSchemaVersion: garglSchemaVersion,
			moduleVersion: "1.0",
			moduleName: moduleName,
			moduleDescription: "",
			functions: convertHarArrayToGarglArray(requests)
		};

		var fileContents = JSON.stringify(garglFormattedRequests, null, "\t");
		var bb = new Blob([fileContents], {type: 'text/plain'});

		var a = prevLink || document.createElement('a');
		a.download = ((moduleName.length > 0 ? moduleName : "gargl") + ".gtf");
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

			removeUnneededMetadataFromItem(harItem);
			addGarglMetadataToItem(harItem);

			garglArray.push(harItem);
		});

		return garglArray;
	}

	function removeUnneededMetadataFromItem(item) {
		delete(item.startedDateTime);
		delete(item.time);
		delete(item.cache);
		delete(item.timings);
		delete(item.pageref);

		delete(item.request.headersSize);
		delete(item.request.bodySize);
		delete(item.request.cookies);

		if(item.request) {
			if(item.request.postData) delete(item.request.postData.text);
			item.request.headers = removeUnneededHeaders(item.request.headers, /Cookie|Content-Type|Content-Length/i, false);
		}

		if(item.response) {
			item.response = {
				headers: removeUnneededHeaders(item.response.headers, /Set-Cookie/i, true)
			};
		}
	}

	function addGarglMetadataToItem(item) {
		item.functionName = "";
		item.functionDescription = "";

		if(item.request) {
			item.request.url = removeQueryStringFromUrl(item.request.url);

			if(item.request.queryString) {
				item.request.queryString.forEach(function(queryArg) {
					queryArg.type = "string";
					queryArg.description = "";
					queryArg.value = decodeURIComponent(queryArg.value);
				});
			}

			if(item.request.postData && item.request.postData.params) {
				item.request.postData.params.forEach(function(postArg) {
					postArg.type = "string";
					postArg.description = "";
					postArg.value = decodeURIComponent(postArg.value);
				});
			}
		}
	}

	function removeUnneededHeaders(headersArray, regexForUnneeded, removeHeaderValuesFromAll) {
		var headersToKeep = [];
		headersArray.forEach(function (header) {
			if(removeHeaderValuesFromAll) delete(header.value);

			if(!header.name.match(regexForUnneeded)) headersToKeep.push(header)
		});

		return headersToKeep;
	}
	
	window.addEventListener('load', function() {
		document.querySelector(garglRecordSelector).onclick = toggleRecord;
		document.querySelector(garglClearSelector).onclick = clearRequestsTable;
		document.querySelector(garglSaveSelector).onclick = createDownloadLink;
	});
	
	chrome.devtools.network.onRequestFinished.addListener(trackRequest);
})();