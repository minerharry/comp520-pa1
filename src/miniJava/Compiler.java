package miniJava;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;

public class Compiler {

	public static final boolean IS_MINI = true;

	// Main function, the file to compile will be an argument.
	public static void main(String[] args) throws FileNotFoundException{
		// TODO: Instantiate the ErrorReporter object


		// TODO: Check to make sure a file path is given in args
		File f1 = new File(args[0]);
		File[] files;
		boolean any_fail = false;
		if (f1.isDirectory()){
			files = f1.listFiles();
		} else {
			File[] files2 = {f1};
			files = files2;
		}


		for (File f : files) {
			if (files.length > 1){
				System.out.println("Compiling File: " + f.toPath().getFileName());
			}
			ErrorReporter reporter = new ErrorReporter();

			// TODO: Create the inputStream using new FileInputStream
			InputStream inputStream = new FileInputStream(f);
			
			// TODO: Instantiate the scanner with the input stream and error object
			Scanner scan = new Scanner(inputStream,reporter);
			
			// TODO: Instantiate the parser with the scanner and error object
			Parser parser = new Parser(scan, reporter);
			
			// TODO: Call the parser's parse function
			parser.parse();
			
			// TODO: Check if any errors exist, if so, println("Error")
			//  then output the errors
			if (files.length == 1){
				if (reporter.hasErrors()){
					System.out.println("Error");
					reporter.outputErrors(true);
				} else {
					System.out.println("Success");
				}
			} else {
				boolean shouldFail = f.getName().contains("fail");
				if (shouldFail != reporter.hasErrors()){
					any_fail = true;
					// System.out.println(shouldFail);
					// System.out.println(reporter.hasErrors());
					// reporter.outputErrors(true);
					System.out.println("Compiler mismatch: incorrect result for file");
				}
			}
			if (files.length > 1){
				System.out.println("---------------------------");
			}
		}
		
		if (!any_fail){
			System.out.println("All tests passed");
		}
		// TODO: If there are no errors, println("Success")
	}
}
