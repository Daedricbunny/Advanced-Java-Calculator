// This file is distributed under the Feel free to use it or add anything to it license.
// You are permitted to:
// - Use the software
// - Add anything to the software
//

package advancedjavacalculator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import advancedjavacalculator.Token.TokenType;

public class Lexer {
	public static List<Token> doString(String input) throws CalcException {
		Lexer lexer = new Lexer(input);

		List<Token> tokens = new ArrayList<>();
		Token token = lexer.nextToken();
		while (token.getType() != TokenType.Eof) {
			tokens.add(token);

			token = lexer.nextToken();
		}
		tokens.add(token);

		return filter(tokens);
	}

	private static List<Token> filter(List<Token> tokens) {
		boolean consume = false;
		List<Token> filterTokens = new ArrayList<>();
		Iterator<Token> itr = tokens.iterator();
		while (itr.hasNext()) {
			Token token = itr.next();
			switch (token.getType()) {
				case OpenParen:
				case Plus:
				case Minus:
				case Times:
				case Divide:
				case Mod:
				case Comma:
					consume = true;
					break;
				case Eol:
					if (consume) {
						continue;
					}
					consume = true;
					break;
				default:
					consume = false;
			}
			filterTokens.add(token);
		}

		return filterTokens;
	}

	private final static char EOF = '\0';
	private final static char EOL = '\n';

	private String input;
	private int len, start, index;
	private char ch;
	private String value;

	private Lexer(String input) {
		this.input = input;

		len = input.length();
		start = index = 0;
		value = "";
	}

	private Token nextToken() throws CalcException {
		ch = getCh();
		start = index;
		while (ch != EOF) {
			if (ch <= ' ') {
				Token token = whitespace();
				if (token != null) {
					return token;
				}
			} else if (Character.isLetter(ch) || ch == '_' || ch == '$') {
				return ident();
			} else if (Character.isDigit(ch)) {
				return completeNumber();
			} else {
				return symbol();
			}

		}
		value = "(eof)";
		return make(TokenType.Eof);
	}

	private Token symbol() throws CalcException {
		value = Character.toString(ch);
		index++;
		for (;;) {
			ch = getCh();
			if (index >= len || Character.isWhitespace(ch) || Character.isLetterOrDigit(ch)) {
				break;
			}

			value += ch;
			index++;

		}
		
		if(value.equals("-") && Character.isDigit(getCh())) {
			ch = '-';
			index--;
			return completeNumber();
		}

		if (TokenType.isValidSymbol(value)) {
			return make(TokenType.tokenToType(value));
		} else if (value.length() > 1) {
			String[] tempSyms = new String[5];

			for (int i = 0; i < tempSyms.length; i++) {
				if (i == value.length())
					break;
				if (i == 0) {
					tempSyms[i] = Character.toString(value.charAt(i));
				} else {
					tempSyms[i] = tempSyms[i - 1] + value.charAt(i);
				}
			}

			for (int i = (tempSyms.length - 1); i >= 0; i--) {
				String tempSym = tempSyms[i];
				if (tempSym != null && !tempSym.isEmpty() && TokenType.isValidSymbol(tempSym)) {

					for (int j = 0; j < (value.length() - (i + 1)); j++) {
						index--;
					}

					value = tempSym;

					return make(TokenType.tokenToType(tempSym));
				}
			}
			throw new CalcException("Lexer", "Unknown symbol");
		} else {
			System.out.println(value);
			throw new CalcException("Lexer", "Unknown symbol");
		}
	}

	private void hexDigits() throws CalcException {
		String hexDigits = "abcdefABCDEF";
		for (;;) {
			ch = getCh();
			if (!Character.isDigit(ch) && !hexDigits.contains(Character.toString(ch))) {
				break;
			}
			index++;
			value += ch;
		}
	}

	private void digits() throws CalcException {
		for (;;) {
			ch = getCh();
			if (!Character.isDigit(ch)) {
				break;
			}
			index++;
			value += ch;
		}
	}

	private Token completeNumber() throws CalcException {
		value = Character.toString(ch);
		index++;

		char ch2 = getCh();
		index++;
		if (ch == '0' && ch2 == 'x') {
			value += ch2;
			hexDigits();
		} else {
			index--;
			digits();

			if (ch == '.') {
				index++;
				value += ch;
				digits();
			}

			if (ch == 'e' || ch == 'E') {
				index++;
				value += ch;
				ch = getCh();

				if (ch == '-' || ch == '+') {
					index++;
					value += ch;
					ch = getCh();
				}
				if (!Character.isDigit(ch)) {
					throw new CalcException("Lexer", "Bad exponent");
				}
				digits();
			}
		}

		if (Character.isLetter(ch)) {
			value += ch;
			index++;
			throw new CalcException("Lexer", "Unexpected character at end of number");
		}

		return make(TokenType.Number);
	}

	private Token ident() throws CalcException {
		value = Character.toString(ch);
		index++;
		for (;;) {
			ch = getCh();
			if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '$') {
				value += ch;
				index++;
			} else {
				break;
			}
		}

		if (TokenType.isValidKeyword(value)) {
			return make(TokenType.tokenToType(value));
		} else {
			return make(TokenType.Ident);
		}
	}

	private Token whitespace() throws CalcException {
		if (ch == '\t') {
			index++;
		} else if (ch == EOL) {
			value = "(eol)";
			index++;
			Token token = make(TokenType.Eol);
			return token;
		} else {
			index++;
		}

		ch = getCh();

		return null;
	}

	private char getCh() {
		return (index >= input.length()) ? EOF : input.charAt(index);
	}

	private Token make(TokenType type) {
		return new Token(value, type, start, index);
	}
}
