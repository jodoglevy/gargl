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
