// This file is distributed under the Feel free to use it or add anything to it license.
// You are permitted to:
// - Use the software
// - Add anything to the software
//

package advancedjavacalculator;

import java.lang.reflect.Method;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import advancedjavacalculator.Expr.AssignExpr;
import advancedjavacalculator.Expr.BinaryOpExpr;
import advancedjavacalculator.Expr.CallExpr;
import advancedjavacalculator.Expr.GroupExpr;
import advancedjavacalculator.Expr.LiteralExpr;
import advancedjavacalculator.Token.TokenType;
import advancedjavacalculator.Type.Func;
import advancedjavacalculator.Type.Num;
import advancedjavacalculator.Type.Obj;
import advancedjavacalculator.Type.RealNum;

public class Interpreter {

	public static class Scope {

		private Map<String, Num> constants = new HashMap<String, Num>();
		private Map<String, Obj> variables = new HashMap<String, Obj>();

		public void addVariable(String name, Obj obj) {
			variables.put(name, obj);
		}

		public void addConstant(String name, Num num) {
			constants.put(name, num);
		}

		public Obj getVariable(String name) throws CalcException {
			Obj obj = variables.get(name);
			if (obj == null) {
				throw new CalcException("Interpreter", "The variable %s was not found", name);
			}
			return obj;
		}

		public Obj getVariableNoError(String name) {
			try {
				return getVariable(name);
			} catch (CalcException e) {
				return null;
			}
		}

		public Num getNumber(String name) throws CalcException {
			Obj obj = variables.get(name);

			if (obj == null) {
				throw new CalcException("Interpreter", "The variable %s was not found", name);
			} else if (obj.getType() != Type.Num) {
				throw new CalcException("Interpreter", "Expected the variable %s to have type Num",
						name);
			}

			return (Num) obj;
		}

		public Num getNumberNoError(String name) {
			try {
				return getNumber(name);
			} catch (CalcException e) {
				return null;
			}
		}

		public Func getFunction(String name) throws CalcException {
			Obj obj = variables.get(name);

			if (obj == null) {
				throw new CalcException("Interpreter", "The function %s was not found", name);
			} else if (obj.getType() != Type.Func) {
				throw new CalcException("Interpreter",
						"Expected the variable %s to have type Func", name);
			}

			return (Func) obj;
		}

		public Func getFunctionNoError(String name) {
			try {
				return getFunction(name);
			} catch (CalcException e) {
				return null;
			}
		}

		public Num getConstant(String name) throws CalcException {
			Num num = constants.get(name);
			if (num == null) {
				throw new CalcException("Interpreter", "The constant %s was not found", name);
			}

			return num;
		}

		public Num getConstantNoError(String name) {
			try {
				return getConstant(name);
			} catch (CalcException e) {
				return null;
			}
		}

		public Num getNumberOrConstant(String name) throws CalcException {
			Num num = getNumberNoError(name);
			if (num == null) {
				num = getConstantNoError(name);
				if (num == null) {
					throw new CalcException("Interpreter", "The variable %s was not found", name);
				}
			}

			return num;
		}

		public Map<String, Num> getConstants() {
			return constants;
		}

		public Map<String, Obj> getVariables() {
			return variables;
		}

	}

	public MathContext mathContext = MathContext.DECIMAL128;

	private final Scope scope = new Scope();
	private Map<String, Method> builtinMethods = new HashMap<String, Method>();

	public Interpreter() {
		try {
			addBuiltinMethod(BuiltinMethods.class, "sqrt");
			addBuiltinMethod(BuiltinMethods.class, "cbrt");
			addBuiltinMethod(BuiltinMethods.class, "root");
		} catch (Exception e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}

		scope.addConstant("E", new RealNum(Double.toString(Math.E), mathContext));
		scope.addConstant("PI", new RealNum(Double.toString(Math.PI), mathContext));
	}

