package gargl.generators;

import java.util.Hashtable;
import java.util.Set;

public class GeneratorFactory
{
	private static Hashtable<String, Generator> generators;

	static
	{
		generators = new Hashtable<String, Generator>();
		generators.put("java", new JavaClassGenerator());
		generators.put("javascript", new JavascriptModuleGenerator());
		generators.put("powershell", new PowerShellModuleGenerator());
	}

	public static Generator getGenerator(String generatorType)
	{
		return generators.get(generatorType.toLowerCase());
	}
	
	public static Set<String> getValidGeneratorTypes() {
		return generators.keySet();
	}
}