package Converters;

import TypeDefinitions.Request;
import Utilities.JsonUtils;
import Utilities.Utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonRequestToRequest {

	/**
	 * @param jsonRequest
	 *            The entire log entry as JSON (includes request, response and
	 *            metadata)
	 * @return
	 */
	public Request Convert(JsonObject jsonRequest) {

		Request request = new Request();

		// Set up Request object with all properties from JSON request
		setFunctionName(request, jsonRequest);
		setRequestMethod(request, jsonRequest);
		setHeaders(request, JsonUtils.findElement(jsonRequest, "headers"));
		setUrl(request, JsonUtils.findElement(jsonRequest, "url"));
		setQueryString(request, JsonUtils.findElement(jsonRequest, "queryString"));
		if("POST".equals(request.getMethod())){
			setPostData(request, JsonUtils.findElement(jsonRequest, "postData"));
		}

		return request;
	}

	private void setFunctionName(Request request, JsonObject jsonRequest) {
		JsonElement functionName = jsonRequest.get("functionName");
		if (functionName != null) {
			request.setFunctionName(functionName.getAsString());
		} else {
			System.out.println("ERROR: Request object did not contain function name");
		}
	}

	private void setHeaders(Request request, JsonElement headers) {
		JsonArray array_headers = JsonUtils.asJsonArray(headers);
		if (array_headers != null) {
			for (JsonElement jHeader : array_headers) {
				JsonObject header = JsonUtils.asJsonObject(jHeader);
				String name = header.get("name").getAsString();
				String value = header.get("value").getAsString();
				request.addHeader(name, value);
				if (Utils.isParameter(value)) {
					String paramName = Utils.parameterDecode(value);
					request.addArgument(paramName);
					System.out.println("Added argument: " + paramName);
				}
			}
		}
	}

	private void setPostData(Request request, JsonElement jsonPostData) {
		JsonElement paramsElement = JsonUtils.findElement(jsonPostData, "params");
		JsonArray array_postDataParams = JsonUtils.asJsonArray(paramsElement);
		if (array_postDataParams != null) {
			for (JsonElement postParam : array_postDataParams) {
				JsonObject param = JsonUtils.asJsonObject(postParam);
				String name = param.get("name").getAsString();
				String value = param.get("value").getAsString();
				request.addPostData(name, value);
				if (Utils.isParameter(value)) {
					String paramName = Utils.parameterDecode(value);
					request.addArgument(paramName);
					System.out.println("Added argument: " + paramName);
				}
			}
		} else {
			// Log if postData is empty for POST request (it shouldn't be)
			if ("POST".equals(request.getMethod())) {
				System.out.println("WARNING: POST request to " + request.getUrl()
						+ " contains no postData");
			}
		}
	}

	private void setQueryString(Request request, JsonElement queryStringParams) {
		JsonArray array_params = JsonUtils.asJsonArray(queryStringParams);
		if (array_params != null) {
			for (JsonElement jQueryParam : array_params) {
				JsonObject queryParam = JsonUtils.asJsonObject(jQueryParam);
				String name = queryParam.get("name").getAsString();
				String value = queryParam.get("value").getAsString();
				request.addQueryStringParam(name, value);
				if (Utils.isParameter(value)) {
					String paramName = Utils.parameterDecode(value);
					request.addArgument(paramName);
					System.out.println("Added argument: " + paramName);
				}
			}
		}
	}

	private void setRequestMethod(Request request, JsonObject jsonRequest) {
		String method = JsonUtils.findElement(jsonRequest, "method").getAsString();
		if (method != null) {
			request.setMethod(method);
		} else {
			System.out.println("WARNING: Request did not contain method");
		}
	}

	// Currently this will break if request is not a direct child of the Entry
	// element
	private void setUrl(Request request, JsonElement jsonRequest) {
		if (jsonRequest != null) {
			String url = jsonRequest.getAsString();
			if (Utils.isParameter(url)) {
				String paramName = Utils.parameterDecode(url);
				request.addArgument(paramName);
				System.out.println("Added argument: " + paramName);
			}

			request.setUrl(url);
		}
	}
}
