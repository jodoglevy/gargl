package Utilities;

import TypeDefinitions.Request;

public class Utils {
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
	public static String processParameter(String param, Request request){
		if(request.getArgs().contains(Utils.parameterDecode(param))){
			return Utils.parameterDecode(param);
		}
		else{
			return "\"" + param + "\"";
		}
	}
}
