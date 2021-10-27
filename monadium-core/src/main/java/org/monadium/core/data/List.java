package org.monadium.core.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.monadium.core.data.Bottom;
import static org.monadium.core.data.Bottom.*;
import org.monadium.core.data.Maybe;
import static org.monadium.core.data.Maybe.*;
import org.monadium.core.data.Unit;
import static org.monadium.core.data.Unit.*;

public sealed interface List<A> extends Iterable<A> {
	record Nil<A>() implements List<A> {
		@Override public boolean isNil() { return true; }
		@Override public boolean isCons() { return false; }
		@Override public A coerceHead() throws Undefined { return undefined(); }
		@Override public List<A> coerceTail() throws Undefined { return undefined(); }

		@Override public int length() { return 0; }
		@Override public List<A> filter(Predicate<A> p) { return nil(); }
		@Override public boolean any(Predicate<A> p) { return false; }
		@Override public boolean all(Predicate<A> p) { return false; }
		@Override public <B> B foldl(BiFunction<B, A, B> f, B b) { return b; }
		@Override public <B> B foldr(BiFunction<A, B, B> f, B b) { return b; }
		@Override public List<A> concat(List<A> list) { return list; }

		@Override public <B> List<B> map(Function<A, B> f) { return nil(); }
		@Override public <B> List<B> applyMap(List<Function<A, B>> fab) { return nil(); }
		@Override public <B> List<B> flatMap(Function<A, List<B>> f) { return nil(); }
		@Override public List<A> plus(List<A> fa) { return fa; }
	}
	record Cons<A>(A head, List<A> tail) implements List<A> {
		@Override public boolean isNil() { return false; }
		@Override public boolean isCons() { return true; }
		@Override public A coerceHead() throws Undefined { return head(); }
		@Override public List<A> coerceTail() throws Undefined { return tail(); }

		@Override public int length() { return 1 + tail().length(); }
		@Override public List<A> filter(Predicate<A> p) { return p.test(head()) ? cons(head(), tail().filter(p)) : tail().filter(p); }
		@Override public boolean any(Predicate<A> p) { return p.test(head()) || tail().any(p); }
		@Override public boolean all(Predicate<A> p) { return p.test(head()) && tail().all(p); }
		@Override public <B> B foldl(BiFunction<B, A, B> f, B b) { return tail().foldl(f, f.apply(b, head())); }
		@Override public <B> B foldr(BiFunction<A, B, B> f, B b) { return f.apply(head(), tail().foldr(f, b)); }
		@Override public List<A> concat(List<A> list) { return cons(head(), tail().concat(list)); }

		@Override public <B> List<B> map(Function<A, B> f) { return cons(f.apply(head()), tail().map(f)); }
		@Override public <B> List<B> applyMap(List<Function<A, B>> fab) { return fab.flatMap(this::map); }
		@Override public <B> List<B> flatMap(Function<A, List<B>> f) { return f.apply(head()).concat(tail().flatMap(f)); }
		@Override public List<A> plus(List<A> fa) { return concat(fa); }
	}

	static <A> List<A> nil() { return new Nil<>(); }
	static <A> List<A> cons(A head, List<A> tail) { return new Cons<>(head, tail); }
	static <A> List<A> singleton(A a) { return cons(a, nil()); }
	@SafeVarargs static <A> List<A> list(A... as) {
		List<A> list = nil();
		for (int i = as.length - 1; i >= 0; i--) list = cons(as[i], list);
		return list;
	}
	static <A> List<A> list(A[] as, int from, int to) throws IndexOutOfBoundsException, IllegalArgumentException {
		if (from < 0 || from > as.length) throw new IndexOutOfBoundsException();
		if (from > to) throw new IllegalArgumentException();
		List<A> list = nil();
		for (int i = to - 1; i >= from; i--) list = cons(as[i], list);
		return list;
	}

	boolean isNil();
	boolean isCons();
	A coerceHead() throws Undefined;
	List<A> coerceTail() throws Undefined;

	int length();
	List<A> filter(Predicate<A> p);
	boolean any(Predicate<A> p);
	boolean all(Predicate<A> p);
	<B> B foldl(BiFunction<B, A, B> f, B b);
	<B> B foldr(BiFunction<A, B, B> f, B b);
	List<A> concat(List<A> list);

	<B> List<B> map(Function<A, B> f);
	<B> List<B> applyMap(List<Function<A, B>> fab);
	<B> List<B> flatMap(Function<A, List<B>> f);
	List<A> plus(List<A> fa);

	static <A> List<A> pure(A a) { return singleton(a); }
	static <A> List<A> empty() { return nil(); }
	static <A> List<Maybe<A>> optional(List<A> fa) { return fa.map(Maybe::just).plus(pure(nothing())); }
	static <A, B> List<B> replace(List<A> fa, B b) { return fa.map(a -> b); }
	static <A> List<Unit> discard(List<A> fa) { return fa.map(a -> unit()); }

	final class ListIterator<A> implements Iterator<A> {
		List<A> current;
		ListIterator(List<A> current) { this.current = current; }
		@Override public boolean hasNext() { return current.isCons(); }
		@Override public A next() {
			if (current.isNil()) throw new NoSuchElementException();
			A next = current.coerceHead();
			current = current.coerceTail();
			return next;
		}
	}
	@Override default Iterator<A> iterator() { return new ListIterator<>(this); }
	@Override default Spliterator<A> spliterator() { return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED | Spliterator.IMMUTABLE); }
	default Stream<A> stream() { return StreamSupport.stream(spliterator(), false); }

	interface Notation {
		static <A, B> List<B> $(List<A> fa, Function<A, List<B>> f) { return fa.flatMap(f); }
		static <A, B> List<B> $(List<A> fa, Supplier<List<B>> fb) { return fa.flatMap(a -> fb.get()); }
		@SafeVarargs static <A> List<A> $sum(List<A>... fs) { return Arrays.stream(fs).reduce(empty(), List::plus); }
	}
}
