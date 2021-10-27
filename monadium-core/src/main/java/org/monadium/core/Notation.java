package org.monadium.core;

public interface Notation {
	static <A> A $do(A a) { return a; }
	static <A> A $(A a) { return a; }
}
