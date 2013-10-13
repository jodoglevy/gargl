package Utilities;

import TypeDefinitions.Function;

public class Parameter {
	
	private String parameterName;
	
	private String type;
	
	private String description;
	
	//TODO: Add support for parameter type and description
	public Parameter(String parameterName){
		this.parameterName = parameterDecode(parameterName);
		System.out.println("Added argument: " + this.parameterName);
	}
	
	public static boolean isParameter(String param){
		return (param.startsWith("@") && param.endsWith("@"));
	}
	
	public static String parameterDecode(String paramName) {		
		return paramName.substring(1,paramName.length()-1);
	}
	
	/**
	 * @param param String that may be a parameter
	 * @param function Request that param is a member of
	 * @return a String appropriately formatted according to whether or not param is a parameter (e.g. is surrounded by quotes or isnt)
	 */
	public static String processParameter(String param, Function function){
		if(function.getParameters().contains(parameterDecode(param))){
			return parameterDecode(param);
		}
		else{
			return "\"" + param + "\"";
		}
	}

	public String getDescription() {
		return description;
	}

	public String getParameterName() {
		return parameterName;
	}

	public String getType() {
		return type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public void setType(String type) {
		this.type = type;
	}
}
