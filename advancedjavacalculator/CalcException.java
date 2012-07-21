// This file is distributed under the Feel free to use it or add anything to it license.
// You are permitted to:
// - Use the software
// - Add anything to the software
//

package advancedjavacalculator;

public class CalcException extends Exception {

	private static final long serialVersionUID = 1L;

	public CalcException(String part, String error, String source, int line, int column) {
		super(String.format("%s: %s %d:%d => %s", part, source, line, column + 1, error));
	}

	public CalcException(String part, String error) {
		super(String.format("%s: %s", part, error));
	}

	public CalcException(String part, String error, Token token) {
		this(part, error, token.getSource(), token.getLine(), token.getColumn());
	}

	public CalcException(String part, String error, Object... args) {
		this(part, String.format(error, args));
	}

}
