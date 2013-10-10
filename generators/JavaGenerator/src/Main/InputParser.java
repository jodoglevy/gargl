package Main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Converters.JsonRequestToRequest;
import TypeDefinitions.Module;
import TypeDefinitions.Request;
import Utilities.JsonUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author mross
 * 
 */
/**
 * @author mross
 * 
 */
public class InputParser {

	private String filename;
	private JsonRequestToRequest converter;

	/**
	 * @param filename
	 *            the input filename for the parser to read
	 */
	public InputParser(String filename) {
		this.filename = filename;
		this.converter = new JsonRequestToRequest();
	}

	/**
	 * This function reads in the file supplied in the constructor, parses the
	 * JSON, and creates a Module with all of the data necessary to generate a
	 * class
	 * 
	 * @return a Module containing all of the requests and metadata for the
	 *         given file
	 */
	public Module parseAndConvert() {
		// Initialize JSON parser
		Gson gson = new Gson();
		String output = new String();
		try {
			output = readFile(filename);
		} catch (FileNotFoundException e) {
			System.out.println("Error: " + filename
					+ " does not exist, please specify another file");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Get metadata for the Module
		List<JsonElement> requestElements = new ArrayList<JsonElement>();
		JsonObject moduleJsonObject = gson.fromJson(output, JsonObject.class);

		JsonElement moduleNameElement = moduleJsonObject.get("moduleName");
		JsonElement moduleDescriptionElement = moduleJsonObject.get("moduleDescription");

		String moduleName = "default_module_name";
		String moduleDescription = "default_module_description";

		if (moduleNameElement != null && !moduleNameElement.getAsString().isEmpty()) {
			moduleName = moduleNameElement.getAsString();
		}

		if (moduleDescriptionElement != null && !moduleDescriptionElement.getAsString().isEmpty()) {
			moduleDescription = moduleDescriptionElement.getAsString();
		}

		// Get all 'functions' as JsonObjects from the file and add them as a
		// list
		JsonElement functions = JsonUtils.findElement(moduleJsonObject, "functions");
		if (functions != null) {
			if (functions.isJsonArray()) {
				JsonArray requests_array = functions.getAsJsonArray();
				for (JsonElement requestElement : requests_array) {
					if (requestElement.isJsonObject()) {
						JsonObject request = requestElement.getAsJsonObject();
						requestElements.add(request);
					}
				}
			}
		}
		else{
			System.out.println("Error: Invalid file format. File does not contain \'functions\' element");
			System.exit(0);
		}

		// Parse all JsonObject requests into Requests
		List<Request> requests = new ArrayList<Request>();
		for (JsonElement jsonRequest : requestElements) {
			JsonObject request = JsonUtils.asJsonObject(jsonRequest);
			if (request != null) {
				if (request.get("functionName") != null
						&& !request.get("functionName").getAsString().isEmpty()) {
					// Convert JSON request to Request
					requests.add(converter.Convert(request));
				} else {
					System.out
							.println("WARNING: Function not generated because function is empty!");
				}
			}
		}

		// return Module containing metadata (e.g. name, description) and
		// Requests
		return new Module(moduleName, moduleDescription, requests);
	}

	/**
	 * @param filename
	 *            the name of a file to read
	 * @return a String with the contents of the file
	 * @throws IOException
	 */
	private static String readFile(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = null;

		StringBuilder sb = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			for (char c : line.toCharArray()) {
				sb.append(c);
			}
			sb.append("\n");
		}

		return sb.toString();
	}

}
