package org.monadium.core.control;

import java.util.function.Function;
import java.util.function.Supplier;

import org.monadium.core.data.Unit;
import static org.monadium.core.data.Unit.*;
import org.monadium.core.data.Bottom;
import static org.monadium.core.data.Bottom.*;

public record Cont<R, A>(Function<Function<A, R>, R> ka) {
	public static <R, A> Cont<R, A> cont(A a) { return cont(ca -> ca.apply(a)); }
	public static <R, A> Cont<R, A> cont(Function<Function<A, R>, R> ka) { return new Cont<>(ka); }

	public static <R, A> Cont<R, A> mapCont(Function<R, R> f, Cont<R, A> cont) { return cont(ca -> f.apply(runCont(cont).apply(ca))); }

	public static <R, A, B> Cont<R, A> callCC(Function<Function<A, Cont<R, B>>, Cont<R, A>> f) { return cont(ca -> runCont(f.apply(a -> cont(cb -> ca.apply(a)))).apply(ca)); }
	public static <R, A> Cont<R, A> label(Function<Function<A, Cont<R, Bottom>>, Cont<R, A>> f) { return callCC(f); }
	public static <R, A> Cont<R, A> reset(Cont<A, A> cont) { return cont(evalCont(cont)); }
	public static <R, A> Cont<R, A> shift(Function<Function<A, Cont<R, Bottom>>, Cont<R, A>> f) { return callCC(f); }

	public static <R, A> Function<Function<A, R>, R> runCont(Cont<R, A> cont) { return cont.ka(); }
	public static <A> A evalCont(Cont<A, A> cont) { return cont.ka().apply(a -> a); }

	public <B> Cont<R, B> map(Function<A, B> f) { return cont(cb -> ka().apply(a -> cb.apply(f.apply(a)))); }
	public <B> Cont<R, B> applyMap(Cont<R, Function<A, B>> fab) { return cont(cb -> runCont(fab).apply(f -> ka().apply(a -> cb.apply(f.apply(a))))); }
	public <B> Cont<R, B> flatMap(Function<A, Cont<R, B>> f) { return cont(cb -> ka().apply(a -> runCont(f.apply(a)).apply(cb))); }

	public static <R, A> Cont<R, A> pure(A a) { return cont(a); }
	public static <R, A, B> Cont<R, B> replace(Cont<R, A> fa, B b) { return fa.map(a -> b); }
	public static <R, A> Cont<R, Unit> discard(Cont<R, A> fa) { return fa.map(a -> unit()); }

	public interface Notation {
		static <R, A, B> Cont<R, B> $(Cont<R, A> fa, Function<A, Cont<R, B>> f) { return fa.flatMap(f); }
		static <R, A, B> Cont<R, B> $(Cont<R, A> fa, Supplier<Cont<R, B>> fb) { return fa.flatMap(a -> fb.get()); }
	}
}
