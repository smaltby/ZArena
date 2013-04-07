package com.github.zarena.commands;

/**
 *
 * @author Joshua
 */
public class ArgumentCountException extends Exception {
	private static final long serialVersionUID = 7160393506468466389L;
	private int errorIndex;

    public ArgumentCountException(int index) {
        super("Insufficient number of arguments for the attempted command.");
        errorIndex = index;
    }

    public int getErrorIndex() {
        return errorIndex;
    }
}
