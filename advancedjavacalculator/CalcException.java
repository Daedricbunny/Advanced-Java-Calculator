// This file is distributed under the Feel free to use it or add anything to it license.
// You are permitted to:
// - Use the software
// - Add anything to the software
//

package advancedjavacalculator;

public class CalcException extends Exception {

	private static final long serialVersionUID = 1L;

	public CalcException(String part, String error) {
		super(String.format("%s: %s", part, error));
	}

	public CalcException(String part, String error, Object... args) {
		this(part, String.format(error, args));
	}

}
