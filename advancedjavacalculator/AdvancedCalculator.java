// This file is distributed under the Feel free to use it or add anything to it license.
// You are permitted to:
// - Use the software
// - Add anything to the software
//

package advancedjavacalculator;

import java.util.Scanner;

import advancedjavacalculator.Parser.CalcParser;

public class AdvancedCalculator {

	public static void main(String[] args) throws CalcException {
		Scanner in = new Scanner(System.in);
		Interpreter interpreter = new Interpreter();

		while (true) {
			System.out.print("> ");
			String input = in.nextLine();
			if (input.equals(":q")) {
				break;
			} else if(input.equals(":r")) {
				interpreter = new Interpreter();
				continue;
			}

			long start = System.currentTimeMillis();
			doString(input + '\n', interpreter);
			msg("Time elapsed: %d ms", (System.currentTimeMillis() - start));
		}

		in.close();
	}

	public static void doString(String input, Interpreter interpreter) {
		try {
			Parser parser = new CalcParser(Lexer.doString(input));

			Expr expr;
			while ((expr = parser.parseExpr()) != null) {
				msg("= %s", interpreter.interpretExpr(expr, interpreter.getScope()));
				parser.consumeEndOfLine();
			}
		} catch (CalcException e) {
			System.out.printf(" Error -> %s\n", e.getMessage());
		}
	}
	
	private static void msg(String msg, Object... args) {
		msg(String.format(msg, args));
	}
	
	private static void msg(String msg) {
		System.out.printf(" %s\n", msg);
	}

}
