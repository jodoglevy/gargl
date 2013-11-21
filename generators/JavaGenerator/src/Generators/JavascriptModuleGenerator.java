package Generators;

import java.util.List;
import java.util.Map;

import TypeDefinitions.Function;
import Utilities.Parameter;

public class JavascriptModuleGenerator extends Generator {

	private static final String JAVASCRIPT_REQUEST_STRING_CONCAT_FORMAT = " + \"%1$s\" + \"=\" + encodeURIComponent(%2$s) + \"&\"";
	
	private static String JAVASCRIPT_STRING_VARIABLE_DECLARATION_FORMAT = "\tvar %1$s = '%2$s';\n\n";
	private static String JAVASCRIPT_CODE_VARIABLE_DECLARATION_FORMAT = "\tvar %1$s = %2$s;\n\n";

	private static String JAVASCRIPT_KEYVALUE_FORMAT = "\t\t\"%1$s\": %2$s,\n";
	private static String JAVASCRIPT_KEYVALUE_FORMAT2 = "\n\t\t\t%1$s: %2$s,";
	
	private static String JAVASCRIPT_MODULE_FORMAT = "// This module requires jQuery.\n\n" +
			"try {\n" +
			"\t// Enable module to work with jQuery in Node.JS\n" +
			"\tvar $ = require('jquery').create();\n" +
			"}\n" +
			"catch(e) {}\n\n" +
			"var %1$s = {};\n\n" + 
			"%2$s\n";
	
	private static String JAVASCRIPT_FUNCTION_FORMAT = "\n%1$s.%2$s = function %3$s {\n%4$s" + 
		"\n};\n" + 
		"if(typeof(exports) != \"undefined\") exports.%2$s = %1$s.%2$s; // For nodeJS\n";
	
	private static String JAVASCRIPT_GET_RESPONSE_FIELD = "\t\t\tvar _%1$s = $html.find('%2$s');\n"; 
	
	private static String JAVASCRIPT_XHR_FORMAT = "\t$.ajax({\n" + 
        "\t\ttype: type,\n" +
        "\t\turl: url,\n" +
        "\t\theaders: headers,\n" +
        "\t\tdata: data,\n" +
        "\t})\n" + 
        "\t.always(\n" +
            "\t\tfunction (response, error) {\n" + 
            	"\t\t\tresponse = response || '';\n\n" +
            	"\t\t\ttry {\n" +
            	"\t\t\t\tvar $html = $(toStaticHTML(response));\n" +
            	"\t\t\t}\n" +
            	"\t\t\tcatch(e) {\n" +
            	"\t\t\t\tvar $html = $(response);\n" +
            	"\t\t\t}\n\n" +
            	"%1$s\n" +
            	"\t\t\tvar fullResponse = {\n" +
		            "\t\t\t\tresponse: response," +
		            "%2$s\n" +
		            "\t\t\t};\n\n" +
                "\t\t\tcallback(null, fullResponse);\n\n" +
           "\t\t}\n" +
        "\t);";
	
	public String generateFunction(String moduleName, Function function) {
		StringBuilder parametersSB = new StringBuilder("(");
		for(Parameter parameter : function.getParameters()) {
			parametersSB.append(parameter.getParameterName());
			parametersSB.append(", ");
		}
		parametersSB.append("callback)");
		
		StringBuilder functionBodySB = new StringBuilder("");
		functionBodySB.append(String.format(JAVASCRIPT_STRING_VARIABLE_DECLARATION_FORMAT, "type", function.getMethod()));
		
		Map<String,String> headers = function.getHeaders();
		StringBuilder headersStringSB = new StringBuilder("{\n");
		for(String headerKey : headers.keySet()) {
			headersStringSB.append(String.format(JAVASCRIPT_KEYVALUE_FORMAT, headerKey, Parameter.processParameter(headers.get(headerKey).replace("\"","\\\""), function)));
		}
		headersStringSB.append("\t}");
		functionBodySB.append(String.format(JAVASCRIPT_CODE_VARIABLE_DECLARATION_FORMAT, "headers", headersStringSB.toString()));
		
		Map<String,String> queryString = function.getQueryString();
		StringBuilder queryStringSB = new StringBuilder("\"?\"");
		for(String queryStringKey : queryString.keySet()) {
			queryStringSB.append(String.format(JAVASCRIPT_REQUEST_STRING_CONCAT_FORMAT, queryStringKey, Parameter.processParameter(queryString.get(queryStringKey).replace("\"","\\\""), function)));
		}
		functionBodySB.append(String.format(JAVASCRIPT_CODE_VARIABLE_DECLARATION_FORMAT, "queryString", queryStringSB.toString()));
		
		Map<String,String> postData = function.getPostData();
		StringBuilder postDataSB = new StringBuilder("\"\"");
		for(String postDataKey : postData.keySet()) {
			postDataSB.append(String.format(JAVASCRIPT_REQUEST_STRING_CONCAT_FORMAT, postDataKey, Parameter.processParameter(postData.get(postDataKey).replace("\"","\\\""), function)));
		}
		functionBodySB.append(String.format(JAVASCRIPT_CODE_VARIABLE_DECLARATION_FORMAT, "data", postDataSB.toString()));
		
		List<String> urlParts = Parameter.processURLParameters(function.getUrl(), function);
		StringBuilder urlStringSB = new StringBuilder();
		for(String urlPart : urlParts) {
			urlStringSB.append(urlPart);
			urlStringSB.append(" + ");
		}
		urlStringSB.append("queryString");
		functionBodySB.append(String.format(JAVASCRIPT_CODE_VARIABLE_DECLARATION_FORMAT, "url", urlStringSB.toString()));
		
		StringBuilder responseFieldsGrabSB = new StringBuilder();
		StringBuilder responseFieldsSB = new StringBuilder();
		Map<String,String> responseFields = function.getResponseFields();
		for(String responseName : responseFields.keySet()) {
			responseFieldsGrabSB.append(String.format(JAVASCRIPT_GET_RESPONSE_FIELD, responseName, responseFields.get(responseName)));
			responseFieldsSB.append(String.format(JAVASCRIPT_KEYVALUE_FORMAT2, "\t" + responseName, "_" + responseName));
		}
		
		functionBodySB.append(String.format(JAVASCRIPT_XHR_FORMAT, responseFieldsGrabSB.toString(), responseFieldsSB.toString()));
		
		return String.format(JAVASCRIPT_FUNCTION_FORMAT, moduleName, function.getFunctionName(), parametersSB.toString(), functionBodySB.toString());
	}

	@Override
	public void generateClass(String outputLocation) {
		String moduleName = this.module.name;
		if(this.module.name == null || this.module.name.isEmpty()){
			moduleName = "gargl_output";
			System.out.println("WARNING: No module name given, module name defaulting to " + moduleName);
		}
		
		StringBuilder functionsSB = new StringBuilder();

		for (Function function : this.module.functions) {
			functionsSB.append(generateFunction(moduleName, function));
			System.out.println("LOG: Function " + function.getFunctionName() + " created");
		}
		
		// File name is currently forced to be the same as module name
		writeFile(outputLocation + moduleName + ".js",
				String.format(JAVASCRIPT_MODULE_FORMAT, moduleName, functionsSB.toString()));
	}
}
