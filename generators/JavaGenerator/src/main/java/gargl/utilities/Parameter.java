package gargl.utilities;

import gargl.typedefinitions.Function;

import java.util.HashSet;
import java.util.Set;

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
	 * @param parameterFormatter An optional formatter used to return the parameter in a language specific structure
	 * @return a String appropriately formatted according to whether or not param is a parameter (e.g. is surrounded by quotes or isnt)
	 */
	public static String processParameter(String param, Function function, String parameterFormatter){
		return processParameterInternal(param, function, parameterFormatter);
	}
	
	/**
	 * @param param String that may be a parameter
	 * @param function Request that param is a member of
	 * @return a String appropriately formatted according to whether or not param is a parameter (e.g. is surrounded by quotes or isnt)
	 */
	public static String processParameter(String param, Function function){
		return processParameterInternal(param, function, "%1$s");
	}
	
	private static String processParameterInternal(String param, Function function, String parameterFormatter) {
		String decodedParam = parameterDecode(param); 
		boolean isParam = false;
		
		for(Parameter p : function.getParameters()) {
			if(p.getParameterName().equals(decodedParam)) {
				isParam = true;
				break;
			}
		}
		
		if(isParam) return String.format(parameterFormatter, decodedParam);
		else return "\"" + param + "\"";
	}
	
	/**
	 * @param url URL string that may contain one or more parameters
	 * @param function Request that url is a member of
	 * @return a Set of strings representing the different parameter and non-parameter parts of the url, appropriately formatted according to whether or not each item is a parameter (e.g. is surrounded by quotes or isnt). The set items are in order of how they should be concatenated.
	 */
	public static Set<String> processURLParameters(String url, Function function){
		return processURLParametersInternal(url, function, "%1$s");
	}
	
	/**
	 * @param url URL string that may contain one or more parameters
	 * @param function Request that url is a member of
	 * @param parameterFormatter An optional formatter used to return the parameter in a language specific structure
	 * @return a Set of strings representing the different parameter and non-parameter parts of the url, appropriately formatted according to whether or not each item is a parameter (e.g. is surrounded by quotes or isnt). The set items are in order of how they should be concatenated.
	 */
	public static Set<String> processURLParameters(String url, Function function, String parameterFormatter){
		return processURLParametersInternal(url, function, parameterFormatter);
	}
	
	private static Set<String> processURLParametersInternal(String url, Function function, String parameterFormatter) {
		String[] urlParts = url.split("@");
		Set<String> parts = new HashSet<String>();
		
		for(int i = 0; i < urlParts.length; i ++) {
			if(i % 2 == 0) {
				urlParts[i] = "\"" + urlParts[i] + "\"";
			}
			else {
				urlParts[i] = String.format(parameterFormatter, urlParts[i]);
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

	@Override
	public boolean equals(Object other) {
		if(other instanceof Parameter) {
			return this.getParameterName().equals(this.getParameterName());
		}
		else return false;
	}

	@Override
	public int hashCode() {
		return this.getParameterName().hashCode();
	}
}
