// This file is distributed under the Feel free to use it or add anything to it license.
// You are permitted to:
// - Use the software
// - Add anything to the software
//

package advancedjavacalculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.MathContext;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import advancedjavacalculator.Parser.CalcParser;
import advancedjavacalculator.Type.Obj;

public class AdvancedCalculator {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			repl();
		}

		if (args[0].equals("repl")) {
			repl();
		} else if (args[0].equals("irc")) {
			if (args.length != 5) {
				return;
			}

			irc(args[1], Integer.parseInt(args[2]), args[3], args[4]);
		}
	}

	public static void irc(String server, int port, String name, final String channelToConnect)
			throws IOException {
		Socket socket = new Socket(server, port);
		final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		final Interpreter interpreter = new Interpreter();

		out.printf("NICK %s\n", name);
		out.printf("USER %s %s foobar :%s\n", name, server, name);
		out.printf("JOIN %s\n", channelToConnect);

		String input;

		while ((input = in.readLine()) != null) {

			if (input.startsWith("PING")) {
				out.printf("PONG %s\n", input.split(" ")[1]);
			} else if (input.contains(" PRIVMSG ")) {
				String[] split = input.split(":");
				String msg = split[2];

				if (msg.startsWith("~")) {
					msg = msg.substring(1, msg.length());
					final String inputMsg = msg;

					new Thread(new Runnable() {
						public void run() {
							ExecutorService executor = Executors.newSingleThreadExecutor();
							String ret = null;

							try {
								ret = executor
										.invokeAll(
												Arrays.asList(new InterpretTask(inputMsg,
														interpreter)), 10L, TimeUnit.SECONDS)
										.get(0).get();
							} catch (Exception e) {
								out.printf("PRIVMSG %s :%s\n", channelToConnect, String.format(
										"Error -> %s\n", "Calculation took longer than 10 seconds"));
								return;
							}

							executor.shutdownNow();

							if (ret.length() > 80 && !ret.startsWith("Error")) {
								ret = ret.substring(0, 81) + "...";
							}
							out.printf("PRIVMSG %s :%s\n", channelToConnect, ret);
						}
					}).start();
				}
			}
		}

		out.close();
		in.close();
		socket.close();
	}

	public static void repl() {
		Scanner in = new Scanner(System.in);
		Interpreter interpreter = new Interpreter();

		while (true) {
			System.out.print("> ");
			String input = in.nextLine();
			String out = new InterpretTask(input, interpreter).call();
			if (out == null) {
				break;
			}
			if (!out.isEmpty()) {
				System.out.println(out);
			}
		}

		in.close();
	}

}

class InterpretTask implements Callable<String> {

	private String input;
	private Interpreter interpreter;

	public InterpretTask(String input, Interpreter interpreter) {
		this.input = input;
		this.interpreter = interpreter;
	}

	@Override
	public String call() {
		if (input.equals("-q")) {
			return null;
		} else if (input.equals("-r")) {
			interpreter.getScope().getVariables().clear();
			return "";
		} else if (input.startsWith("-p")) {
			if (input.contains(" ")) {
				int prec = Integer.parseInt(input.split(" ")[1]);
				interpreter.setMathContext(new MathContext(prec));
				return "";
			} else {
				return String.format("%d", interpreter.getMathContext().getPrecision());
			}
		}

		try {
			Parser parser = new CalcParser(Lexer.doString(input));
			Expr expr;

			while ((expr = parser.parseExpr()) != null) {
				Obj ret = interpreter.interpretExpr(expr, interpreter.getScope());
				if (ret != null) {
					return ret.toString();
				}
				parser.consumeEndOfLine();
			}
		} catch (Exception e) {
			return String.format("Error -> %s", e.getMessage());
		}

		return null;
	}

}
