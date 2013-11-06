package Utilities;

import java.util.ArrayList;
import java.util.List;

import TypeDefinitions.Function;

public class Parameter {
	
	private String parameterName;
	
	private String description;
	
	//TODO: Add support for parameter description
	public Parameter(String parameterName){
		this.parameterName = parameterDecode(parameterName);
		System.out.println("Added argument: " + this.parameterName);
	}
	
	public static boolean isParameter(String param){
		return (param.startsWith("@") && param.endsWith("@"));
	}
	
	public static String parameterDecode(String paramName) {		
		try {
			return paramName.substring(1,paramName.length()-1);
		}
		catch(Exception e) {
			return "";
		}
	}
	
	/**
	 * @param param String that may be a parameter
	 * @param function Request that param is a member of
	 * @return a String appropriately formatted according to whether or not param is a parameter (e.g. is surrounded by quotes or isnt)
	 */
	public static String processParameter(String param, Function function){
		String decodedParam = parameterDecode(param); 
		for(Parameter p : function.getParameters()) {
			if(p.getParameterName().equals(decodedParam)) return decodedParam;
		}
		
		return "\"" + param + "\"";
	}
	
	/**
	 * @param url URL string that may contain one or more parameters
	 * @param function Request that url is a member of
	 * @return a List of strings representing the different parameter and non-parameter parts of the url, appropriately formatted according to whether or not each item is a parameter (e.g. is surrounded by quotes or isnt). The list items are in order of how they should be concatenated.
	 */
	public static List<String> processURLParameters(String url, Function function){
		String[] urlParts = url.split("@");
		List<String> parts = new ArrayList<String>();
		
		for(int i = 0; i < urlParts.length; i ++) {
			if(i % 2 == 0) {
				urlParts[i] = "\"" + urlParts[i] + "\"";
			}
			
			parts.add(urlParts[i]);
		}
		
		return parts;
	}

	public String getDescription() {
		return description;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
}
