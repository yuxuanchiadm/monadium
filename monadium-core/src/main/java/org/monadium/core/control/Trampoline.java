package org.monadium.core.control;

import java.util.function.Function;
import java.util.function.Supplier;

import org.monadium.core.data.Unit;
import static org.monadium.core.data.Unit.*;

import org.monadium.core.data.Either;
import static org.monadium.core.data.Either.*;
import org.monadium.core.data.Maybe;
import static org.monadium.core.data.Maybe.*;

public sealed interface Trampoline<A> {
	record Done<A>(A a) implements Trampoline<A> {}
	record More<A>(Function<Unit, Trampoline<A>> ka) implements Trampoline<A> {}
	record FlatMap<X, A>(Trampoline<X> tx, Function<X, Trampoline<A>> ka) implements Trampoline<A> {}

	static <A> Trampoline<A> done(A a) { return new Done<>(a); }
	static <A> Trampoline<A> more(Function<Unit, Trampoline<A>> ka) { return new More<>(ka); }
	static <A> Trampoline<A> more(Supplier<Trampoline<A>> ka) { return more(u -> ka.get()); }

	default <B> Trampoline<B> map(Function<A, B> f) {
		return switch (this) {
			case Done<A> p0 -> new FlatMap<>(p0, f.andThen(Done::new));
			case More<A> p0 -> new FlatMap<>(p0, f.andThen(Done::new));
			case FlatMap<?, A> p0 -> new Object() {
				<X> Trampoline<B> unpack(FlatMap<X, A> p0) {
					return new FlatMap<>(p0.tx(), x -> p0.ka().apply(x).map(f));
				}
			}.unpack(p0);
		};
	}
	default <B> Trampoline<B> applyMap(Trampoline<Function<A, B>> tf) {
		return switch (tf) {
			case Done<Function<A, B>> p0 -> new FlatMap<>(p0, this::map);
			case More<Function<A, B>> p0 -> new FlatMap<>(p0, this::map);
			case FlatMap<?, Function<A, B>> p0 -> new Object() {
				<X> Trampoline<B> unpack(FlatMap<X, Function<A, B>> p0) {
					return new FlatMap<>(p0.tx(), x -> p0.ka().apply(x).flatMap(a -> map(a)));
				}
			}.unpack(p0);
		};
	}
	default <B> Trampoline<B> flatMap(Function<A, Trampoline<B>> f) {
		return switch (this) {
			case Done<A> p0 -> new FlatMap<>(p0, f);
			case More<A> p0 -> new FlatMap<>(p0, f);
			case FlatMap<?, A> p0 -> new Object() {
				<X> Trampoline<B> unpack(FlatMap<X, A> p0) {
					return new FlatMap<>(p0.tx(), x -> p0.ka().apply(x).flatMap(f));
				}
			}.unpack(p0);
		};
	}

	static <A> Trampoline<A> pure(A a) { return done(a); }
	static <A, B> Trampoline<B> replace(Trampoline<A> fa, B b) { return fa.map(a -> b); }
	static <A> Trampoline<Unit> discard(Trampoline<A> fa) { return fa.map(a -> unit()); }

	default <T extends Throwable> Either<Function<Unit, Trampoline<A>>, A> resume(Maybe<Thrower<T>> interrupter) throws T {
		Either<Trampoline<A>, Either<Function<Unit, Trampoline<A>>, A>> tco = left(this);
		while (tco.isLeft()) tco = interrupter.isJust() && Thread.interrupted()
			? interrupter.coerceJust().apply()
			: switch (tco.coerceLeft()) {
				case Done<A> p0 -> right(right(p0.a()));
				case More<A> p0 -> right(left(p0.ka()));
				case FlatMap<?, A> p0 -> new Object() {
					<X> Either<Trampoline<A>, Either<Function<Unit, Trampoline<A>>, A>> unpack(FlatMap<X, A> p0) {
						return switch (p0.tx()) {
							case Done<X> p1 -> left(p0.ka().apply(p1.a()));
							case More<X> p1 -> right(left(u -> p1.ka().apply(unit()).flatMap(p0.ka())));
							case FlatMap<?, X> p1 -> new Object() {
								<Y> Either<Trampoline<A>, Either<Function<Unit, Trampoline<A>>, A>> unpack(FlatMap<Y, X> p1) {
									return left(p1.tx().flatMap(y -> p1.ka().apply(y).flatMap(p0.ka())));
								}
							}.unpack(p1);
						};
					}
				}.unpack(p0);
			};
		return tco.coerceRight();
	}
	default <T extends Throwable> A run(Maybe<Thrower<T>> interrupter) throws T {
		Either<Trampoline<A>, A> tco = left(this);
		while (tco.isLeft()) tco = interrupter.isJust() && Thread.interrupted()
			? interrupter.coerceJust().apply()
			: switch (tco.coerceLeft().resume(interrupter)) {
				case Left<Function<Unit, Trampoline<A>>, A> p0 -> left(p0.a().apply(unit()));
				case Right<Function<Unit, Trampoline<A>>, A> p0 -> right(p0.b());
			};
		return tco.coerceRight();
	}
	default A run() { return run(nothing()); }
	default A interruptibleRun() throws InterruptedException { return run(just(Thrower.of(InterruptedException::new))); }

	interface Notation {
		static <A, B> Trampoline<B> $(Trampoline<A> fa, Function<A, Trampoline<B>> f) { return fa.flatMap(f); }
		static <A, B> Trampoline<B> $(Trampoline<A> fa, Supplier<Trampoline<B>> fb) { return fa.flatMap(a -> fb.get()); }
	}
}
