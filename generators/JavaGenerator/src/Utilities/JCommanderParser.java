package Utilities;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class JCommanderParser {
	 
	  @Parameter(names = { "-i", "-input" }, description = "Name of input file")
	  public String inputFilename;
	 
	  @Parameter(names = {"-l", "-lang"}, description = "String identifying which language to generate requests in")
	  public String language;
	  
	  @Parameter(names = {"-o", "-outdir"}, description = "Name of output directory")
	  public String outputDirectory;
	 
	  @Parameter(names = "-debug", description = "Debug mode")
	  public boolean debug = false;
}
