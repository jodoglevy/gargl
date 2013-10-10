package TypeDefinitions;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

	private String functionName;
	private List<String> args;
	private Map<String, String> headers;
	private String url;
	private String httpVersion;
	private Map<String, String> queryString;
	private Cookie cookie;
	private String method;
	private Map<String, String> postData;


	public Request() {
		args = new ArrayList<String>();
		headers = new HashMap<String, String>();
		queryString = new HashMap<String,String>();
		postData = new HashMap<String, String>();
	}

	public void addArgument(String argument) {
		args.add(argument);
	}

	public void addHeader(String header, String value) {
		headers.put(header, value);
	}

	public void addPostData(String name, String value) {
		this.postData.put(name, value);
	}

	public void addQueryStringParam(String name, String value) {
		this.queryString.put(name, value);
	}

	public List<String> getArgs() {
		return args;
	}

	public Cookie getCookie() {
		return cookie;
	}

	public String getFunctionName() {
		return functionName;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getHttpVersion() {
		return httpVersion;
	}

	public String getMethod() {
		return method;
	}

	public Map<String, String> getPostData() {
		return postData;
	}

	public Map<String, String>getQueryString() {
		return queryString;
	}

	public String getUrl() {
		return url;
	}

	public void printHeaders() {
		System.out.println(headers);
	}

	public void removeHeader(String header) {
		headers.remove(header);
	}

	public void setCookie(Cookie cookie) {
		this.cookie = cookie;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	
	public void setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
