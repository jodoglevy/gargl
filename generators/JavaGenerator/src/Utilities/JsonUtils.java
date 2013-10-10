package Utilities;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtils {

	/**
	 * @param element
	 *            The element to search within for a JsonElement named
	 *            elementName
	 * @param elementName
	 *            The name of the element to find
	 * @return The element named 'elementName' that is a child of 'element'
	 */
	public static JsonElement findElement(JsonElement element,
			String elementName) {
		if(element == null){
			return null;
		}
		
		Queue<JsonElement> stack = new LinkedList<JsonElement>();
		stack.add(element);
		return search(stack, elementName);
	}

	private static JsonElement search(Queue<JsonElement> queue, String elementName) {
		JsonElement ret = null;
		while (queue.size() > 0) {
			JsonElement element = queue.poll();
			if (element.isJsonObject()) {
				JsonObject object = element.getAsJsonObject();
				Set<Entry<String, JsonElement>> members = object.entrySet();
				for (Entry<String, JsonElement> member : members) {
					if (member.getKey().equals(elementName)) {
						return member.getValue();
					} else {
						queue.add(member.getValue());
					}
				}

			} else if (element.isJsonArray()) {
				JsonArray array = element.getAsJsonArray();
				for (JsonElement array_element : array) {
					queue.add(array_element);
				}
			}
		}
		
		return ret;
	}

	public static JsonObject asJsonObject(JsonElement element) {
		if (element.isJsonObject()) {
			return element.getAsJsonObject();
		} else {
			return null;
		}
	}

	public static JsonArray asJsonArray(JsonElement element) {
		if (element.isJsonArray()) {
			return element.getAsJsonArray();
		} else {
			return null;
		}
	}
}
