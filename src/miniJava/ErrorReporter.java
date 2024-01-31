package miniJava;

import java.util.List;
import java.util.ArrayList;

// TODO: Note this class lacks a lot of things.
//  First of all, errors are simple strings,
//  perhaps it may be worthwhile to augment this reporter
//  with requiring line numbers.
public class ErrorReporter {
	private List<CompilerError> _errorQueue;
	
	public ErrorReporter() {
		this._errorQueue = new ArrayList<CompilerError>();
	}
	
	public boolean hasErrors() {
		// TODO: Check if errorQueue is non-empty
		return !_errorQueue.isEmpty();
	}
	
	public void outputErrors(boolean stackTrace) {
		// TODO: output all errors in the errorQueue
		for (CompilerError err : _errorQueue){
			System.out.println(err.toString(stackTrace));
		}
	}

	public void outputErrors(){
		outputErrors(false);
	}
	
	public void reportError(CompilerError e) {
		_errorQueue.add(e);
	}
}
