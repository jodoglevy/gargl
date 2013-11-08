(function() {
	var shouldRecord = false;
	var garglItems = [];
	var nextId = 0;
	
	const maxUrlLength = 90;
	const garglSchemaVersion = "1.0";

	const removeButtonHtml = "<input type='button' value='Remove'";
	const editButtonHtml = "<input type='button' value='Edit'";
	const detailsButtonHtml = "<input type='button' value='Details'";
	const functionNameInputHtml = "<input type='text' name='funcName'";

	const garglRequestFieldElementClass = "garglRequestFieldElement";
	const garglResponseFieldElementClass = "garglResponseFieldElement";
	
	const garglNewSelector = "#garglNew";
	const garglExistingSelector = "#garglExisting";
	const garglOpenSelector = "#garglOpen";
	const garglOpenFormSelector = "#openForm";
	const garglEditFormSelector = "#editForm";
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
	const garglEditSaveSelector = "#garglEditSave";
	const garglEditCancelSelector = "#garglEditCancel";
	const garglEditFormFunctionNameSelector = "#editFormFunctionName";
	const garglEditFormFunctionDescriptionSelector = "#editFormFunctionDescription";
	const garglEditFormFunctionRequestURLSelector = "#editFormFunctionRequestURL";
	const garglEditFormFunctionRequestMethodSelector = "#editFormFunctionRequestMethod";
	const garglEditFormFunctionRequestHeadersSelector = "#editFormFunctionRequestHeaders";
	const garglEditFormFunctionRequestQueryStringSelector = "#editFormFunctionRequestQueryString";
	const garglEditFormFunctionRequestPostDataSelector = "#editFormFunctionRequestPostData";
	const garglEditFormFunctionIdNumberSelector = "#garglFunctionIdNumber";
	const garglNewResponseFieldSelector = "#garglNewResponseField";
	const editFormFunctionResponseFieldsSelector = "#editFormFunctionResponseFields";
	const garglViewResponseSelector = "#garglViewResponse";
	const garglViewResponseHolderSelector = "#garglViewResponseHolder";

	const recordingButtonText = {
		false: "Start Recording",
		true: "Stop Recording"
	};

	const garglFileGetResponseError = "This gargl function was loaded from a file and so the response body is not available.";


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
		var funcUrlId = "funcUrlId" + idNumber;
		var funcMethodId = "funcMethodId" + idNumber;

		var url = garglItem.request.url;
		var method = garglItem.request.method;

		if(url.length > maxUrlLength) url = url.substr(0,maxUrlLength) + "..."

		var tr = document.createElement("tr");
		tr.setAttribute("class","garglTableEntry");
		tr.innerHTML = "<td>" + functionNameInputHtml + " id='" + funcNameInputId + "' value='" + (garglItem.functionName || "") + "' /></td>";
		tr.innerHTML += "<td id='" + funcUrlId + "'>" + url + "</td>";
		tr.innerHTML += "<td id='" + funcMethodId + "'>" + method + "</td>";
		tr.innerHTML += "<td>" + detailsButtonHtml + " id='" + detailsButtonId + "' /></td>";
		tr.innerHTML += "<td>" + editButtonHtml + " id='" + editButtonId + "' /></td>";
		tr.innerHTML += "<td>" + removeButtonHtml + " id='" + removeButtonId + "' /></td>";
		
		document.querySelector(garglTableSelector).appendChild(tr);
		
		var removeButton = document.querySelector("#" + removeButtonId);
		removeButton.addEventListener('click', function() {
			removeGarglItem(idNumber, removeButton);
		});

		var editButton = document.querySelector("#" + editButtonId);
		editButton.addEventListener('click', function() {
			showEditForm(idNumber);
		});

		var detailsButton = document.querySelector("#" + detailsButtonId);
		detailsButton.addEventListener('click', function() {
			showDetails(idNumber);
		});
	}

	function updateRowInGarglItemsTable(idNumber, garglItem) {
		var funcNameInputId = "funcNameInput" + idNumber;
		var funcUrlId = "funcUrlId" + idNumber;
		var funcMethodId = "funcMethodId" + idNumber;

		var url = garglItem.request.url;
		if(url.length > maxUrlLength) url = url.substr(0,maxUrlLength) + "..."
		
		document.querySelector("#"+funcNameInputId).value = garglItem.functionName;
		document.querySelector("#"+funcUrlId).innerHTML = url;
		document.querySelector("#"+funcMethodId).innerHTML = garglItem.request.method;		
	}

	function showEditForm(idNumber) {
		var garglItem = garglItems[idNumber];
		garglItem.functionName = getFunctionName(idNumber);

		document.querySelector(garglEditFormFunctionIdNumberSelector).value = idNumber;
		document.querySelector(garglEditFormFunctionNameSelector).value = garglItem.functionName;
		document.querySelector(garglEditFormFunctionDescriptionSelector).value = garglItem.functionDescription || "";
		document.querySelector(garglEditFormFunctionRequestURLSelector).value = garglItem.request.url;
		document.querySelector(garglEditFormFunctionRequestMethodSelector).value = garglItem.request.method;

		createRequestFieldForm(garglEditFormFunctionRequestHeadersSelector, garglItem.request.headers);
		createRequestFieldForm(garglEditFormFunctionRequestQueryStringSelector, garglItem.request.queryString);
		
		if(garglItem.request.postData) {
			createRequestFieldForm(garglEditFormFunctionRequestPostDataSelector, garglItem.request.postData);
		}
		else createRequestFieldForm(garglEditFormFunctionRequestPostDataSelector, null);

		if(garglItem.response.fields) {
			garglItem.response.fields.forEach(function(field) {
				addResponseField(null, field.name, field.cssSelector);
			})
		};

		document.querySelector(garglRecordAreaSelector).style.display = "none";
		document.querySelector(garglStartFormSelector).style.display = "none";
		document.querySelector(garglOpenFormSelector).style.display = "none";
		document.querySelector(garglEditFormSelector).style.display = "block";
	}

	function createRequestFieldForm(formAreaSelector, requestFieldArray) {
		var formAreaElement = document.querySelector(formAreaSelector);

		if(!requestFieldArray || requestFieldArray.length == 0) formAreaElement.style.display = "none";
		else {
			var lastRequestFieldElement = null;
			formAreaElement.style.display = "block";

			requestFieldArray.forEach(function(requestField) {
				var requestFieldId = "requestField" + requestField.name;

				var span = document.createElement("span");
				span.setAttribute("class", garglRequestFieldElementClass);
				
				span.innerHTML = "<br /><label for='" + requestFieldId + "'>" + requestField.name + ": </label>";
				span.innerHTML += "<input type='text' value='" + requestField.value + "' id='" + requestFieldId + "' class='garglRequestFieldValue' />";
				
				formAreaElement.appendChild(span);
				lastRequestFieldElement = span;
			});

			var br1 = document.createElement("br");
			var br2 = document.createElement("br");
			lastRequestFieldElement.appendChild(br1);
			lastRequestFieldElement.appendChild(br2);
		}
	}

	function grabRequestFieldFormData(formAreaSelector) {
		var fieldData = [];
		var formAreaElement = document.querySelector(formAreaSelector);

		if(formAreaElement.style.display != "none") { 

			var fieldElements = formAreaElement.querySelectorAll("." + garglRequestFieldElementClass);

			for(var i = 0; i < fieldElements.length; i ++) {
				var field = {
					description: "",
					name: fieldElements[i].querySelector("label").innerHTML.replace(": ",""),
					value: fieldElements[i].querySelector("input").value,
				};

				fieldData.push(field);
			}
		}

		return fieldData;
	}

	function cancelEditGarglItem() {
		var requestFieldElements = document.querySelectorAll("." + garglRequestFieldElementClass);
		var responseFieldElements = document.querySelectorAll("." + garglResponseFieldElementClass);

		if(requestFieldElements) {
			for(var i = 0; i < requestFieldElements.length; i ++) {
				requestFieldElements[i].parentNode.removeChild(requestFieldElements[i]);
			};
		}

		if(responseFieldElements) {
			for(var i = 0; i < responseFieldElements.length; i ++) {
				responseFieldElements[i].parentNode.removeChild(responseFieldElements[i]);
			};
		}

		startGargl();
	}

	function saveEditGarglItem() {
		var idNumber = document.querySelector(garglEditFormFunctionIdNumberSelector).value;

		var garglItem = garglItems[idNumber];

		garglItem.functionName = document.querySelector(garglEditFormFunctionNameSelector).value;
		garglItem.functionDescription =  document.querySelector(garglEditFormFunctionDescriptionSelector).value;
		garglItem.request.url = document.querySelector(garglEditFormFunctionRequestURLSelector).value;
		garglItem.request.method =  document.querySelector(garglEditFormFunctionRequestMethodSelector).value;

		garglItem.request.headers = grabRequestFieldFormData(garglEditFormFunctionRequestHeadersSelector);
		garglItem.request.queryString = grabRequestFieldFormData(garglEditFormFunctionRequestQueryStringSelector);
		
		if(garglItem.request.postData) {
			garglItem.request.postData = grabRequestFieldFormData(garglEditFormFunctionRequestPostDataSelector);
		}

		garglItem.response.fields = grabResponseFieldFormData();

		updateRowInGarglItemsTable(idNumber, garglItem);
		cancelEditGarglItem();
	}

	function removeGarglItem(idNumber, removeButton) {
		garglItems[idNumber] = null;
			
		var tr = removeButton.parentNode.parentNode;
		tr.parentNode.removeChild(tr);
	}

	function showDetails(idNumber) {
		var garglItem = garglItems[idNumber];
		var garglRequest = garglItem.request

		garglItem.functionName = getFunctionName(idNumber);

		var detailsString = "Function Name: " + garglItem.functionName || "";
		detailsString += "\nFunction Description: " + garglItem.functionDescription || "";

		detailsString += "\n\nFunction Request URL: " + garglRequest.url;
		detailsString += "\nFunction Request Method: " + garglRequest.method;
		
		if(garglRequest.queryString && garglRequest.queryString.length > 0) {
			detailsString += "\n\nFunction Request Query String:\n" + convertRequestFieldArrayToString(garglRequest.queryString, "\n", ": ");
		}

		if(garglRequest.postData && garglRequest.postData.length > 0) {
			detailsString += "\n\nFunction Request Post Data:\n" + convertRequestFieldArrayToString(garglRequest.postData, "\n", ": ");
		}

		alert(detailsString);
	}

	function getFunctionName(id) {
		return document.querySelector("#funcNameInput" + id).value.replace(/ /g, "-");
	}
	
	function trackRequest(request) {
		var urlWithoutQueryString = removeQueryStringFromUrl(request.request.url);
		var domain = getDomainOfUrl(urlWithoutQueryString);
		
		if(shouldRecord && !urlWithoutQueryString.match(/.gif$|.jpeg$|.jpg$|.png$|.js$|.css$|.swf$/)) {
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
			if(item.request.postData) item.request.postData = item.request.postData.params;
			item.request.headers = removeUnneededHeaders(item.request.headers, /Cookie|Content-Length/i, false);
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
					queryArg.name = decodeURIComponent(queryArg.name);
					queryArg.value = decodeURIComponent(queryArg.value);
				});
			}

			if(item.request.postData) {
				item.request.postData.params.forEach(function(postArg) {
					postArg.description = "";
					postArg.name = decodeURIComponent(postArg.name);
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
		document.querySelector(garglEditFormSelector).style.display = "none";
	}

	function showOpenForm() {
		document.querySelector(garglRecordAreaSelector).style.display = "none";
		document.querySelector(garglStartFormSelector).style.display = "none";
		document.querySelector(garglOpenFormSelector).style.display = "block";
		document.querySelector(garglEditFormSelector).style.display = "none";
	}

	function processFile() {
		var file = document.querySelector(garglOpenSelector).files[0];

		var extensionIndex = file.name.indexOf(".gtf");
		if(extensionIndex == -1 || extensionIndex != (file.name.length - 4)) {
			alert("File must be a gargle template file (.gtf)");
		}
		else {
			var reader = new FileReader();

			reader.addEventListener("load", function(event) {
				var text = event.target.result;
				var garglModule = JSON.parse(text);

				document.querySelector(garglModuleNameSelector).value = garglModule.moduleName;
				document.querySelector(garglModuleDescriptionSelector).value = garglModule.moduleDescription;

				shouldRecord = true;
				garglModule.functions.forEach(function(item) { trackRequest(item); });
				shouldRecord = false;
				
				startGargl();
			});

			reader.readAsText(file);
		}
	}

	function addResponseField(clickEvent, responseName, responseSelector) {
		responseName = responseName || "";
		responseSelector = responseSelector || "";

		var responseFieldsArea = document.querySelector(editFormFunctionResponseFieldsSelector);
		
		var nextResponseId = responseFieldsArea.querySelectorAll("."+garglResponseFieldElementClass).length;
		var fieldNameId = "editFormFunctionResponseFieldName" + nextResponseId;
		var fieldSelectorId = "editFormFunctionResponseFieldSelector" + nextResponseId;
		var fieldSelectorTestId = "editFormFunctionResponseFieldTest" + nextResponseId;

		var span = document.createElement("span");
		span.setAttribute("class", garglResponseFieldElementClass);
				
		span.innerHTML = '<label for="' + fieldNameId + '">Response Field Name: </label>';
		span.innerHTML += '<input id="' + fieldNameId + '" type="text" value="' + responseName + '"/>';
		span.innerHTML += '<br />';
		span.innerHTML += '<label for="' + fieldSelectorId + '">CSS Selector: </label>';
		span.innerHTML += '<input id="' + fieldSelectorId + '" type="text" value="' + responseSelector + '" />';
		span.innerHTML += '<input id="' + fieldSelectorTestId + '" type="button" value="Test Selector" />'
		span.innerHTML += '<br /><br />';

		responseFieldsArea.appendChild(span);

		document.querySelector("#"+fieldSelectorTestId).onclick = function() {
			var idNumber = document.querySelector(garglEditFormFunctionIdNumberSelector).value;
			var selectorString = document.querySelector("#"+fieldSelectorId).value;

			var garglItem = garglItems[idNumber];
			
			if(garglItem.getContent) {
				garglItem.getContent(function(content, encoding) {
					try {
						content = content.replace(/\n|\t/g,"")
						
						var holder = document.createElement("span");
						holder.innerHTML = content;

						var matches = holder.querySelectorAll(selectorString);

						if(matches.length > 0) {
							var matchString = "Inner HTML of matching elements:\n\n"
							
							for(var i = 0; i < matches.length; i ++) {
								matchString += (matches[i].innerHTML + "\n\n")
							}

							alert(matchString);
						}
						else alert("No matching elements found");
					}
					catch(e) {
						alert(e);
					}
				});
			}
			else {
				alert(garglFileGetResponseError);
			}
		};

		document.querySelector("#"+fieldNameId).focus();
	}

	function viewResponse() {
		var idNumber = document.querySelector(garglEditFormFunctionIdNumberSelector).value;
		var garglItem = garglItems[idNumber];
			
		if(garglItem.getContent) {
			garglItem.getContent(function(content, encoding) {
				window.URL = window.webkitURL || window.URL;

				var prevLink = document.querySelector('a');
				if (prevLink) window.URL.revokeObjectURL(prevLink.href);

				var fileContents = content;
				var bb = new Blob([fileContents], {type: 'text/plain'});

				var a = prevLink || document.createElement('a');
				a.download = "response.txt";
				a.href = window.URL.createObjectURL(bb);
				a.textContent = 'Click to download';

				a.dataset.downloadurl = ['text/plain', a.download, a.href].join(':');

				document.querySelector(garglViewResponseHolderSelector).appendChild(a);
				
				a.onclick = function(e) {
					if ('disabled' in this.dataset) return false;
					cleanUpDownloadLink(this);
				};
			});
		}
		else {
			alert(garglFileGetResponseError);
		}					
	}

	function grabResponseFieldFormData() {
		var fieldData = [];
		var fieldElements = document.querySelectorAll("." + garglResponseFieldElementClass);

		for(var i = 0; i < fieldElements.length; i ++) {
			var fieldInputs = fieldElements[i].querySelectorAll("input");

			var field = {
				name: fieldInputs[0].value.replace(/ /g,"-"),
				cssSelector: fieldInputs[1].value
			};

			if(field.name.length > 0 && field.cssSelector.length > 0) fieldData.push(field);
		}

		return fieldData;
	}
	
	window.addEventListener('load', function() {
		document.querySelector(garglRecordSelector).onclick = toggleRecord;
		document.querySelector(garglClearSelector).onclick = clearGarglItemsTable;
		document.querySelector(garglSaveSelector).onclick = createDownloadLink;
		document.querySelector(garglNewSelector).onclick = startGargl;
		document.querySelector(garglExistingSelector).onclick = showOpenForm;
		document.querySelector(garglEditSaveSelector).onclick = saveEditGarglItem;
		document.querySelector(garglEditCancelSelector).onclick = cancelEditGarglItem;
		document.querySelector(garglNewResponseFieldSelector).onclick = addResponseField;
		document.querySelector(garglViewResponseSelector).onclick = viewResponse;

		document.querySelector(garglOpenSelector).onchange = processFile;

		document.querySelector(garglRecordAreaSelector).style.display = "none";
		document.querySelector(garglOpenFormSelector).style.display = "none";
		document.querySelector(garglEditFormSelector).style.display = "none";
	});
	
	chrome.devtools.network.onRequestFinished.addListener(trackRequest);
})();