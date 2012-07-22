// This file is distributed under the Feel free to use it or add anything to it license.
// You are permitted to:
// - Use the software
// - Add anything to the software
//

package advancedjavacalculator;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import advancedjavacalculator.Expr.BinaryOpExpr;
import advancedjavacalculator.Expr.CallExpr;
import advancedjavacalculator.Expr.GroupExpr;
import advancedjavacalculator.Expr.LiteralExpr;
import advancedjavacalculator.Token.TokenType;

public class Interpreter {

	public static class Num {

		private BigDecimal internalNum;

		protected Num(BigDecimal internalNum) {
			this.internalNum = internalNum;
		}

		protected Num(String internalNum) {
			this(new BigDecimal(internalNum));
		}

		public BigDecimal getInternalNum() {
			return internalNum;
		}

		@Override
		public String toString() {
			return internalNum.toString();
		}

	}

	public static class Scope {

		private Map<String, Num> constants = new HashMap<String, Num>();
		private Map<String, Num> variables = new HashMap<String, Num>();
		private Map<String, Func> functions = new HashMap<String, Func>();

		public Map<String, Num> getConstants() {
			return constants;
		}

		public Map<String, Num> getVariables() {
			return variables;
		}

		public Map<String, Func> getFunctions() {
			return functions;
		}

	}

	public static class Func {

		private List<String> arguments;
		private Expr body;

		public Func(List<String> arguments, Expr body) {
			this.arguments = arguments;
			this.body = body;
		}

		public List<String> getArguments() {
			return arguments;
		}

		public Expr getBody() {
			return body;
		}

	}

	private final Scope scope = new Scope();
	private Map<String, Method> builtinMethods = new HashMap<String, Method>();

	public Interpreter() {
		addBuiltinMethod(BuiltinMethods.class, "times2");
	}

	public Num interpretExpr(Expr expr, Scope scope) throws CalcException {
		String match = expr.getToken().getMatch();

		if (expr instanceof LiteralExpr) {
			if (expr.getToken().getType() == TokenType.Number) {
				if (match.equals("0")) {
					return new Num(BigDecimal.ZERO);
				} else if (match.equals("1")) {
					return new Num(BigDecimal.ONE);
				} else if (match.equals("10")) {
					return new Num(BigDecimal.TEN);
				} else {
					if (match.contains("x")) {
						return new Num(Integer.toString(Integer.parseInt(match.split("x")[1], 16)));
					} else {
						return new Num(match);
					}
				}
			} else {
				Num num = scope.getVariables().get(match);

				if (num == null) {
					num = scope.getConstants().get(match);

					if (num == null) {
						throw new CalcException("Interpreter", String.format(
								"The variable %s was not found", match), expr);
					}
				}

				return num;
			}
		} else if (expr instanceof GroupExpr) {
			return interpretExpr(((GroupExpr) expr).getExpr(), scope);
		} else if (expr instanceof BinaryOpExpr) {
			try {
				return interpretBinaryOp((BinaryOpExpr) expr, scope);
			} catch (Exception e) {
				throw new CalcException("Interpreter", e.getLocalizedMessage());
			}
		} else if (expr instanceof CallExpr) {
			CallExpr callExpr = (CallExpr) expr;
			String name = callExpr.getExpr().toString();

			Method method = builtinMethods.get(name);

			if (method != null) {
				List<Num> arguments = new ArrayList<Num>();

				for (Expr arg : callExpr.getArguments()) {
					arguments.add(interpretExpr(arg, scope));
				}

				try {
					return (Num) method.invoke(null, new Object[] { arguments.toArray(new Num[arguments.size()]) });
				} catch (Exception e) {
					throw new CalcException("Interpreter", e.getLocalizedMessage());
				}
			} else {
				Func func = scope.getFunctions().get(name);

				if (func == null) {
					throw new CalcException("Interpreter", "The function %s does not exist", name);
				}

				if (func.getArguments().size() != callExpr.getArguments().size()) {
					throw new CalcException("Interpreter",
							"The number of arguments given does not match the number required");
				}

				Scope funcScope = new Scope();
				for (int i = 0; i < func.getArguments().size(); i++) {
					String argName = func.getArguments().get(i);
					Num arg = interpretExpr(callExpr.getArguments().get(i), scope);
					funcScope.getVariables().put(argName, arg);
				}

				return interpretExpr(callExpr.getExpr(), funcScope);
			}
		}

		throw new CalcException("Interpreter", "Not implemented yet");
	}

	private Num interpretBinaryOp(BinaryOpExpr expr, Scope scope) throws Exception {
		BigDecimal left = interpretExpr(expr.getLeftExpr(), scope).getInternalNum();
		BigDecimal right = interpretExpr(expr.getRightExpr(), scope).getInternalNum();

		BigInteger leftInt = new BigInteger(Integer.toString(left.intValueExact()));
		BigInteger rightInt = new BigInteger(Integer.toString(right.intValueExact()));

		switch (expr.getToken().getType()) {
			case Plus:
				return new Num(left.add(right));
			case Minus:
				return new Num(left.subtract(right));
			case Times:
				return new Num(left.multiply(right));
			case Divide:
				return new Num(left.divide(right));
			case Mod:
				return new Num(left.divideAndRemainder(right)[1]);
			case Pow:
				return pow(left, right);
			case BitAnd:
				return new Num(new BigDecimal(leftInt.and(rightInt).toString()));
			case BitOr:
				return new Num(new BigDecimal(leftInt.or(rightInt).toString()));
			case BitXor:
				return new Num(new BigDecimal(leftInt.xor(rightInt).toString()));
			case ShiftRight:
				return new Num(new BigDecimal(leftInt.shiftRight(rightInt.intValue()).toString()));
			case ShiftLeft:
				return new Num(new BigDecimal(leftInt.shiftLeft(rightInt.intValue()).toString()));
			default:
				throw new CalcException("Interpreter", "Not implemented yet");
		}
	}

	private Num pow(BigDecimal left, BigDecimal right) throws Exception {
		int signOf2 = right.signum();
		double leftDouble = left.doubleValue();

		right = right.multiply(new BigDecimal(signOf2));
		BigDecimal remainderOf2 = right.remainder(BigDecimal.ONE);
		BigDecimal rightIntPart = right.subtract(remainderOf2);

		BigDecimal intPow = left.pow(rightIntPart.intValueExact());
		BigDecimal doublePow = new BigDecimal(Math.pow(leftDouble, remainderOf2.doubleValue()));

		return new Num(intPow.multiply(doublePow));
	}

	public Scope getScope() {
		return scope;
	}

	public Map<String, Method> getBuiltinMethods() {
		return builtinMethods;
	}

	public void addBuiltinMethod(Class<?> clazz, String methodName) {
		addBuiltinMethod(clazz, methodName, methodName);
	}

	public void addBuiltinMethod(Class<?> clazz, String methodName, String alias) {
		try {
			builtinMethods.put(alias, clazz.getMethod(methodName, Num[].class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
