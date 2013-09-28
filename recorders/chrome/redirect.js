function getURLParameterByName(url, name) {
  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+name+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec(url);

  if(results == null) return null;
  else return decodeURIComponent(results[1].replace(/\+/g, " "));
}

function addURLParam(url, key, value) {
    key = escape(key);
    value = escape(value);
    
    var re = new RegExp("([?|&])" + key + "=.*?(&|$)", "i"),
        separator = url.indexOf('?') !== -1 ? "&" : "?";

    if (url.match(re)) return url.replace(re, '$1' + key + "=" + value + '$2');
    else return url + separator + key + "=" + value;
}

function getInnerMostText(node) {
    if (node.nodeType == 3) {
        // Filter out text nodes that contain only whitespace
        if (!/^\s*$/.test(node.data)) {
            return node.data;
        }
    } else if (node.hasChildNodes()) {
        for (var i = 0, len = node.childNodes.length; i < len; ++i) {
            var text = getInnerMostText(node.childNodes[i]);
            if(text != null) return text;
        }
        return null;
    }
    else return null;
}

function endsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
}

function replaceOpenGraphTags() {
	var links = document.querySelectorAll('a[href*="connect/uiserver.php"]');
	
    for (var i = 0; i < links.length; i++) {
		var redirectURI = getURLParameterByName( links[i].getAttribute('href'), "redirect_uri");
		
        if (redirectURI) {
            links[i].setAttribute('href', redirectURI);
            links[i].removeAttribute('rel');
            links[i].setAttribute('target', '_blank');
        }
	}
}

function addStoryTitlesToFBAppURLs() {
    var urlsToTitles = {};
    var links = document.querySelectorAll('a[href*="apps.facebook.com"]');

    for (var i = 0; i < links.length; i++) {
        var title = getInnerMostText(links[i]);
        if(title && title.length > 2) {
            if(endsWith(title,"...")) title = title.substring(0, title.lastIndexOf("..."));
            urlsToTitles[links[i].getAttribute('href')] = title;
        }
    }

    for (var i = 0; i < links.length; i++) {
        var url = links[i].getAttribute('href');
        var title = urlsToTitles[url];

        if(title) {
            links[i].setAttribute('href', addURLParam(url,"redirectTitle",title));
            links[i].setAttribute('target', '_blank');
        }
    }
}

function checkForOpenGraphOAuth() {
    var lastFBPageAccessedString = "lastFBPageAccessed";

    var isLooping = localStorage.getItem(lastFBPageAccessedString) == document.URL;  
    localStorage.setItem(lastFBPageAccessedString, document.URL);  

    if(document.URL.indexOf("dialog/oauth") !== -1 || document.URL.indexOf("connect/uiserver") !== -1) {
        var redirectURI = getURLParameterByName( document.URL, "redirect_uri");
        var redirectTitle = getURLParameterByName(redirectURI, "redirectTitle");
        var scope = getURLParameterByName( document.URL, "scope");

        if((redirectURI && scope && scope.indexOf("publish_actions") !== -1) || redirectTitle) {        
            if(redirectURI.indexOf("apps.facebook.com") === -1 && (!isLooping)) window.location = redirectURI;
            else if(redirectTitle) window.location = "https://www.google.com/search?btnI&q=" + unescape(redirectTitle);
        }
    }
}


checkForOpenGraphOAuth();

document.addEventListener('DOMContentLoaded', function (evt) {
    replaceOpenGraphTags();
    addStoryTitlesToFBAppURLs();

    document.addEventListener('DOMSubtreeModified', replaceOpenGraphTags, false);
    document.addEventListener('DOMSubtreeModified', addStoryTitlesToFBAppURLs, false);

}, false);