	public Obj interpretExpr(Expr expr, Scope scope) throws CalcException {
		String match = expr.getToken().getMatch();
		TokenType tokenType = expr.getToken().getType();

		if (expr instanceof LiteralExpr) {
			if (tokenType == TokenType.Ident) {
				Obj obj = scope.getConstantNoError(match);
				if (obj == null) {
					obj = scope.getVariable(match);
				}
				return obj;
			} else {
				if (match.startsWith("0x")) {
					match = match.substring(2, match.length());
					return new RealNum(Integer.toString(Integer.parseInt(match, 16)), mathContext);
				} else {
					return new RealNum(match, mathContext);
				}
			}
		} else if (expr instanceof GroupExpr) {
			return interpretExpr(((GroupExpr) expr).getExpr(), scope);
		} else if (expr instanceof BinaryOpExpr) {
			BinaryOpExpr binaryOp = (BinaryOpExpr) expr;

			Obj leftObj = interpretExpr(binaryOp.getLeftExpr(), scope);
			if (!leftObj.isNumber()) {
				throw new CalcException("Interpreter", "Invalid left type '%s' for a binary operation", leftObj.getType());
			}

			Obj rightObj = interpretExpr(binaryOp.getRightExpr(), scope);
			if (!rightObj.isNumber()) {
				throw new CalcException("Interpreter", "Invalid right type '%s' for a binary operation", rightObj.getType());
			}

			Num left = (Num) leftObj;
			Num right = (Num) rightObj;

			switch (expr.getToken().getType()) {
				case Plus:
					return left.add(right);
				case Minus:
					return left.sub(right);
				case Times:
					return left.mul(right);
				case Divide:
					return left.div(right);
				case Mod:
					return left.mod(right);
				case Pow:
					return left.pow(right);
				default:
					break;
			}

		} else if (expr instanceof CallExpr) {
			CallExpr call = (CallExpr) expr;
			String name = call.getExpr().toString();
			Method method = builtinMethods.get(name);
			if (method != null) {
				List<Obj> args = new ArrayList<Obj>();
				for (Expr arg : call.getArguments()) {
					args.add(interpretExpr(arg, scope));
				}

				Obj ret;
				try {
					ret = (Obj) method.invoke(null,
							new Object[] { this, args.toArray(new Obj[args.size()]) });
				} catch (Exception e) {
					throw new CalcException("Interpreter", e.getCause().getLocalizedMessage());
				}

				if (!ret.isNumber()) {
					throw new CalcException("Interpreter", "Invalid return from builtin method %s",
							name);
				}
				return (Num) ret;
			}

			Func func = scope.getFunction(name);

			if (func.getArguments().size() != call.getArguments().size()) {
				throw new CalcException("Interpreter",
						"Number of arguments given doesn't match expected number");
			}

			Scope funcScope = new Scope();
			funcScope.getConstants().putAll(this.scope.getConstants());
			for (int i = 0; i < func.getArguments().size(); i++) {
				funcScope.addVariable(func.getArguments().get(i),
						interpretExpr(call.getArguments().get(i), scope));
			}

			Obj ret = interpretExpr(func.getBody(), funcScope);
			if (!ret.isNumber()) {
				throw new CalcException("Interpreter", "Invalid return from function %s", name);
			}
			return (Num) ret;
		} else if (expr instanceof AssignExpr) {
			AssignExpr assign = (AssignExpr) expr;

			if (assign.getDef().getToken().getType() == TokenType.Ident) {
				Obj obj = interpretExpr(assign.getVal(), scope);
				if (obj.getType() != Type.Num) {
					throw new CalcException("Interpreter", "Invalid value for variable %s", assign
							.getDef().toString());
				}
				scope.addVariable(assign.getDef().toString(), obj);
				return obj;
			} else {
				CallExpr call = (CallExpr) assign.getDef();
				List<String> args = new ArrayList<String>();
				for (Expr arg : call.getArguments()) {
					args.add(arg.toString());
				}

				Func func = new Func(args, assign.getVal());
				scope.addVariable(call.getExpr().toString(), func);
				return func;
			}
		}

		throw new CalcException("Interpreter", "Not implemented yet");

	}

	public Scope getScope() {
		return scope;
	}

	public MathContext getMathContext() {
		return mathContext;
	}

	public void setMathContext(MathContext matchContext) {
		this.mathContext = matchContext;
	}

	public void addBuiltinMethod(Class<?> clazz, String methodName) throws Exception {
		addBuiltinMethod(clazz, methodName, methodName);
	}

	public void addBuiltinMethod(Class<?> clazz, String methodName, String alias) throws Exception {
		builtinMethods.put(alias,
				clazz.getMethod(methodName, new Class<?>[] { Interpreter.class, Obj[].class }));
	}

}
