package gargl.typedefinitions;

import java.util.List;

public class GarglModule {
	public String name;
	public String description;
	public List<Function> functions;
	public String version;

	public GarglModule(String name, String version, String description, List<Function> functions) {
		this.name = name;
		this.description = description;
		this.functions = functions;
		this.version = version;
	}
}
