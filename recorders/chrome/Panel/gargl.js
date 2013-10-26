(function() {
	var shouldRecord = false;
	var garglItems = [];
	var nextId = 0;
	
	const garglSchemaVersion = "1.0";

	const removeButtonHtml = "<input type='button' value='Remove'";
	const editButtonHtml = "<input type='button' value='Edit'";
	const detailsButtonHtml = "<input type='button' value='Details'";
	const functionNameInputHtml = "<input type='text' name='funcName'";
	
	const garglNewSelector = "#garglNew";
	const garglExistingSelector = "#garglExisting";
	const garglOpenSelector = "#garglOpen";
	const garglOpenFormSelector = "#openForm";
	const garglStartFormSelector = "#startForm";
	const garglRecordAreaSelector = "#recordArea";
	const garglRecordSelector = "#garglRecord";
	const garglTableEntrySelector = ".garglTableEntry";
	const garglTableSelector = "#garglTable";
	const garglDomainSearchSelector = "#garglDomainSearch";
	const garglModuleNameSelector = "#garglModuleName";
	const garglModuleDescriptionSelector = "#garglModuleDescription";
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
	
	function clearGarglItemsTable() {
		garglItems = [];
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
	
	function convertRequestFieldArrayToString(requestFieldArray, alternateSeperatorBetweenFields, alternateSeperatorBetweenKeyAndValue) {
		var string = "";
		for(var i = 0; i < requestFieldArray.length; i++) {
			var param = requestFieldArray[i];
			
			if(i != 0) string += (alternateSeperatorBetweenFields || "&");
			string += param.name;
			if(param.value && param.value.length > 0) string += (alternateSeperatorBetweenKeyAndValue || "=") + param.value;
		}
		
		return string;
	}
	
	function addRowToGarglItemsTable(idNumber, garglItem) {
		var removeButtonId = "removeButton" + idNumber;
		var editButtonId = "editButton" + idNumber;
		var detailsButtonId = "detailsButton" + idNumber;
		var funcNameInputId = "funcNameInput" + idNumber;
		
		var url = garglItem.request.url;
		var method = garglItem.request.method;

		if(url.length > 90) url = url.substr(0,90) + "..."

		var tr = document.createElement("tr");
		tr.setAttribute("class","garglTableEntry");
		tr.innerHTML = "<td>" + functionNameInputHtml + " id='" + funcNameInputId + "' /></td>";
		tr.innerHTML += "<td>" + url + "</td>";
		tr.innerHTML += "<td>" + method + "</td>";
		tr.innerHTML += "<td>" + detailsButtonHtml + " id='" + detailsButtonId + "' /></td>";
		tr.innerHTML += "<td>" + editButtonHtml + " id='" + editButtonId + "' /></td>";
		tr.innerHTML += "<td>" + removeButtonHtml + " id='" + removeButtonId + "' /></td>";
		
		document.querySelector(garglTableSelector).appendChild(tr);
		
		var removeButton = document.querySelector("#" + removeButtonId);
		removeButton.addEventListener('click', function() {
			garglItems[idNumber] = null;
			
			var tr = removeButton.parentNode.parentNode;
			tr.parentNode.removeChild(tr);
		});

		var detailsButton = document.querySelector("#" + detailsButtonId);
		detailsButton.addEventListener('click', function() {
			garglItems[idNumber].functionName = getFunctionName(idNumber);

			var garglItem = garglItems[idNumber];
			var garglRequest = garglItem.request;

			var detailsString = "Function Name: " + garglItem.functionName || "";
			detailsString += "\nFunction Description: " + garglItem.functionDescription || "";

			detailsString += "\n\nFunction Request URL: " + garglRequest.url;
			detailsString += "\nFunction Request Method: " + garglRequest.method;
			
			if(garglRequest.queryString && garglRequest.queryString.length > 0)
				detailsString += "\n\nFunction Request Query String:\n" + convertRequestFieldArrayToString(garglRequest.queryString, "\n", ": ");

			if(garglRequest.postData && garglRequest.postData.params && garglRequest.postData.params.length > 0)
				detailsString += "\n\nFunction Request Post Data:\n" + convertRequestFieldArrayToString(garglRequest.postData.params, "\n", ": ");
			
			alert(detailsString);
		});
	}

	function getFunctionName(id) {
		return document.querySelector("#funcNameInput" + id).value.replace(/ /g, "-");
	}
	
	function trackRequest(request) {
		var urlWithoutQueryString = removeQueryStringFromUrl(request.request.url);
		var domain = getDomainOfUrl(urlWithoutQueryString);
		
		if(shouldRecord && !urlWithoutQueryString.match(/.gif$|.jpeg$|.jpg$|.png$|.js$|.css$/)) {
			var domainMustContain = document.querySelector(garglDomainSearchSelector).value;
			if(domainMustContain.length > 0 && domain.indexOf(domainMustContain) === -1) return;
			
			addGarglMetadataToItem(request);
			removeUnneededMetadataFromItem(request);

			garglItems.push(request);			
			
			addRowToGarglItemsTable(nextId, request);
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
		var moduleName = document.querySelector(garglModuleNameSelector).value.replace(/ /g, "-");
		var moduleDescription = document.querySelector(garglModuleDescriptionSelector).value;
		
		if (prevLink) window.URL.revokeObjectURL(prevLink.href);

		var garglItemsWithoutEmptys = [];
		garglItems.forEach(function(item, itemIndex) {
			if(item) {
				item.functionName = getFunctionName(itemIndex);
				garglItemsWithoutEmptys.push(item);
			}
		});

		var garglFormattedItems = {
			garglSchemaVersion: garglSchemaVersion,
			moduleVersion: "1.0",
			moduleName: moduleName,
			moduleDescription: moduleDescription,
			functions: garglItemsWithoutEmptys
		};

		var fileContents = JSON.stringify(garglFormattedItems, null, "\t");
		var bb = new Blob([fileContents], {type: 'text/plain'});

		var a = prevLink || document.createElement('a');
		a.download = ((moduleName.length > 0 ? moduleName.toLowerCase() : "gargl") + ".gtf");
		a.href = window.URL.createObjectURL(bb);
		a.textContent = 'Click to download';

		a.dataset.downloadurl = ['text/plain', a.download, a.href].join(':');

		document.querySelector(garglSaveHolderSelector).appendChild(a);

		a.onclick = function(e) {
			if ('disabled' in this.dataset) return false;
			cleanUpDownloadLink(this);
		};
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
				headers: removeUnneededHeaders(item.response.headers, null, true)
			};
		}
	}

	function addGarglMetadataToItem(item) {
		if(item.request) {
			item.request.url = removeQueryStringFromUrl(item.request.url);

			if(item.request.queryString) {
				item.request.queryString.forEach(function(queryArg) {
					queryArg.description = "";
					queryArg.value = decodeURIComponent(queryArg.value);
				});
			}

			if(item.request.postData && item.request.postData.params) {
				item.request.postData.params.forEach(function(postArg) {
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

	function startGargl() {
		document.querySelector(garglRecordAreaSelector).style.display = "block";
		document.querySelector(garglStartFormSelector).style.display = "none";
		document.querySelector(garglOpenFormSelector).style.display = "none";
	}

	function showOpenForm() {
		document.querySelector(garglRecordAreaSelector).style.display = "none";
		document.querySelector(garglStartFormSelector).style.display = "none";
		document.querySelector(garglOpenFormSelector).style.display = "block";
	}
	
	window.addEventListener('load', function() {
		document.querySelector(garglRecordSelector).onclick = toggleRecord;
		document.querySelector(garglClearSelector).onclick = clearGarglItemsTable;
		document.querySelector(garglSaveSelector).onclick = createDownloadLink;
		document.querySelector(garglNewSelector).onclick = startGargl;
		document.querySelector(garglExistingSelector).onclick = showOpenForm;

		document.querySelector(garglRecordAreaSelector).style.display = "none";
		document.querySelector(garglOpenFormSelector).style.display = "none";
	});
	
	chrome.devtools.network.onRequestFinished.addListener(trackRequest);
})();