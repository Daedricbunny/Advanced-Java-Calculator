// This file is distributed under the Feel free to use it or add anything to it license.
// You are permitted to:
// - Use the software
// - Add anything to the software
//

package advancedjavacalculator;

import java.math.BigDecimal;
import java.math.MathContext;

import advancedjavacalculator.Type.Obj;
import advancedjavacalculator.Type.RealNum;

public final class BuiltinMethods {

	private BuiltinMethods() {}

	public static RealNum sqrt(Interpreter interpreter, Obj[] args) throws Exception {
		checkArgs(args, new Type[] { Type.Num });
		return root(interpreter, new Obj[] { new RealNum("2", interpreter.getMathContext()), getRational(args, 0) });
	}

	public static RealNum cbrt(Interpreter interpreter, Obj[] args) throws Exception {
		checkArgs(args, new Type[] { Type.Num });
		return root(interpreter, new Obj[] { new RealNum("3", interpreter.getMathContext()), getRational(args, 0) });
	}

	public static RealNum root(Interpreter interpreter, Obj[] args) throws Exception {
		checkArgs(args, new Type[] { Type.Num, Type.Num });
		int maxIterations = 500;

		BigDecimal x = new BigDecimal("1", interpreter.getMathContext());
		BigDecimal prevX = null;
		MathContext m = interpreter.getMathContext();
		BigDecimal root = getRational(args, 0).getNumber();
		BigDecimal n = getRational(args, 1).getNumber();
		int rootInt = root.intValueExact();

		for (int i = 0; i < maxIterations; i++) {
			try {
				prevX = x;
				x = x.subtract(
						x.pow(rootInt, m).subtract(n, m)
								.divide(root.multiply(x.pow(rootInt - 1, m), m), m), m);

				if (x.compareTo(prevX) == 0) {
					break;
				}
			} catch (ArithmeticException e) {
				throw new Exception(e.getLocalizedMessage());
			}
		}

		return new RealNum(x);
	}

	private static void checkArgs(Obj[] args, Type[] types) throws CalcException {
		for (int i = 0; i < args.length; i++) {
			if (args[i].getType() != types[i]) {
				if(types[i] == Type.Num && !args[i].isNumber()) {
					throw new CalcException("Interpreter", "Argument type mismatch");
				}
			}
		}
	}

	private static RealNum getRational(Obj[] args, int index) {
		return (RealNum) args[index];
	}

}
