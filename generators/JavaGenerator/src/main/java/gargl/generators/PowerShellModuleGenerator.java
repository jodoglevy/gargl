package gargl.generators;

import gargl.typedefinitions.Function;
import gargl.utilities.Parameter;

import java.util.Set;
import java.util.List;
import java.util.Map;

public class PowerShellModuleGenerator extends Generator {

	private static String POWERSHELL_MAKE_REQUEST_FORMAT = "\tif($WebSession) {\n" +
			"\t\tInvoke-WebRequest -Uri $url -Headers $headers -Body $body -Method $method -WebSession $WebSession\n" +
			"\t}\n" +
			"\telse {\n" +
			"\t\tInvoke-WebRequest -Uri $url -Headers $headers -Body $body -Method $method -SessionVariable $SessionVariable\n" + 
			"\t\tInvoke-Expression \"`$global:$SessionVariable = `$$SessionVariable\"\n" +
			"\t}";

	private static String POWERSHELL_KEYVALUE_FORMAT = "\t\t\"%1$s\" = %2$s;\n";

	private static String POWERSHELL_REQUEST_STRING_CONCAT_FORMAT = " + \"&%1$s=\" + [System.Web.HttpUtility]::UrlEncode(%2$s)";

	private static String POWERSHELL_VARIABLE_FORMAT = "$%1$s";
	
	private static String POWERSHELL_STRING_VARIABLE_DECLARATION_FORMAT = "$%1$s = \"%2$s\"\n\n";
	private static String POWERSHELL_CODE_VARIABLE_DECLARATION_FORMAT = "\t$%1$s = %2$s\n\n";

	private static String POWERSHELL_MODULE_FORMAT = "Add-Type -AssemblyName System.Web\n\n" +
		"%1$s\n" +
		"Export-ModuleMember %2$s";
	
	private static String POWERSHELL_FUNCTION_FORMAT = "function %1$s {\n" +
				"\t%2$s\n\n" +
				"\tif(!$SessionVariable) { $SessionVariable = \"_session\" }\n\n" +
				"\t%3$s\n" +
			"}\n\n";
	
	private static String POWERSHELL_FUNCTION_NAME_FORMAT = "Invoke-%1$s%2$s";
	
	private static String POWERSHELL_PARAMETER_FORMAT = "\n\t\t[Parameter(Mandatory=$%3$s)][%1$s] $%2$s";
	
	public String generateFunction(String moduleName, Function function) {
		Set<Parameter> parameters = function.getParameters();
		StringBuilder parametersSB = new StringBuilder("param(");
		for(Parameter parameter : parameters) {
			parametersSB.append(String.format(POWERSHELL_PARAMETER_FORMAT, "string", parameter.getParameterName(), "true"));
			parametersSB.append(",");
		}
		parametersSB.append(String.format(POWERSHELL_PARAMETER_FORMAT, "string", "SessionVariable", "false") + ",");
		parametersSB.append(String.format(POWERSHELL_PARAMETER_FORMAT, "Microsoft.PowerShell.Commands.WebRequestSession", "WebSession", "false"));
		parametersSB.append("\n\t)");
		
		StringBuilder functionBodySB = new StringBuilder("");
		functionBodySB.append(String.format(POWERSHELL_STRING_VARIABLE_DECLARATION_FORMAT, "method", function.getMethod()));
		
		Map<String,String> headers = function.getHeaders();
		StringBuilder headersStringSB = new StringBuilder("@{\n");
		for(String headerKey : headers.keySet()) {
			if(headerKey.equalsIgnoreCase("Connection")) continue;
			headersStringSB.append(String.format(POWERSHELL_KEYVALUE_FORMAT, headerKey, Parameter.processParameter(headers.get(headerKey).replace("\"","`\""), function, POWERSHELL_VARIABLE_FORMAT)));
		}
		headersStringSB.append("\t}");
		functionBodySB.append(String.format(POWERSHELL_CODE_VARIABLE_DECLARATION_FORMAT, "headers", headersStringSB.toString()));
		
		Map<String,String> queryString = function.getQueryString();
		StringBuilder queryStringSB = new StringBuilder("\"?\"");
		for(String queryStringKey : queryString.keySet()) {
			queryStringSB.append(String.format(POWERSHELL_REQUEST_STRING_CONCAT_FORMAT, queryStringKey, Parameter.processParameter(queryString.get(queryStringKey).replace("\"","`\""), function, POWERSHELL_VARIABLE_FORMAT)));
		}
		functionBodySB.append(String.format(POWERSHELL_CODE_VARIABLE_DECLARATION_FORMAT, "queryString", queryStringSB.toString()));
		
		Map<String,String> postData = function.getPostData();
		StringBuilder postDataSB = new StringBuilder("@{\n");
		for(String postDataKey : postData.keySet()) {
			postDataSB.append(String.format(POWERSHELL_KEYVALUE_FORMAT, postDataKey, Parameter.processParameter(postData.get(postDataKey).replace("\"","`\""), function, POWERSHELL_VARIABLE_FORMAT)));
		}
		postDataSB.append("\t}");
		functionBodySB.append(String.format(POWERSHELL_CODE_VARIABLE_DECLARATION_FORMAT, "body", postDataSB.toString()));
		
		List<String> urlParts = Parameter.processURLParameters(function.getUrl(), function, POWERSHELL_VARIABLE_FORMAT);
		StringBuilder urlStringSB = new StringBuilder();
		for(String urlPart : urlParts) {
			urlStringSB.append(urlPart);
			urlStringSB.append(" + ");
		}
		urlStringSB.append("$queryString");
		functionBodySB.append(String.format(POWERSHELL_CODE_VARIABLE_DECLARATION_FORMAT, "url", urlStringSB.toString()));
		
		functionBodySB.append(String.format(POWERSHELL_MAKE_REQUEST_FORMAT, function.getMethod()));
		
		String functionName = String.format(POWERSHELL_FUNCTION_NAME_FORMAT, moduleName, function.getFunctionName());
		return String.format(POWERSHELL_FUNCTION_FORMAT, functionName, parametersSB.toString(), functionBodySB.toString());
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
		
		String moduleMembersString = generateModuleMembersDeclaration(moduleName, this.module.functions);
		
		// File name is currently forced to be the same as module name
		writeFile(outputLocation + moduleName + ".psm1",
				String.format(POWERSHELL_MODULE_FORMAT, functionsSB.toString(), moduleMembersString)
		);
	}

	private String generateModuleMembersDeclaration(String moduleName, List<Function> functions) {
		StringBuilder moduleMembersDeclarationSB = new StringBuilder();
		for (int i = 0; i < functions.size(); i ++) {
			moduleMembersDeclarationSB.append(String.format(POWERSHELL_FUNCTION_NAME_FORMAT, moduleName, functions.get(i).getFunctionName()));
			
			if(i != functions.size() - 1) moduleMembersDeclarationSB.append(", ");
		}
		
		return moduleMembersDeclarationSB.toString();
	}
}
