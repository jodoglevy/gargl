package Generators;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import TypeDefinitions.GarglModule;
import TypeDefinitions.Function;
import Utilities.Parameter;

public class Windows8JavascriptModuleGenerator extends Generator {

	private static String JAVASCRIPT_MODULE_FORMAT = "(function () {\n" +
			"\"use strict\";\n" +
			"%1$s \n" +
			"})();";
	
	private static String JAVASCRIPT_NAMESPACE_FUNCTION_FORMAT = "%1$ : %1$s ,";
	
	private static String JAVASCRIPT_NAMESPACE_FORMAT = "WinJS.Namespace.define('%1$s', {\n" + 
			"%2$s \n" +
			"});";
	
	public Windows8JavascriptModuleGenerator(GarglModule module){
		super(module);
	}
	
	public String generateFunction(Function function) {
		return "";
	}

	@Override
	public void generateClass(String outputLocation) {
		StringBuilder functionsSB = new StringBuilder();

		for (Function function : this.module.functions) {
			functionsSB.append(generateFunction(function));
			System.out.println("LOG: Function " + function.getFunctionName() + " created");
		}

		String moduleName = this.module.name;
		if(this.module.name == null || this.module.name.isEmpty()){
			moduleName = "gargl_output";
			System.out.println("WARNING: No module name given, module name defaulting to " + moduleName);
		}
		
		functionsSB.append(generateNamespaceDefinition(moduleName, this.module.functions));
		System.out.println("LOG: Namespace Definition created");
		
		// File name is currently forced to be the same as module name
		writeFile(outputLocation + moduleName + ".js",
				String.format(JAVASCRIPT_MODULE_FORMAT, functionsSB.toString()));
	}

	private String generateNamespaceDefinition(String moduleName, List<Function> functions) {
		StringBuilder namespaceSB = new StringBuilder();
		for (Function function : functions) {
			namespaceSB.append(String.format(JAVASCRIPT_NAMESPACE_FUNCTION_FORMAT, function.getFunctionName()));
		}
		
		return String.format(JAVASCRIPT_NAMESPACE_FORMAT, moduleName, namespaceSB.toString());
	}
}