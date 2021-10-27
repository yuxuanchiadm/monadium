package org.monadium.core.data;

public record Tuple<A, B>(A a, B b) {
	public static <A, B> Tuple<A, B> tuple(A a, B b) { return new Tuple<>(a, b); }

	public A first() { return a(); }
	public B second() { return b(); }
}
