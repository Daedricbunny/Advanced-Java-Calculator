package daedricbunny.advancedjavacalculator;
public class Token {
	
	enum TokenType {

		Number(), Ident(), Unknown(),

		Plus("+"), Minus("-"), Times("*"), Divide("/"), Mod("%"), 
		PlusAssign("+=", false), MinusAssign("-=", false), TimesAssign("*=", false), DivideAssign("/=", false),
		ModAssign("%=", false), BitOr("|"), BitAnd("&"), BitOrAssign("|=", false), BitAndAssign("&=", false),
		Xor("^"), XorAssign("^=", false), ShiftRight(">>", false), ShiftLeft("<<", false), ShiftRightAssign(">>=", false),
		ShiftLeftAssign("<<=", false), UnsignRight(">>>", false), UnsignRightAssign(">>>=", false),
		
		Assign("="), Dot("."), OpenParen("("), CloseParen(")"), Comma(","), Eol("\n"), Eof("\0");

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
	
	private String match, source;
	private TokenType type;
	private int start, end;
	private int line, column;

	Token(String match, String source, TokenType type, int start, int end, int line, int column) {
		this.match = (match.equals("\n")) ? "\\n" : match;
		this.source = source;
		this.type = type;
		this.start = start;
		this.end = end;
		this.line = line;
		this.column = column - (end - start);
	}

	public String getMatch() {
		return match;
	}

	public String getSource() {
		return source;
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

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

}