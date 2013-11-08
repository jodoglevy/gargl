package Generators;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import TypeDefinitions.GarglModule;
import TypeDefinitions.Function;
import Utilities.Parameter;

public class Windows8JavascriptModuleGenerator extends Generator {

	private static final String JAVASCRIPT_REQUEST_STRING_CONCAT_FORMAT = " + \"%1$s\" + \"=\" + encodeURIComponent(%2$s) + \"&\"";
	
	private static String JAVASCRIPT_STRING_VARIABLE_DECLARATION_FORMAT = "\t\tvar %1$s = '%2$s';\n\n";
	private static String JAVASCRIPT_CODE_VARIABLE_DECLARATION_FORMAT = "\t\tvar %1$s = %2$s;\n\n";

	private static String JAVASCRIPT_KEYVALUE_FORMAT = "\t\t\t\"%1$s\": %2$s,\n";
	private static String JAVASCRIPT_KEYVALUE_FORMAT2 = "\n\t\t\t\t%1$s: %2$s,";
	
	private static String JAVASCRIPT_MODULE_FORMAT = "(function () {\n\t" +
		"\"use strict\";" +
		"%1$s \n" +
		"})();";
	
	private static String JAVASCRIPT_NAMESPACE_FUNCTION_FORMAT = "\n\t\t%1$s: %1$s,";
	
	private static String JAVASCRIPT_NAMESPACE_FORMAT = "\n\tWinJS.Namespace.define('%1$s', {" + 
		"%2$s\n\t" +
		"});\n";
	
	private static String JAVASCRIPT_FUNCTION_FORMAT = "\n\n\tfunction %1$s %2$s {\n%3$s" + 
		"\n\t};\n";
	
	private static String JAVASCRIPT_GET_RESPONSE_FIELD = "\t\t\t\tvar _%1$s = holder.querySelectorAll('%2$s');\n"; 
	
	private static String JAVASCRIPT_XHR_FORMAT = "\t\tWinJS.xhr({\n" + 
        "\t\t\ttype: type,\n" +
        "\t\t\turl: url,\n" +
        "\t\t\theaders: headers,\n" +
        "\t\t\tdata: data,\n" +
        "\t\t})\n" + 
        "\t\t.then(\n" +
            "\t\t\tfunction (response) {\n" + 
            	"\t\t\t\tvar holder = document.createElement('span');\n" +
            	"\t\t\t\tholder.innerHTML = toStaticHTML(response.responseText) || '';\n\n" +
            	"%1$s\n" +
            	"\t\t\t\tvar fullResponse = {\n" +
		            "\t\t\t\t\tresponse: response," +
		            "%2$s\n" +
		            "\t\t\t\t};\n\n" +
                "\t\t\t\tcallback(null, fullResponse);\n\n" +
           "\t\t\t},\n" + 
           "\t\t\tfunction (err) {\n" + 
               "\t\t\t\treturn callback(err);\n" + 
           "\t\t\t}\n" +
        "\t\t);";
	
	public Windows8JavascriptModuleGenerator(GarglModule module){
		super(module);
	}
	
	public String generateFunction(Function function) {
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
		headersStringSB.append("\t\t}");
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
		
		return String.format(JAVASCRIPT_FUNCTION_FORMAT, function.getFunctionName(), parametersSB.toString(), functionBodySB.toString());
	}

	@Override
	public void generateClass(String outputLocation) {
		StringBuilder functionsSB = new StringBuilder();

		for (Function function : this.module.functions) {
			functionsSB.append(generateFunction(function));
			System.out.println("LOG: Function " + function.getFunctionName() + " created");
		}

		String moduleName = this.module.name;
		if(this.module.name == null || this.module.name.isEmpty()){
			moduleName = "gargl_output";
			System.out.println("WARNING: No module name given, module name defaulting to " + moduleName);
		}
		
		functionsSB.append(generateNamespaceDefinition(moduleName, this.module.functions));
		System.out.println("LOG: Namespace Definition created");
		
		// File name is currently forced to be the same as module name
		writeFile(outputLocation + moduleName + ".js",
				String.format(JAVASCRIPT_MODULE_FORMAT, functionsSB.toString()));
	}

	private String generateNamespaceDefinition(String moduleName, List<Function> functions) {
		StringBuilder namespaceSB = new StringBuilder();
		for (Function function : functions) {
			namespaceSB.append(String.format(JAVASCRIPT_NAMESPACE_FUNCTION_FORMAT, function.getFunctionName()));
		}
		
		return String.format(JAVASCRIPT_NAMESPACE_FORMAT, moduleName, namespaceSB.toString());
	}
}
