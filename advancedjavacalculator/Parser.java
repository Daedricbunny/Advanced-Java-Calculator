// This file is distributed under the Feel free to use it or add anything to it license.
// You are permitted to:
// - Use the software
// - Add anything to the software
//

package advancedjavacalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import advancedjavacalculator.Expr.BinaryOpExpr;
import advancedjavacalculator.Expr.CallExpr;
import advancedjavacalculator.Expr.GroupExpr;
import advancedjavacalculator.Expr.LiteralExpr;
import advancedjavacalculator.Token.TokenType;

public class Parser {

	private interface PrefixParser {
		Expr parse(Parser parser, Token token) throws CalcException;
	}

	private interface InfixParser {
		Expr parse(Parser parser, Token token, Expr leftExpr) throws CalcException;

		int getPrecedence();
	}

	public static class LiteralParser implements PrefixParser {

		@Override
		public Expr parse(Parser parser, Token token) throws CalcException {
			return new LiteralExpr(token);
		}

	}

	public static class BinaryOpParser implements InfixParser {

		private int precedence;
		private boolean right;

		public BinaryOpParser(int precedence, boolean right) {
			this.precedence = precedence;
		}

		@Override
		public Expr parse(Parser parser, Token token, Expr leftExpr) throws CalcException {
			Expr rightExpr = parser.parseExpr(right ? (precedence - 1) : precedence);
			parser.checkIfValidType(leftExpr);
			parser.checkIfValidType(rightExpr);

			return new BinaryOpExpr(token, leftExpr, rightExpr);
		}

		@Override
		public int getPrecedence() {
			return precedence;
		}

	}

	public static class GroupParser implements PrefixParser {

		@Override
		public Expr parse(Parser parser, Token token) throws CalcException {
			Expr expr = parser.parseExpr();
			parser.consume(TokenType.CloseParen);
			return new GroupExpr(token, expr);
		}

	}

	public static class CallParser implements InfixParser {

		@Override
		public Expr parse(Parser parser, Token token, Expr leftExpr) throws CalcException {
			if (leftExpr.getToken().getType() != TokenType.Ident) {
				throw new CalcException("Parser", "Invalid function name");
			}

			List<Expr> arguments = new ArrayList<Expr>();
			if (!parser.match(TokenType.CloseParen)) {
				do {
					Expr argument = parser.parseExpr();
					parser.checkIfValidType(argument);
					arguments.add(argument);
				} while (parser.matchConsume(TokenType.Comma));
			}
			parser.consume(TokenType.CloseParen);

			return new CallExpr(token, leftExpr, arguments);
		}

		@Override
		public int getPrecedence() {
			return CALL;
		}

	}

	public static class CalcParser extends Parser {

		public CalcParser(List<Token> tokens) {
			super(tokens);

			register(TokenType.Number, new LiteralParser());
			register(TokenType.Ident, new LiteralParser());

			register(TokenType.Plus, new BinaryOpParser(SUM, false));
			register(TokenType.Minus, new BinaryOpParser(SUM, false));
			register(TokenType.Times, new BinaryOpParser(EXPONENT, false));
			register(TokenType.Divide, new BinaryOpParser(EXPONENT, false));
			register(TokenType.Mod, new BinaryOpParser(EXPONENT, false));
			register(TokenType.Pow, new BinaryOpParser(EXPONENT, true));

			register(TokenType.BitOr, new BinaryOpParser(EXPONENT, false));
			register(TokenType.BitAnd, new BinaryOpParser(EXPONENT, false));
			register(TokenType.BitXor, new BinaryOpParser(EXPONENT, false));
			register(TokenType.ShiftLeft, new BinaryOpParser(EXPONENT, false));
			register(TokenType.ShiftRight, new BinaryOpParser(EXPONENT, false));

			register(TokenType.OpenParen, new GroupParser());
			register(TokenType.OpenParen, new CallParser());
		}

	}

	public static final int ASSIGNMENT = 10;
	public static final int CONDITIONAL = 20;
	public static final int SUM = 30;
	public static final int PRODUCT = 40;
	public static final int EXPONENT = 50;
	public static final int PREFIX = 60;
	public static final int POSTFIX = 70;
	public static final int CALL = 80;

