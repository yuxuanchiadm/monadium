package org.monadium.core.data;

import org.monadium.core.data.Bottom;
import static org.monadium.core.data.Bottom.*;
import org.monadium.core.data.Unit;
import static org.monadium.core.data.Unit.*;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<A, B> {
	public static final class Left<A, B> extends Either<A, B> {
		final A a;

		Left(A a) { this.a = a; }

		public interface Case<A, B, R> { R caseLeft(A a); }
		@Override public <R> R caseof(Left.Case<A, B, R> caseLeft, Right.Case<A, B, R> caseRight) { return caseLeft.caseLeft(a); }

		@Override public boolean isLeft() { return true; }
		@Override public boolean isRight() { return false; }
		@Override public A fromLeft(A other) { return a; }
		@Override public B fromRight(B other) { return other; }
		@Override public A coerceLeft() throws Undefined { return a; }
		@Override public B coerceRight() throws Undefined { return undefined(); }

		@Override public <C> Either<A, C> map(Function<B, C> f) { return left(a); }
		@Override public <C> Either<A, C> applyMap(Either<A, Function<B, C>> fbc) { return fbc.flatMap(f -> map(f)); }
		@Override public <C> Either<A, C> flatMap(Function<B, Either<A, C>> f) { return left(a); }

		@Override public boolean equals(Object x) { return x instanceof Left && Objects.equals(a, ((Left<?, ?>) x).a); }
		@Override public int hashCode() { return Objects.hash(1, a); }
	}
	public static final class Right<A, B> extends Either<A, B> {
		final B b;

		Right(B b) { this.b = b; }

		public interface Case<A, B, R> { R caseRight(B b); }
		@Override public <R> R caseof(Left.Case<A, B, R> caseLeft, Right.Case<A, B, R> caseRight) { return caseRight.caseRight(b); }

		@Override public boolean isLeft() { return false; }
		@Override public boolean isRight() { return true; }
		@Override public A fromLeft(A other) { return other; }
		@Override public B fromRight(B other) { return b; }
		@Override public A coerceLeft() throws Undefined { return undefined(); }
		@Override public B coerceRight() throws Undefined { return b; }

		@Override public <C> Either<A, C> map(Function<B, C> f) { return right(f.apply(b)); }
		@Override public <C> Either<A, C> applyMap(Either<A, Function<B, C>> fbc) { return fbc.flatMap(f -> map(f)); }
		@Override public <C> Either<A, C> flatMap(Function<B, Either<A, C>> f) { return f.apply(b); }

		@Override public boolean equals(Object x) { return x instanceof Right && Objects.equals(b, ((Right<?, ?>) x).b); }
		@Override public int hashCode() { return Objects.hash(2, b); }
	}

	Either() {}

	public interface Match<A, B, R> extends Left.Case<A, B, R>, Right.Case<A, B, R> {}
	public final <R> R match(Match<A, B, R> match) { return caseof(match, match); }
	public abstract <R> R caseof(Left.Case<A, B, R> caseLeft, Right.Case<A, B, R> caseRight);

	public static <A, B> Either<A, B> left(A a) { return new Left<>(a); }
	public static <A, B> Either<A, B> right(B b) { return new Right<>(b); }

	public abstract boolean isLeft();
	public abstract boolean isRight();
	public abstract A fromLeft(A other);
	public abstract B fromRight(B other);
	public abstract A coerceLeft() throws Undefined;
	public abstract B coerceRight() throws Undefined;

	public abstract <C> Either<A, C> map(Function<B, C> f);
	public abstract <C> Either<A, C> applyMap(Either<A, Function<B, C>> fbc);
	public abstract <C> Either<A, C> flatMap(Function<B, Either<A, C>> f);

	public static <A, B> Either<A, B> pure(B b) { return right(b); }
	public static <A, B, C> Either<A, C> replace(Either<A, B> fb, C c) { return fb.map(b -> c); }
	public static <A, B> Either<A, Unit> discard(Either<A, B> fb) { return fb.map(b -> unit()); }

	@Override public abstract boolean equals(Object x);
	@Override public abstract int hashCode();

	public static final class Notation {
		Notation() {}

		public static <A, B, C> Either<A, C> $(Either<A, B> fb, Function<B, Either<A, C>> f) { return fb.flatMap(f); }
		public static <A, B, C> Either<A, C> $(Either<A, B> fb, Supplier<Either<A, C>> fc) { return fb.flatMap(a -> fc.get()); }
	}
}
