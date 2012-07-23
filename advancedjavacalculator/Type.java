package advancedjavacalculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

public enum Type {
	Func, Num, RealNum, ComplexNum;

	public abstract static class Obj {

		private Type type;

		private Obj(Type type) {
			this.type = type;
		}

		public Type getType() {
			return type;
		}

		public boolean isNumber() {
			return type == Num || type == RealNum || type == ComplexNum;
		}

	}

	public static class Func extends Obj {

		private List<String> arguments;
		private Expr body;

		public Func(List<String> arguments, Expr body) {
			super(Type.Func);
			this.arguments = arguments;
			this.body = body;
		}

		public List<String> getArguments() {
			return arguments;
		}

		public Expr getBody() {
			return body;
		}

		@Override
		public String toString() {
			return arguments + " -> " + body;
		}

	}

	public static abstract class Num extends Obj {

		public Num(Type type) {
			super(type);
		}

		public abstract Num add(Num other) throws CalcException;

		public abstract Num sub(Num other) throws CalcException;

		public abstract Num mul(Num other) throws CalcException;

		public abstract Num div(Num other) throws CalcException;

		public abstract Num mod(Num other) throws CalcException;

		public abstract Num pow(Num other) throws CalcException;

	}

	public static class RealNum extends Num {

		private BigDecimal number;

		public RealNum(BigDecimal rational) {
			super(Type.RealNum);
			this.number = rational;
		}

		public RealNum(String rational, MathContext mathContext) {
			this(new BigDecimal(rational, mathContext));
		}

		@Override
		public Num add(Num other) throws CalcException {
			switch (other.getType()) {
				case RealNum:
					return new RealNum(number.add(((RealNum) other).getNumber()));
				default:
					throw new CalcException("Interpreter", "Cannot add a Rational and a(n) %s",
							other.getType());
			}

		}

		@Override
		public Num sub(Num other) throws CalcException {
			switch (other.getType()) {
				case RealNum:
					return new RealNum(number.subtract(((RealNum) other).getNumber()));
				default:
					throw new CalcException("Interpreter", "Cannot add a Rational and a(n) %s",
							other.getType());
			}
		}

		@Override
		public Num mul(Num other) throws CalcException {
			switch (other.getType()) {
				case RealNum:
					return new RealNum(number.multiply(((RealNum) other).getNumber()));
				default:
					throw new CalcException("Interpreter", "Cannot add a Rational and a(n) %s",
							other.getType());
			}
		}

		@Override
		public Num div(Num other) throws CalcException {
			switch (other.getType()) {
				case RealNum:
					return new RealNum(number.divide(((RealNum) other).getNumber()));
				default:
					throw new CalcException("Interpreter", "Cannot add a Rational and a(n) %s",
							other.getType());
			}
		}

		@Override
		public Num mod(Num other) throws CalcException {
			switch (other.getType()) {
				case RealNum:
					return new RealNum(number.remainder(((RealNum) other).getNumber()));
				default:
					throw new CalcException("Interpreter", "Cannot add a Rational and a(n) %s",
							other.getType());
			}
		}

		@Override
		public Num pow(Num other) throws CalcException {
			switch (other.getType()) {
				case RealNum:
					return pow(((RealNum) other).getNumber());
				default:
					throw new CalcException("Interpreter", "Cannot add a Rational and a(n) %s",
							other.getType());
			}
		}

		public BigDecimal getNumber() {
			return number;
		}

		private RealNum pow(BigDecimal right) throws CalcException {
			double leftDouble = number.doubleValue();
			MathContext mathContext = new MathContext(number.precision());

			BigDecimal remainderOf2 = right.remainder(BigDecimal.ONE, mathContext);
			BigDecimal rightIntPart = right.subtract(remainderOf2, mathContext);

			BigDecimal intPow = number.pow(rightIntPart.intValueExact(), mathContext);
			BigDecimal doublePow = new BigDecimal(Math.pow(leftDouble, remainderOf2.doubleValue()),
					mathContext);

			return new RealNum(intPow.multiply(doublePow, mathContext));
		}

		@Override
		public String toString() {
			return number.stripTrailingZeros().toPlainString();
		}

	}

	public static class ComplexNum extends Num {

		private BigDecimal real;
		private BigDecimal imaginary;

		public ComplexNum(BigDecimal real, BigDecimal imaginary) {
			super(Type.ComplexNum);
			this.real = real;
			this.imaginary = imaginary;
		}

		public ComplexNum(String real, String imaginary, MathContext mc) {
			this(new BigDecimal(real, mc), new BigDecimal(imaginary, mc));
		}

		@Override
		public Num add(Num other) throws CalcException {
			switch (other.getType()) {
				case ComplexNum:
					ComplexNum otherComplex = (ComplexNum) other;
					return new ComplexNum(real.add(otherComplex.getReal()),
							imaginary.add(otherComplex.getImaginary()));
				default:
					throw new CalcException("Interpreter", "Cannot add a ComplexNum and a(n) %s",
							other.getType());
			}
		}

		@Override
		public Num sub(Num other) throws CalcException {
			switch (other.getType()) {
				case ComplexNum:
					ComplexNum otherComplex = (ComplexNum) other;
					return new ComplexNum(real.subtract(otherComplex.getReal()),
							imaginary.subtract(otherComplex.getImaginary()));
				default:
					throw new CalcException("Interpreter", "Cannot add a ComplexNum and a(n) %s",
							other.getType());
			}
		}

		@Override
		public Num mul(Num other) throws CalcException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Num div(Num other) throws CalcException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Num mod(Num other) throws CalcException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Num pow(Num other) throws CalcException {
			// TODO Auto-generated method stub
			return null;
		}

		public BigDecimal getReal() {
			return real;
		}

		public BigDecimal getImaginary() {
			return imaginary;
		}

	}

}