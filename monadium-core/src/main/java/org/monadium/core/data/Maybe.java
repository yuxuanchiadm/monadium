package org.monadium.core.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.monadium.core.data.Bottom;
import static org.monadium.core.data.Bottom.*;
import org.monadium.core.data.List;
import static org.monadium.core.data.List.*;
import org.monadium.core.data.Unit;
import static org.monadium.core.data.Unit.*;

public sealed interface Maybe<A> extends Iterable<A> {
	record Nothing<A>() implements Maybe<A> {
		@Override public boolean isNothing() { return true; }
		@Override public boolean isJust() { return false; }
		@Override public A fromJust(A other) { return other; }
		@Override public A coerceJust() throws Undefined { return undefined(); }

		@Override public boolean any(Predicate<A> p) { return false; }
		@Override public boolean all(Predicate<A> p) { return false; }
		@Override public List<A> toList() { return nil(); }

		@Override public <B> Maybe<B> map(Function<A, B> f) { return nothing(); }
		@Override public <B> Maybe<B> applyMap(Maybe<Function<A, B>> fab) { return nothing(); }
		@Override public <B> Maybe<B> flatMap(Function<A, Maybe<B>> f) { return nothing(); }
		@Override public Maybe<A> plus(Maybe<A> fa) { return fa; }
	}
	record Just<A>(A a) implements Maybe<A> {
		@Override public boolean isNothing() { return false; }
		@Override public boolean isJust() { return true; }
		@Override public A fromJust(A other) { return a(); }
		@Override public A coerceJust() throws Undefined { return a(); }

		@Override public boolean any(Predicate<A> p) { return p.test(a()); }
		@Override public boolean all(Predicate<A> p) { return p.test(a()); }
		@Override public List<A> toList() { return singleton(a()); }

		@Override public <B> Maybe<B> map(Function<A, B> f) { return just(f.apply(a())); }
		@Override public <B> Maybe<B> applyMap(Maybe<Function<A, B>> fab) { return fab.flatMap(this::map); }
		@Override public <B> Maybe<B> flatMap(Function<A, Maybe<B>> f) { return f.apply(a()); }
		@Override public Maybe<A> plus(Maybe<A> fa) { return this; }
	}

	static <A> Maybe<A> nothing() { return new Nothing<>(); }
	static <A> Maybe<A> just(A a) { return new Just<>(a); }
	static <A> Maybe<A> maybe(Optional<A> optional) { return optional.map(Maybe::just).orElse(nothing()); }

	boolean isNothing();
	boolean isJust();
	A fromJust(A other);
	A coerceJust() throws Undefined;

	boolean any(Predicate<A> p);
	boolean all(Predicate<A> p);
	List<A> toList();

	<B> Maybe<B> map(Function<A, B> f);
	<B> Maybe<B> applyMap(Maybe<Function<A, B>> fab);
	<B> Maybe<B> flatMap(Function<A, Maybe<B>> f);
	Maybe<A> plus(Maybe<A> fa);

	static <A> Maybe<A> pure(A a) { return just(a); }
	static <A> Maybe<A> empty() { return nothing(); }
	static <A> Maybe<Maybe<A>> optional(Maybe<A> fa) { return fa.map(Maybe::just).plus(pure(nothing())); }
	static <A, B> Maybe<B> replace(Maybe<A> fa, B b) { return fa.map(a -> b); }
	static <A> Maybe<Unit> discard(Maybe<A> fa) { return fa.map(a -> unit()); }

	final class MaybeIterator<A> implements Iterator<A> {
		Maybe<A> current;
		MaybeIterator(Maybe<A> current) { this.current = current; }
		@Override public boolean hasNext() { return current.isJust(); }
		@Override public A next() {
			if (current.isNothing()) throw new NoSuchElementException();
			A next = current.coerceJust();
			current = nothing();
			return next;
		}
	}
	@Override default Iterator<A> iterator() { return new MaybeIterator<>(this); }
	@Override default Spliterator<A> spliterator() { return Spliterators.spliterator(iterator(), 1, Spliterator.ORDERED | Spliterator.IMMUTABLE); }
	default Stream<A> stream() { return StreamSupport.stream(spliterator(), false); }

	interface Notation {
		static <A, B> Maybe<B> $(Maybe<A> fa, Function<A, Maybe<B>> f) { return fa.flatMap(f); }
		static <A, B> Maybe<B> $(Maybe<A> fa, Supplier<Maybe<B>> fb) { return fa.flatMap(a -> fb.get()); }
		@SafeVarargs static <A> Maybe<A> $sum(Maybe<A>... fs) { return Arrays.stream(fs).reduce(empty(), Maybe::plus); }
	}
}
