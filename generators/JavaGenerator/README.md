# Java Gargl Generator

### Overview

This is a Gargl generator implemented in Java.

### Use

The Java Gargl Generator comes as a runnable jar. To run the Java Gargl Generator from the command line:

	java -jar bld\GarglJavaGenerator.jar -outdir some\output\location -input someGarglFile.gtf -lang someLanguage 

Current possible values for lang (language) are:
- java (Java)
- javascript (Browser, Windows 8 app, and Node.js compatible JavaScript)
- powershell (PowerShell)

### Building Source

To build the code, use the `maven` build tool. Start by running the following command:

    mvn compile

This will make sure that the code builds and will download any dependencies you need. After this, you may start working with the code.

To create an executable binary, type the following command:
    
    mvn package
    
The resulting jar should be in `target/JavaGenerator-1.0-SNAPSHOT.jar`

All of these commands must be executed in the `JavaGenerator` directory. A pre-compiled jar of this generator can be found [here](bld).
