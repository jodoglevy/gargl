package Utilities;

import TypeDefinitions.Function;

public class Parameter {
	public static String parameterDecode(String paramName) {		
		return paramName.substring(1,paramName.length()-1);
	}
	
	public static boolean isParameter(String param){
		return (param.startsWith("@") && param.endsWith("@"));
	}
	
	/**
	 * @param param String that may be a parameter
	 * @param request Request that param is a member of
	 * @return a String appropriately formatted according to whether or not param is a parameter (e.g. is surrounded by quotes or isnt)
	 */
	public static String processParameter(String param, Function request){
		if(request.getArgs().contains(Parameter.parameterDecode(param))){
			return Parameter.parameterDecode(param);
		}
		else{
			return "\"" + param + "\"";
		}
	}
}
