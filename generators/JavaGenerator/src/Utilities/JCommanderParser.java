package Utilities;

import com.beust.jcommander.Parameter;

public class JCommanderParser {
	 
	  @Parameter(names = { "-i", "-input" }, description = "Name of gargl template file to generate a module from")
	  public String inputFilename;
	 
	  @Parameter(names = {"-l", "-lang"}, description = "String identifying which language the generated module should be composed of. Valid values are: java")
	  public String language;
	  
	  @Parameter(names = {"-o", "-outdir"}, description = "Name of output directory for created module")
	  public String outputDirectory;
	  
	  @Parameter(names = {"--help", "/?"}, description = "Print usage")
	  public boolean help = false;
}
