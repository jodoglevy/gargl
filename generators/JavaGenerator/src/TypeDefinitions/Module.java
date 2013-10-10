package TypeDefinitions;

import java.util.List;

public class Module {
	public String name;
	public String description;
	public List<Request> requests;

	public Module(String name, String description, List<Request> requests) {
		this.name = name;
		this.description = description;
		this.requests = requests;
	}
}