	private Map<TokenType, InfixParser> infixParsers;
	private Map<TokenType, PrefixParser> prefixParsers;
	private List<Token> read;
	private Iterator<Token> tokensItr;

	public Parser(List<Token> tokens) {
		tokensItr = tokens.iterator();

		read = new ArrayList<Token>();
		infixParsers = new HashMap<TokenType, InfixParser>();
		prefixParsers = new HashMap<TokenType, PrefixParser>();
	}

	public void register(TokenType type, PrefixParser parser) {
		prefixParsers.put(type, parser);
	}

	public void register(TokenType type, InfixParser parser) {
		infixParsers.put(type, parser);
	}

	public Expr parseExpr() throws CalcException {
		return parseExpr(0);
	}

	public Expr parseExpr(int precedence) throws CalcException {
		Token token = consume();

		if (token.getType() == TokenType.Eof) {
			return null;
		}

		PrefixParser prefix = prefixParsers.get(token.getType());

		if (prefix == null) {
			if (token.getType() == TokenType.Eol) {
				throw new CalcException("Parser", "Unexpected end of line");
			} else {
				throw new CalcException("Parser", "Could not parse token");
			}
		}

		Expr leftExpr = prefix.parse(this, token);

		if (!tokensItr.hasNext() || lookAhead().getType() == TokenType.Eol) {
			return leftExpr;
		}

		while (precedence < getPrecedence()) {
			token = consume();

			InfixParser infix = infixParsers.get(token.getType());
			leftExpr = infix.parse(this, token, leftExpr);
		}

		return leftExpr;
	}

	public boolean match(TokenType expectedTokenType) {
		return match(0, expectedTokenType);
	}

	public boolean match(int distance, TokenType expectedTokenType) {
		Token token = lookAhead(distance);
		if (token.getType() != expectedTokenType) {
			return false;
		}

		return true;
	}

	public boolean match(TokenType... expectedTokenTypes) {
		return match(0, expectedTokenTypes);
	}

	public boolean match(int distance, TokenType... expectedTokenTypes) {
		for (TokenType expectedTokenType : expectedTokenTypes) {
			if (match(distance, expectedTokenType)) {
				return true;
			}
		}

		return false;
	}

	public boolean matchConsume(TokenType expectedTokenType) {
		if (match(expectedTokenType)) {
			consume();
			return true;
		}

		return false;
	}

	public boolean matchConsume(TokenType... expectedTokenTypes) {
		for (TokenType expectedTokenType : expectedTokenTypes) {
			if (matchConsume(expectedTokenType)) {
				return true;
			}
		}

		return false;
	}

	public Token consume() {
		lookAhead();
		return read.remove(0);
	}

	public Token consume(TokenType expectedTokenType) throws CalcException {
		if (!match(expectedTokenType)) {
			throw new CalcException("Parser", String.format("Expected token type %s, but found %s",
					expectedTokenType, lookAhead().getType()));
		}

		return consume();
	}

	public Token consume(TokenType... expectedTokenTypes) throws CalcException {
		for (TokenType expectedTokenType : expectedTokenTypes) {
			if (match(expectedTokenType)) {
				return consume();
			}

		}

		throw new CalcException("Parser", String.format("Expected tokens %s, but found %s",
				Arrays.toString(expectedTokenTypes), lookAhead().getType()));
	}

	public Token lookAhead(int distance) {
		while (distance >= read.size()) {
			read.add(tokensItr.next());
		}

		return read.get(distance);
	}

	public Token lookAhead() {
		return lookAhead(0);
	}

	private int getPrecedence() {
		InfixParser parser = infixParsers.get(lookAhead().getType());
		if (parser != null) {
			return parser.getPrecedence();
		}

		return 0;
	}

	public void consumeEndOfLine() throws CalcException {
		consume(TokenType.Eol);
	}

	public void checkIfValidType(Expr expr) throws CalcException {
		if (!(expr instanceof LiteralExpr || expr instanceof GroupExpr
				|| expr instanceof BinaryOpExpr || expr instanceof CallExpr)) {
			throw new CalcException("Parser", "Invalid type");
		}
	}

}
