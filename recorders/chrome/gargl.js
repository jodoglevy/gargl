var garglPanel = null;
var shouldRecord = false;

chrome.devtools.panels.create("Gargl", "icon.png", "gargl.html", function(panel) {
	gagrPanel = panel;
	
	$("#gargl")
});

chrome.devtools.network.onRequestFinished.addListener(function(request) {
	if(shouldRecord) {
	
	}
});