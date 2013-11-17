package Generators;

import java.util.Map;
import java.util.Map.Entry;
import TypeDefinitions.Function;
import Utilities.Parameter;

public class JavaClassGenerator extends Generator {

	private static String JAVA_CLASS_FORMAT = "import java.io.BufferedReader;\n" + 
			"import java.io.DataOutputStream;\n" +
			"import java.io.InputStreamReader;\n" + 
			"import java.net.HttpURLConnection;\n" +
			"import java.io.InputStream;\n" +
			"import java.util.zip.GZIPInputStream;\n" +
			"import java.net.URL;\n\n" + 
			"public class %1$s \n{\n %2$s \n}";
	private static String JAVA_METHOD_FORMAT = "public String %1$s throws Exception \n{\n %2$s \n}\n\n";
	private static String JAVA_METHOD_SIGNATURE = "%1$s (%2$s)";
	private static String JAVA_HEADER_FORMAT = "con.setRequestProperty(%1$s);\n";
	private static String JAVA_HTTP_CONNECTION_FORMAT = "String url = %1$s;\n"
			+ "URL obj = new URL(url);\n"
			+ "HttpURLConnection con = (HttpURLConnection) obj.openConnection();\n\n"

			+ "%2$s\n" +

			"int responseCode = con.getResponseCode();\n"
			+ "System.out.println(\"Sending 'GET' request to URL : \" + url);\n"
			+ "System.out.println(\"Response Code : \" + responseCode);\n\n" +
			"InputStream input = con.getInputStream()\n;" +
			"if (\"gzip\".equals(con.getContentEncoding())) {\n" +
					"input = new GZIPInputStream(input);\n" +
			"}\n\n"+

			"BufferedReader in = new BufferedReader(new InputStreamReader(input));\n"
			+ "String inputLine;\n" + "StringBuffer response = new StringBuffer();\n\n" +

			"while ((inputLine = in.readLine()) != null) {" + "	response.append(inputLine);\n"
			+ "}\n\n" +

			"in.close();\n" + "System.out.println(response.toString());\n"
			+ "return response.toString();";

	private static String JAVA_POST_REQUEST_FORMAT = "String postData = \"%1$s\";\n"
			+ "con.setDoOutput(true);\n\n"
			+ "DataOutputStream wr = new DataOutputStream(con.getOutputStream());\n"
			+ "wr.writeBytes(postData);\n" + "wr.flush();\n" + "wr.close();\n";

	public String generateFunction(Function function) {
		// Generate method signature using all necessary parameters
		String methodSignature = generateMethodSignature(function);

		// Generate strings for setting request method and headers
		String methodAndHeaders = generateMethodAndHeaders(function);

		// Generate querystring by appending url params and applying
		// formatting
		String queryString = generateQueryString(function);

		// Surround request.getUrl() with quotes if it is a parameter, and
		// do nothing if it is a parameter name
		String url = Parameter.processParameter(function.getUrl(), function) + " + \"?" + queryString
				+ "\"";

		// Handle specifics for each type of request
		if ("POST".equals(function.getMethod())) {
			String postDataString = generatePostDataString(function.getPostData());
			methodAndHeaders += String.format(JAVA_POST_REQUEST_FORMAT, postDataString);
		} else if ("GET".equals(function.getMethod())) {
			// Currently there is no special behavior for GET requests
		}

		// Insert connection specific properties into connection format
		// string
		String fullMethodBody = String.format(JAVA_HTTP_CONNECTION_FORMAT, url,
				methodAndHeaders);

		// Insert full method body in method format string
		return String.format(JAVA_METHOD_FORMAT, methodSignature, fullMethodBody);
	}

	@Override
	public void generateClass(String outputLocation) {
		StringBuilder methodsSB = new StringBuilder();

		for (Function request : this.module.functions) {
			methodsSB.append(generateFunction(request));
			System.out.println("LOG: Function " + request.getFunctionName() + " created");
		}

		String filename = this.module.name;
		if(this.module.name == null || this.module.name.isEmpty()){
			filename = "gargl_output";
			System.out.println("WARNING: No module name given, filename defaulting to " + filename);
		}		
		
		// File name is currently forced to be the same as module name
		writeFile(outputLocation + filename + ".java",
				String.format(JAVA_CLASS_FORMAT, filename, methodsSB.toString()));
	}

	private String generateMethodAndHeaders(Function function) {
		StringBuilder sb = new StringBuilder();

		// Set request method
		sb.append("con.setRequestMethod(\"" + function.getMethod() + "\");\n\n");

		// Set request headers
		sb.append(generateHeaders(function) + "\n");

		return sb.toString();
	}

	private String generateHeaders(Function function) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> header : function.getHeaders().entrySet()) {
			StringBuilder headerSB = new StringBuilder("\"" + header.getKey() + "\", ");
			headerSB.append(Parameter.processParameter(header.getValue(), function));

			// Adding 'con.setRequestProperty(header, value) to overall
			// StringBuilder
			sb.append(String.format(JAVA_HEADER_FORMAT, headerSB.toString()));
		}

		return sb.toString();
	}

	String generateMethodSignature(Function function) {
		StringBuilder sb = new StringBuilder();
		if (function.getParameters().size() > 0) {
			for (Parameter param : function.getParameters()) {
				sb.append("String " + param.getParameterName() + ",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		return String.format(JAVA_METHOD_SIGNATURE, function.getFunctionName(), sb.toString());
	}

	private String generatePostDataString(Map<String, String> postData) {
		StringBuilder sb = new StringBuilder();
		if (postData != null) {
			for (Entry<String, String> param : postData.entrySet()) {
				if (Parameter.isParameter(param.getValue())) {
					String paramName = Parameter.parameterDecode(param.getValue());
					sb.append(param.getKey() + "=\" + " + paramName + " + \"&");
				} else {
					sb.append(param.getKey() + "=" + param.getValue() + "&");
				}
			}

			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			
		} else {
			System.out.println("POST request generated without postData");
		}

		return sb.toString();
	}

	private String generateQueryString(Function function) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> param : function.getQueryString().entrySet()) {
			if (Parameter.isParameter(param.getValue())) {
				String paramName = Parameter.parameterDecode(param.getValue());
				sb.append(param.getKey() + "=\" + " + paramName + " + \"&");
			} else {
				sb.append(param.getKey() + "=" + param.getValue() + "&");
			}
		}

		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}
}
