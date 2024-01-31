package miniJava;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CompilerError extends Error {
    public String message;
    public String file;
    public int line;
    private StackTraceElement[] trace;

    public CompilerError(String message){
        this.message = message;
        trace = this.getStackTrace();
        this.line = trace[0].getLineNumber();
        this.file = trace[0].getFileName();
    }
    public CompilerError(String message, String file, int line){
        this.message = message;
        this.file = file;
        this.line = line;
    }

    public String toString(){
        return this.toString(false);
    }

    public String toString(boolean printStack){
        String out = this.file + ":" + this.line + ":: " + this.message;
        if (printStack){
            for (StackTraceElement e : trace){
                out += "\n" + e;
            }
        }
        return out;
    }
}