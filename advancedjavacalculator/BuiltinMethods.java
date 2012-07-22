// This file is distributed under the Feel free to use it or add anything to it license.
// You are permitted to:
// - Use the software
// - Add anything to the software
//

package advancedjavacalculator;

import java.math.BigDecimal;

import advancedjavacalculator.Interpreter.Num;

public final class BuiltinMethods {

	private BuiltinMethods() {
	}
	
	public static Num times2(Num[] args) {
		return new Num(args[0].getInternalNum().multiply(new BigDecimal("2")));
	}
	
}
