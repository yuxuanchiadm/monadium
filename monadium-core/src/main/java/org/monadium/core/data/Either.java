package org.monadium.core.data;

import java.util.function.Function;
import java.util.function.Supplier;

import org.monadium.core.data.Bottom;
import static org.monadium.core.data.Bottom.*;
import org.monadium.core.data.Unit;
import static org.monadium.core.data.Unit.*;

public sealed interface Either<A, B> {
	record Left<A, B>(A a) implements Either<A, B> {
		@Override public boolean isLeft() { return true; }
		@Override public boolean isRight() { return false; }
		@Override public A fromLeft(A other) { return a(); }
		@Override public B fromRight(B other) { return other; }
		@Override public A coerceLeft() throws Undefined { return a(); }
		@Override public B coerceRight() throws Undefined { return undefined(); }

		@Override public <C> Either<A, C> map(Function<B, C> f) { return left(a()); }
		@Override public <C> Either<A, C> applyMap(Either<A, Function<B, C>> fbc) { return fbc.flatMap(this::map); }
		@Override public <C> Either<A, C> flatMap(Function<B, Either<A, C>> f) { return left(a()); }
	}
	record Right<A, B>(B b) implements Either<A, B> {
		@Override public boolean isLeft() { return false; }
		@Override public boolean isRight() { return true; }
		@Override public A fromLeft(A other) { return other; }
		@Override public B fromRight(B other) { return b(); }
		@Override public A coerceLeft() throws Undefined { return undefined(); }
		@Override public B coerceRight() throws Undefined { return b(); }

		@Override public <C> Either<A, C> map(Function<B, C> f) { return right(f.apply(b())); }
		@Override public <C> Either<A, C> applyMap(Either<A, Function<B, C>> fbc) { return fbc.flatMap(this::map); }
		@Override public <C> Either<A, C> flatMap(Function<B, Either<A, C>> f) { return f.apply(b()); }
	}

	static <A, B> Either<A, B> left(A a) { return new Left<>(a); }
	static <A, B> Either<A, B> right(B b) { return new Right<>(b); }

	boolean isLeft();
	boolean isRight();
	A fromLeft(A other);
	B fromRight(B other);
	A coerceLeft() throws Undefined;
	B coerceRight() throws Undefined;

	<C> Either<A, C> map(Function<B, C> f);
	<C> Either<A, C> applyMap(Either<A, Function<B, C>> fbc);
	<C> Either<A, C> flatMap(Function<B, Either<A, C>> f);

	static <A, B> Either<A, B> pure(B b) { return right(b); }
	static <A, B, C> Either<A, C> replace(Either<A, B> fb, C c) { return fb.map(b -> c); }
	static <A, B> Either<A, Unit> discard(Either<A, B> fb) { return fb.map(b -> unit()); }

	interface Notation {
		static <A, B, C> Either<A, C> $(Either<A, B> fb, Function<B, Either<A, C>> f) { return fb.flatMap(f); }
		static <A, B, C> Either<A, C> $(Either<A, B> fb, Supplier<Either<A, C>> fc) { return fb.flatMap(a -> fc.get()); }
	}
}
