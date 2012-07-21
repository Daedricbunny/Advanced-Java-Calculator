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
	public static List<Token> doString(String input, String source) throws CalcException {
		Lexer lexer = new Lexer(input, source);

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
	private final static char COMMENT = '/';

	private String input;
	private String source;
	private int len, start, index, col, line, lastLineCol;
	private char ch;
	private String value;

	private Lexer(String input, String source) {
		this.input = input;
		this.source = source;

		len = input.length();
		start = index = 0;
		col = line = lastLineCol = 1;
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
			} else if (ch == COMMENT) {
				boolean commt = comment();
				start = index;
				if (commt) {
					continue;
				}
			} else {
				return symbol();
			}
		}
		value = "(eof)";
		return make(TokenType.Eof);
	}

	private Token symbol() throws CalcException {
		value = Character.toString(ch);
		advCol();
		for (;;) {
			ch = getCh();
			if (index >= len || Character.isWhitespace(ch) || Character.isLetterOrDigit(ch)) {
				break;
			}

			value += ch;
			advCol();
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
						revCol();
					}

					value = tempSym;

					return make(TokenType.tokenToType(tempSym));
				}
			}

			throw new CalcException("Lexer", "Unknown symbol", source, line, col);
		} else {
			throw new CalcException("Lexer", "Unknown symbol", source, line, col);
		}
	}

	private boolean comment() throws CalcException {
		advCol();
		ch = getCh();
		if (ch == COMMENT) {
			for (;;) {
				ch = getCh();
				if (ch == EOL || ch == EOF) {
					advCol();
					ch = getCh();
					break;
				}

				advCol();
			}

			return true;
		} else if (ch == '*') {
			for (;;) {
				ch = getCh();
				if (ch == '*') {
					advCol();
					ch = getCh();
					if (ch == '/') {
						advCol();
						ch = getCh();

						if (ch == EOL) {
							advCol();
							ch = getCh();
						}

						break;
					}
				} else if (ch == EOL) {
					lastLineCol = col;
					col = 1;
					line++;
				} else if (ch == EOF) {
					throw new CalcException("Lexer", "Unterminated long comment", source, line, col);
				}

				advCol();
			}

			return true;
		} else {
			revCol();
			revCol();
			return false;
		}
	}

	private void hexDigits() throws CalcException {
		String hexDigits = "abcdefABCDEF";
		for (;;) {
			ch = getCh();
			if (!Character.isDigit(ch) && !hexDigits.contains(Character.toString(ch))) {
				break;
			}
			advCol();
			value += ch;
		}
	}

	private void digits() throws CalcException {
		for (;;) {
			ch = getCh();
			if (!Character.isDigit(ch)) {
				break;
			}
			advCol();
			value += ch;
		}
	}

	private Token completeNumber() throws CalcException {
		value = Character.toString(ch);
		advCol();

		char ch2 = getCh();
		advCol();
		if (ch == '0' && ch2 == 'x') {
			hexDigits();
		} else {
			revCol();
			digits();

			if (ch == '.') {
				advCol();
				value += ch;
				digits();
			}

			if (ch == 'e' || ch == 'E') {
				advCol();
				value += ch;
				ch = getCh();

				if (ch == '-' || ch == '+') {
					advCol();
					value += ch;
					ch = getCh();
				}
				if (!Character.isDigit(ch)) {
					throw new CalcException("Lexer", "Bad exponent", source, line, col);
				}
				digits();
			}
		}

		if (Character.isLetter(ch)) {
			value += ch;
			advCol();
			throw new CalcException("Lexer", "Unexpected character at end of number", source, line,
					col);
		}

		return make(TokenType.Number);
	}

	private Token ident() throws CalcException {
		value = Character.toString(ch);
		advCol();
		for (;;) {
			ch = getCh();
			if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '$') {
				value += ch;
				advCol();
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
			col += 4;
		} else if (ch == EOL) {
			value = "(eol)";
			advCol();
			Token token = make(TokenType.Eol);
			lastLineCol = col;
			col = 1;
			line++;
			return token;
		} else {
			advCol();
		}

		ch = getCh();

		return null;
	}

	private char getCh() {
		return (index >= input.length()) ? EOF : input.charAt(index);
	}

	private void advCol() {
		index++;
		col++;
	}

	private void revCol() {
		if (line > 1) {
			if (col == 1) {
				col = lastLineCol;
				line--;
				index--;
			} else {
				col--;
				index--;
			}
		} else if (col > 1) {
			col--;
			index--;
		}
	}

	private Token make(TokenType type) {
		return new Token(value, source, type, start, index, line, col);
	}
}
