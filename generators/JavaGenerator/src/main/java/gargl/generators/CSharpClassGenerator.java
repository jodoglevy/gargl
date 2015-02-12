package gargl.generators;

import gargl.typedefinitions.Function;
import gargl.utilities.Parameter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.List;
import java.util.Arrays;

public class CSharpClassGenerator extends Generator {

	private static String CSHARP_CLASS_FORMAT = "using System;\n"
		+ "using System.Net;\n" 
		+ "using System.IO;\n\n" 
		+ "public class %1$s \n"
		+ "{\n"
		+ "%2$s\n"
		+ "}";
	private static String CSHARP_METHOD_FORMAT = "public string %1$s \n"
		+ "{\n"
		+ "%2$s \n"
		+ "}\n\n";
	private static String CSHARP_METHOD_SIGNATURE = "%1$s (%2$s)";
	private static String CSHARP_HEADER_FORMAT = "req.Headers.Add(%1$s);\n";
	private static String CSHARP_HEADER_FORMAT_FIX = "req.%1$s = \"%2$s\";\n";
	private static String CSHARP_HTTP_CONNECTION_FORMAT = "string url = %1$s;\n"
		+ "HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url);\n"
		+ "req.AutomaticDecompression = DecompressionMethods.GZip | DecompressionMethods.Deflate;\n\n"

		+ "%2$s\n" 

		+ "HttpWebResponse resp = (HttpWebResponse)req.GetResponse();\n"
		+ "int responseCode = (int)resp.StatusCode;\n"
		+ "Console.WriteLine(\"Sending 'GET' request to URL : \" + url);\n"
		+ "Console.WriteLine(\"Response Code : \" + responseCode);\n\n" 
		
		+ "string response;\n" 
		+ "using(StreamReader input = new StreamReader(resp.GetResponseStream()))\n"
		+ "{\n" 
		+ 	"response = input.ReadToEnd();\n"
		+ "}\n\n"

		+"Console.WriteLine(response.ToString());\n\n"
		+ "return response.ToString();";

	private static String CSHARP_POST_REQUEST_FORMAT = "string postData = \"%1$s\";\n"
		+ "using(StreamWriter writer = new StreamWriter(req.GetRequestStream()))\n"
		+ "{\n"
		+ 	"writer.Write(postData);\n"
		+ 	"writer.Flush();\n"
		+ "}\n";

	private static List<String> CSHARP_RESTRICTED_HEADERS = Arrays.asList(
		"Accept",
		"Connection",
		"Content-Length",
		"Content-Type",
		"Date",
		"Expect",
		"Host",
		"If-Modified-Since",
		"Keep-Alive",
		"Proxy-Connection",
		"Range",
		"Referer",
		"Transfer-Encoding",
		"User-Agent"
	);
			
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
			methodAndHeaders += String.format(CSHARP_POST_REQUEST_FORMAT, postDataString);
		} else if ("GET".equals(function.getMethod())) {
			// Currently there is no special behavior for GET requests
		}

		// Insert connection specific properties into connection format
		// string
		String fullMethodBody = String.format(CSHARP_HTTP_CONNECTION_FORMAT, url,
				methodAndHeaders);

		// Insert full method body in method format string
		return String.format(CSHARP_METHOD_FORMAT, methodSignature, fullMethodBody);
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
		writeFile(outputLocation + filename + ".cs",
				String.format(CSHARP_CLASS_FORMAT, filename, methodsSB.toString()));
	}

	private String generateMethodAndHeaders(Function function) {
		StringBuilder sb = new StringBuilder();

		// Set request method
		sb.append("req.Method = \"" + function.getMethod() + "\";\n\n");

		// Set request headers
		sb.append(generateHeaders(function) + "\n");

		return sb.toString();
	}
 
	private String generateHeaders(Function function) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> header : function.getHeaders().entrySet()) {
			StringBuilder headerSB = new StringBuilder("\"" + header.getKey() + "\", ");
			headerSB.append(Parameter.processParameter(header.getValue(), function));

			// derived from http://stackoverflow.com/a/23070923
			// sets headers differently if they're restricted (See CSHARP_RESTRICTED_HEADERS)
			if(CSHARP_RESTRICTED_HEADERS.contains(header.getKey()))
			{
				// of course, connection is another special header... 
				if(header.getKey().equals("Connection") && header.getValue().equals("keep-alive"))
				{
					sb.append("req.KeepAlive = true;\n");
				}
				else
				{
					sb.append(String.format(CSHARP_HEADER_FORMAT_FIX, header.getKey().replace("-", ""), header.getValue()));
				}
			}
			else
			{
				sb.append(String.format(CSHARP_HEADER_FORMAT, headerSB.toString()));
			}
		}

		return sb.toString();
	}

	String generateMethodSignature(Function function) {
		StringBuilder sb = new StringBuilder();
		if (function.getParameters().size() > 0) {
			for (Parameter param : function.getParameters()) {
				sb.append("string " + param.getParameterName() + ",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		return String.format(CSHARP_METHOD_SIGNATURE, function.getFunctionName(), sb.toString());
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
