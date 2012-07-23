// This file is distributed under the Feel free to use it or add anything to it license.
// You are permitted to:
// - Use the software
// - Add anything to the software
//

package advancedjavacalculator;

public class Token {

	enum TokenType {

		Number(), Ident(), Unknown(),

		Plus("+"), Minus("-"), Times("*"), Divide("/"), Mod("%"), Pow("^"), PlusAssign(
				"+=", false), MinusAssign("-=", false), TimesAssign("*=", false), DivideAssign(
				"/=", false), ModAssign("%=", false), PowAssign("**=", false),

		Assign("="), OpenParen("("), CloseParen(")"), Comma(","), Eol("\n"), Eof("\0");

		private String match;
		private boolean keyword;
		private boolean canMatch;

		TokenType() {
			canMatch = false;
		}

		TokenType(String match) {
			this(match, (match.length() == 1) ? false : true);
		}

		TokenType(String match, boolean keyword) {
			this.match = match;
			this.keyword = keyword;
			canMatch = true;

		}

		public static boolean isValidToken(String token) {
			return isValidKeyword(token) || isValidSymbol(token);
		}

		public static boolean isValidKeyword(String token) {
			for (TokenType type : values()) {
				if (type.canMatch && type.keyword && type.match.equals(token)) {
					return true;
				}
			}

			return false;
		}

		public static boolean isValidSymbol(String token) {
			for (TokenType type : values()) {
				if (type.canMatch && !type.keyword && type.match.equals(token)) {
					return true;
				}
			}

			return false;
		}

		public static TokenType tokenToType(String token) {
			for (TokenType type : values()) {
				if (type.canMatch && type.match.equals(token)) {
					return type;
				}
			}

			return Unknown;
		}

	}

	private String match;
	private TokenType type;
	private int start, end;

	Token(String match, TokenType type, int start, int end) {
		this.match = (match.equals("\n")) ? "\\n" : match;
		this.type = type;
		this.start = start;
		this.end = end;
	}

	public String getMatch() {
		return match;
	}

	public TokenType getType() {
		return type;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

}
