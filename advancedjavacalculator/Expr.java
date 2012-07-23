// This file is distributed under the Feel free to use it or add anything to it license.
// You are permitted to:
// - Use the software
// - Add anything to the software
//

package advancedjavacalculator;

import java.util.List;

public abstract class Expr {

	public static class LiteralExpr extends Expr {

		public LiteralExpr(Token token) {
			super(token);
		}

		@Override
		public String toString() {
			return getToken().getMatch();
		}

	}

	public static class BinaryOpExpr extends Expr {

		private Expr leftExpr, rightExpr;

		public BinaryOpExpr(Token token, Expr leftExpr, Expr rightExpr) {
			super(token);
			this.leftExpr = leftExpr;
			this.rightExpr = rightExpr;
		}

		@Override
		public String toString() {
			return leftExpr + " " + token.getMatch() + " " + rightExpr;
		}

		public Expr getLeftExpr() {
			return leftExpr;
		}

		public Expr getRightExpr() {
			return rightExpr;
		}

	}

	public static class GroupExpr extends Expr {

		private Expr expr;

		public GroupExpr(Token token, Expr expr) {
			super(token);
			this.expr = expr;
		}

		@Override
		public String toString() {
			return "(" + expr + ")";
		}

		public Expr getExpr() {
			return expr;
		}

	}

	public static class CallExpr extends Expr {

		private Expr expr;
		private List<Expr> arguments;

		public CallExpr(Token token, Expr expr, List<Expr> arguments) {
			super(token);
			this.expr = expr;
			this.arguments = arguments;
		}

		@Override
		public String toString() {
			return expr.toString() + arguments;
		}

		public Expr getExpr() {
			return expr;
		}

		public List<Expr> getArguments() {
			return arguments;
		}

	}

	public static class AssignExpr extends Expr {

		private Expr def;
		private Expr val;

		public AssignExpr(Token token, Expr def, Expr val) {
			super(token);
			this.def = def;
			this.val = val;
		}

		@Override
		public String toString() {
			return def + " = " + val;
		}

		public Expr getDef() {
			return def;
		}

		public Expr getVal() {
			return val;
		}

	}

	Token token;

	public Expr(Token token) {
		this.token = token;
	}

	public Token getToken() {
		return token;
	}

	@Override
	public abstract String toString();
}