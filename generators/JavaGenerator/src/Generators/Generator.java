package Generators;

import java.io.IOException;
import java.io.PrintWriter;

import TypeDefinitions.GarglModule;

public abstract class Generator {

	protected GarglModule module;

	public Generator(GarglModule module) {
		setModule(module);
	}
	
	public Generator() {}

	/** 
	 * This method generates a file at outputLocation using the data contained in Module
	 * 
	 * @param outputLocation a String specifying the (optional) output location
	 */
	public abstract void generateClass(String outputLocation);

	public void setModule(GarglModule module) {
		this.module = module;
	}
	
	protected void writeFile(String filename, String fileContent) {
		// Create file
		try {
			PrintWriter writer = new PrintWriter(filename, "UTF-8");
			writer.print(fileContent);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("LOG: File written to " +  filename);
	}

}