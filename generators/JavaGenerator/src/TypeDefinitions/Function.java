package TypeDefinitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import Utilities.JsonUtils;
import Utilities.Parameter;

public class Function {

	private String functionName;
	private List<Parameter> parameters;
	private Map<String, String> headers;
	private String url;
	private String httpVersion;
	private Map<String, String> queryString;
	private String method;
	private Map<String, String> postData;

	public Function(JsonObject jsonRequest) {
		parameters = new ArrayList<Parameter>();
		headers = new HashMap<String, String>();
		queryString = new HashMap<String, String>();
		postData = new HashMap<String, String>();

		// Set up Request object with all properties from JSON request
		this.setFunctionName(jsonRequest);
		this.setRequestMethod(jsonRequest);
		this.setHeaders(JsonUtils.findElement(jsonRequest, "headers"));
		this.setUrl(JsonUtils.findElement(jsonRequest, "url"));
		this.setQueryString(JsonUtils.findElement(jsonRequest, "queryString"));
		this.setPostData(JsonUtils.findElement(jsonRequest, "postData"));
	}

	private void setFunctionName(JsonObject jsonRequest) {
		JsonElement functionName = jsonRequest.get("functionName");
		if (functionName != null) {
			this.functionName = functionName.getAsString();
		} else {
			System.out.println("ERROR: Request object did not contain function name");
		}
	}

	private void setHeaders(JsonElement headers) {
		JsonArray array_headers = JsonUtils.asJsonArray(headers);
		if (array_headers != null) {
			for (JsonElement jHeader : array_headers) {
				JsonObject header = JsonUtils.asJsonObject(jHeader);
				String name = header.get("name").getAsString();
				String value = header.get("value").getAsString();
				this.addHeader(name, value);
				if (Parameter.isParameter(value)) {
					this.addParameter(new Parameter(value));
				}
			}
		}
	}

	private void setPostData(JsonElement jsonPostData) {
		JsonArray array_postDataParams = null;
		if(jsonPostData != null) array_postDataParams = JsonUtils.asJsonArray(jsonPostData);
		
		if (array_postDataParams != null) {
			for (JsonElement postParam : array_postDataParams) {
				JsonObject param = JsonUtils.asJsonObject(postParam);
				String name = param.get("name").getAsString();
				String value = param.get("value").getAsString();
				this.addPostData(name, value);
				if (Parameter.isParameter(value)) {
					this.addParameter(new Parameter(value));
				}
			}
		} else {
			// Log if postData is empty for POST request (it shouldn't be)
			if ("POST".equals(this.getMethod())) {
				System.out.println("WARNING: POST request to " + this.getUrl()
						+ " contains no postData");
			}
		}
	}

	private void setQueryString(JsonElement queryStringParams) {
		JsonArray array_params = JsonUtils.asJsonArray(queryStringParams);
		if (array_params != null) {
			for (JsonElement jQueryParam : array_params) {
				JsonObject queryParam = JsonUtils.asJsonObject(jQueryParam);
				String name = queryParam.get("name").getAsString();
				String value = queryParam.get("value").getAsString();
				this.addQueryStringParam(name, value);
				if (Parameter.isParameter(value)) {
					this.addParameter(new Parameter(value));
				}
			}
		}
	}

	private void setRequestMethod(JsonObject jsonRequest) {
		String method = JsonUtils.findElement(jsonRequest, "method").getAsString();
		if (method != null) {
			this.method = method;
		} else {
			System.out.println("WARNING: Request did not contain method");
		}
	}

	private void setUrl(JsonElement jsonRequest) {
		if (jsonRequest != null) {
			String url = jsonRequest.getAsString();
			String[] urlParts = url.split("@");
			
			for(int i = 0; i < urlParts.length; i ++) {
				if(i % 2 == 1) this.addParameter( new Parameter("@" + urlParts[i] + "@"));
			}

			this.url = url;
		}
	}

	private void addParameter(Parameter param) {
		parameters.add(param);
	}

	private void addHeader(String header, String value) {
		headers.put(header, value);
	}

	private void addPostData(String name, String value) {
		this.postData.put(name, value);
	}

	private void addQueryStringParam(String name, String value) {
		this.queryString.put(name, value);
	}

	public List<Parameter> getParameters() {
		return parameters;
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

	public Map<String, String> getQueryString() {
		return queryString;
	}

	public String getUrl() {
		return url;
	}

	public void printHeaders() {
		System.out.println(headers);
	}

	private void removeHeader(String header) {
		headers.remove(header);
	}
}
